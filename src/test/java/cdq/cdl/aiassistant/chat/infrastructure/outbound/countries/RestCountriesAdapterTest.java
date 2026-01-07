package cdq.cdl.aiassistant.chat.infrastructure.outbound.countries;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.CityInformation;
import cdq.cdl.aiassistant.chat.domain.model.Country;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.assertj.core.api.Assertions.*;

class RestCountriesAdapterTest
{
    private MockWebServer mockWebServer;
    private RestCountriesAdapter adapter;

    @BeforeEach
    void setUp() throws IOException
    {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        adapter = new RestCountriesAdapter(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException
    {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnCapitalWhenCountryExists()
    {
        // Given
        String germanyResponse = """
                [
                  {
                    "name": {"common": "Germany"},
                    "capital": ["Berlin"],
                    "region": "Europe",
                    "population": 83240525,
                    "currencies": {"EUR": {"name": "Euro", "symbol": "€"}}
                  }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(germanyResponse)
                .addHeader("Content-Type", "application/json"));

        // When
        City capital = adapter.getCapitalOf(Country.of("Germany"));

        // Then
        assertThat(capital.name()).isEqualTo("Berlin");
    }

    @Test
    void shouldReturnCityInformationWhenCapitalExists()
    {
        // Given
        String berlinResponse = """
                [
                  {
                    "name": {"common": "Germany"},
                    "capital": ["Berlin"],
                    "region": "Europe",
                    "population": 83240525,
                    "currencies": {"EUR": {"name": "Euro", "symbol": "€"}}
                  }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(berlinResponse)
                .addHeader("Content-Type", "application/json"));

        // When
        CityInformation info = adapter.getCityInformation(City.of("Berlin"));

        // Then
        assertThat(info.city().name()).isEqualTo("Berlin");
        assertThat(info.country().name()).isEqualTo("Germany");
        assertThat(info.region()).isEqualTo("Europe");
        assertThat(info.population()).isEqualTo(83240525);
        assertThat(info.currency()).isEqualTo("EUR");

        String description = info.toDescription();
        assertThat(description)
                .contains("Berlin")
                .contains("Germany")
                .contains("Europe")
                .contains("83,240,525");
    }

    @Test
    void shouldThrowExceptionWhenCountryNotFound()
    {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        // When/Then
        assertThatThrownBy(() -> adapter.getCapitalOf(Country.of("NonExistentCountry")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No country found with name: NonExistentCountry");
    }

    @Test
    void shouldThrowExceptionWhenCityNotFound()
    {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        // When/Then
        assertThatThrownBy(() -> adapter.getCityInformation(City.of("NonExistentCity")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No capital found with name: NonExistentCity");
    }
}
