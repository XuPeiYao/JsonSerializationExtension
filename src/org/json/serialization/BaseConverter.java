package org.json.serialization;

/**
 * Created by XuPeiYao on 2015/10/13.
 */
public class BaseConverter implements IJSONConverter {
    private static boolean checkBaseType(Class Type){
        Class[] BaseTypes = new Class[]{byte.class,short.class,int.class,long.class,float.class,double.class,boolean.class,char.class};
        boolean result = false;
        for(Class t : BaseTypes){
            if(t.equals(Type))
                result = true;
        }

        return result;
    }

    @Override
    public Object serialize(Object obj) throws SerializeException {
        if(checkBaseType(obj.getClass())){
            return obj;
        }
        return JSONConvert.serialize(obj);//return empty json object
    }

    @Override
    public <T> Object deserialize(Class<T> type, Object obj) throws DeserializeException {
        if(obj.getClass().equals(type)){
            return obj;
        }else if(checkBaseType(type)){
            return type.cast(obj);
        }
        return JSONConvert.deserialize(type,obj);
    }
}
