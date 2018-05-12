package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class AESWrapper {

    private AES aes = new AES();

    public byte[] init(String myKey) throws Exception {
        aes.init();
        return aes.expandKey(myKey.getBytes());
    }

    public String encrypt(String myString, byte[] key) throws IOException {

        byte[] bytes = myString.getBytes();

        if (bytes.length > 16) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            for(int i = 0; i < bytes.length; i += 16){
                if (i + 16 > bytes.length) {
                    byte[] t = encryptBlock(Arrays.copyOfRange(bytes, i, bytes.length), key);
                    output.write(t);
                } else {
                    byte[] t = encryptBlock(Arrays.copyOfRange(bytes, i, i + 16), key);
                    output.write(t);
                }
            }

            return new String(Base64.getEncoder().encode(output.toByteArray()));

        } else {

            if (bytes.length == 16) {
                byte[] c = concatenateByteArrays(encryptBlock(bytes,key), encryptBlock("".getBytes(),key));
                return new String(Base64.getEncoder().encode(c));
            } else {
                byte[] encrypted = encryptBlock(bytes,key);
                return new String(Base64.getEncoder().encode(encrypted));
            }

        }

    }

    public String decrypt (String myString, byte[] key) throws IOException {

        byte[] bytes = Base64.getDecoder().decode(myString);

        if (bytes.length > 16) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            for(int i = 0; i < bytes.length; i += 16){
                if (i + 16 > bytes.length) {
                    byte[] t = decryptBlock(Arrays.copyOfRange(bytes, i, bytes.length), key);
                    output.write(t);
                } else {
                    byte[] t = decryptBlock(Arrays.copyOfRange(bytes, i, i + 16), key);
                    output.write(t);
                }
            }

            return new String(output.toByteArray());

        } else {

            byte[] encoded = decryptBlock(bytes, key);
            return new String(encoded);

        }

    }

    private byte[] encryptBlock (byte[] bytes, byte[] key) {
        byte[] full_block = completeBlock(bytes);
        return aes.encrypt(full_block, key);
    }

    private byte[] decryptBlock (byte[] bytes, byte[] key) {
        byte[] decrypted = aes.decrypt(bytes, key);
        return deleteNullBytes(decrypted);
    }

    private byte[] completeBlock(byte[] block) {

        if (block.length == 16) {

            return block;

        } else {

            ByteBuffer d_block = ByteBuffer.allocate(16);
            d_block.put(block);
            byte len = (byte)(16 - block.length);

            for (int i = 0; i < len; i++) {
                d_block.put(len);
            }

            return d_block.array();
        }
    }

    private byte[] deleteNullBytes(byte[] block) {

        List<Byte> blist = Arrays.asList(byteArrayToByteObj(block));

        for (int i = 0; i < blist.size(); i++) {
            if(blist.get(i) == 0) {
                blist.remove(i);
            }
        }

        return byteListToBytes(blist);
    }

    private byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private byte[] byteListToBytes (List<Byte> array) {

        byte[] temp = new byte[array.size()];

        for (int i = 0; i < array.size(); i++) {
           temp[i] = array.get(i);
        }

        return temp;
    }

    private Byte[] byteArrayToByteObj (byte[] array) {

        Byte[] byteObjects = new Byte[array.length];

        int i=0;

        for(byte b: array) {
            byteObjects[i++] = b;
        }

        return byteObjects;
    }
}
