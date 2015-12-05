package org.json.serialization;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONConvert {
    private static String getMethodKey(Method method,JSONProperty property){
        String result = method.getName();
        if(property.key() != null){
            String newKey = property.key().trim();
            if(newKey.length()>0)result = newKey;
        }
        return result;
    }

    private static String getFieldKey(Field field,JSONProperty property){
        String result = field.getName();
        if(property.key() != null){
            String newKey = property.key().trim();
            if(newKey.length()>0)result = newKey;
        }
        return result;
    }

    public static <T> T serialize(Object obj) throws SerializeException {
        Class objType = obj.getClass();
        try {
            if (objType.isArray()) {
                return serializeArray(obj);
            } else if (objType.isAnnotationPresent(JSONSerializable.class)) {//custom class              
                return serializeObject(obj);
            }
        }catch(Exception e){
            throw new SerializeException();
        }
        return null;//know object
    }

    private static <T> T serializeArray(Object obj) throws InvocationTargetException, JSONException, IllegalAccessException, SerializeException {
        IJSONConverter BaseConverter = new BaseConverter();
        JSONArray result = new JSONArray();
        int aryLength=Array.getLength(obj);
        for(int i = 0 ; i < aryLength ; i++){
            Object value = Array.get(obj,i);
            Class valueType = value.getClass();
            if(value == null) {
            }else if(valueType.isArray()){//has subarray
                value = serialize(value);
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){//this value can serializable
                value = serialize(value);
            }else if(valueType.isEnum()){
                value = value.toString();
            }else{//call base converter
                value = BaseConverter.serialize(value);
            }
            result.put(value);
        }
        return (T)result;
    }

    private static <T> T serializeObject(Object obj) throws DeserializeException, InvocationTargetException, IllegalAccessException, JSONException, SerializeException, NoSuchMethodException, InstantiationException {
        IJSONConverter BaseConverter = new BaseConverter();
        JSONObject result = new JSONObject();
        
        Class objType = obj.getClass();
        JSONSerializable serializableSetting = (JSONSerializable) objType.getAnnotation(JSONSerializable.class);
        
        if(!serializableSetting.converter().equals(JSONConvert.class)){
            IJSONConverter converter = (IJSONConverter) serializableSetting.converter().getConstructor().newInstance();
            return (T)converter.serialize(obj);
        }
        
        Method[] methods = objType.getDeclaredMethods();//get all methods
        Field[] fields = objType.getDeclaredFields();//get all fields

        for(Method m : methods){
            if(!m.isAnnotationPresent(JSONProperty.class))continue;
            m.setAccessible(true);

            JSONProperty setting = m.getAnnotation(JSONProperty.class);
            String key = getMethodKey(m, setting);

            if(!setting.getable())continue;

            Object value = m.invoke(obj);
            Class valueType = null;
            if(value!=null)valueType = value.getClass();
            
            if(value == null){
            }else if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.serialize(value);
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){
                value = serialize(value);
            }else if(valueType.isArray()){
                value = serialize(value);
            }else if(valueType.isEnum()){
                value = value.toString();
            }else{
                value = BaseConverter.serialize(value);
            }

            result.put(key, value);
        }

        for(Field f : fields){
            if(!f.isAnnotationPresent(JSONProperty.class))continue;
            f.setAccessible(true);

            JSONProperty setting = f.getAnnotation(JSONProperty.class);

            String key = getFieldKey(f,setting);

            if(!setting.getable())continue;

            Object value = f.get(obj);
            Class valueType = null;
            if(value!=null)valueType = value.getClass();
            
            if(value == null){
            }else if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.serialize(value);
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){
                value = serialize(value);
            }else if(valueType.isArray()){
                value = serialize(value);
            }else if(valueType.isEnum()){
                value = value.toString();
            }else{
                value = BaseConverter.serialize(value);
            }

            result.put(key, value);
        }
        return (T)result;
    }

    public static <T> T deserialize(Class<T> type,Object json) throws DeserializeException {
        Class objType = json.getClass();
        if (json == null) return null;
        if (json == JSONObject.NULL)return null;
        
        try {
            if (type.isArray()) {
                return deserializeArray(type, (JSONArray) json);
            } else if (type.isAnnotationPresent(JSONSerializable.class)) {
                return deserializeObject(type, (JSONObject) json);
            }
        }catch(Exception e){
            throw new DeserializeException();
        }
        return null;
    }

    private static <T> T deserializeArray(Class type,JSONArray json) throws JSONException, DeserializeException {
        IJSONConverter BaseConverter = new BaseConverter();

        T result = (T) Array.newInstance(type.getComponentType(), json.length());
        Class valueType = type.getComponentType();

        for(int i = 0 ; i < json.length() ; i++){
            Object value = json.get(i);
            if(valueType.isArray()){
                value = deserialize(valueType,value);
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){
                value = deserialize(valueType,value);
            }else if(valueType.isEnum()){
                value = Enum.valueOf(valueType, (String) value);
            }else{
                value = valueType.cast(BaseConverter.deserialize(valueType,value));
            }
            Array.set(result,i,value);
        }

        return result;
    }

    private static <T> T deserializeObject(Class type,JSONObject json) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, JSONException, DeserializeException {
        IJSONConverter BaseConverter = new BaseConverter();

        T result = (T) type.getConstructor().newInstance();
        
        JSONSerializable serializableSetting = (JSONSerializable) type.getAnnotation(JSONSerializable.class);
        
        if(!serializableSetting.converter().equals(JSONConvert.class)){
            IJSONConverter converter = (IJSONConverter) serializableSetting.converter().getConstructor().newInstance();
            return (T) converter.deserialize(type, json);
        }
        
        Method[] methods = type.getDeclaredMethods();//get all methods
        Field[] fields = type.getDeclaredFields();//get all fields

        for (Method m : methods) {
            if (!m.isAnnotationPresent(JSONProperty.class)) continue;

            JSONProperty setting = m.getAnnotation(JSONProperty.class);
            if (!setting.setable()) continue;
            m.setAccessible(true);

            String key = getMethodKey(m, setting);

            Object value = null;
            try {
                value = json.get(key);
            } catch (Exception ex) {
                continue;
            }

            Class setType = m.getParameterTypes()[0];
            if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.deserialize(setType, value);
            } else if (setType.isAnnotationPresent(JSONSerializable.class)) {
                value = deserialize(setType, value);
            }else if(setType.isEnum()){
                value = Enum.valueOf(setType, (String) value);
            } else if (setType.isArray()) {
                value = deserialize(setType, value);
            }else{
                value = BaseConverter.deserialize(setType, value);
            }

            m.invoke(result, value);
        }

        for (Field f : fields) {
            if (!f.isAnnotationPresent(JSONProperty.class)) continue;

            Annotation[] Anno = f.getDeclaredAnnotations();

            JSONProperty setting = f.getAnnotation(JSONProperty.class);
            if (!setting.setable()) continue;
            f.setAccessible(true);

            String key = getFieldKey(f, setting);

            Object value = null;
            try {
                value = json.get(key);
            } catch (Exception e) {
                continue;
            }

            Class fType = f.getType();
            if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.deserialize(fType,value);
            } else if (fType.isAnnotationPresent(JSONSerializable.class)) {
                value = deserialize(fType, value);
            }else if(fType.isEnum()){
                value = Enum.valueOf(fType, (String) value);
            } else if (fType.isArray()) {
                value = deserialize(fType, value);
            }else{
                value = BaseConverter.deserialize(fType, value);
            }

            f.set(result, value);
        }
        return result;
    }
}