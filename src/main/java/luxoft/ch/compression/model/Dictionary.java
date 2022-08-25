package luxoft.ch.compression.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import luxoft.ch.compression.CompressionException;

import java.util.StringJoiner;
import java.util.stream.Stream;

public class Dictionary {

	private static final int INITIAL_TOKEN_LENGTH = 2;
	private static final int BUFFER_CAPACITY = 1 * 1024 * 1024;

	private static final Comparator<Entry<String, List<Integer>>> TOKEN_TOTAL_SPACE_REVERSED_COMPARATOR = Comparator
			.comparingInt(Dictionary::getTokenTotalSpace).reversed();

	private final Map<String, List<Integer>> tokens;
	private final CharBuffer buffer;

	public Dictionary() {
		tokens = new HashMap<>();
		this.buffer = CharBuffer.allocate(BUFFER_CAPACITY);
	}

	public void addTokenEntry(String token, Integer entryIndex) {
		addTokenEntry(tokens, token, entryIndex);
	}

	private static void addTokenEntry(Map<String, List<Integer>> tokens, String token, Integer entryIndex) {
		var entries = tokens.get(token);
		if (entries == null) {
			entries = new ArrayList<>();
		}
		entries.add(entryIndex);
		tokens.put(token, entries);
	}

	public void initialize(String sourceFileName) {
		try (Reader reader = new BufferedReader(new FileReader(new File(sourceFileName)))) {
			reader.read(buffer);
			buffer.flip();
			char[] tokenData = new char[Dictionary.INITIAL_TOKEN_LENGTH];
			final int size = buffer.limit() - tokenData.length + 1;
			for (int index = 0; index < size; index++) {
				buffer.get(index, tokenData);
				addTokenEntry(String.valueOf(tokenData), index);
			}
			deleteSolitaries(Dictionary.INITIAL_TOKEN_LENGTH);
		} catch (IOException e) {
			throw new CompressionException("cannot open file %s".formatted(sourceFileName), e);
		}
	}

	private void deleteSolitaries(int length) {
		for (var iter = tokens.entrySet().iterator(); iter.hasNext();) {
			var entry = iter.next();
			if (isSolitary(entry) && hasLength(entry, length)) {
				iter.remove();
			}
		}
	}

	private static boolean isSolitary(Entry<String, List<Integer>> entry) {
		return entry.getValue().size() <= 1;
	}

	private static boolean hasLength(Entry<String, List<Integer>> entry, int length) {
		return entry.getKey().length() == length;
	}

	public void growLargerTokens() {
		int tokenLength = INITIAL_TOKEN_LENGTH;
		while (growTokensOfLength(tokenLength) > 0) {
			tokenLength++;
		}
	}

	private int growTokensOfLength(int tokenLength) {
		Map<String, List<Integer>> newTokenEntries = new HashMap<>();
		for (var tokenIter = tokens.entrySet().iterator(); tokenIter.hasNext();) {
			final var token = tokenIter.next();
			if (hasLength(token, tokenLength) && !isSolitary(token)) {
				expandTokenEntries(newTokenEntries, token);
				if (isSolitary(token)) {
					tokenIter.remove();
				}
			}
		}
		tokens.putAll(newTokenEntries);
		return newTokenEntries.size();
	}

	private int expandTokenEntries(Map<String, List<Integer>> newTokenEntries,
			Entry<String, List<Integer>> tokenEntry) {
		int expandedTokenEntries = 0;
		Map<Character, Integer> charEntries = countCharEntries(tokenEntry);
		for (var indexIter = tokenEntry.getValue().iterator(); indexIter.hasNext();) {
			final int index = indexIter.next();
			final Optional<Character> nextChar = getNextChar(tokenEntry, index);
			if (nextChar.isPresent()) {
				final Integer charCount = charEntries.get(nextChar.get());
				if (charCount != null && charCount.intValue() > 1) {
					String expandedToken = tokenEntry.getKey() + nextChar.get();
					addTokenEntry(newTokenEntries, expandedToken, index);
					indexIter.remove();
					expandedTokenEntries++;
				}
			}
		}
		return expandedTokenEntries;
	}

	private Map<Character, Integer> countCharEntries(Entry<String, List<Integer>> tokenEntry) {
		Map<Character, Integer> counts = new HashMap<>();
		for (var index : tokenEntry.getValue()) {
			getNextChar(tokenEntry, index)
					.ifPresent(nextChar -> counts.merge(nextChar, 1, (oldValue, value) -> oldValue + 1));
		}
		return counts;
	}

	private Optional<Character> getNextChar(Entry<String, List<Integer>> tokenEntry, int index) {
		final int nextCharIndex = index + tokenEntry.getKey().length();
		if (nextCharIndex >= buffer.limit()) {
			return Optional.empty();
		}
		return Optional.of(buffer.get(nextCharIndex));
	}

	public char getNextChar(int index) {
		if (index >= buffer.limit()) {
			throw new CompressionException(
					"wrong index %d greater than buffer limit %d".formatted(index, buffer.limit()));
		}
		return buffer.get(index);
	}

	public int getCharCount() {
		return buffer.limit();
	}

	public Stream<Entry<String, List<Integer>>> getTokensByTotalSpaceReversed() {
		return tokens.entrySet().stream().sorted(TOKEN_TOTAL_SPACE_REVERSED_COMPARATOR);
	}

	private static int getTokenTotalSpace(Entry<String, List<Integer>> entry) {
		return entry.getKey().length() * entry.getValue().size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for (var entry : tokens.entrySet()) {
			builder.append("  token: ").append(entry.getKey()).append(", indices: ");
			StringJoiner join = new StringJoiner(",", "[", "]");
			for (var index : entry.getValue()) {
				join.add(Integer.toString(index));
			}
			builder.append(join.toString()).append("\n");
		}
		builder.append("}\n");
		return builder.toString();
	}

}
