package org.json.serialization.test;
import org.json.serialization.converters.DateConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.serialization.*;
import org.json.serialization.converters.BytesToBase64Converter;
public class Test {
    
    @JSONSerializable
    public static class T {
        @JSONProperty(converterType =BytesToBase64Converter.class)
        public byte[] P;
    
        
        @JSONProperty(converterType =DateConverter.class)
        public Date time;
    }
    public static void main(String[] args) throws SerializeException, DeserializeException, JSONException{
        
        T test = new T();
        test.time = new Date();
        test.P = new byte[]{1,2,3,4,5,8,8,56,55};
        JSONObject d = JSONConvert.serialize(test);
    }
}
