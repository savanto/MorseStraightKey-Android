package com.savanto.morsekeyboard;

import android.util.SparseArray;


public final class MorseCode {
    private static final SparseArray<String> MORSE = new SparseArray<>(54);
    static {
        MORSE.put(0b10111,               "A"); // A .-
        MORSE.put(0b111010101,           "B"); // B -...
        MORSE.put(0b11101011101,         "C"); // C -.-.
        MORSE.put(0b1110101,             "D"); // D -..
        MORSE.put(0b1,                   "E"); // E .
        MORSE.put(0b101011101,           "F"); // F ..-.
        MORSE.put(0b111011101,           "G"); // G --.
        MORSE.put(0b1010101,             "H"); // H ....
        MORSE.put(0b101,                 "I"); // I ..
        MORSE.put(0b1011101110111,       "J"); // J .---
        MORSE.put(0b111010111,           "K"); // K -.-
        MORSE.put(0b101110101,           "L"); // L .-..
        MORSE.put(0b1110111,             "M"); // M --
        MORSE.put(0b11101,               "N"); // N -.
        MORSE.put(0b11101110111,         "O"); // O ---
        MORSE.put(0b10111011101,         "P"); // P .--.
        MORSE.put(0b1110111010111,       "Q"); // Q --.-
        MORSE.put(0b1011101,             "R"); // R .-.
        MORSE.put(0b10101,               "S"); // S ...
        MORSE.put(0b111,                 "T"); // T -
        MORSE.put(0b1010111,             "U"); // U ..-
        MORSE.put(0b101010111,           "V"); // V ...-
        MORSE.put(0b101110111,           "W"); // W .--
        MORSE.put(0b11101010111,         "X"); // X -..-
        MORSE.put(0b1110101110111,       "Y"); // Y -.--
        MORSE.put(0b11101110101,         "Z"); // Z --..

        MORSE.put(0b1110111011101110111, "0"); // 0 -----
        MORSE.put(0b10111011101110111,   "1"); // 1 .----
        MORSE.put(0b101011101110111,     "2"); // 2 ..---
        MORSE.put(0b1010101110111,       "3"); // 3 ...--
        MORSE.put(0b10101010111,         "4"); // 4 ....-
        MORSE.put(0b101010101,           "5"); // 5 .....
        MORSE.put(0b11101010101,         "6"); // 6 -....
        MORSE.put(0b1110111010101,       "7"); // 7 --...
        MORSE.put(0b111011101110101,     "8"); // 8 ---..
        MORSE.put(0b11101110111011101,   "9"); // 9 ----.

        MORSE.put(0b10111010111010111,   "."); // . .-.-.-
        MORSE.put(0b1110111010101110111, ","); // , --..--
        MORSE.put(0b101011101110101,     "?"); // ? ..--..
        MORSE.put(0b1011101110111011101, "'"); // ' .----.
        MORSE.put(0b1110101110101110111, "!"); // ! -.-.--
        MORSE.put(0b1110101011101,       "/"); // / -..-.
        MORSE.put(0b111010111011101,     "("); // ( -.--.
        MORSE.put(0b1110101110111010111, ")"); // ) -.--.-
        MORSE.put(0b10111010101,         "&"); // & .-...
        MORSE.put(0b11101110111010101,   ":"); // : ---...
        MORSE.put(0b11101011101011101,   ";"); // ; -.-.-.
        MORSE.put(0b1110101010111,       "="); // = -...-
        MORSE.put(0b1011101011101,       "+"); // + .-.-.
        MORSE.put(0b111010101010111,     "-"); // - -....-
        MORSE.put(0b10101110111010111,   "_"); // _ ..--.-
        MORSE.put(0b101110101011101,    "\""); // " .-..-.
        MORSE.put(0b10101011101010111,   "$"); // $ ...-..-
        MORSE.put(0b10111011101011101,   "@"); // @ .--.-.
    }

    public static String lookup(int morse) {
        return MORSE.get(morse);
    }
}
