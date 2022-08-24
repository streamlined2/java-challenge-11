package luxoft.ch.compression.tool;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import luxoft.ch.compression.model.Dictionary;
import luxoft.ch.compression.model.Token;
import luxoft.ch.compression.model.TokenEntry;

public class Compressor {

	private final Dictionary dictionary;
	private final Set<Token> tokens;
	private final NavigableMap<Integer, TokenEntry> tokenChain;

	public Compressor(String sourceFileName) {
		dictionary = new Dictionary();
		tokens = new HashSet<>();
		tokenChain = new TreeMap<>();
		dictionary.initialize(sourceFileName);
	}
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public Set<Token> getTokens() {
		return tokens;
	}

	public NavigableMap<Integer, TokenEntry> getTokenChain() {
		return tokenChain;
	}

	public void process() {
		dictionary.growLargerTokens();
		formTokenChain();
	}

	private void formTokenChain() {
		int tokenId = 0;
		for (var iter = dictionary.getTokensByTotalSpaceReversed().iterator(); iter.hasNext();) {
			var token = iter.next();
			if (atLeastTwoTokenEntriesMayBeApplied(token)) {
				tokens.add(new Token(tokenId, token.getKey()));
				for (var startPosition : token.getValue()) {
					final int endPosition = getEndPosition(token, startPosition);
					if (isTokenEntryMayBeApplied(startPosition, endPosition)) {
						tokenChain.put(endPosition, new TokenEntry(tokenId, startPosition, endPosition));
					}
				}
				tokenId++;
			}
		}
	}

	private int getEndPosition(Entry<String, List<Integer>> token, int startPosition) {
		return startPosition + token.getKey().length() - 1;
	}

	private boolean isTokenEntryMayBeApplied(int startPosition, int endPosition) {
		for (var iter = tokenChain.tailMap(startPosition, true).entrySet().iterator(); iter.hasNext();) {
			if (iter.next().getValue().isIntersected(startPosition, endPosition)) {
				return false;
			}
		}
		return true;
	}

	private boolean atLeastTwoTokenEntriesMayBeApplied(Entry<String, List<Integer>> tokenEntry) {
		return tokenEntry.getValue().stream().filter(
				startPosition -> isTokenEntryMayBeApplied(startPosition, getEndPosition(tokenEntry, startPosition)))
				.count() >= 2;
	}

}
