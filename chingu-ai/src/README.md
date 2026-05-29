📝 Bağımlılık Yönetimi (POM.xml)
BOM (Bill of Materials): Spring AI projelerinde versiyon karmaşasını önlemek için spring-ai-bom kullanılır. Bu sayede her kütüphaneye tek tek versiyon yazmak zorunda kalmazsın, hepsi birbiriyle uyumlu çalışır.

Scope Runtime: Veritabanı sürücüleri (MSSQL gibi) için scope: runtime kullanılır. Çünkü bu kütüphaneye kod yazarken değil, uygulama çalışırken ihtiyaç duyarız.

🔌 Veritabanı (MSSQL)
DDL-Auto: update: Bu ayar çok kritiktir. Java tarafında bir tablo (Entity) oluşturduğunda, MSSQL'e gidip elle tablo yaratmana gerek kalmaz; Spring Boot senin yerine tabloyu oluşturur veya günceller.

Dialect: Hibernate'e hangi SQL dilini konuşacağını söyler. MSSQL kullandığımız için SQLServerDialect belirlemek performans ve uyumluluk için önemlidir.

🤖 Spring AI & Mimari
ChatClient.Builder: Spring AI'ın yeni nesil akışıdır. Eskiden sadece ChatModel kullanılırdı ama Builder yapısı bize "akıcı" (fluent) bir kod yazma imkanı sunar (Örn: .user().call().content()).

Stateless Yapı: Şu an yazdığımız AI servisi "durumsuzdur" (stateless). Yani her mesaj yeni bir başlangıçtır. Bir önceki mesajı hatırlatmak için ilerde Advisor yapılarını veya DB'yi kullanacağız.
[ ] Hata Çözümü: @SpringBootTest hatasını aşmak için ya DB ayarlarını tam yap ya da testi @Disabled ile kapat.

[ ] Güvenlik: API Key'leri asla GitHub'a açık bir şekilde yükleme (.gitignore dosyasını kontrol et).

[ ] İsimlendirme: Proje adı "Chingu" (Arkadaş) olduğu için AI'yı kişiselleştirirken "System Prompt" kullanmayı unutma.
Entity: Veritabanındaki her bir satırı temsil eden Java nesnesidir.

JPA & Hibernate: "Ben Java nesnesini veriyorum, sen bunu arka planda SQL'e çevir ve tabloya kaydet" dememizi sağlayan teknolojidir.

NVARCHAR(MAX): MSSQL'de uzun mesajlar için kullanılır. Normal String bazen kısa kalabilir, bu yüzden @Column ile bunu belirttik.