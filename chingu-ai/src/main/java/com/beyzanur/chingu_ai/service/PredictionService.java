package com.beyzanur.chingu_ai.service;

import org.springframework.stereotype.Service;

@Service
public class PredictionService {

    // 🧠 Python scikit-learn modelimizin arkada hesapladığı gerçek katsayılar (Ağırlıklar)
    private final float w_metrekare = 30125.63f; // m² başına düşen fiyat çarpanı
    private final float w_odaSayisi = 45226.13f;  // Oda sayısı çarpanı
    private final float w_binaYasi  = -15075.38f; // Bina yaşının fiyatı düşürme katsayısı (eksi değer)
    private final float bias        = -85427.13f;  // Sabit başlangıç değeri (Intercept)

    public PredictionService() {
        // Artık ONNX ortamı yüklemesine gerek yok, anında açılır!
    }

    public float predictHousePrice(float metrekare, float odaSayisi, float binaYasi) {
        // 📈 Çoklu Doğrusal Regresyon Formülü: Y = (w1*X1) + (w2*X2) + (w3*X3) + b
        float tahminiFiyat = (metrekare * w_metrekare) +
                (odaSayisi * w_odaSayisi) +
                (binaYasi * w_binaYasi) +
                bias;

        return tahminiFiyat;
    }
}