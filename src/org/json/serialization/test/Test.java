package org.json.serialization.test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import org.json.serialization.converters.DateConverter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import javax.print.attribute.standard.DateTimeAtCompleted;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.serialization.*;
import org.json.serialization.converters.BytesToBase64Converter;
public class Test {
    @JSONSerializable
    public static class Item{
        @JSONProperty
        public String route_no;
        @JSONProperty
        public String city_route_no;
        @JSONProperty
        public String route_branch;
        @JSONProperty
        public int run_id;
        @JSONProperty
        public int goBack;
        @JSONProperty
        public String run_name;
        @JSONProperty
        public String start_city;
        @JSONProperty
        public String end_city;
        @JSONProperty
        public String department;
        @JSONProperty
        public int attribute;
    }
        
    public static void main(String[] args) throws SerializeException, DeserializeException, JSONException, IOException{
        JSONArray data = readJsonFromUrl("http://www.taiwanbus.tw/app_api/APP_QueryRunRouteCity.ashx?ProviderName=%E5%9C%8B%E5%85%89%E5%AE%A2%E9%81%8B");
        
        
        //String FirstName = data.getJSONObject("Name").getString("First");//普通存取JSON資料方式
        long time0 = Calendar.getInstance().getTimeInMillis();
        Item[] Item = JSONConvert.deserialize(Item[].class, data,true);//反序列化為物件
        long time1 = Calendar.getInstance().getTimeInMillis() - time0;
        //String LastName = user.Name.Last;//直接忖取屬性
        System.out.println(time1);
        long time2 = Calendar.getInstance().getTimeInMillis();
        Item[] user2 = JSONConvert.deserialize(Item[].class, data,false);//反序列化為物件
        long time3 = Calendar.getInstance().getTimeInMillis() - time2;
        System.out.println(time3);
        
    }
    
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
    }
    public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JSONArray json = new JSONArray(jsonText);
          return json;
        } finally {
          is.close();
        }
    }
}
