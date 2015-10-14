package org.json.serialization;

import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by XuPeiYao on 2015/10/13.
 */
public interface IJSONConverter {
    Object serialize(Object obj) throws SerializeException;
    <T> T deserialize(Class<T> type, Object obj) throws DeserializeException;
}
