# Deploy na AWS: Amazon Linux + Docker Hub + GitHub Actions

A publicação no Docker Hub continua no workflow `dockerhub.yml`. Depois que as
três imagens forem publicadas pela `main`, ele chama `deploy-aws.yml` quando a
variável `AWS_DEPLOY_ENABLED` estiver definida como `true`.

O deploy conecta por SSH, envia somente o Compose e o script para
`~/estoque-lsvp`, baixa as imagens com a tag do commit e atualiza os serviços.
Não precisa clonar o projeto, instalar Java/Node ou compilar na EC2.

## 1. Preparar a instância

Use Amazon Linux 2023 com arquitetura **x86_64**, compatível com as imagens
`linux/amd64` publicadas. O usuário padrão é `ec2-user`.

No Security Group, permita HTTP na porta 80 para quem acessará o sistema e SSH
na porta 22 a partir do runner de deploy e da sua conexão administrativa. Banco
e backend usam apenas a rede interna do Docker, sem portas públicas.

O runner `ubuntu-latest` precisa alcançar o IP/DNS da EC2 por SSH. Seus endereços
de saída podem variar: se sua rede exige um IP fixo ou a EC2 é privada, use um
runner com acesso à rede e ajuste `runs-on` no workflow. A pipeline não altera
o Security Group. Um IP/DNS estável evita atualizar o secret a cada reinício.

Conecte à EC2 e execute:

```bash
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
```

Encerre a sessão e conecte novamente para aplicar o grupo. Confira:

```bash
docker info
```

No Amazon Linux 2, a instalação do Docker usa
`sudo amazon-linux-extras install docker -y`; os demais passos são iguais.
Referência: [instalação no Amazon Linux](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-docker.html).

Instale o plugin Compose para o mesmo usuário que a pipeline usará:

```bash
mkdir -p ~/.docker/cli-plugins
curl -fSL https://github.com/docker/compose/releases/download/v5.6.0/docker-compose-linux-x86_64 \
  -o ~/.docker/cli-plugins/docker-compose
chmod +x ~/.docker/cli-plugins/docker-compose
docker compose version
```

O deploy usa `docker compose`, com espaço, incluindo `--wait`. Referência:
[instalação do plugin Compose](https://docs.docker.com/compose/install/linux/).

## 2. Configurar o banco no servidor

Como esta é uma instalação nova, crie uma única vez o volume do PostgreSQL:

```bash
docker volume create estoque-lsvp-postgres
mkdir -p ~/estoque-lsvp
chmod 700 ~/estoque-lsvp
umask 077
nano ~/estoque-lsvp/.env
```

Use como modelo `App/deploy/aws.env.example`, substituindo a senha:

```dotenv
COMPOSE_PROJECT_NAME=app
POSTGRES_DB=estoque_lsvp
POSTGRES_USER=postgres
POSTGRES_PASSWORD='SUA_SENHA_DO_BANCO'
POSTGRES_VOLUME=estoque-lsvp-postgres
FRONTEND_PORT=80
```

Depois de salvar:

```bash
chmod 600 ~/estoque-lsvp/.env
```

O Compose exige um volume externo existente. Nos próximos deploys, mantenha
esse volume e a senha configurada. O script não executa `down -v`, limpeza de
volumes ou remoção de imagens. A imagem do banco não contém dados locais.

Se futuramente reutilizar um banco existente, configure `POSTGRES_VOLUME` com
o nome real de seu volume e use as credenciais atuais. Não execute dois
PostgreSQL sobre o mesmo diretório de dados. O backend mantém
`hibernate.ddl-auto=update`, como no Compose local; mudanças de schema devem ser
consideradas no backup e numa eventual reversão.

## 3. Acesso às imagens do Docker Hub

Se os repositórios forem privados, faça login na EC2 como o mesmo usuário SSH:

```bash
docker login --username felipenagai
```

Quando solicitar a senha, informe um token Docker Hub com permissão de leitura.
Esse login é feito uma vez no servidor, sem `sudo`; renove-o quando o token
expirar. A pipeline usa as credenciais Docker desse usuário. Para imagens
públicas, o login é opcional.

## 4. Cadastrar secrets e variáveis no GitHub

No repositório, abra **Settings → Secrets and variables → Actions → Secrets** e
cadastre os seguintes repository secrets:

| Secret | Conteúdo |
| --- | --- |
| `AWS_HOST` | IP público ou DNS da EC2, sem `http://`, usuário ou porta |
| `AWS_SSH_PRIVATE_KEY` | Conteúdo completo da chave privada `.pem` autorizada na EC2, incluindo as linhas BEGIN/END; chave sem passphrase para uso não interativo |
| `AWS_SSH_KNOWN_HOSTS` | Chave pública SSH do servidor no formato `known_hosts`, conforme abaixo |

Para obter a chave do servidor, use uma sessão administrativa confiável na EC2:

```bash
sudo cat /etc/ssh/ssh_host_ed25519_key.pub
```

Monte o valor de `AWS_SSH_KNOWN_HOSTS` usando o mesmo IP/DNS de `AWS_HOST`, seguido
do tipo e da chave retornados (sem o comentário final):

```text
SEU_IP_OU_DNS ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAA...
```

Para uma porta SSH diferente de 22, use `[SEU_IP_OU_DNS]:PORTA` no início da linha.
A pipeline verifica essa chave; se a instância for substituída, atualize o secret
com a chave do novo servidor.

Na aba **Variables**, configure:

| Variável | Valor |
| --- | --- |
| `AWS_DEPLOY_ENABLED` | `true` para ativar o deploy automático depois da publicação |
| `AWS_USER` | Opcional; padrão `ec2-user` |
| `AWS_SSH_PORT` | Opcional; padrão `22` |

O secret `DOCKERHUB_TOKEN` já existente continua sendo usado apenas para publicar
as imagens no job de build. A senha do PostgreSQL fica no `.env` da EC2 e não é
enviada pelo workflow.

## 5. Executar a pipeline

Envie estes arquivos para a `main`. Com `AWS_DEPLOY_ENABLED=true`, um push que
aciona a publicação seguirá o fluxo:

1. Compilar e publicar banco, backend e frontend.
2. Aguardar sucesso dos três jobs.
3. Conectar à EC2 e enviar os arquivos de deploy.
4. Fazer `docker compose pull` das três imagens `sha-<commit completo>`.
5. Atualizar banco e backend e aguardar os checks de saúde.
6. Recriar o frontend para o Nginx resolver o endereço atual do backend.
7. Aguardar o frontend e registrar a tag em `~/estoque-lsvp/.deployed-image-tag`.

Atualizações podem causar uma breve interrupção; não há troca sem downtime ou
rollback automático. Se o download falhar, a atualização dos containers não
começa. Se a inicialização falhar, o job falha e o estado deve ser inspecionado.

Para usar uma versão já publicada, abra **Actions → Deploy AWS Amazon Linux →
Run workflow**, selecione a branch **main** e informe `image_tag`, por exemplo
`sha-<commit completo>`, `v1.0.0` ou `latest`. Essa execução manual independe de
`AWS_DEPLOY_ENABLED`. Prefira uma tag de commit para saber exatamente a versão.
Tags de release não fazem deploy automático; use esta execução manual.

Acesse `http://SEU_IP_OU_DNS`. O frontend já usa `/api` pelo Nginx; não é necessário
trocar o IP no código Angular.

## 6. Diagnóstico na EC2

Use a tag mostrada na execução do GitHub. O arquivo `.deployed-image-tag` contém
apenas a última versão concluída com sucesso:

```bash
cd ~/estoque-lsvp
export IMAGE_TAG=sha-SUBSTITUA_PELO_COMMIT_COMPLETO
docker compose --env-file .env -f docker-compose.aws.yml ps
docker compose --env-file .env -f docker-compose.aws.yml logs --tail=100 backend frontend db
```

| Falha | Verificação |
| --- | --- |
| Timeout SSH | IP/DNS, porta, Security Group e conectividade do runner |
| Permission denied (publickey) | Usuário SSH, chave privada e chave autorizada na EC2 |
| Host key verification failed | Secret `AWS_SSH_KNOWN_HOSTS` e chave real do servidor |
| Acesso ao Docker negado | Grupo `docker` e nova sessão do usuário SSH |
| Pull negado ou tag inexistente | Login Docker na EC2 e tag publicada nos três repositórios |
| Volume externo não encontrado | Nome exato em `POSTGRES_VOLUME` |
| Backend unhealthy | Logs, conexão ao banco e recursos disponíveis na EC2 |
