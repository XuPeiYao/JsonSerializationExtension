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
    public static class User {
        @JSONProperty
        public Name Name;
        
        @JSONProperty
        public String GG;
    }
    @JSONSerializable
    public static class Name{
        @JSONProperty
        public String First;
        @JSONProperty
        public String Last;
    }
    public static void main(String[] args) throws SerializeException, DeserializeException, JSONException{
        JSONObject data = new JSONObject("{\"Name\":null,\"GG\":\"TT\"}");
        
        //String FirstName = data.getJSONObject("Name").getString("First");//普通存取JSON資料方式
        
        User user = JSONConvert.deserialize(User.class, data);//反序列化為物件
        //String LastName = user.Name.Last;//直接忖取屬性
    }
}
