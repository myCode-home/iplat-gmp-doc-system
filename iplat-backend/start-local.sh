#!/usr/bin/env bash
# 启动本地后端（RuoYi-Vue-Plus + DMS 后端 + Redis + MinIO）
set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
JAVA_BIN="$JAVA_HOME/bin/java"
JAR="$BASE_DIR/ruoyi-admin/target/ruoyi-admin.jar"

if [ ! -x "$JAVA_BIN" ]; then
  echo "未找到 JDK 17：$JAVA_BIN"
  echo "请安装后重试，或通过 JAVA_HOME 指定 JDK 路径。"
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "未找到后端 jar：$JAR"
  echo "请先执行："
  echo "  mvn -pl ruoyi-admin -am package -DskipTests"
  exit 1
fi

# 本地专用 Redis（端口 16379，密码 ruoyi123），避免影响系统已有的 6379
if ! redis-cli -p 16379 -a ruoyi123 ping >/dev/null 2>&1; then
  echo "启动本地 Redis (127.0.0.1:16379)..."
  mkdir -p "$BASE_DIR/.local/redis-data"
  redis-server --port 16379 --bind 127.0.0.1 --requirepass ruoyi123 --daemonize yes \
    --dir "$BASE_DIR/.local/redis-data" --pidfile "$BASE_DIR/.local/redis.pid" \
    --logfile "$BASE_DIR/.local/redis.log" --save ""
  sleep 1
fi

# 本地 MinIO（官方 OSS 模块默认配置指向 127.0.0.1:9000，账号 ruoyi / ruoyi123）
if ! curl -s -o /dev/null -m 3 http://127.0.0.1:9000/minio/health/live; then
  if command -v minio >/dev/null 2>&1; then
    echo "启动本地 MinIO (127.0.0.1:9000)..."
    mkdir -p "$BASE_DIR/.local/minio-data"
    MINIO_ROOT_USER=ruoyi MINIO_ROOT_PASSWORD=ruoyi123 nohup minio server "$BASE_DIR/.local/minio-data" \
      --address :9000 --console-address :9001 > "$BASE_DIR/.local/minio.log" 2>&1 &
    sleep 3
  elif docker ps --format '{{.Names}}' 2>/dev/null | grep -q '^iplat-minio$'; then
    echo "启动已存在的 MinIO 容器 iplat-minio..."
    docker start iplat-minio >/dev/null
    sleep 3
  else
    echo "警告：未安装 MinIO（brew install minio），文件上传功能不可用。"
  fi
fi

echo "后端启动中：http://127.0.0.1:18080"
# 规避本机 ~/.aws/config 格式问题对 AWS SDK 的影响（OSS 使用静态凭证，不受影响）
AWS_CONFIG_FILE=/dev/null AWS_SHARED_CREDENTIALS_FILE=/dev/null exec "$JAVA_BIN" -jar "$JAR"
