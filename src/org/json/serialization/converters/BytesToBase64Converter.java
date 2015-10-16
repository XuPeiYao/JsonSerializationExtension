/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.json.serialization.converters;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.serialization.DeserializeException;
import org.json.serialization.IJSONConverter;
import org.json.serialization.SerializeException;

/**
 *
 * @author XuPeiYao
 */
public class BytesToBase64Converter implements IJSONConverter {

    @Override
    public Object serialize(Object obj) throws SerializeException {
        return Base64.encode((byte[])obj);
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        try {
            return (T)Base64.decode((String)obj);
        } catch (Base64DecodingException ex) {
            Logger.getLogger(BytesToBase64Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
