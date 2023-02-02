public interface GetRateListener {
    /**
     * Returns the rate of BASE/QUOTE
     *
     * @return the rate of BASE/QUOTE
     */
    double getRate();

    /**
     * Say BASE/QUOTE is the currency conversion. Returns the rate QUOTE/USD
     *
     * @return QUOTE currency rate to USD
     */
    double getRateToUSD();
}
