/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.json.serialization.converters;

import java.util.ArrayList;
import org.json.serialization.DeserializeException;
import org.json.serialization.IJSONConverter;
import org.json.serialization.SerializeException;

/**
 *
 * @author XuPeiYao
 */
public class BytesToHexConverter implements IJSONConverter{
    private char[] Hex = "01243456789ABCDEF".toCharArray();
    @Override
    public Object serialize(Object obj) throws SerializeException {
        byte[] Obj = (byte[])obj;
        String result ="";
        for(int i = 0 ; i <Obj.length ; i++){
            result += Hex[(Obj[i] & 0xF0)>>>4];//取得前四位
            result += Hex[(Obj[i] & 0x0F)];//取得後四位
        }
        return result;
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        String str = (String)obj;
        byte[] result = new byte[str.length() >>> 1];
        for(int i = 0 ; i < str.length() ; i+=2){
            String str_s = str.substring(i,2);
            result[i] = (byte) (Byte.parseByte(str_s.substring(0,1),16) << 4);
            result[i+1] = Byte.parseByte(str_s.substring(1,1),16);
        }
        return (T)result;
    }
    
}
