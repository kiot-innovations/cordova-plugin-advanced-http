package com.silkimen.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import java.util.Base64;

public class CustomTrustManager implements X509TrustManager {
    private final X509TrustManager originalTrustManager;
    private final String expectedPublicKey;

    public CustomTrustManager(X509TrustManager originalTrustManager, String expectedPublicKey) {
        this.originalTrustManager = originalTrustManager;
        this.expectedPublicKey = expectedPublicKey;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        originalTrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        String rawpublicKey = getRawPublicKey(chain[0]);
        String publicKey = getPublicKey(chain[0]);
        if (expectedPublicKey.equals(rawpublicKey)) {
            originalTrustManager.checkServerTrusted(chain, authType);
        } else {
            throw new CertificateException("Server's public key does not match the expected public key");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return originalTrustManager.getAcceptedIssuers();
    }

    private String getPublicKey(X509Certificate certificate) throws CertificateException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] publicKey = digest.digest(certificate.getPublicKey().getEncoded());
            return Base64.getEncoder().encodeToString(publicKey);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException("No such algorithm", e);
        }
    }

    private String getRawPublicKey(X509Certificate certificate) {
        // Get the raw public key bytes, not hashed
        byte[] publicKeyBytes = certificate.getPublicKey().getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }
}
