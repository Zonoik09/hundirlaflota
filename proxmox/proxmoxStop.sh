#!/bin/bash

source ./config.env

# Obtenir configuració dels paràmetres
USER=${1:-$DEFAULT_USER}
RSA_PATH=${2:-$DEFAULT_RSA_PATH}
SERVER_PORT=${3:-$DEFAULT_SERVER_PORT}

echo "User: $USER"
echo "Ruta RSA: $RSA_PATH"
echo "Server port: $SERVER_PORT"

JAR_NAME="server-package.jar"

cd ..

# Comprovem que els arxius existeixen
if [[ ! -f "$RSA_PATH" ]]; then
  echo "Error: No s'ha trobat el fitxer de clau privada: $RSA_PATH"
  cd proxmox
  exit 1
fi

# Iniciar ssh-agent i carregar la clau RSA
eval "$(ssh-agent -s)"
ssh-add "$RSA_PATH"

# SSH al servidor per trobar i matar el procés del JAR
ssh -t -p 20127 "$USER@ieticloudpro.ieti.cat" << EOF
    PID=\$(ps aux | grep 'java -jar $JAR_NAME' | grep -v 'grep' | awk '{print \$2}')
    if [ -n "\$PID" ]; then
      # Mata el procés si es troba
      kill \$PID
      echo "Procés $JAR_NAME amb PID \$PID aturat."
    else
      echo "No s'ha trobat el procés $JAR_NAME."
    fi
EOF

# Finalitzar l'agent SSH
ssh-agent -k

cd proxmox