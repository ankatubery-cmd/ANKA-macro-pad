# ANKA Macro Pad — Uzantı Paketi Sistemi

ANKA Macro Pad uzantıları Android APK'sı değildir. Uzantılar ZIP olarak içe aktarılır ve uygulamanın özel `extensions/` dizininde çalıştırılır.

## Paket yapısı

Bir uzantı ZIP'i şu yapıda olabilir:

- `manifest.json`
- `index.html`
- `css/`
- `js/`
- `assets/`

ZIP'in içinde tek bir üst klasör bulunması da desteklenir.

## manifest.json

Gerekli alanlar:

- `id`: sadece harf, rakam, `.`, `_`, `-`; en fazla 64 karakter
- `name`
- `entry`: HTML giriş dosyasının göreli yolu

Desteklenen diğer alanlar:

- `version`
- `description`
- `icon`
- `developer`
- `minAnkaVersion`
- `permissions`
- `actions`

Örnek:

```json
{
  "id": "my-extension",
  "name": "Benim Uzantım",
  "version": "1.0.0",
  "description": "ANKA Macro Pad için örnek uzantı.",
  "icon": "extension",
  "entry": "index.html",
  "developer": "Geliştirici",
  "minAnkaVersion": "1.0.0",
  "permissions": ["keyboard", "notification"],
  "actions": [
    {
      "id": "hello",
      "name": "Kısayol Gönder",
      "icon": "keyboard",
      "type": "EXTENSION_ACTION",
      "value": "CTRL+H"
    }
  ]
}
```

## JavaScript API

İzin verilmişse uzantının HTML/JavaScript kodu `window.anka` üzerinden ANKA Macro Pad ile iletişim kurabilir.

- `anka.pressKey("CTRL+H")`
- `anka.openApp("obs")`
- `anka.showNotification("Mesaj")`
- `anka.getProfiles()`
- `anka.getPermissions()`

Her API çağrısı manifest'teki izinlerle kontrol edilir.

## ZIP güvenliği

İçe aktarma sırasında:

- `../` ile klasör dışına çıkma engellenir.
- Mutlak dosya yolları engellenir.
- Uzantı ID'si doğrulanır.
- Paket en fazla 250 dosya içerebilir.
- Açılmış toplam boyut 15 MB ile sınırlıdır.
- `entry` dosyası uzantının kendi klasörü içinde olmak zorundadır.

Bu sistem APK derlemesi gerektirmez; yeni bir uzantı eklemek için yalnızca geçerli bir ZIP paketi içe aktarılır.


## HTML Widget desteği

Bir uzantının `entry` dosyası HTML ise, uzantıdan ana panele **HTML widget** eklenebilir.

- Uzantı ZIP'i kurulduktan sonra uzantı menüsünden **Widget Oluştur** seçilir.
- `actions` listesi boşsa widget, manifestteki `entry` dosyasını doğrudan widget içinde WebView olarak gösterir.
- Eski sürümlerde oluşturulmuş, `actions` listesi boş olan uzantı widget'ları da otomatik olarak HTML widget olarak açılır.
- HTML widget içindeki JavaScript, normal uzantı ile aynı `window.anka` köprüsünü kullanır.
- `notification`, `keyboard`, `pc_connection` ve `profiles` gibi izinler manifestten uygulanır.
- Widget HTML'si yalnızca kendi uzantı klasörü içindeki yerel dosyalara erişebilir; `../`, mutlak yollar ve uzantı dışı `file://` gezinmeleri engellenir.

Örneğin Timer gibi bir uzantı için `manifest.json` içinde `"entry": "index.html"` ve `"actions": []` bulunması yeterlidir. Timer'ın HTML arayüzü widget'ın içinde çalışır.
