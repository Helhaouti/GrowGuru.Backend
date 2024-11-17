# Powershell script to setup a dockerized mysql database.

# Run by:
# - CD into project directory, and run: ./mysql-setup.ps1;
# - or by clicking on `Run` this file in the context menu (right click) in IntelliJ *(development mode in windows required)*.

# Make sure Docker is installed
$dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue
if (-Not$dockerInstalled)
{
    Write-Host "Docker desktop is not installed, yet. Starting instalation..."
    winget install Docker.DockerDesktop
}

# Start Docker
Write-Host "Starting Docker..."
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"

# Wait for Docker to become active
while ($true)
{
    if ((docker info 2>&1) -ilike "*error*")
    {
        Write-Host "Waiting for Docker to start..."
        Start-Sleep -Seconds 1
    }
    else
    {
        break
    }
}

# Check if MySQL container already exists
$mysqlContainerAvailable = docker ps -a --filter "name=mysql" -q

if (-Not$mysqlContainerAvailable)
{
    Write-Host "No mysql container found, making one..."

    docker run --name mysql -d `
        -p 3306:3306 `
        -e "MYSQL_ROOT_PASSWORD=clover" `
        -e "MYSQL_DATABASE=clover" `
        -e "MYSQL_USER=clover" `
        -e "MYSQL_PASSWORD=clover" `
        mysql:8.0
}
else
{
    Write-Host "Mysql container found, starting it..."
    docker start mysql
}