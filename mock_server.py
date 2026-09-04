#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""本地模拟后端：对任意请求返回 200 + 小延迟，仅用于验证压测脚本。"""
import time, json
from http.server import BaseHTTPRequestHandler, HTTPServer

class H(BaseHTTPRequestHandler):
    def _resp(self):
        time.sleep(0.005)  # 模拟 5ms 处理耗时
        body = json.dumps({"code": 0, "data": "ok", "ts": time.time()}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    def do_GET(self):  self._resp()
    def do_POST(self): self._resp()
    def log_message(self, *a): pass  # 静默

if __name__ == "__main__":
    srv = HTTPServer(("127.0.0.1", 9099), H)
    print("mock server on http://127.0.0.1:9099")
    srv.serve_forever()
