/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.json.serialization.converters;
import java.util.Date;
import org.json.serialization.DeserializeException;
import org.json.serialization.IJSONConverter;
import org.json.serialization.SerializeException;

public class DateConverter implements IJSONConverter{

    @Override
    public Object serialize(Object obj) throws SerializeException {
        Date Obj = (Date)obj;
        return Obj.getTime();
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        return (T)new Date((long)obj);
    }
    
}

