#!/system/bin/sh

# 每30秒检查一次服务
while true; do
    if ! pgrep -f "com.example.batteryoverlay"; then
        am startservice -n com.example.batteryoverlay/.OverlayService
    fi
    sleep 999
done
