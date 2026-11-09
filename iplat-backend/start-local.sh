#!/usr/bin/env bash
# 启动本地演示后端（RuoYi-Vue-Plus + DMS 演示接口）
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

echo "后端启动中：http://127.0.0.1:18080"
exec "$JAVA_BIN" -jar "$JAR"
