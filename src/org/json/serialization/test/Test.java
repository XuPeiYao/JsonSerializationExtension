package org.json.serialization.test;
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

        @JSONProperty
        public long time;

        public String GetTimeString() {
            Date time_ = new Date(time);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time_);
        }
    }
    public static void main(String[] args) throws SerializeException, DeserializeException, JSONException{
        
        JSONObject jObj = new JSONObject("{\"type\":\"Text\",\"sourceUId\":\"1085727241444815\",\"targetUId\":\"850283428397119\",\"content\":\"\",\"time\":1444810071867}");
        ChatData obj = JSONConvert.deserialize(ChatData.class,jObj);
    }
}
