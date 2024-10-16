# Llegir les línies de l'arxiu config.env
$configFile = "config.env"
Get-Content $configFile | ForEach-Object {
    $line = $_
    if ($line -match "(\S+)\s*=\s*(.+)") {
        $key = $matches[1]
        $value = $matches[2]

        if ($value -like "*HOME*") {
            $value = $value -replace "HOME", "$HOME"
            $value = $value -replace '\$', ''
        }

        $value = $value -replace '/', '\'
        $value = $value -replace '"', ''

        # Configurar la variable en PowerShell
        Set-Variable -Name $key -Value $value
    }
}

$USER = if ($args.Count -ge 1) { $args[0] } else { $DEFAULT_USER }
$RSA_PATH = if ($args.Count -ge 2) { $args[1] } else { $DEFAULT_RSA_PATH }
$SERVER_PORT = if ($args.Count -ge 3) { $args[2] } else { $DEFAULT_SERVER_PORT }

Write-Host "User: $USER"
Write-Host "Ruta RSA: $RSA_PATH"
Write-Host "Server port: $SERVER_PORT"

if (-Not (Test-Path $RSA_PATH)) {
    Write-Host "Error: No s'ha trobat el fitxer de clau privada: $RSA_PATH"
    exit 1
}

# Demanar la contrasenya remota
$SUDO_PASSWORD = Read-Host "Introdueix la contrasenya de sudo per al servidor remot" -AsSecureString
$SUDO_PASSWORD_Plain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SUDO_PASSWORD))

# SSH al servidor i executar la comanda amb sudo, utilitzant la clau privada directament
$sshCommand = @"
    echo '$SUDO_PASSWORD_Plain' | sudo -S iptables-save -t nat | grep -q -- '--dport 80' || echo '$SUDO_PASSWORD_Plain' | sudo -S iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port $SERVER_PORT
"@ -replace "`r", ""

ssh -i $RSA_PATH -t -p 20127 "$USER@ieticloudpro.ieti.cat" $sshCommand
