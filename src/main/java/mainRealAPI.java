import java.util.*;

public class mainRealAPI {
    private static final Random random = new Random();

    public static void main(String[] args) {
        // limit web to 10 currencies because of API rate limit
        List<String> currencies = new ArrayList<>(ExchangeRateAPI.getAvailableCurrencies()).subList(0, 10);

        CurrencyWeb currencyWeb = ExchangeRateAPI.buildWeb(currencies);

        int size = currencies.size();
        for (int i = 0; i < 5; i++) {
            String base = currencies.get(random.nextInt(size));
            String quote = currencies.get(random.nextInt(size));
            if (base != quote) {
                System.out.println("Arbitrage b/w: " + base + " " + quote);
                System.out.println(currencyWeb.arbitragePathJSON(base, quote));
            }
        }
    }

}
