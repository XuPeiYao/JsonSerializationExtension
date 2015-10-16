/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.json.serialization.converters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.serialization.DeserializeException;
import org.json.serialization.IJSONConverter;
import org.json.serialization.SerializeException;

/**
 *
 * @author XuPeiYao
 */
public class URIConverter implements IJSONConverter{

    @Override
    public Object serialize(Object obj) throws SerializeException {
        return obj.toString();
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        try {
            return (T)new URI((String)obj);
        } catch (URISyntaxException ex) {
            Logger.getLogger(URIConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
