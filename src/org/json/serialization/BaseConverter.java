package org.json.serialization;

import org.json.JSONObject;

/**
 * Created by XuPeiYao on 2015/10/13.
 */
public class BaseConverter implements IJSONConverter {
    private static boolean checkBaseType(Class Type){
        Class[] BaseTypes = new Class[]{byte.class,short.class,int.class,long.class,float.class,double.class,boolean.class,char.class};
        Class[] ClassType = new Class[]{Byte.class,Short.class,Integer.class,Long.class,Float.class,Double.class,Boolean.class,Character.class,String.class,Object.class};
        for(Class t : BaseTypes){
            if(t.equals(Type))return true;
        }
        for(Class t : ClassType){
            if(t.equals(Type))return true;
        }

        return false;
    }

    @Override
    public Object serialize(Object obj) throws SerializeException {        
        if(checkBaseType(obj.getClass())){
            return obj;
        }
        return JSONConvert.serialize(obj);
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        if(obj.getClass().equals(type)){
            return (T) obj;
        }else if(checkBaseType(type)){
            if(JSONObject.NULL.equals(obj))return null;
            return (T)obj;
        }
        return JSONConvert.deserialize(type,obj);
    }
}
