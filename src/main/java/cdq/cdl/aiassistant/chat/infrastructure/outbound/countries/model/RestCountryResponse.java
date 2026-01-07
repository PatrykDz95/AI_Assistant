package cdq.cdl.aiassistant.chat.infrastructure.outbound.countries.model;

import java.util.List;
import java.util.Map;

public record RestCountryResponse(CountryName name,
                                  List<String> capital,
                                  String region,
                                  long population,
                                  Map<String, Object> currencies)
{
}