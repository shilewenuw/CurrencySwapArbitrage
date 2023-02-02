import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This represents an immutable path between two objects of type E, particularly
 * Path#getStart() and Path#getEnd(). Also contains a cached
 * version of the total rate along this path, for efficient repeated access.
 */
public class Path<E> implements Iterable<Path<E>.Segment> {

    /**
     * The total product of the rates along all the segments in this path.
     */
    private double rate;

    /**
     * Keeps track of the "cost" or current value relative to $1 USD. Think cost not in a financial sense, but instead
     * of a cost of a path. For arbitrage the higher the abs(1 - cost), the bigger the profit
     */
    private double cost;

    /**
     * The E at the beginning of this path.
     */
    private E start;

    /**
     * The ordered sequence of segments representing a path between E's.
     */
    private List<Segment> path;

    private GetRateListener startListener;

    /**
     * Creates a new, empty path containing a start E. Essentially this represents a path
     * from the start E to itself with a total rate of "0".
     *
     * @param start The starting E of the path.
     */
    public Path(E start, GetRateListener startListener) {
        this.start = start;
        this.rate = 1 / startListener.getRateToUSD();
        this.cost = 1;
        this.startListener = startListener;
        this.path = new ArrayList<>();
    }

    /**
     * Appends a new single segment to the end of this path, originating at the current last E
     * in this path and terminating at {@code newEnd}. The rate of adding this additional segment
     * to the existing path is {@code segmentrate}. Thus, the returned Path represents a path
     * from {@code this.getStart()} to {@code newEnd}, with a rate of {@code this.getrate() +
     * segmentrate}.
     *
     * @param newEnd      The E being added at the end of the segment being appended to this path
     * @return A new path representing the current path with the given segment appended to the end.
     */
    public Path<E> extend(E newEnd, GetRateListener rateListener) {

        Path<E> extendedPath = new Path<E>(start, startListener);
        extendedPath.path.addAll(this.path);
        
        extendedPath.path.add(new Segment(this.getEnd(), newEnd, rateListener.getRate()));
        extendedPath.rate = this.rate * rateListener.getRate();
        extendedPath.cost = extendedPath.rate * rateListener.getRateToUSD();

        return extendedPath;
    }

    /**
     * @return The total product of the rates along this path.
     */
    public double getRate() {
        return rate;
    }

    /**
     * @return The cost along this path
     */
    public double getCost() {
        return cost;
    }

    /**
     * @return The E at the beginning of this path.
     */
    public E getStart() {
        return start;
    }

    /**
     * @return The E at the end of this path, which may be the start E if this path
     * contains no segments (i.e. this path is from the start E to itself).
     */
    public E getEnd() {
        if(path.size() == 0) {
            return start;
        }
        return path.get(path.size() - 1).getEnd();
    }

    /**
     * @return An iterator of the segments in this path, in order, beginning from the starting
     * E and ending at the end E. In the case that this path represents a path between
     * the start E and itself, this iterator contains no elements. This iterator does not
     * support the optional Iterator#remove() operation and will throw an
     * UnsupportedOperationException if Iterator#remove() is called.
     */
    @Override
    public Iterator<Segment> iterator() {
        // Create a wrapping iterator to guarantee exceptional behavior on Iterator#remove.
        return new Iterator<Segment>() {

            private Iterator<Segment> backingIterator = path.iterator();

            @Override
            public boolean hasNext() {
                return backingIterator.hasNext();
            }

            @Override
            public Segment next() {
                return backingIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Paths may not be modified.");
            }
        };
    }


    /**
     * Checks this path for equality with another object. Two paths are equal if and only if
     * they contain exactly the same sequence of segments in the same order. In the case that
     * both paths are empty, they are only equal if their starting E is equal.
     *
     * @param obj The object to compare with {@code this}.
     * @return {@literal true} if and only if {@code obj} is equal to {@code this}.
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof Path<?>)) {
            return false;
        }
        Path<?> other = (Path<?>) obj;
        if(this.path.size() != other.path.size()) {
            return false;
        }
        if(this.path.size() == 0 && !this.start.equals(other.start)) {
            return false;
        }
        for(int i = 0; i < this.path.size(); i++) {
            if(!this.path.get(i).equals(other.path.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (31 * start.hashCode()) + path.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(start.toString());
        for(Segment segment : path) {
            sb.append(" =(");
            sb.append(String.format("%.3f", segment.getRate()));
            sb.append(")=> ");
            sb.append(segment.getEnd().toString());
        }
        return sb.toString();
    }

    public String toJSON() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.add("percent_profit", new JsonPrimitive(cost - 1));
        JsonArray currencies = new JsonArray();
        currencies.add(start.toString());
        for (Segment segment : path) {
            currencies.add(segment.getEnd().toString());
        }
        jsonObj.add("path", currencies);
        return jsonObj.toString();
    }

    /**
     * Segment represents a single segment as part of a longer, more complex path between E's.
     * Segments are immutable parts of a larger path that cannot be instantiated directly, and
     * are created as part of larger paths by calling Path#extend(E, double).
     */
    public class Segment {

        /**
         * The beginning of this segment.
         */
        private final E start;

        /**
         * The end of this segment.
         */
        private final E end;

        /**
         * The rate of travelling this segment.
         */
        private final double rate;

        /**
         * Constructs a new segment with the provided characteristics.
         *
         * @param start The starting E of this segment.
         * @param end   The ending E of this segment.
         * @param rate  The exchange rate of travelling this segment.
         * @throws NullPointerException     if either E is null.
         * @throws IllegalArgumentException if rate is infinite or NaN
         */
        private Segment(E start, E end, double rate) {
            if(start == null || end == null) {
                throw new NullPointerException("Segments cannot have null E's.");
            }
            if(!Double.isFinite(Path.this.rate)) {
                throw new IllegalArgumentException("Segment rate may not be NaN or infinite.");
            }
            this.start = start;
            this.end = end;
            this.rate = rate;
        }

        /**
         * If E is modified, the future behavior of this is undefined.
         *
         * @return The beginning E of this segment.
         */
        public E getStart() {

            return this.start;
        }

        /**
         * If E is modified, the future behavior of this is undefined.
         *
         * @return The ending E of this segment.
         */
        public E getEnd() {
            return this.end;
        }

        /**
         * @return The rate of this segment.
         */
        public double getRate() {
            return this.rate;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(start.toString());
            sb.append(" -> ");
            sb.append(end.toString());
            sb.append(" (");
            sb.append(String.format("%.3f", Path.this.rate));
            sb.append(")]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(!(obj instanceof Path<?>.Segment)) {
                return false;
            }
            Path<?>.Segment other = (Path<?>.Segment) obj;
            return other.getStart().equals(this.getStart())
                   && other.getEnd().equals(this.getEnd())
                   && (Double.compare(this.rate, other.rate) == 0);
        }

        @Override
        public int hashCode() {
            int result = start.hashCode();
            result += (31 * result) + end.hashCode();
            result += (31 * result) + Double.hashCode(Path.this.rate);
            return result;
        }

    }
}
