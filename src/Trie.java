import java.util.ArrayList;
import java.util.List;

public class Trie {
	TrieNode root;
	
	public Trie() {
		root = new TrieNode('\0');
	}

	public void add(String roadName) {
		TrieNode current = root;
		char[] roadNameCharArray = roadName.toCharArray();
		
		for (int i = 0; i < roadNameCharArray.length; i++) {
			char c = roadNameCharArray[i];
			
			if(current.getChildren().get(c) == null) {
				current.getChildren().put(c, new TrieNode(c));
			}
			current = current.getChildren().get(c);
		}
		current.setWord(true);
	}
	
	public boolean contains(String roadName) {
		TrieNode current = root;
		char[] roadNameCharArray = roadName.toCharArray();
		
		for (int i = 0; i < roadNameCharArray.length; i++) {
			char c = roadNameCharArray[i];
			if(current.getChildren().containsKey(c)) {
				current = current.getChildren().get(c);
			}
			//else {
			//	return false;
			//}
		}
		return current.isWord();
	}
	
	public List<String> getRoadsWithPrefix(String prefix) {
		return getRoadsWithPrefix("_"+prefix, "", root);
	}
	
	private List<String> getRoadsWithPrefix(String prefix, String generatedWord, TrieNode current) {	
		List<String> listOfRoadNames = new ArrayList<String>();
		
		//adding current node letter to the word being generated (even if null)
		generatedWord += current == root ? "" : current.getLetter();
		
		
		//if current is EOL, we check if its a word or not altogether and return accordingly
		if(current.getChildren().isEmpty()) {
			if(current.isWord()) {
				listOfRoadNames.add(generatedWord);
			} 
			return listOfRoadNames;
		} 
		
		//checking if prefix still has characters to recurse through
		if(prefix.length() > 1) {
			prefix = prefix.substring(1);
			char c = prefix.charAt(0);
			//prefix = prefix.substring(1);
			
			if(current.getChildren().containsKey(c)) {
				listOfRoadNames.addAll(getRoadsWithPrefix(prefix, generatedWord, current.getChildren().get(c)));
			} else {
				//failure to find word with whole prefix, but return what we have now
				return listOfRoadNames; 
			}
		} else {
			//we found the word that matches the string completely, now find additional words in the sub-tree
			for(TrieNode n: current.getChildren().values()) {
				listOfRoadNames.addAll(getRoadsWithPrefix(prefix, generatedWord, n));
			}
		}
		
		//finally adds the word at the end (due to depth first travsal method)
		if(current.isWord()) {
			listOfRoadNames.add(generatedWord);
		} 
		
		//return list if it makes it to here
		return listOfRoadNames;
	
	}
	
	//Function to test if trie is actually working
	/*public static void main(String[] args) { 
		Trie t = new Trie();
		Road r = new Road(0, 0, "d", "d", false, 2, 3, false, false, false);
		t.add("car");
		t.add("cart");
		t.add("carts");
		t.add("carl");
		t.add("carlton");
		t.add("amazon");
		t.add("a");
		t.add("am");
		t.add("amaze");
		
		//should all be true
		System.out.println(t.contains("car"));
		System.out.println(t.contains("cart"));
		System.out.println(t.contains("carts"));
		System.out.println(t.contains("carl"));
		System.out.println(t.contains("carlton"));
		System.out.println(t.contains("am"));
		
		System.out.println(t.getRoadsWithPrefix("ca")); //output should be [carts, cart, carlton, carl, car]
		System.out.println(t.getRoadsWithPrefix("a")); //output should be [amaze, amazon, am, a]
	}*/
}