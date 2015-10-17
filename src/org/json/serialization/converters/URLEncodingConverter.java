/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.json.serialization.converters;

import java.net.URLDecoder;
import java.net.URLEncoder;
import org.json.serialization.DeserializeException;
import org.json.serialization.IJSONConverter;
import org.json.serialization.SerializeException;

/**
 *
 * @author XuPeiYao
 */
public class URLEncodingConverter implements IJSONConverter{

    @Override
    public Object serialize(Object obj) throws SerializeException {
        return URLEncoder.encode((String)obj);
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        return (T)URLDecoder.decode((String)obj);
    }
    
}
