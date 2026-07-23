@echo off
echo ========================================
echo   DataAgent 一键启动
echo ========================================
echo.

REM 1. 检查 Docker
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker Desktop is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

REM 2. 检查 Redis
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] Redis is not running. Please start Redis first.
)

REM 3. 检查 MySQL
mysqladmin ping -u root -p123456 --silent >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] MySQL is not running. Please start MySQL first.
)

echo [OK] Prerequisites checked.
echo.

REM 4. 启动基础设施（Milvus + Prometheus + Grafana）
echo [1/3] Starting infrastructure (Milvus + Prometheus + Grafana)...
docker compose -f docker-compose.infra.yml up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start infrastructure.
    pause
    exit /b 1
)
echo [OK] Infrastructure started.

REM 5. 等待 Milvus 就绪
echo [2/3] Waiting for Milvus to become healthy...
:wait_milvus
timeout /t 5 >nul
docker ps --filter name=milvus-standalone --filter health=healthy -q >nul 2>&1
if %errorlevel% neq 0 goto wait_milvus
echo [OK] Milvus is healthy.

REM 6. 启动后端
echo [3/3] Starting backend...
cd data-agent-management
start "DataAgent Backend" cmd /k "..\mvnw.cmd spring-boot:run"
cd ..
echo [OK] Backend starting...

echo.
echo ========================================
echo   All services started!
echo.
echo   Frontend:  cd data-agent-frontend ^&^& npm run dev
echo   Backend:   http://localhost:8065
echo   Grafana:   http://localhost:3001 (admin/admin)
echo   Prometheus: http://localhost:9090
echo   Druid:     http://localhost:8065/druid (admin/admin)
echo ========================================
echo.
echo To stop infrastructure: docker compose -f docker-compose.infra.yml down
pause
