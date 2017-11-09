package org.apache.ignite.ci.conf;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import jersey.repackaged.com.google.common.base.Throwables;
import org.apache.ignite.ci.HelperConfig;
import org.jetbrains.annotations.NotNull;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * Created by Дмитрий on 05.11.2017.
 */
public class PasswordEncoder {

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final int PREF_LEN = 16;
    public static final int POSTF_LEN = 16;
    public static final char CHAR = 'A';

    public static String decode(String encPass) {
        final String clearBlk = printHexBinary(d(parseHexBinary(encPass)));
        final String passBlk = clearBlk.substring(PREF_LEN * 2, clearBlk.length() - POSTF_LEN * 2);
        final String len = passBlk.substring(0, 2);
        final int i = Integer.parseInt(len, 16) - CHAR;
        final String p = passBlk.substring(2);
        return new String(parseHexBinary(p), CHARSET);
    }

    public static String encode(String pass) {
        byte[] bytes = pass.getBytes(CHARSET);
        SecureRandom random = new SecureRandom();
        byte[] pref = random.generateSeed(PREF_LEN);
        byte[] suffix = random.generateSeed(POSTF_LEN);
        int length = bytes.length + CHAR ;
        if ((length & ~0xFF) != 0)
            throw new IllegalStateException();
        byte[] len = get1bLen(length);
        byte[] data = concat(concat(pref, len), concat(bytes, suffix));
        return DatatypeConverter.printHexBinary(e(data));
    }

    private static byte[] e(byte[] data) {
        return doC(data, Cipher.ENCRYPT_MODE);
    }

    private static byte[] d(byte[] data) {
        return doC(data, Cipher.DECRYPT_MODE);
    }

    private static byte[] doC(byte[] data, int mode) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(mode, k());
            return cipher.doFinal(data);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw Throwables.propagate(e);
        }
    }

    @NotNull private static SecretKeySpec k() {
        int reqBytes = 128 / 8;
        String pattern = "Ignite";
        byte[] raw = Strings.repeat(pattern, reqBytes / pattern.length() + 1).substring(0, reqBytes).getBytes();
        return new SecretKeySpec(raw, "AES");
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }

    private static byte[] get1bLen(int length) {
        return parseHexBinary(Strings.padStart(Integer.toHexString(length), 2, '0'));
    }

    public static void main(String[] args) {
        String pass = "mmm";
        String encode = encode(pass);
        System.err.println("Encoded: " +
            HelperConfig.ENCODED_PASSWORD + "=" + encode);
        String decode = decode(encode);
        Preconditions.checkState(decode.equals(pass));
    }
}