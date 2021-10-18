/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.regex.parser;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sandeemi
 */
public class SpecialChar {
    
    public static Map<Character, RegEx> sChars = buildHashMap('t', new Primitive('\t') ,
			'n', new Primitive('\n') ,
			'r', new Primitive('\r') ,
			'f', new Primitive('\f') ,
			'\\', new Primitive('\\') ,
                        's', buildWhiteSpaceEscape(false),
                        'd', buildDigitEscape(false),
                        'w', buildAlphaNumericEscape(false),
                        'S', buildWhiteSpaceEscape(true),
                        'D', buildDigitEscape(true),
                        'W', buildAlphaNumericEscape(true)
    );
//    
//    public static Character[] lineterminators = new Character[]
//    
//    public static HashSet<Character> LINE_TERMINATORS = new HashSet<>(Arrays.asList());
    
    public static RegEx buildWhiteSpaceEscape(boolean negation){
        
        CharClass charClass = new CharClass(negation, false);
        
        charClass.addToClass(new Primitive('\t'), 
                                new Primitive('\n'), 
                                new Primitive('\f'),
                                new Primitive('\r'),
                                new Primitive(' '));
        return charClass;
    }
    
    public static RegEx buildDigitEscape(boolean negation){
        CharClass charClass = new CharClass(negation, false);
        charClass.addToClass(new Range('0','9'));
        return charClass;
    }
    
    public static RegEx buildAlphaNumericEscape(boolean negation){
        CharClass charClass = new CharClass(false, false);
        charClass.addToClass(new Range('1','9'), 
                                new Range('a','z'),
                                new Range('A','Z'),
                                new Primitive('_'));
        
        return charClass;
    }
    
    public static HashMap<Character, RegEx> buildHashMap(Object... data) {
        HashMap<Character, RegEx> result = new HashMap<Character, RegEx>();

        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of arguments");
        }

        Character key = null;
        Integer step = -1;

        for (Object value : data) {
            step++;
            switch (step % 2) {
                case 0:
                    if (value == null) {
                        throw new IllegalArgumentException("Null key value");
                    }
                    key = (Character)value;
                    continue;
                case 1:
                    result.put(key, (RegEx)value);
                    break;
            }
        }

        return result;
    }
    
}
