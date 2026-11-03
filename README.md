# Sistema de Controle de Estoque  
## Lar São Vicente de Paulo

Este repositório contém o código-fonte de um **Sistema de Controle de Estoque** desenvolvido como **Projeto Integrador da FATEC**, com foco nas necessidades reais do *Lar São Vicente de Paulo*.

O sistema foi projetado para resolver problemas complexos de logística interna, como:

- Controle rigoroso de **validade de alimentos**
- Distinção clara entre itens **comprados** e **doados**
- Rastreabilidade completa de **lotes físicos**


# Sistema de Controle de Estoque  
## Lar São Vicente de Paulo

Este repositório contém o código-fonte de um **Sistema de Controle de Estoque** desenvolvido como **Projeto Integrador da FATEC**, com foco nas necessidades reais do *Lar São Vicente de Paulo*.

O sistema foi projetado para resolver problemas complexos de logística interna, como:

- Controle rigoroso de **validade de alimentos**
- Distinção clara entre itens **comprados** e **doados**
- Rastreabilidade completa de **lotes físicos**
- Movimentação segura entre **Estoque Central** e **Cozinha**

---



##  Equipe de Desenvolvimento

Projeto acadêmico desenvolvido por:

- Gustavo Germano Lemos Pereira  - gustavo.germanolemos@gmail.com
- José Roberto Lisboa da Silva Filho - jlisboa600@gmail.com
- Kaique Alves Mesquita - kaiquealvesmesquita@gmail.com
- Lucas Feitosa Almeida Rocha - lucasfarocha44@gmail.com
- Luiz Filipe de Camargo - luizfilipedecamargo@gmail.com

 **Instituição:** FATEC  
 **Disciplina:** Projeto Integrador  

---

##  Tecnologias Utilizadas

O sistema adota uma arquitetura moderna, com **separação total entre Backend e Frontend**, facilitando manutenção, escalabilidade e evolução do projeto.

### Backend

- **Java** (JDK 17+)
- **Spring Boot** (Framework principal)
- **Spring Data JPA** (Persistência de dados)
- **Banco de Dados:** PostgreSQL 
- **Porta Padrão:** `8080`

### Frontend

- **Angular 19.2**
- **Standalone Components**
- **TypeScript**
- **Porta Padrão:** `4200`

---

##  Arquitetura do Sistema

O projeto segue princípios de:

- Arquitetura em **camadas**
- **Separação de responsabilidades**
- **Domínio rico**, com regras de negócio bem definidas
- Facilidade de integração com leitores de **código de barras (EAN)**

---

##  Lógica de Negócio e Estrutura de Dados

Um dos grandes diferenciais do sistema é a **modelagem de dados orientada à rastreabilidade física**, evitando redundâncias e inconsistências.

A estrutura é dividida em três níveis principais:

###  Categorias (Abstração)

Representam o conceito genérico do item.

**Exemplos:**
- Arroz Branco  
- Leite Integral  
- Feijão Carioca  

Responsabilidades:
- Definição de **estoque mínimo**
- Definição de **estoque máximo**
- Agrupamento lógico de produtos

---

###  Produtos (Definição)

São os itens cadastrados com especificações técnicas, sem validade associada.

**Atributos principais:**
- Código de barras (EAN)
- Marca
- Unidade de medida (Kg, L, Unidade)

**Exemplo:**  
> Arroz Tio João 5kg → Categoria: *Arroz Branco*

---

###  Unidades (Físico)

Representam o **lote físico real** que entra no estoque.

Aqui residem as informações críticas:
- Data de validade
- Lote
- Origem (**Doação** ou **Compra**)
- Quantidade disponível

O sistema permite múltiplas unidades do mesmo produto, cada uma com validade e lote próprios.

---

##  Funcionalidades Principais

###  Gestão de Estoque

- **Entrada Inteligente** via leitor de código de barras
- Detecção automática de produtos existentes
- Cadastro simplificado para novos lotes
- Controle de **origem** (Doação x Compra)
- Movimentação entre:
  - Estoque Central
  - Cozinha

---

###  Alertas e Monitoramento

- Controle de validade com priorização **PEPS (FIFO)**
- Alertas para itens próximos do vencimento
- Avisos automáticos de:
  - Estoque mínimo
  - Estoque acima do limite

---

###  Relatórios

- Relatórios de entradas e saídas
- Relatórios de perdas e vencimentos
- Inventário atualizado em tempo real
- Apoio à prestação de contas e auditoria

---

##  Como Executar o Projeto

### 🔹 Com Docker Compose

Com Docker e o plugin Docker Compose instalados, execute na raiz do repositório:

```bash
cd App
docker compose up --build
```

O frontend ficará disponível em `http://localhost` e a API em `http://localhost:8080`.
O PostgreSQL fica disponível na porta `5432`, e os dados são mantidos no volume
`postgres-data`.

As portas e credenciais podem ser personalizadas pelas variáveis
`FRONTEND_PORT`, `BACKEND_PORT`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`
e `POSTGRES_PASSWORD`. Os valores padrão são voltados somente para desenvolvimento
local; defina credenciais próprias antes de usar em qualquer ambiente compartilhado.

### 🔹 Publicar no Docker Hub

O workflow `.github/workflows/dockerhub.yml` compila e publica três imagens
para `linux/amd64`:

| Serviço | Imagem |
| --- | --- |
| Banco PostgreSQL 15 | `felipenagai/estoque-lsvp-db` |
| Backend Spring Boot | `felipenagai/estoque-lsvp-backend` |
| Frontend Angular + Nginx | `felipenagai/estoque-lsvp-frontend` |

Na conta `felipenagai` do Docker Hub, prepare os três repositórios acima com a
visibilidade desejada e crie um [token de acesso pessoal](https://docs.docker.com/security/access-tokens/)
com permissão de leitura e escrita. No repositório do GitHub, acesse
**Settings → Secrets and variables → Actions → New repository secret** e salve
esse token como `DOCKERHUB_TOKEN`. O usuário `felipenagai` já está definido no workflow.

Depois de enviar os arquivos ao GitHub:

- Push na `main` com alterações em `App/` ou no workflow publica `latest`, `main`
  e `sha-<SHA completo do commit>`.
- Push de uma tag `v*`, como `v1.0.0`, publica essa tag e a tag do commit,
  sem substituir `latest`.
- **Actions → Publicar imagens no Docker Hub → Run workflow** permite publicar
  manualmente a referência selecionada. Apenas a branch padrão atualiza `latest`.
- Pull requests para `main` nos mesmos caminhos apenas validam a compilação;
  não fazem login nem publicam imagens.

A imagem do banco contém o PostgreSQL, sem copiar dados ou senhas locais.
O banco é inicializado pelas variáveis `POSTGRES_*`, e seus dados permanecem no
volume `postgres-data`. A imagem não substitui um backup do banco.

Para usar as imagens publicadas, execute na pasta `App`:

```bash
docker compose -f docker-compose.yml -f docker-compose.hub.yml pull
docker compose -f docker-compose.yml -f docker-compose.hub.yml up -d --no-build
```

O arquivo `docker-compose.hub.yml` complementa o Compose local, mantendo a rede,
as portas e o volume. Por padrão usa `latest`; defina `IMAGE_TAG=v1.0.0` no ambiente
ou no arquivo `App/.env` para usar a mesma versão nos três serviços. Configure
também `POSTGRES_PASSWORD` nesse ambiente; para um volume existente, mantenha a
senha já configurada no banco. Se as imagens forem privadas, execute `docker login`
antes do `pull`.

### 🔹 Pré-requisitos

- Java JDK 17+
- Node.js (LTS) + NPM
- Banco de dados PostgreSQL ou MySQL

---

### 🔹 Backend

```bash
# Acesse a pasta do backend
cd backend/InventoryManagement

# Configure o banco em:
src/main/resources/application.properties

# Execute a aplicação
mvnw clean install

```

Backend disponível em:  
- http://localhost:8080

---

Swagger do backend disponível em:  
- http://localhost:8080/swagger-ui/index.html


---

### 🔹 Frontend

```bash

# Acesse a pasta do frontend
cd frontend/Estoque_WebApp

# Instale as dependências
npm install

# Execute a aplicação
ng serve
```

Frontend disponível em:  
 http://localhost:4200

---



## Licença

Projeto acadêmico sem licença específica.

Este repositório foi desenvolvido como atividade acadêmica na FATEC e, no momento, não possui uma licença de código aberto aplicada. Os autores não concederam autorização explícita para reutilização, modificação ou distribuição; portanto, considere que todos os direitos estão reservados aos autores até que uma licença seja adicionada.


