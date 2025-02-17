package org.example;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Keys {

    public static PublicKey stringToPublicKey(String keys) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(keys);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    public static  String publickeyToString(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}
