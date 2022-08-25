package luxoft.ch.compression.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import luxoft.ch.compression.CompressionException;
import luxoft.ch.compression.model.Token;
import luxoft.ch.compression.model.TokenEntry;

public class Decompressor {

	private final String sourceFileName;

	public Decompressor(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public void decompressAndSave(String targetFileName) {
		try (ObjectInputStream inStream = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(new File(sourceFileName))))) {
			Set<Token> tokens = (Set<Token>) inStream.readObject();
			NavigableMap<Integer, TokenEntry> tokenChain = (NavigableMap<Integer, TokenEntry>) inStream.readObject();
			List<String> ranges = (List<String>) inStream.readObject();
			save(targetFileName, tokens, tokenChain, ranges);
		} catch (IOException | ClassNotFoundException e) {
			throw new CompressionException("cannot open file %s".formatted(sourceFileName), e);
		}
	}

	private void save(String targetFileName, Set<Token> tokens, NavigableMap<Integer, TokenEntry> tokenChain,
			List<String> ranges) {
		try (DataOutputStream outStream = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(new File(targetFileName))))) {
			var tokenIterator = tokenChain.values().iterator();
			for (int index = 0; index < ranges.size(); index++) {
				outStream.writeChars(ranges.get(index));
				if (tokenIterator.hasNext()) {
					var tokenId = tokenIterator.next().tokenId();
					outStream.writeChars(findTokenById(tokens, tokenId));
				}
			}
		} catch (IOException e) {
			throw new CompressionException("cannot open file %s".formatted(targetFileName), e);
		}
	}

	private String findTokenById(Set<Token> tokens, int tokenId) {
		return tokens.stream().filter(token -> token.id() == tokenId).map(Token::value).findFirst()
				.orElseThrow(() -> new CompressionException("can't find token with id %d".formatted(tokenId)));
	}

}
