package luxoft.ch.compression.tool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import luxoft.ch.compression.CompressionException;
import luxoft.ch.compression.model.Dictionary;
import luxoft.ch.compression.model.Token;
import luxoft.ch.compression.model.TokenEntry;

public class Compressor {

	private static final int MIN_TOKEN_LENGTH = 10;

	private final Dictionary dictionary;
	private final Set<Token> tokens;
	private final NavigableMap<Integer, TokenEntry> tokenChain;

	public Compressor(String sourceFileName) {
		dictionary = new Dictionary();
		tokens = new HashSet<>();
		tokenChain = new TreeMap<>();
		dictionary.initialize(sourceFileName);
	}

	public SortedSet<Token> getTokens() {
		return new TreeSet<>(tokens);
	}

	public SortedSet<TokenEntry> getTokenEntries() {
		return new TreeSet<>(tokenChain.values());
	}

	public void compress() {
		dictionary.growLargerTokens();
		formSetOfTokensAndChain();
	}

	public void save(String targetFileName) {
		try (ObjectOutputStream outStream = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(new File(targetFileName))))) {
			outStream.writeObject(tokens);
			outStream.writeObject(tokenChain);
			outStream.writeObject(collectOriginalRanges());
		} catch (IOException e) {
			throw new CompressionException("cannot open file %s".formatted(targetFileName), e);
		}
	}

	private List<String> collectOriginalRanges() {
		List<String> ranges = new ArrayList<>();
		int start = 0;
		for (var iter = tokenChain.values().iterator(); iter.hasNext();) {
			var tokenEntry = iter.next();
			ranges.add(getOriginalData(start, tokenEntry.startPosition()));
			start = tokenEntry.endPosition() + 1;
		}
		ranges.add(getOriginalData(start, dictionary.getCharCount()));
		return ranges;
	}

	private String getOriginalData(int start, int finish) {
		StringBuilder builder = new StringBuilder();
		for (int index = start; index < finish; index++) {
			builder.append(dictionary.getNextChar(index));
		}
		return builder.toString();
	}

	private void formSetOfTokensAndChain() {
		int tokenId = 0;
		for (var iter = dictionary.getTokensByTotalSpaceReversed(MIN_TOKEN_LENGTH).iterator(); iter.hasNext();) {
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
