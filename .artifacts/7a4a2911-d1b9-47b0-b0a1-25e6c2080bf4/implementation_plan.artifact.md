# Akıllı Ürün Eşleştirme ve Arama Optimizasyonu

"Aynı 3 alakasız resim" sorunu, veritabanındaki verilerin İngilizce olması ve yapay zekanın ürettiği arama terimlerinin veritabanı içeriğiyle tam örtüşmemesinden kaynaklanmaktadır. Bu plan, arama motorunu daha hassas ve "nokta atışı" yapar hale getirecektir.

## Proposed Changes

### [Component] AI Servisi (Gemini)

#### [MODIFY] [AI_Service.kt](file:///C:/Users/yayyi/AndroidStudioProjects/Data_SearchCatolog/app/src/main/java/com/example/data_searchcatolog/AI_Service.kt)
*   **Arama Terimleri Ayrıştırması:** `AiProductMatch` modeline `searchKeywords` (İngilizce - DB için) ve `displayKeywords` (Türkçe - UI için) alanları eklenecek.
*   **Prompt Güçlendirme:** Gemini'a veritabanındaki gerçek kategori isimleri (`smartphones`, `electronics` vb.) öğretilecek. "anahtar1" gibi kötü örnekler yerine, gerçekçi İngilizce arama terimleri (örn: "Samsung", "5G", "Fold") üretmesi söylenecek.

### [Component] Veri Katmanı (Room)

#### [MODIFY] [Dao.kt](file:///C:/Users/yayyi/AndroidStudioProjects/Data_SearchCatolog/app/src/main/java/com/example/data_searchcatolog/room/Dao.kt)
*   **Hassas Arama Sorgusu:** `findSimilarProducts` sorgusu, `OR` mantığından ziyade `AND` ve `Weight` (Ağırlık) mantığına yaklaştırılacak. Alakasız sonuçları engellemek için filtreleme kriterleri sıkılaştırılacak.
*   **Çoklu Anahtar Kelime Araması:** Birden fazla anahtar kelimeyi aynı anda arayan ve eşleşme sayısına göre sıralayan bir mantık kurulacak.

### [Component] UI Katmanı (ResultScreen)

#### [MODIFY] [ResultScreen.kt](file:///C:/Users/yayyi/AndroidStudioProjects/Data_SearchCatolog/app/src/main/java/com/example/data_searchcatolog/ui/screens/ResultScreen.kt)
*   Yapay zekadan gelen `searchKeywords` (İngilizce) veritabanına gönderilecek.
*   Ekranda kullanıcının gördüğü çipler (chips) ise `displayKeywords` (Türkçe) üzerinden oluşturulacak.

## Verification Plan

### Manual Verification
*   Logcat üzerinden üretilen **İngilizce** arama terimleri kontrol edilecek.
*   Bir telefon fotoğrafı çekildiğinde veritabanındaki en yakın telefon modellerinin geldiği doğrulanacak.
*   Eğer veritabanında o ürüne dair hiç veri yoksa (örn: elma çektin ama DB'de sadece telefon var), listenin alakasız sonuçlarla dolmak yerine boş kalması sağlanacak.
