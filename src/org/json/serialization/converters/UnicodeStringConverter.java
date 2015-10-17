package org.json.serialization.converters;

import java.util.Formatter;
import org.json.serialization.DeserializeException;
import org.json.serialization.IJSONConverter;
import org.json.serialization.SerializeException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author XuPeiYao
 */
public class UnicodeStringConverter implements IJSONConverter{

    @Override
    public Object serialize(Object obj) throws SerializeException {
        String input = (String)obj;
        StringBuilder b = new StringBuilder(input.length());
        Formatter f = new Formatter(b);
        for (char c : input.toCharArray()) {
         if (c < 128) {
          b.append(c);
         } else {
          f.format("\\u%04x", (int) c);
         }
        }
        return b.toString();
    }

    @Override
    public <T> T deserialize(Class<T> type, Object obj) throws DeserializeException {
        String str = (String)obj;
        String result = "";
        int sz = str.length();
        StringBuffer unicode = new StringBuffer(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                        int value = Integer.parseInt(unicode.toString(), 16);
                        result += ((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
                    case '\\':
                        result += '\\';
                        break;
                    case '\'':
                        result +='\'';
                        break;
                    case '\"':
                        result +='"';
                        break;
                    case 'r':
                        result +='\r';
                        break;
                    case 'f':
                        result +='\f';
                        break;
                    case 't':
                        result +='\t';
                        break;
                    case 'n':
                        result +='\n';
                        break;
                    case 'b':
                        result +='\b';
                        break;
                    case 'u':
                        {
                            // uh-oh, we're in unicode country....
                            inUnicode = true;
                            break;
                        }
                    default :
                        result +=ch;
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            result +=ch;
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            result +='\\';
        }
        
        return (T)result;
    }
}
