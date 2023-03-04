/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.tools.java.generator;

import java.util.*;
import java.lang.reflect.Modifier;

//import org.openide.src.*;

/**
 * Holds declared parslets by parslet name.
 * <!ELEMENT parslet #EMPTY>
 * <!ATTLIST parslet id ID #REQUIRED>
 * <!ATTLIST parslet return CDATA #REQUIRED>  //primitive type or fully classified class
 */
public class ParsletBindings extends HashMap {

    //TODO: Retouche
    /** Serial Version UID */
    private static final long serialVersionUID =5328744032505397530L;


    // parameter names to generated methods
    
    static final String DATA = "data";  // NOI18N
    static final String META = "meta";  // NOI18N

    static final String STRING_TYPE = "java.lang.String";
//    
//    static final MethodParameter DEFAULT_DATA_PARAMETER = 
//        new MethodParameter(DATA, STRING_TYPE, true);
//    
//    static final MethodParameter[] DEFAULT_DATA_PARAMETERS = 
//        new MethodParameter[] { DEFAULT_DATA_PARAMETER };    
//        
    /** Create empty map. */
    public ParsletBindings() {            
    }

    /** 
      * Typed put.
      * @see java.util.Map#put(Object,Object)
      */
    public Entry put(String parslet, String returnType) {
        
            return (Entry) super.put(parslet, new Entry(parslet, returnType));
        
    }

    public Entry put(String parslet, Entry entry) {
        if (parslet == null) return null;
        if (parslet.equals(entry.getId()) == false) return null;
        
        return (Entry) super.put(parslet, entry);
    }
    
//    /** 
//      * Get a MethodParameter produced by given parslet.
//      * @param parslet id of parslet or null
//      * @return parslet return type as MethodParameter or DEFAULT_DATA_PARAMETER if null param
//      */
 //   public MethodParameter getReturnAsParameter(String parslet) {
//        Entry param = seek(parslet);
//        if (param == null) {
//            return DEFAULT_DATA_PARAMETER;
//        } else {
//            return new MethodParameter(DATA, param.type, true);
//        }
 //   }
//
    public Entry getEntry(String parslet) {
        return (Entry) super.get(parslet);
    }

    /** 
      * Get return Type produced by given parslet.
      * @param parslet id of parslet or null
      * @return parslet return Type or String Type if null param
      */        
    public String getReturnType(String parslet) {

        Entry param = seek(parslet);
        if (param == null) {
            return STRING_TYPE;
        } else {
            return param.type;
        }            
    }

//    /**
//      * Get Method element representing parslet method or null.
//      */
//    public MethodElement getMethod(String parslet) throws SourceException {
//        Entry param = seek(parslet);
//        if (param == null) {
//            return null;
//        } else {
//            MethodElement method = new MethodElement();
//            method.setName(Identifier.create(param.id));
//            method.setParameters(DEFAULT_DATA_PARAMETERS);
//            method.setReturn(param.type);
//            method.setModifiers(Modifier.PUBLIC);
//            method.setExceptions (new Identifier[] { Identifier.create("SAXException") }); // NOI18N
//            return method;
//        }
//    }
//
    /**
      * Get parslet from map or null.
      */
    private Entry seek(String parslet) {
        if (parslet == null) {
            return null;
        } else {
            Entry param = (Entry) super.get(parslet);
            if (param == null) {
                return null;
            } else {
                return param;
            }
        }            
    }
 
    /**
     * Holds information about a parslet.
     * Not used yet.
     */    
    public static final class Entry {

        /** Holds value of property parslet id. */
        private String id;
//
        /** Holds value of property type. */
        private String type;
//
        /** Creates new ParsletEntry */
        public Entry(String id, String type)  {
            this.id = id;
            this.type = type;
        }

        /** 
         * Getter for property id.
         * @return Value of property id.
         */
        public String getId() {
            return id;
        }

        /** 
         * Getter for property type.
         * @return Value of property type.
         */
        public String getType() {
            return type;
        }

        void setReturnType(String type) {
            this.type = type;
        }

        public String toString() {
            return "(" + id + " => " + type + ")"; // NOI18N
        }
    }
}
