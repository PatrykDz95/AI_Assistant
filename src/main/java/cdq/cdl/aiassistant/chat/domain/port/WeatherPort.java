package cdq.cdl.aiassistant.chat.domain.port;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.Temperature;

public interface WeatherPort
{
    Temperature getCurrentTemperature(City city);
}
