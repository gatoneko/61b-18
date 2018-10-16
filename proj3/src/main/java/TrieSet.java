import java.util.*;

public class TrieSet {
    private class Node {
        Map<Character, Node> links;
        boolean exists;

        public Node() {
            links = new TreeMap<Character, Node>();
            exists = false;
        }
    }

    private Node root;

    public TrieSet() {
        root = new Node();
    }

    public void put(String key) {
        put(root, key, 0);
    }

    private Node put(Node n, String key, int depth) {
        if (n == null) {
            n = new Node();
        }

        if (depth == key.length()) {
            n.exists = true;
            return n;
        }

        char c = key.charAt(depth);

        n.links.put(c, put(n.links.get(c), key, depth + 1));
        return n;
    }

    public List<String> getMatches(String prefix) {
        Node startNode = goToStart(root, prefix, 0);
        if (startNode == null) return null;
        return getMatches(startNode, prefix, new ArrayList<String>());
    }

    private Node goToStart(Node n, String prefix, int depth) {
        if (n == null) { return null; }
        if (depth == prefix.length()) { return n; }
        return goToStart(n.links.get(prefix.charAt(depth)), prefix, depth + 1);
    }

    private List<String> getMatches(Node n, String builtString, List<String> result){
        if (n == null) { return result; }
        if (n.exists) { result.add(builtString); }

        Iterator<Character> iter = n.links.keySet().iterator();
        while (iter.hasNext()) {
            Character currentKey = iter.next();
            String evenMoreBuiltString = builtString + currentKey;
            result = getMatches(n.links.get(currentKey), evenMoreBuiltString, result);
        }

        return result;
    }
}
