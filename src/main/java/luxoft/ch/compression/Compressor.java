package luxoft.ch.compression;

import luxoft.ch.compression.model.Dictionary;

public class Compressor {

	private final Dictionary dictionary;

	public Compressor(String sourceFileName) {
		dictionary = new Dictionary();
		dictionary.initialize(sourceFileName);
	}

	public void extractTokens() {
		dictionary.extractTokens();
	}

	Dictionary getDictionary() {
		return dictionary;
	}

}
