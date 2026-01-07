package cdq.cdl.aiassistant.chat.domain.port;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.CityInformation;
import cdq.cdl.aiassistant.chat.domain.model.Country;

public interface CountriesPort
{
    City getCapitalOf(Country country);

    CityInformation getCityInformation(City city);
}
