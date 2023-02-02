public class mainMockAPI {

    public static void main(String[] args) {
        // build a web with a few fake rates
        CurrencyWeb web = new CurrencyWeb();
        web.addExchangeRate("USD", "YEN", new GetRateListener() {
            @Override
            public double getRate() {
                return MockAPI.mockExchangeRateAPICall("USD", "YEN");
            }

            @Override
            public double getRateToUSD() {
                return MockAPI.mockExchangeRateTOUSDAPICall("YEN");
            }
        }, new GetRateListener() {
            @Override
            public double getRate() {
                return MockAPI.mockExchangeRateAPICall("YEN", "USD");
            }

            @Override
            public double getRateToUSD() {
                return MockAPI.mockExchangeRateTOUSDAPICall("USD");
            }
        });


        web.addExchangeRate("YEN", "RMB", new GetRateListener() {
            @Override
            public double getRate() {
                return MockAPI.mockExchangeRateAPICall("YEN", "RMB");
            }

            @Override
            public double getRateToUSD() {
                return MockAPI.mockExchangeRateTOUSDAPICall("RMB");
            }
        }, new GetRateListener() {
            @Override
            public double getRate() {
                return MockAPI.mockExchangeRateAPICall("RMB", "YEN");
            }

            @Override
            public double getRateToUSD() {
                return MockAPI.mockExchangeRateTOUSDAPICall("YEN");
            }
        });

        String res = web.arbitragePathJSON("USD", "RMB");

        System.out.println(res);
    }

    /**
     * mocked api calls
     */
    private static class MockAPI {
        private static double mockExchangeRateAPICall(String currency1, String currency2) {
            if (currency1.equals("USD") && currency2.equals("YEN")) {
                return 107.0;
            } else if (currency1.equals("YEN") && currency2.equals("RMB")) {
                return .066;
            } else {
                return 1;
            }
        }
        private static double mockExchangeRateTOUSDAPICall(String currency) {
            if (currency.equals("USD")){
                return 1;
            } else if (currency.equals("YEN")) {
                return .0094;
            } else if (currency.equals("RMB")) {
                return .14;
            } else {
                return 1;
            }
        }
    }
}
