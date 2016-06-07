package org.json.serialization;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.applet.AppletThreadGroup;
import sun.nio.ch.ThreadPool;

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

    public static <T> T serialize(Object obj) throws SerializeException{
        return serialize(obj, false);
    }
    
    public static <T> T serialize(Object obj,boolean multithread) throws SerializeException {
        Class objType = obj.getClass();
        try {
            if (objType.isArray()) {
                return serializeArray(obj,multithread);
            } else if (objType.isAnnotationPresent(JSONSerializable.class)) {//custom class              
                return serializeObject(obj,multithread);
            }
        }catch(Exception e){
            throw new SerializeException();
        }
        return null;//know object
    }

    public static class serializeArrayRunnable extends Thread{
        private Object value;
        private JSONArray result;
        private int index;
        public serializeArrayRunnable(Object value,JSONArray ary,int index){
            this.value = value;
            this.result = ary;
            this.index = index;
        }

        @Override
        public void run(){
            try {
                result.put(index,(Object)serialize(this.value,true));
            } catch (Exception ex) {
                try {
                    result.put(index,(Object)null);
                } catch (JSONException ex1) {
                    Logger.getLogger(JSONConvert.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }
    
    public static class serializeObjectRunnable extends Thread{
        private Object value;
        private JSONObject result;
        private String key;
        public serializeObjectRunnable(Object value,JSONObject target,String key){
            this.value = value;
            this.result = target;
            this.key = key;
        }

        @Override
        public void run(){
            try {
                result.put(key,(Object)serialize(this.value,true));
            } catch (Exception ex) {
                result = null;
            }
        }
    }
    
    public static class deserializeArrayRunnable extends Thread{
        private Object value;
        private Object result;
        private int index;
        private Class type;
        public deserializeArrayRunnable(Object value,Object ary,int index,Class type){
            this.value = value;
            this.result = ary;
            this.index = index;
            this.type = type;
        }

        @Override
        public void run(){
            try {
                Array.set(value, index, deserialize(type,result,true));
            } catch (Exception ex) {

            }
        }
    }
    
    public static class deserializeObjectRunnable extends Thread{
        private Object value;
        private JSONObject result;
        private Method key1;
        private Field key2;
        private Class type;
        public deserializeObjectRunnable(Object value,JSONObject target,Method key1,Field key2,Class type){
            this.value = value;
            this.result = target;
            this.key1 = key1;
            this.key2 = key2;
            this.type = type;
        }

        @Override
        public void run(){
            try {
                if(this.key1 != null){
                    key1.invoke(value, deserialize(type, result, true));
                }else{
                    key2.set(value, deserialize(type, result, true));
                }
            } catch (Exception ex) {
                result = null;
            }
        }
    }
    
    private static <T> T serializeArray(Object obj,boolean multithread) throws InvocationTargetException, JSONException, IllegalAccessException, SerializeException, InterruptedException {
        IJSONConverter BaseConverter = new BaseConverter();
        JSONArray result = new JSONArray();
        int aryLength=Array.getLength(obj);
        
        Thread[] threads = new serializeArrayRunnable[aryLength];
        
        for(int i = 0 ; i < aryLength ; i++){
            Object value = Array.get(obj,i);
            Class valueType = value.getClass();
            if(value == null) {
            }else if(valueType.isArray()){//has subarray
                if(multithread){
                    threads[i] = new serializeArrayRunnable(value,result,i);
                    threads[i].start();
                    continue;
                }else{
                    value = serialize(value, multithread);
                }
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){//this value can serializable
                if(multithread){
                    threads[i] = new serializeArrayRunnable(value,result,i);
                    threads[i].start();
                    continue;
                }else{
                    value = serialize(value, multithread);
                }
            }else if(valueType.isEnum()){
                value = value.toString();
            }else{//call base converter
                value = BaseConverter.serialize(value);
            }
            result.put(i,value);
        }
        if(multithread){
            waitAllThread(threads);
        }
        return (T)result;
    }
    
    private static void waitAllThread(Thread[] threads) throws InterruptedException{
        if(threads == null)return;
        for(int i = 0 ; i < threads.length ; i++){
            if(threads[i] == null)continue;
            synchronized (threads[i]) {
                if(threads[i].isAlive()){
                    try{
                        threads[i].wait();
                    }catch(Exception e){
                        System.out.println(e);
                    }
                }
            }
        }
    }

    private static <T> T serializeObject(Object obj,boolean multithread) throws DeserializeException, InvocationTargetException, IllegalAccessException, JSONException, SerializeException, NoSuchMethodException, InstantiationException, InterruptedException {
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

        Thread[] methodThreads = new serializeArrayRunnable[methods.length];
        Thread[] fieldThreads = new serializeArrayRunnable[fields.length];
        
        for(int i = 0 ; i < methods.length ; i++){
            if(!methods[i].isAnnotationPresent(JSONProperty.class))continue;
            methods[i].setAccessible(true);

            JSONProperty setting = methods[i].getAnnotation(JSONProperty.class);
            String key = getMethodKey(methods[i], setting);

            if(!setting.getable())continue;

            Object value = methods[i].invoke(obj);
            Class valueType = null;
            if(value!=null)valueType = value.getClass();
            
            if(value == null){
            }else if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.serialize(value);
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){
                if(multithread){
                    methodThreads[i] = new serializeObjectRunnable(value,result,key);
                    methodThreads[i].start();
                    continue;
                }else{
                    value = serialize(value,multithread);
                }
            }else if(valueType.isArray()){
                if(multithread){
                    methodThreads[i] = new serializeObjectRunnable(value,result,key);
                    methodThreads[i].start();
                    continue;
                }else{
                    value = serialize(value,multithread);
                }
            }else if(valueType.isEnum()){
                value = value.toString();
            }else{
                value = BaseConverter.serialize(value);
            }

            result.put(key, value);
        }

        for(int i = 0 ; i < fields.length ;i++){
            if(!fields[i].isAnnotationPresent(JSONProperty.class))continue;
            fields[i].setAccessible(true);

            JSONProperty setting = fields[i].getAnnotation(JSONProperty.class);

            String key = getFieldKey(fields[i],setting);

            if(!setting.getable())continue;

            Object value = fields[i].get(obj);
            Class valueType = null;
            if(value!=null)valueType = value.getClass();
            
            if(value == null){
            }else if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.serialize(value);
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){
                if(multithread){
                    fieldThreads[i] = new serializeObjectRunnable(value,result,key);
                    fieldThreads[i].start();
                    continue;
                }else{
                    value = serialize(value,multithread);
                }
            }else if(valueType.isArray()){
                if(multithread){
                    fieldThreads[i] = new serializeObjectRunnable(value,result,key);
                    fieldThreads[i].start();
                    continue;
                }else{
                    value = serialize(value,multithread);
                }
            }else if(valueType.isEnum()){
                value = value.toString();
            }else{
                value = BaseConverter.serialize(value);
            }

            result.put(key, value);
        }
        if(multithread){
            waitAllThread(methodThreads);
            waitAllThread(fieldThreads);
        }
        return (T)result;
    }

    public static <T> T deserialize(Class<T> type,Object json,boolean multithread) throws DeserializeException {
        Class objType = json.getClass();
        if (json == null) return null;
        if (json == JSONObject.NULL)return null;
        
        try {
            if (type.isArray()) {
                return deserializeArray(type, (JSONArray) json, multithread);
            } else if (type.isAnnotationPresent(JSONSerializable.class)) {
                return deserializeObject(type, (JSONObject) json, multithread);
            }
        }catch(Exception e){
            throw new DeserializeException();
        }
        return null;
    }

    private static <T> T deserializeArray(Class type,JSONArray json,boolean multithread) throws JSONException, DeserializeException, InterruptedException {
        IJSONConverter BaseConverter = new BaseConverter();

        T result = (T) Array.newInstance(type.getComponentType(), json.length());
        Class valueType = type.getComponentType();

        Thread[]  threads = new Thread[json.length()];
        
        for(int i = 0 ; i < json.length() ; i++){
            Object value = json.get(i);
            if(valueType.isArray()){
                if(multithread){
                    threads[i] = new deserializeArrayRunnable(result, value,i,valueType);
                    threads[i].start();
                    continue;
                }else{
                    value = deserialize(valueType,value, multithread);
                }
            }else if(valueType.isAnnotationPresent(JSONSerializable.class)){
                if(multithread){
                    threads[i] = new deserializeArrayRunnable(result, value,i,valueType);
                    threads[i].start();
                    continue;
                }else{
                    value = deserialize(valueType,value, multithread);
                }
            }else if(valueType.isEnum()){
                value = Enum.valueOf(valueType, (String) value);
            }else{
                value = valueType.cast(BaseConverter.deserialize(valueType,value));
            }
            Array.set(result,i,value);
        }
        if(multithread){
            waitAllThread(threads);
        }
        return result;
    }

    private static <T> T deserializeObject(Class type,JSONObject json,boolean multithread) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, JSONException, DeserializeException, InterruptedException {
        IJSONConverter BaseConverter = new BaseConverter();

        T result = (T) type.getConstructor().newInstance();
        
        JSONSerializable serializableSetting = (JSONSerializable) type.getAnnotation(JSONSerializable.class);
        
        if(!serializableSetting.converter().equals(JSONConvert.class)){
            IJSONConverter converter = (IJSONConverter) serializableSetting.converter().getConstructor().newInstance();
            return (T) converter.deserialize(type, json);
        }
        
        Method[] methods = type.getDeclaredMethods();//get all methods
        Field[] fields = type.getDeclaredFields();//get all fields

        Thread[] methodThreads = new deserializeObjectRunnable[methods.length];
        Thread[] fieldThreads = new deserializeObjectRunnable[fields.length];
        
        for (int i = 0 ; i <  methods.length ; i++) {
            if (!methods[i].isAnnotationPresent(JSONProperty.class)) continue;

            JSONProperty setting = methods[i].getAnnotation(JSONProperty.class);
            if (!setting.setable()) continue;
            methods[i].setAccessible(true);

            String key = getMethodKey(methods[i], setting);

            Object value = null;
            try {
                value = json.get(key);
            } catch (Exception ex) {
                continue;
            }

            Class setType = methods[i].getParameterTypes()[0];
            if(JSONObject.NULL.equals(value)){
                value = null;
            } else if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.deserialize(setType, value);
            } else if (setType.isAnnotationPresent(JSONSerializable.class)) {
                if(multithread){
                    methodThreads[i] = new deserializeObjectRunnable(result,(JSONObject)value,methods[i],null, setType);
                    methodThreads[i].start();
                    continue;
                }else{
                    value = deserialize(setType, value, multithread);
                }
            }else if(setType.isEnum()){
                value = Enum.valueOf(setType, (String) value);
            } else if (setType.isArray()) {
                if(multithread){
                    methodThreads[i] = new deserializeObjectRunnable(result,(JSONObject)value,methods[i],null, setType);
                    methodThreads[i].start();
                    continue;
                }else{
                    value = deserialize(setType, value, multithread);
                }
            }else{
                value = BaseConverter.deserialize(setType, value);
            }

            methods[i].invoke(result, value);
        }

        for (int i = 0 ; i < fields.length ; i++) {
            if (!fields[i].isAnnotationPresent(JSONProperty.class)) continue;

            Annotation[] Anno = fields[i].getDeclaredAnnotations();

            JSONProperty setting = fields[i].getAnnotation(JSONProperty.class);
            if (!setting.setable()) continue;
            fields[i].setAccessible(true);

            String key = getFieldKey(fields[i], setting);

            Object value = null;
            try {
                value = json.get(key);
            } catch (Exception e) {
                continue;
            }

            Class fType = fields[i].getType();
            if(JSONObject.NULL.equals(value)){
                value= null;
            } else if(!setting.converterType().equals(BaseConverter.class)) {
                IJSONConverter converter = (IJSONConverter) setting.converterType().newInstance();
                value =converter.deserialize(fType,value);
            } else if (fType.isAnnotationPresent(JSONSerializable.class)) {
                if(multithread){
                    fieldThreads[i] = new deserializeObjectRunnable(result,(JSONObject)value,null,fields[i], fType);
                    fieldThreads[i].start();
                    continue;
                }else{
                    value = deserialize(fType, value, multithread);
                }
            }else if(fType.isEnum()){
                value = Enum.valueOf(fType, (String) value);
            } else if (fType.isArray()) {
                if(multithread){
                    fieldThreads[i] = new deserializeObjectRunnable(result,(JSONObject)value,null,fields[i], fType);
                    fieldThreads[i].start();
                    continue;
                }else{
                    value = deserialize(fType, value, multithread);
                }
            }else{
                value = BaseConverter.deserialize(fType, value);
            }

            fields[i].set(result, value);
        }
        if(multithread){
            waitAllThread(methodThreads);
            waitAllThread(fieldThreads);
        }
        
        return result;
    }
}