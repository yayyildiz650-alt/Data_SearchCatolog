# Model ve Mimari Düzenleme Planı

Projenizde şu an iki ana sorun bulunuyor:
1. **Model Çakışması:** Hem `retrofit` hem de `room` paketlerinde aynı isimli (`Product_Model`) sınıflar var ama yapıları farklı. Bu durum, internetten gelen veriyi veritabanına kaydederken "tip uyumsuzluğu" hatasına yol açıyor.
2. **MainActivity Yapısı:** `Greeting` gibi arayüz (Composable) fonksiyonları `onCreate` içerisinde tanımlanmış, bu durum uygulamanın çalışmasını engeller.

Bu plan ile modelleri tek bir yerde toplayıp, MainActivity'deki hataları düzelteceğiz.

## Kullanıcı İncelemesi Gerekenler

> [!IMPORTANT]
> - `Product_Model` sınıfı tek bir paket altında (`com.example.data_searchcatolog.model`) toplanacak.
> - `RetrofitClient` ve `ProductApiService` bu yeni modeli kullanacak şekilde güncellenecek.
> - `MainActivity` içindeki Composable fonksiyonlar dışarı çıkarılacak.

## Önerilen Değişiklikler

### 1. Model Katmanı Düzenleme
*   [DELETE] `retrofit/Product_Model.kt` (Eski basit model silinecek)
*   [DELETE] `room/Room_Model.kt` (İsim karışıklığı için silinecek)
*   [NEW] `model/ProductModels.kt` (Hem Room hem Retrofit için kullanılacak ortak ve kapsamlı model buraya gelecek)

### 2. Arayüz ve Mantık Düzenleme (MainActivity)
*   `Greeting` ve `GreetingPreview` fonksiyonları sınıf dışına taşınacak.
*   `lifecycleScope` içindeki veri çekme ve kaydetme mantığı yeni model yapısına göre güncellenecek.

### 3. API Servis Güncellemesi
*   `ProductApiService` içindeki `@GET` isteği ve dönüş tipi ortak modele göre güncellenecek.

## Doğrulama Planı

### Otomatik Testler
*   `gradle_sync` ile bağımlılık ve paket yolları doğrulanacak.
*   `gradle_build` ile tüm projenin hatasız derlendiği kontrol edilecek.

### Manuel Doğrulama
*   Logcat üzerinden "RoomTest" etiketi ile verilerin internetten çekilip Room'a başarıyla kaydedildiği izlenecek.
