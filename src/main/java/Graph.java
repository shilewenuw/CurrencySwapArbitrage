import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <b>Graph</b> represents a <b>mutable</b> set of <b>nodes</b> and the <b>edges</b> between them. Graph is
 * <b>directed</b>, which means the edges have one direction, but nodes can have two edges that go both directions.
 * A node can also point to itself.
 */
public class Graph<N, L> {


    private static final boolean DEBUG = false;

    private Map<N, Set<Edge>> nodes;


    /**
     * Constructs an empty graph.
     *
     * @spec.effects Constructs a new empty graph
     */
    public Graph() {
        nodes = new HashMap<>();
    }

    /**
     * Adds given data to the graph, and returns true if graph didn't already contain given data
     *
     * @param data the added node
     * @spec.requires {@code data != null}
     * @spec.modifies this
     * @spec.effects this_post = this + data
     * @return true iff graph doesn't already contain given data
     */
    public boolean addNode(N data) {
        if (containsNode(data)) {
            return false;
        } else {
            nodes.put(data, new HashSet<>());
            return true;
        }
    }

    /**
     * Creates an Edge between the parent and child nodes, and returns true if the method was successful
     *
     * @param parent source of the Edge
     * @param child destination of the Edge
     * @param label name of the Edge formed
     * @spec.requires {@code parent != null  && child != null}
     * @spec.modifies this
     * @spec.effects an Edge is created so that given parent points to given child
     * @return true iff {@code contains(parent) && contains(child) && the given edge doesn't already exist}
     */
    public boolean connectNodes(N parent, N child, L label) {
        Edge edge = new Edge(parent, child, label);
        if (!nodes.containsKey(edge.getParent()) || !nodes.containsKey(edge.getChild())
                || nodes.get(edge.getParent()).contains(edge)) {
            return false;
        } else {
            Set<Edge> edges = nodes.get(edge.getParent());
            if (edges.contains(edge)) {
                return false;
            }
            edges.add(edge);
            return true;
        }
    }

    /**
     * Returns true if this contains a node with given data
     *
     * @param data what a desired node may contain
     * @return true iff this contains a node with given data
     */
    public boolean containsNode(N data) {
        return nodes.containsKey(data);
    }


    /**
     * Returns a set of all the data within the children of the given parent
     *
     * @param parent whose children to be returned
     * @spec.requires {@code parent != null}
     * @return a set of all Edges within the children of the given parent, or null if
     * {@code !contains(parent)}
     */
    public Set<Edge> getOutGoingEdges(N parent) {
        return nodes.get(parent);
    }

    /**
     * Returns a set of all the data within the nodes in this
     *
     * @return a set of all the data within the nodes in this
     */
    public Set<N> getNodes() {
        return new HashSet<>(nodes.keySet());
    }

    /**
     * <b>Edge</b> represents an <b>immutable</b> edge from a parent to a child, with a label as the name of the edge
     */
    public class Edge {

        private final N parent; //though I use an adjacency list, this field is helpful
        private final N child;
        private final L label;

        /**
         * Constructs a new edge that points from the given parent to the given child, and is labeled by given label
         *
         * @param parent source of the edge
         * @param child destination of the edge
         * @param label the name of the edge
         * @spec.requires {@code parent != null && child != null && label != null}
         * @spec.effects Constructs a new edge that points to the given child, which is labeled by given label
         */
        private Edge(N parent, N child, L label) {
            this.parent = parent;
            this.child = child;
            this.label = label;
        }

        /**
         * Returns the source of the edge
         *
         * @return the source of the edge
         */
        public N getParent() {
            return parent;
        }

        /**
         * Returns the destination of the edge
         *
         * @return the destination of the edge
         */
        public N getChild() {
            return child;
        }

        /**
         * Returns the label of the edge
         *
         * @return the label of the edge
         */
        public L getLabel() {
            return label;
        }

        /**
         * Standard equality operation
         *
         * @param other the other object to be tested for equality
         * @return true iff both are Edges and they represent the same Edge
         */
        public boolean equals(Object other) {
            if (!(other instanceof Graph<?, ?>.Edge)) {
                return false;
            } else {
                Edge edge = (Edge) other;
                boolean equals = this.parent.equals(edge.parent)
                        && this.label.equals(edge.label) && this.child.equals(edge.child);
                return equals;
            }
        }

        /**
         * Standard hashcode operation
         *
         * @return an int all objects equal to this will also return
         */
        public int hashCode() {
            int hashCode = parent.hashCode();
            hashCode = 31 * hashCode + child.hashCode();
            hashCode = 31 * hashCode + label.hashCode();
            return hashCode;
        }
    }
}
