import java.util.*;

public class TrieSet<T> {
    private class Node {

        T value;
        Map<Character, Node> links;
        boolean exists;

        public Node() {
            this.value = null;
            this.links = new TreeMap<Character, Node>();
            this.exists = false;
        }

        public Node(T value) {
            this.value = value;
            this.links = new TreeMap<Character, Node>();
            this.exists = false;
        }
    }

    private Node root;

    public TrieSet() {
        root = new Node();
    }

    public void put(String key, T value) {
        put(root, key, 0, value);
    }

    private Node put(Node n, String key, int depth, T value) {
        if (n == null) {
            n = new Node();
        }

        if (depth == key.length()) {
            n.value = value;
            n.exists = true;
            return n;
        }

        char c = key.charAt(depth);

        n.links.put(c, put(n.links.get(c), key, depth + 1, value));
        return n;
    }

    public List<T> getMatches(String prefix) {
        Node startNode = goToStart(root, prefix, 0);
        if (startNode == null) return null;
        return getMatches(startNode, prefix, new ArrayList<T>());
    }

    private Node goToStart(Node n, String prefix, int depth) {
        if (n == null) { return null; }
        if (depth == prefix.length()) { return n; }
        return goToStart(n.links.get(prefix.charAt(depth)), prefix, depth + 1);
    }

    private List<T> getMatches(Node n, String builtString, List<T> result){
        if (n == null) { return result; }
        if (n.exists) { result.add(n.value); }

        Iterator<Character> iter = n.links.keySet().iterator();
        while (iter.hasNext()) {
            Character currentKey = iter.next();
            String evenMoreBuiltString = builtString + currentKey;
            result = getMatches(n.links.get(currentKey), evenMoreBuiltString, result);
        }

        return result;
    }
}
