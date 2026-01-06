package cdq.cdl.aiassistant.chat.infrastructure.outbound.countries;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cdq.cdl.aiassistant.chat.domain.port.CountriesPort;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class RestCountriesAdapter implements CountriesPort
{
    private static final String BASE_URL = "https://restcountries.com/v3.1";

    private final WebClient webClient;

    public RestCountriesAdapter()
    {
        this(WebClient.builder().baseUrl(BASE_URL).build());
    }

    public RestCountriesAdapter(WebClient webClient)
    {
        this.webClient = webClient;
    }

    @Override
    public String capitalOf(String country)
    {
        log.info("Fetching capital for country: [{}]", country);

        RestCountry countryData = fetchCountryByName(country);

        if (countryData.capital() == null || countryData.capital().isEmpty())
        {
            throw new IllegalStateException("No capital found for country: " + country);
        }

        return countryData.capital().getFirst();
    }

    @Override
    public String aboutCity(String city)
    {
        log.info("Fetching information about city: [{}]", city);

        RestCountry countryData = fetchCountryByCapital(city);

        String currencyCode = countryData.currencies().keySet().stream()
                .findFirst()
                .orElse("Unknown");

        return String.format(
                "%s is the capital of %s. " +
                        "Country region: %s. " +
                        "Population: %,d. " +
                        "Currency: %s.",
                city,
                countryData.name().common(),
                countryData.region(),
                countryData.population(),
                currencyCode
        );
    }

    private RestCountry fetchCountryByName(String country)
    {
        RestCountry[] response = webClient.get()
                .uri("/name/{country}", country)
                .retrieve()
                .bodyToMono(RestCountry[].class)
                .block();

        return extractFirstCountry(response, "country", country);
    }

    private RestCountry fetchCountryByCapital(String capital)
    {
        RestCountry[] response = webClient.get()
                .uri("/capital/{capital}", capital)
                .retrieve()
                .bodyToMono(RestCountry[].class)
                .block();

        return extractFirstCountry(response, "capital", capital);
    }

    private RestCountry extractFirstCountry(RestCountry[] response, String searchType, String searchValue)
    {
        if (response == null || response.length == 0)
        {
            String errorMsg = String.format("No %s found with name: %s", searchType, searchValue);
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        return response[0];
    }

    record RestCountry(
            Name name,
            List<String> capital,
            String region,
            long population,
            Map<String, Object> currencies
    )
    {
    }

    record Name(String common)
    {
    }
}