package com.savanto.morsekeyboard;

import android.util.SparseArray;

public class MorseCode
{
	private SparseArray<String> lookupMorse;

	public MorseCode()
	{
		this.lookupMorse = new SparseArray<String>(54);

		// A .-			10111				23
		this.lookupMorse.put(23, "a");
		// B -...		111010101			469
		this.lookupMorse.put(469, "b");
		// C -.-.		11101011101			1885
		this.lookupMorse.put(1885, "c");
		// D -..		1110101				117
		this.lookupMorse.put(117, "d");
		// E .			1					1
		this.lookupMorse.put(1, "e");
		// F ..-.		101011101			349
		this.lookupMorse.put(349, "f");
		// G --.		111011101			477
		this.lookupMorse.put(477, "g");
		// H ....		1010101				85
		this.lookupMorse.put(85, "h");
		// I ..			101					5
		this.lookupMorse.put(5, "i");
		// J .---		1011101110111		6007
		this.lookupMorse.put(6007, "j");
		// K -.-		111010111			471
		this.lookupMorse.put(471, "k");
		// L .-..		101110101			373
		this.lookupMorse.put(373, "l");
		// M --			1110111				119
		this.lookupMorse.put(119, "m");
		// N -.			11101				29
		this.lookupMorse.put(29, "n");
		// O ---		11101110111			1911
		this.lookupMorse.put(1911, "o");
		// P .--.		10111011101			1501
		this.lookupMorse.put(1501, "p");
		// Q --.-		1110111010111		7639
		this.lookupMorse.put(7639, "q");
		// R .-.		1011101				93
		this.lookupMorse.put(93, "r");
		// S ...		10101				21
		this.lookupMorse.put(21, "s");
		// T -			111					7
		this.lookupMorse.put(7, "t");
		// U ..-		1010111				87
		this.lookupMorse.put(87, "u");
		// V ...-		101010111			343
		this.lookupMorse.put(343, "v");
		// W .--		101110111			375
		this.lookupMorse.put(375, "w");
		// X -..-		11101010111			1879
		this.lookupMorse.put(1879, "x");
		// Y -.--		1110101110111		7543
		this.lookupMorse.put(7543, "y");
		// Z --..		11101110101			1909
		this.lookupMorse.put(1909, "z");

		// 0 -----		1110111011101110111	489335
		this.lookupMorse.put(489335, "0");
		// 1 .----		10111011101110111	96119
		this.lookupMorse.put(96119, "1");
		// 2 ..---		101011101110111		22391
		this.lookupMorse.put(22391, "2");
		// 3 ...--		1010101110111		5495
		this.lookupMorse.put(5495, "3");
		// 4 ....-		10101010111			1367
		this.lookupMorse.put(1367, "4");
		// 5 .....		101010101			341
		this.lookupMorse.put(341, "5");
		// 6 -....		11101010101			1877
		this.lookupMorse.put(1877, "6");
		// 7 --...		1110111010101		7637
		this.lookupMorse.put(7637, "7");
		// 8 ---..		111011101110101		30581
		this.lookupMorse.put(30581, "8");
		// 9 ----.		11101110111011101	122333
		this.lookupMorse.put(122333, "9");

		// . .-.-.-		10111010111010111	95703
		this.lookupMorse.put(95703, ".");
		// , --..--		1110111010101110111	488823
		this.lookupMorse.put(488823, ",");
		// ? ..--..		101011101110101		22389
		this.lookupMorse.put(22389, "?");
		// ' .----.		1011101110111011101	384477
		this.lookupMorse.put(384477, "'");
		// ! -.-.--		1110101110101110111	482679
		this.lookupMorse.put(482679, "!");
		// / -..-.		1110101011101		7517
		this.lookupMorse.put(7517, "/");
		// ( -.--.		111010111011101		30173
		this.lookupMorse.put(30173, "(");
		// ) -.--.-		1110101110111010111	482775
		this.lookupMorse.put(482775, ")");
		// & .-...		10111010101			1493
		this.lookupMorse.put(1493, "&");
		// : ---...		11101110111010101	122325
		this.lookupMorse.put(122325, ":");
		// ; -.-.-.		11101011101011101	120669
		this.lookupMorse.put(120669, ";");
		// = -...-		1110101010111		7511
		this.lookupMorse.put(7511, "=");
		// + .-.-.		1011101011101		5981
		this.lookupMorse.put(5981, "+");
		// - -....-		111010101010111		30039
		this.lookupMorse.put(30039, "-");
		// _ ..--.-		10101110111010111	89559
		this.lookupMorse.put(89559, "_");
		// " .-..-.		101110101011101		23901
		this.lookupMorse.put(23901, "\"");
		// $ ...-..-	10101011101010111	87895
		this.lookupMorse.put(87895, "$");
		// @ .--.-.		10111011101011101	96093
		this.lookupMorse.put(96093, "@");
	}

	public String lookup(int morse)
	{
		return this.lookupMorse.get(morse);
	}
}
