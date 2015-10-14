package org.json.serialization.test;
import org.json.serialization.converters.DateConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.serialization.*;
public class Test {
    public static enum ChatType {
        Text,
        Typing,
        Status,
        HookStatus,
        UnhookStatus,
        ClearHook,
        RequestInfo
    }
    
    @JSONSerializable
    public static class ChatData {
        @JSONProperty
        public ChatType type;

        @JSONProperty
        public Object content;

        @JSONProperty
        public String sourceUId;

        @JSONProperty
        public String targetUId;

        @JSONProperty(converterType =DateConverter.class)
        public Date time;
    }
    public static void main(String[] args) throws SerializeException, DeserializeException, JSONException{
        
        ChatData test = new ChatData();
        test.time = new Date();
                
        JSONObject d = JSONConvert.serialize(test);
    }
}
