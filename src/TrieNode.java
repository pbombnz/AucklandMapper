import java.util.Map;
import java.util.HashMap;

public class TrieNode {	
	private Map<Character, TrieNode> children;
	private char letter;
	private boolean word;
	
	public TrieNode(char c) { 
		children = new HashMap<Character, TrieNode>();
		letter = c;
	}
	
	public Map<Character, TrieNode> getChildren() {
		return children;
	}

	public boolean isWord() {
		return word;
	}


	public void setWord(boolean word) {
		this.word = word;
	}
	
	public char getLetter() {
		return letter;
	}
}
