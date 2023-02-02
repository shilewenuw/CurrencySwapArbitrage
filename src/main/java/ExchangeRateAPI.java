import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ExchangeRateAPI {
    private static final String API_KEY;
    private static final String CONVERSION_RATE = "conversion_rate";
    private static final String CONVERSION_RATES = "conversion_rates";
    public static final String USD = "usd";
    private static Set<String> availableCurrencies = null;

    static {
        API_KEY = System.getenv("EXCHANGE_RATE_API_KEY");
    }

    public static CurrencyWeb buildWeb(Collection<String> currencies) {
        for (String currency : currencies) {
            if (!getAvailableCurrencies().contains(currency)) {
                throw new IllegalArgumentException("invalid currency: " + currency);
            }
        }

        CurrencyWeb currencyWeb = new CurrencyWeb();

        for (String baseCurrency : currencies) {
            for (String quoteCurrency : currencies) {
                if (!baseCurrency.equals(quoteCurrency)) {
                    currencyWeb.addExchangeRate(baseCurrency, quoteCurrency,
                            getRateListenerFactory(baseCurrency, quoteCurrency),
                            getRateListenerFactory(quoteCurrency, baseCurrency));
                }
            }
        }
        return currencyWeb;
    }

    private static GetRateListener getRateListenerFactory(String baseCurrency, String quoteCurrency) {
        return new GetRateListener() {
            @Override
            public double getRate() {
                return getExchangeRate(baseCurrency, quoteCurrency);
            }

            @Override
            public double getRateToUSD() {
                return getExchangeRate(quoteCurrency, USD);
            }
        };
    }

    private static JsonObject getJsonResponse(String path) throws IOException{
        URL url = new URL(path);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
        return root.getAsJsonObject();
    }

    public static Set<String> getAvailableCurrencies() {
        return getAvailableCurrencies(false);
    }

    public static Set<String> getAvailableCurrencies(boolean refresh) {
        try {
            if (refresh || availableCurrencies == null) {
                JsonObject responseJson = getJsonResponse(
                        "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/usd");
                JsonObject ratesJson = responseJson.getAsJsonObject(CONVERSION_RATES);
                availableCurrencies = ratesJson.keySet();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
        return availableCurrencies;
    }

    public static double getExchangeRate(String baseCurrency, String quoteCurrency) {
        try {
            JsonObject responseJson = getJsonResponse(
                    String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s", API_KEY, baseCurrency, quoteCurrency));
            return responseJson.get(CONVERSION_RATE).getAsDouble();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return 0;
        }
    }

}
