package cdq.cdl.aiassistant.chat.infrastructure.outbound.countries;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.CityInformation;
import cdq.cdl.aiassistant.chat.domain.model.Country;
import cdq.cdl.aiassistant.chat.domain.port.CountriesPort;
import cdq.cdl.aiassistant.chat.infrastructure.outbound.countries.model.RestCountryResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class RestCountriesAdapter implements CountriesPort
{
    private final WebClient webClient;

    public RestCountriesAdapter(@Value("${external-api.rest-countries.base-url}") String baseUrl)
    {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public City getCapitalOf(Country country)
    {
        log.info("Fetching capital for country: [{}]", country.name());

        RestCountryResponse countryData = fetchCountryByName(country.name());

        if (countryData.capital() == null || countryData.capital().isEmpty())
        {
            throw new IllegalStateException("No capital found for country: " + country.name());
        }

        return City.of(countryData.capital().getFirst());
    }

    @Override
    public CityInformation getCityInformation(City city)
    {
        log.info("Fetching information about city: [{}]", city.name());

        RestCountryResponse countryData = fetchCountryByCapital(city.name());

        String currencyCode = countryData.currencies().keySet().stream()
                .findFirst()
                .orElse("Unknown");

        return new CityInformation(
                city,
                Country.of(countryData.name().common()),
                countryData.region(),
                countryData.population(),
                currencyCode
        );
    }

    private RestCountryResponse fetchCountryByName(String country)
    {
        RestCountryResponse[] response = webClient.get()
                .uri("/name/{country}", country)
                .retrieve()
                .bodyToMono(RestCountryResponse[].class)
                .block();

        return extractFirstCountry(response, "country", country);
    }

    private RestCountryResponse fetchCountryByCapital(String capital)
    {
        RestCountryResponse[] response = webClient.get()
                .uri("/capital/{capital}", capital)
                .retrieve()
                .bodyToMono(RestCountryResponse[].class)
                .block();

        return extractFirstCountry(response, "capital", capital);
    }

    private RestCountryResponse extractFirstCountry(RestCountryResponse[] response, String searchType, String searchValue)
    {
        if (response == null || response.length == 0)
        {
            String errorMsg = String.format("No %s found with name: %s", searchType, searchValue);
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        return response[0];
    }
}