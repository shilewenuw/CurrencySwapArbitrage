import java.util.*;

/**
 * CurrencyWeb is a <b>Graph</b> that keeps tracks of various exchange rates passed in, and can find arbitrage opportunities between two currencies
 *
 */
public class CurrencyWeb {
    private Graph<String, GetRateListener> graph;
    private Map<String, GetRateListener> toUSDPriceListeners;

    public CurrencyWeb() {
        graph = new Graph<>();
        toUSDPriceListeners = new HashMap<>();
    }

    /**
     * Adds an exchange rate to the web
     * @param baseCurrency the base currency
     * @param quoteCurrency the quote currency
     * @param baseCurrencyListener the base currency's listener
     * @param quoteCurrencyListener the quote currency's listener
     */
    public void addExchangeRate(String baseCurrency, String quoteCurrency, GetRateListener baseCurrencyListener, GetRateListener quoteCurrencyListener) {
        graph.addNode(baseCurrency);
        graph.addNode(quoteCurrency);
        graph.connectNodes(baseCurrency, quoteCurrency, baseCurrencyListener);
        graph.connectNodes(quoteCurrency, baseCurrency, quoteCurrencyListener);
        if (!toUSDPriceListeners.containsKey(baseCurrency)) {
            toUSDPriceListeners.put(baseCurrency, quoteCurrencyListener);
        }
        if (!toUSDPriceListeners.containsKey(quoteCurrency)) {
            toUSDPriceListeners.put(quoteCurrency, baseCurrencyListener);
        }

    }

    /**
     * Returns the currencies in the graph
     * @return the currencies in the graph
     */
    public Set<String> getCurrencies() {
        return graph.getNodes();
    }

    /**
     *
     * @param start the starting currency
     * @param dest the ending currency
     * @return a json path with all the currencies in between
     */
    public String arbitragePathJSON(String start, String dest) {
        Path<String> path = findPath(start, dest);
        return path.toJSON();
    }

    /**
     * Finds arbitrage using Dijkstra's algorithm
     *
     * @param start the starting currency
     * @param dest the ending currency
     * @return a Path with all the currencies in between
     * @throws IllegalArgumentException if start or dest are not currencies in the graph
     */
    private Path<String> findPath(String start, String dest)
            throws IllegalArgumentException {
        if (!graph.containsNode(start) || !graph.containsNode(dest)) {
            throw new IllegalArgumentException();
        }
        Queue<Path<String>> active = new PriorityQueue<>(new PathComparator());
        Set<String> finished = new HashSet<>();
        Path<String> startPath = new Path<>(start, toUSDPriceListeners.get(start));
        //although startPath should already represent a path from start to start,
        //  an update to Path's Segments needs to be made
        Path<String> startToStart = startPath.extend(start, new GetRateListener() {
            @Override
            public double getRate() {
                return 1;
            }

            @Override
            public double getRateToUSD() {
                return toUSDPriceListeners.get(start).getRateToUSD();
            }
        });
        active.add(startToStart);
        while (!active.isEmpty()) {
            Path<String> minPath = active.remove();
            String minDest = minPath.getEnd();

            if (minDest.equals(dest)) {
                return minPath;
            }

            if (finished.contains(minDest)) {
                continue;
            }

            for (Graph<String, GetRateListener>.Edge edge : graph.getOutGoingEdges(minDest)) {
                if (!finished.contains(edge.getChild())) {

                    if (edge.getParent().equals(start)) {
                        Path<String> newPath = startPath.extend(edge.getChild(), edge.getLabel());
                        active.add(newPath);
                    } else {
                        Path<String> newPath = minPath.extend(edge.getChild(), edge.getLabel());
                        active.add(newPath);
                    }
                }
            }
            finished.add(minDest);
        }
        return null;
    }

    // PathComparator is used to compare path costs
    private static class PathComparator implements Comparator<Path<?>> {

        public static final int MAX_OUT_COST = 0;
        public static final int MIN_OUT_COST = 1;

        private int code;

        /**
         *
         */
        public PathComparator() {
            this(MAX_OUT_COST);
        }

        /**
         * Sets up the PathComparator to min or max out the cost of the path
         * @param code should be MAX_OUT_COST to maximize cost or MIN_OUT_COST for the opposite
         */
        public PathComparator(int code) {
            if (code != MAX_OUT_COST && code != MIN_OUT_COST) {
                throw new IllegalArgumentException("Please enter a valid code");
            }
            this.code = code;
        }

        public int compare(Path<?> path1, Path<?> path2) {
            if (code == MIN_OUT_COST) {
                return Double.compare(path1.getCost(), path2.getCost());
            } else {
                return Double.compare(path2.getCost(), path1.getCost());
            }
        }
    }
}
