package com.pandamedia.utilities;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**q
 *
 * @author Hau Gilles Che
 */
public class PasswordHelper {
    private static SecureRandom secureRand= new SecureRandom();
    
    public PasswordHelper(){
    }
    
    /**
     * Generates a random salt
     *
     * @author The Bodzay
     * @return The randomly generated salt
     */
    public String getSalt() {
        return new BigInteger(140, secureRand).toString(32);
    }

    /**
     * Creates a hash from the given password and salt
     *
     * @author The Bodzay
     * @param password The password to be used in the hash
     * @param salt The salt to be used in the hash
     * @return The has
     */
    public byte[] hash(String password, String salt) {
        if (password == null || password.equals("")) {
            return null;
        }

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 1024, 512);

            SecretKey key = skf.generateSecret(spec);
            byte[] hash = key.getEncoded();
            return hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
