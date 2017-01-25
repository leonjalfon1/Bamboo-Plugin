package com.cx.client.rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.List;

/**
 * Objects in lists and member lists containing different implementations of superclass must be annotated appropriately
 * <p>
 * Created by: zoharby.
 * Date: 31/05/2016.
 */
public class JacksonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static Object readSingleJson(String json, Class pojoClass) throws IOException {

        return mapper.readValue(json, pojoClass);
    }

    //for json that contain array only {[...]}
    public static List<?> readJsonArr(String json, Class pojoClass) throws IOException {

        CollectionType ObjectList_t = mapper.getTypeFactory().constructCollectionType(List.class, pojoClass);

        return mapper.readValue(json, ObjectList_t);
    }

    public static String writeJson(Object toWrite) throws IOException {

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toWrite);
    }

    public static String writeJsonArr(List<Object> toWrite) throws IOException {

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toWrite);
    }

}