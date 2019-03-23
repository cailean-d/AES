package sample;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AESCore {

    private int[] Sbox = {
            0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
            0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
            0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
            0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
            0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
            0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
            0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
            0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
            0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
            0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
            0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
            0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
            0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
            0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
            0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
            0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16
    };

    private int[] shiftRowTab = {0x00,0x05,0x0A,0x0F,0x04,0x09,0x0E,0x03,0x08,0x0D,0x02,0x07,0x0C,0x01,0x06,0x0B};
    private int[] sbox_inv = new int[256];
    private int[] shiftRowTab_Inv = new int[16];
    private int[] xtime = new int[256];

    public void init() {

        for(int i = 0; i < 256; i++)
            sbox_inv[Sbox[i]] = i;

        for(int i = 0; i < 16; i++)
            shiftRowTab_Inv[shiftRowTab[i]] = i;

        for(int i = 0; i < 128; i++) {
            xtime[i] = i << 1;
            xtime[128 + i] = (i << 1) ^ 0x1b;
        }

    }

    private byte[] subArray(ByteBuffer buffer, int start, int end) {
        byte[] temp = buffer.array();
        return Arrays.copyOfRange(temp, start, end);
    }

    public byte[] expandKey(byte[] key) throws Exception {

        int kl = key.length, ks = 0, rcon = 1;

        switch (kl) {
            case 16: ks = 16 * (10 + 1); break;
            case 24: ks = 16 * (12 + 1); break;
            case 32: ks = 16 * (14 + 1); break;
            default:
                throw new Exception("Ошибка: Ключ должен состоять из 16, 24, 32 байт!");
        }

        ByteBuffer bbuf = ByteBuffer.allocate(ks);
        bbuf.put(key);

        for(int i = kl; i < ks; i += 4) {

            byte[] temp = subArray(bbuf, i -4, i);

            if (i % kl == 0) {

                temp = new byte[]{
                        (byte)(Sbox[temp[1] & 0xFF] ^ rcon),
                        (byte)(Sbox[temp[2] & 0xFF]),
                        (byte)(Sbox[temp[3] & 0xFF]),
                        (byte)(Sbox[temp[0] & 0xFF])
                };

                if ((rcon <<= 1) >= 256) {
                    rcon ^= 0x11b;
                }

            } else if ((kl > 24) && (i % kl == 16)) {

                temp = new byte[]{
                        (byte)(Sbox[temp[0] & 0xFF]),
                        (byte)(Sbox[temp[1] & 0xFF]),
                        (byte)(Sbox[temp[2] & 0xFF]),
                        (byte)(Sbox[temp[3] & 0xFF])
                };

            }

            for(int j = 0; j < 4; j++) {
                bbuf.put(i + j, (byte)(bbuf.get(i + j - kl) ^ temp[j]));
            }

        }

        return bbuf.array();
    }

    public byte[] encrypt(byte[] block, byte[] key) {

        int l = key.length;
        int i;

        addRoundKey(block, Arrays.copyOfRange(key, 0, 16));

        for(i = 16; i < l - 16; i += 16) {
            subBytes(block, Sbox);
            shiftRows(block, shiftRowTab);
            mixColumns(block);
            addRoundKey(block, Arrays.copyOfRange(key, i, i + 16));
        }
        subBytes(block, Sbox);
        shiftRows(block, shiftRowTab);
        addRoundKey(block, Arrays.copyOfRange(key, i, l));

        return block;
    }

    public byte[] decrypt(byte[] block, byte[] key) {
        int l = key.length;
        addRoundKey(block, Arrays.copyOfRange(key, l - 16, l));
        shiftRows(block, shiftRowTab_Inv);
        subBytes(block, sbox_inv);
        for(int i = l - 32; i >= 16; i -= 16) {
            addRoundKey(block, Arrays.copyOfRange(key, i, i + 16));
            mixColumnsInv(block);
            shiftRows(block, shiftRowTab_Inv);
            subBytes(block, sbox_inv);
        }
        addRoundKey(block, Arrays.copyOfRange(key, 0, 16));

        return block;
    }

    private void subBytes(byte[] state, int[] sbox) {

        for(int i = 0; i < 16; i++) {
            state[i] = (byte)sbox[state[i] & 0xFF];
        }

    }

    private void addRoundKey(byte[] state, byte[] rkey) {

        for(int i = 0; i < 16; i++) {
            state[i] ^= rkey[i];
        }

    }

    private void shiftRows(byte[] state, int[] shift_tab) {

        byte[] h = state.clone();

        for(int i = 0; i < 16; i++) {
            state[i] = h[shift_tab[i]];
        }

    }

    private void mixColumns(byte[] state) {

        for(int i = 0; i < 16; i += 4) {
            byte s0 = state[i], s1 = state[i + 1];
            byte s2 = state[i + 2], s3 = state[i + 3];
            byte h = (byte)(s0 ^ s1 ^ s2 ^ s3);
            state[i] ^= h ^ xtime[(s0 ^ s1) & 0xFF];
            state[i + 1] ^= h ^ xtime[(s1 ^ s2) & 0xFF];
            state[i + 2] ^= h ^ xtime[(s2 ^ s3) & 0xFF];
            state[i + 3] ^= h ^ xtime[(s3 ^ s0) & 0xFF];
        }

    }

    private void mixColumnsInv(byte[] state) {
        for(int i = 0; i < 16; i += 4) {
            byte s0 = state[i], s1 = state[i + 1];
            byte s2 = state[i + 2], s3 = state[i + 3];
            byte h = (byte)(s0 ^ s1 ^ s2 ^ s3);
            byte xh = (byte)xtime[h & 0xFF];
            byte h1 = (byte)(xtime[xtime[(xh ^ s0 ^ s2) & 0xFF]] ^ h);
            byte h2 = (byte)(xtime[xtime[(xh ^ s1 ^ s3) & 0xFF]] ^ h);
            state[i] ^= h1 ^ xtime[(s0 ^ s1) & 0xFF];
            state[i + 1] ^= h2 ^ xtime[(s1 ^ s2) & 0xFF];
            state[i + 2] ^= h1 ^ xtime[(s2 ^ s3) & 0xFF];
            state[i + 3] ^= h2 ^ xtime[(s3 ^ s0) & 0xFF];
        }
    }
}
