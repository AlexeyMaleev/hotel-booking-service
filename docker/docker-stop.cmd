@echo off
echo Stopping and removing containers...
docker-compose -f docker\docker-compose.yml stop
echo Infrastructure stopped.