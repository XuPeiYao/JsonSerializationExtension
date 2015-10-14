package org.json.serialization.test;
import org.json.JSONObject;
import org.json.serialization.*;
public class Test {
    public static enum Type{Student,Teacher}
    @JSONSerializable
    public static class User{
        @JSONProperty
        public Type type = Type.Teacher;
        
        @JSONProperty
        public String name="XPY";
        
        @JSONProperty
        public int year;
        
        @JSONProperty
        public double weight;
    }
    public static void main(String[] args) throws SerializeException, DeserializeException{
        User obj_ = new User();
        obj_.weight=81.2222;
        obj_.year = 2015;
        
        JSONObject jObj = JSONConvert.serialize(obj_);
        User obj = JSONConvert.deserialize(User.class,jObj);
    }
}
