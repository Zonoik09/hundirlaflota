#!/bin/bash

source ./config.env

# Obtenir configuració dels paràmetres
USER=${1:-$DEFAULT_USER}
RSA_PATH=${2:-$DEFAULT_RSA_PATH}
SERVER_PORT=${3:-$DEFAULT_SERVER_PORT}

echo "User: $USER"
echo "Ruta RSA: $RSA_PATH"
echo "Server port: $SERVER_PORT"

if [[ ! -f "$RSA_PATH" ]]; then
  echo "Error: No s'ha trobat el fitxer de clau privada: $RSA_PATH"
  exit 1
fi

# Demanar la contrasenya remota
read -s -p "Introdueix la contrasenya de sudo per al servidor remot: " SUDO_PASSWORD
echo ""

eval "$(ssh-agent -s)"
ssh-add "$RSA_PATH"

# SSH al servidor i executar la comanda amb sudo, passant la contrasenya
ssh -t -p 20127 "$USER@ieticloudpro.ieti.cat" << EOF
    echo "$SUDO_PASSWORD" | sudo -S iptables-save -t nat | grep -q -- "--dport 80" || echo "$SUDO_PASSWORD" | sudo -S iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port $SERVER_PORT
EOF

ssh-agent -k

