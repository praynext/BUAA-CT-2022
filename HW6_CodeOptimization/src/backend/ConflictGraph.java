package backend;

import midend.MidCode.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ConflictGraph {
    private final FuncBlock funcBlock;
    private final HashMap<Value, HashSet<Value>> graph = new HashMap<>();
    private final HashSet<Value> blacklist = new HashSet<>();

    public ConflictGraph(FuncBlock funcBlock) {
        this.funcBlock = funcBlock;
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    public HashSet<Value> getBlacklist() {
        return blacklist;
    }

    public void addNode(Value value) {
        if (!graph.containsKey(value) && !blacklist.contains(value)) {
            graph.put(value, new HashSet<>());
        }
    }

    public void addEdge(Value a, Value b) {
        if (blacklist.contains(a) || blacklist.contains(b)) return;
        if (!graph.containsKey(a)) {
            graph.put(a, new HashSet<>());
        }
        if (!graph.containsKey(b)) {
            graph.put(b, new HashSet<>());
        }
        if (a.equals(b)) return;
        graph.get(a).add(b);
        graph.get(b).add(a);
        if (graph.get(a).size() > 1000) {
            blacklist.add(a);
            removeNode(a);
        }
        if (graph.get(b).size() > 1000) {
            blacklist.add(b);
            removeNode(b);
        }
    }

    public Value findNode(int size) {
        for (Value value : graph.keySet()) {
            if (graph.get(value).size() < size) {
                return value;
            }
        }
        return null;
    }

    public HashSet<Value> removeNode(Value value) {
        HashSet<Value> edges = graph.remove(value);
        for (Value v : graph.keySet()) {
            graph.get(v).remove(value);
        }
        return edges;
    }

    public Value discardNode() {
        List<Value> nodes = new HashSet<>(graph.keySet()).stream().sorted(
                (a, b) -> {
                    double aWeight = Math.pow(2, funcBlock.getDepth(a)) / graph.get(a).size();
                    double bWeight = Math.pow(2, funcBlock.getDepth(b)) / graph.get(b).size();
                    return Double.compare(aWeight, bWeight);
                }).collect(Collectors.toList());
        return nodes.get(0);
    }

    public void store(Value value, HashSet<Value> values) {
        graph.put(value, values);
        for (Value v : values) {
            if (graph.containsKey(v)) {
                graph.get(v).add(value);
            }
        }
    }
}
