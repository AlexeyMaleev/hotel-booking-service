@echo off
REM Получаем локальный IPv4 адрес (кроме 127.0.0.1)
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /R "IPv4"') do (
    set IP=%%a
    goto :continue
)

:continue
set DOCKERHOST=%IP: =%
echo Detected host IP: %DOCKERHOST%

docker-compose -f docker\docker-compose.yml up -d