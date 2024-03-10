package com.bootnext.platform.sla.model.template;
 

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

 

@Converter

public class JpaConverterForJSONArray implements AttributeConverter<JSONArray, String> {

 

  private Logger logger = LoggerFactory.getLogger(JpaConverterForJSONArray.class);

 

  @Override

  public String convertToDatabaseColumn(JSONArray meta) {

    if (meta != null) {

      return meta.toString();

    } else {

      return new JSONArray().toString();

    }

  }

 

  @Override

  public JSONArray convertToEntityAttribute(String dbData) {

    if (dbData != null && !dbData.equals("") && !dbData.equals("[]") && !dbData.equals("null")) {

      try {

        return new JSONArray(dbData);

      } catch (IllegalStateException e) {

        return new JSONArray();

      } catch (JSONException e) {

        logger.error("Error in convertToEntityAttribute,Error : {} ", e.getMessage());

      }

    }

    return new JSONArray();

  }

}
