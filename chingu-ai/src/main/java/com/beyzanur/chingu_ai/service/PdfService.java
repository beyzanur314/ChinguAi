package com.beyzanur.chingu_ai.service; // Kendi paket yoluna göre düzenle

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    public List<Document> extractAndSplitPdf(MultipartFile file) throws IOException {
        // 1. PDF dosyasını bir InputStreamResource olarak sarmala
        InputStreamResource resource = new InputStreamResource(file.getInputStream());

        // 2. TikaDocumentReader kullanarak PDF içindeki metni oku
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get(); // PDF'teki metni Document nesnesine çevirir

        // 3. Büyük metni Llama'nın rahat anlayacağı 800 token'lık (yaklaşık 500-600 kelime) parçalara böl
        TokenTextSplitter splitter = new TokenTextSplitter(800, 350, 5, 10000, true);

        // 4. Parçalanmış döküman listesini geri döndür
        return splitter.apply(documents);
    }
}