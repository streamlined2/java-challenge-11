package luxoft.ch.compression.model;

import luxoft.ch.compression.CompressionException;

public record TokenEntry(int tokenId, int startPosition, int endPosition) implements Comparable<TokenEntry> {

	public TokenEntry {
		if (startPosition > endPosition)
			throw new CompressionException("starting position %d should be less or equal to ending position %d"
					.formatted(startPosition, endPosition));
	}

	@Override
	public int hashCode() {
		return startPosition;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TokenEntry tokenEntry) {
			return compareTo(tokenEntry) == 0;
		}
		return false;
	}

	@Override
	public String toString() {
		return "{token: %d, range: [%d, %d]}".formatted(tokenId, startPosition, endPosition);
	}

	public int getLength() {
		return endPosition - startPosition + 1;
	}

	@Override
	public int compareTo(TokenEntry token) {
		return Integer.compare(startPosition, token.startPosition);
	}

	public boolean isIntersected(int start, int end) {
		return end >= startPosition() && endPosition() >= start;
	}

}
