package luxoft.ch.compression.model;

import java.io.Serializable;

public record Token(int id, String value) implements Comparable<Token>, Serializable {

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Token token) {
			return compareTo(token) == 0;
		}
		return false;
	}

	@Override
	public String toString() {
		return "{id: %d, value: %s}".formatted(id, value);
	}

	public int getLength() {
		return value.length();
	}

	@Override
	public int compareTo(Token token) {
		return Integer.compare(id, token.id);
	}

}
