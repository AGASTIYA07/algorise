#!/usr/bin/env bash
# Quick launcher for Stock Market Portfolio Tracker Web Application

# Find Java binary
JAVA_BIN=""
if [ -x "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="java"
else
    CANDIDATE=$(find /Library/Java/JavaVirtualMachines -name java 2>/dev/null | grep '/bin/java$' | head -n 1)
    if [ -n "$CANDIDATE" ]; then
        JAVA_BIN="$CANDIDATE"
    fi
fi

if [ -z "$JAVA_BIN" ]; then
    echo "❌ Error: Java not found. Please install Java (JDK 17+) to run the web server."
    echo "💡 Note: You can still open web/index.html directly in your browser!"
    open index.html 2>/dev/null || xdg-open index.html 2>/dev/null
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PORT=${1:-8080}

echo "🚀 Starting Stock Market Web Server on port $PORT..."
echo "🌐 URL: http://localhost:$PORT/"

# Open browser after a brief pause
(sleep 1 && (open "http://localhost:$PORT/" 2>/dev/null || xdg-open "http://localhost:$PORT/" 2>/dev/null)) &

exec "$JAVA_BIN" -jar "$DIR/StockMarketWebServer.jar" "$PORT"
