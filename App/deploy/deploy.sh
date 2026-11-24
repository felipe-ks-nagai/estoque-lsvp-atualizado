#!/usr/bin/env bash
set -euo pipefail

image_tag=${1:?Informe a tag publicada no Docker Hub}
[[ "$image_tag" =~ ^[a-zA-Z0-9_][a-zA-Z0-9_.-]{0,127}$ ]] || { echo 'Tag de imagem invalida.' >&2; exit 1; }
deploy_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
cd "$deploy_dir"

[[ -f .env ]] || { echo "Configure $deploy_dir/.env antes do primeiro deploy." >&2; exit 1; }
[[ $(uname -m) == x86_64 ]] || { echo 'As imagens publicadas exigem uma EC2 x86_64.' >&2; exit 1; }
export IMAGE_TAG="$image_tag"
docker info > /dev/null
docker compose version
compose=(docker compose --env-file .env -f docker-compose.aws.yml)
"${compose[@]}" config --quiet

# Baixar todas as imagens antes de alterar os containers em execucao.
"${compose[@]}" pull
"${compose[@]}" up -d --no-build --pull never --wait --wait-timeout 240 db backend

# Recriar o Nginx para resolver o IP atual do backend, mesmo se sua imagem nao mudou.
"${compose[@]}" up -d --no-build --pull never --force-recreate --no-deps --wait --wait-timeout 90 frontend
"${compose[@]}" ps
printf '%s\n' "$IMAGE_TAG" > .deployed-image-tag
echo "Deploy concluido: $IMAGE_TAG"
