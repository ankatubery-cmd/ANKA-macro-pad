package com.example.network

object PcServerScript {
    val PYTHON_SERVER_CODE = """
# =========================================================
# ANKA MACRO PAD - PC SUNUCUSU (server.py)
# =========================================================
# Bu Python sunucusu, Android uygulamanızdan gelen makro
# komutlarını dinler ve bilgisayarınızda gerçekleştirir.
#
# Gereksinimler:
#   pip install pyautogui keyboard
#
# Çalıştırma:
#   python server.py
# =========================================================

import socket
import json
import os
import sys
import threading
import time
import subprocess

try:
    import pyautogui
    pyautogui.FAILSAFE = False
except ImportError:
    pyautogui = None
    print("[UYARI] pyautogui kütüphanesi bulunamadı! 'pip install pyautogui' komutunu çalıştırın.")

try:
    import keyboard
except ImportError:
    keyboard = None
    print("[UYARI] keyboard kütüphanesi bulunamadı! 'pip install keyboard' komutunu çalıştırın.")

HOST = '0.0.0.0' # Tüm ağ arayüzlerini dinler
PORT = 8080      # Android uygulamasında girdiğiniz Port ile aynı olmalıdır

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def handle_key(key_str):
    key_clean = key_str.lower().strip()
    # Özel tuş eşleşmeleri
    key_map = {
        "enter": "enter", "return": "enter",
        "space": "space", "esc": "escape", "escape": "escape",
        "tab": "tab", "backspace": "backspace",
        "win": "win", "windows": "win",
        "volume_mute": "volumemute", "volume_up": "volumeup", "volume_down": "volumedown"
    }
    key_to_send = key_map.get(key_clean, key_clean)

    if keyboard:
        try:
            keyboard.send(key_to_send)
            return True
        except Exception:
            pass
    if pyautogui:
        try:
            pyautogui.press(key_to_send)
            return True
        except Exception as e:
            print(f"[HATA] Key press hatası: {e}")
            return False
    return False

def handle_shortcut(shortcut_str):
    # ör. "CTRL+C", "ALT+TAB", "WIN+SHIFT+S"
    keys = [k.strip().lower() for k in shortcut_str.replace(" ", "").split("+") if k.strip()]
    if not keys:
        return False

    if keyboard:
        try:
            combo = "+".join(keys)
            keyboard.send(combo)
            return True
        except Exception:
            pass
    if pyautogui:
        try:
            pyautogui.hotkey(*keys)
            return True
        except Exception as e:
            print(f"[HATA] Shortcut hatası: {e}")
            return False
    return False

def handle_program(path):
    if not path:
        return False
    try:
        if sys.platform == "win32":
            os.startfile(path)
        elif sys.platform == "darwin":
            subprocess.Popen(["open", path])
        else:
            subprocess.Popen(["xdg-open", path])
        return True
    except Exception as e:
        print(f"[HATA] Program açma hatası ({path}): {e}")
        # İkinci yöntem fallback
        try:
            subprocess.Popen(path, shell=True)
            return True
        except Exception as e2:
            print(f"[HATA] Shell fallback hatası: {e2}")
            return False

def handle_client(conn, addr):
    print(f"\n[+] Telefon bağlandı: {addr[0]}:{addr[1]}")
    conn.settimeout(10.0)
    
    while True:
        try:
            data = conn.recv(4096)
            if not data:
                break
            
            lines = data.decode('utf-8', errors='ignore').strip().split('\n')
            for line in lines:
                if not line:
                    continue
                try:
                    msg = json.loads(line)
                    msg_type = msg.get("type", "")
                    
                    if msg_type == "ping" or msg_type == "handshake":
                        response = json.dumps({"status": "pong", "server": "ANKA PC Server"}) + "\n"
                        conn.sendall(response.encode('utf-8'))
                        continue
                    
                    macro_type = msg.get("macroType", "")
                    primary_val = msg.get("primaryValue", "")
                    extra_json = msg.get("extraValuesJson", "[]")
                    
                    print(f"[KOMUT ALINDI] Tür: {macro_type} | Değer: '{primary_val}'")
                    
                    success = False
                    if macro_type == "KEY":
                        success = handle_key(primary_val)
                    elif macro_type == "SHORTCUT":
                        success = handle_shortcut(primary_val)
                    elif macro_type == "PROGRAM":
                        success = handle_program(primary_val)
                    elif macro_type == "MULTI_PROGRAM":
                        try:
                            paths = json.loads(extra_json)
                            if not paths and primary_val:
                                paths = [primary_val]
                            for p in paths:
                                handle_program(p)
                                time.sleep(0.2)
                            success = True
                        except Exception as e:
                            print(f"[HATA] Multi-program ayrıştırma hatası: {e}")
                    
                    res_status = "ok" if success else "failed"
                    response = json.dumps({"status": res_status, "action": macro_type}) + "\n"
                    conn.sendall(response.encode('utf-8'))
                    
                except json.JSONDecodeError:
                    pass
        except socket.timeout:
            continue
        except Exception as e:
            print(f"[-] İletişim hatası: {e}")
            break

    print(f"[-] Telefon bağlantısı kesildi: {addr[0]}")
    conn.close()

def start_server():
    ip = get_local_ip()
    print("=========================================================")
    print("          🔥 ANKA MACRO PAD - PC SUNUCUSU 🔥           ")
    print("=========================================================")
    print(f"[*] Sunucu Çalışıyor!")
    print(f"[*] Bilgisayar Yerel IP Adresi : {ip}")
    print(f"[*] Port                       : {PORT}")
    print("---------------------------------------------------------")
    print("[*] Android uygulamasının Ayarlar bölümüne yukarıdaki")
    print(f"    IP adresi ({ip}) ve Port ({PORT}) değerlerini girin.")
    print("=========================================================\n")

    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_sock.bind((HOST, PORT))
    server_sock.listen(5)

    print("[*] Telefon bağlantısı bekleniyor...")
    while True:
        try:
            conn, addr = server_sock.accept()
            t = threading.Thread(target=handle_client, args=(conn, addr), daemon=True)
            t.start()
        except KeyboardInterrupt:
            print("\n[*] Sunucu durduruluyor...")
            break
        except Exception as e:
            print(f"[HATA] Sunucu hatası: {e}")

if __name__ == "__main__":
    start_server()
""".trimIndent()
}
