package com.example.data

enum class MacroType(val displayName: String, val description: String) {
    KEY("Klavye Tuşu", "Tek bir klavye tuşu (ör. Enter, F5, Space, H)"),
    SHORTCUT("Klavye Kısayolu", "Tuş kombinasyonu (ör. Ctrl + C, Alt + Tab, Win + D)"),
    PROGRAM("Program Açma", "Bilgisayardaki bir programı başlatır"),
    MULTI_PROGRAM("Çoklu Program", "Tek butonla birden fazla programı sırayla açar"),
    EXTENSION_ACTION("Uzantı İşlemi", "Yüklü bir uzantının sunduğu özel işlemi çalıştırır"),
    WIDGET("Widget", "Uzantı widget'ı - çoklu mini kontrol, sayaç ve durum paneli")
}
