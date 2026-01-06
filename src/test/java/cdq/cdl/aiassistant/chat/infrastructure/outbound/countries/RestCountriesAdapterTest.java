package cdq.cdl.aiassistant.chat.infrastructure.outbound.countries;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

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
        WebClient webClient = WebClient.create(baseUrl);
        adapter = new RestCountriesAdapter(webClient);
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
        String capital = adapter.capitalOf("Germany");

        // Then
        assertThat(capital).isEqualTo("Berlin");
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
        String info = adapter.aboutCity("Berlin");

        // Then
        assertThat(info)
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
        assertThatThrownBy(() -> adapter.capitalOf("NonExistentCountry"))
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
        assertThatThrownBy(() -> adapter.aboutCity("NonExistentCity"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No capital found with name: NonExistentCity");
    }
}
