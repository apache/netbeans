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
import org.openide.util.NbBundle;


/**
 * Holds binding: element => {handler type, method name, parslet}.
 *
 * <!ELEMENT bind (attbind*)>
 * <!ATTLIST bind element ID #REQUIRED>
 * <!ATTLIST bind method CDATA #REQUIRED>
 * <!ATTLIST bind type (data, container, ignore, mix, empty) "data">
 * <!ATTLIST bind parslet IDREF #IMPLIED>
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ElementBindings extends HashMap {

    /** Serial Version UID */
    private static final long serialVersionUID =-1529089675411853246L;    

    // some display names
    
    public static final String DELIMITER_HANDLING = 
        NbBundle.getMessage(ElementBindings.class, "DELIMITER_HANDLING");
    public static final String DATA_HANDLING = 
        NbBundle.getMessage(ElementBindings.class, "DATA_HANDLING");
    public static final String IGNORE_HANDLING = 
        NbBundle.getMessage(ElementBindings.class, "IGNORE_HANDLING");
    public static final String MIXED_HANDLING = 
        NbBundle.getMessage(ElementBindings.class, "MIXED_HANDLING");
    public static final String EMPTY_HANDLING =
        NbBundle.getMessage(ElementBindings.class, "EMPTY_HANDLING");
    
    /** Create empty map. */
    public ElementBindings() {            
    }

    /** 
      * Typed put.
      * @see java.util.Map#put(Object,Object)
      */
    public Entry put(String element, Entry entry) {
        if (element == null) return null;
        if (element.equals(entry.getElement()) == false) return null;
        return (Entry) super.put(element, entry);
    }

    /**
     * Settle the map by following values.
     */
    public Entry put(String element, String method, String parslet, String type) {
        return (Entry) super.put(element, new Entry(element, method, parslet, type)); 
    }

    /**
     * Get a method postfix by element name.     
     */
    public String getMethod(String element) {
        Entry entry = getEntry(element);
        if (entry == null) {
            return null;
        } else { 
            return entry.getMethod();
        }
    }

    /**
     * Get a parslet name by 
     */
    public String getParslet(String element) {
        Entry entry = getEntry(element);
        if (entry == null) {
            return null;
        } else {
            return entry.getParslet();
        }
    }
    
    /*
     * @return true it there is used given parslet
     */
    public boolean containsParslet(String name) {
        
        if (name == null) return false;
        
        Iterator<Entry> it = values().iterator();
        while (it.hasNext()) {
            Entry next = it.next();
            if (name.equals(next.parslet)) return true;
        }
        
        return false;
    }
    
    /**
     * @return number of Entires using given parslet 
     */
    public int getParsletUsageCount(String parslet) {
        int toret = 0;
        Iterator<Entry> it = values().iterator();
        
        while (it.hasNext()) {
            Entry next = it.next();
            if (parslet != null && parslet.equals(next.parslet)) {            
                toret++;
            }
        }
        
        return toret;
    }

    /**
     * Get entry keyed by given element name.
     */
    public final Entry getEntry(String element) {
        return (Entry) get(element);
    }

    /**
     * Get entry keyed by given index. Suitable for table models.
     * @param index 0 based index
     */
    public final Entry getEntry(final int index) {
        int myindex = index;
        Iterator<Entry> it = values().iterator();
        while (it.hasNext()) {
            Entry next = it.next();
            if (myindex-- == 0) 
                return next;
        }
        return null;
    }
    
    public String toString() {
        Iterator<String> it = values().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("{"); // NOI18N
        while (it.hasNext()) {
            sb.append(it.next());
        }
        sb.append("}"); // NOI18N
        return sb.toString();
    }

    /**
     * Holds binding element name -> (method  name, hadler type, parslet name).
     */        
    public static final class Entry {

        // Entry types as clasified by a user
        
        public static final String EMPTY = "EMPTY"; // NOI18N
        public static final String CONTAINER = "CONTAINER"; // NOI18N
        public static final String DATA = "DATA"; // NOI18N
        public static final String MIXED = "MIXED"; // NOI18N
        public static final String IGNORE = "IGNORE"; // NOI18N

        private String type;

        // other properties 
        
        private String method;

        private String element;

        private String parslet;

        
        /** Creates new HandlerMappingEntry */
        public Entry(String element, String method, String parslet, String type) {
            this.method = method;
            this.element = element;
            this.parslet = parslet;
            this.type = type;
        }

        /** 
          * Getter for property method.
          * @return Value of property method.
          */
        public String getMethod() {
            return method;
        }

        /** 
          * Getter for property element.
          * @return Value of property element.
          */
        public String getElement() {
            return element;
        }

        /** 
          * Getter for property parslet.
          * @return Value of property parslet.
          */
        public String getParslet() {
            return parslet;
        }
        
        /** Getter for property type.
          * @return Value of property type.
          */
        public String getType() {
            return type;
        }
                
        public String toString() {
            return element + " => (" + method + ", " + parslet + ", " + type + ")"; // NOI18N
        }
        
        void setType(String type) {
            this.type = type;
        }
        
        void setMethod(String method) {
            this.method = method;
        }
        
        void setParslet(String parslet) {
            this.parslet = parslet;
        }
        
        /** 
         * Translate type code into its textual representation 
         */
        public static final String displayTypeFor(String type) {

            if (ElementBindings.Entry.CONTAINER.equals(type)) {
                return DELIMITER_HANDLING;
            } else if (ElementBindings.Entry.DATA.equals(type)) {            
                 return DATA_HANDLING;
            } else if (ElementBindings.Entry.MIXED.equals(type)) {
                return MIXED_HANDLING;
            } else if (ElementBindings.Entry.IGNORE.equals(type)) {
                return IGNORE_HANDLING;
            } else if (ElementBindings.Entry.EMPTY.equals(type)) {
                return EMPTY_HANDLING;
            } else {
                return IGNORE_HANDLING;
            }
        }

        /**
         * Translate type sring to type code int
         */
        public static final String typeFor(String type) {
            if (DELIMITER_HANDLING.equals(type)) {
                return ElementBindings.Entry.CONTAINER;
            } else if (DATA_HANDLING.equals(type)) {
                return ElementBindings.Entry.DATA;
            } else if (MIXED_HANDLING.equals(type)) {
                return ElementBindings.Entry.MIXED;
            } else if (EMPTY_HANDLING.equals(type)) {
                return ElementBindings.Entry.EMPTY;
            } else {
                return ElementBindings.Entry.IGNORE;
            }
        }

        /**
         * Return suitable types (Vector<Strings>) for given element.
         */
        public static final Vector displayTypesFor(ElementDeclarations.Entry entry) {
            Vector<String> list = new Vector<>(4);
            list.add(IGNORE_HANDLING);
            if ((entry.getType() & ElementDeclarations.Entry.DATA) == 1) 
                list.add(DATA_HANDLING);
            if ((entry.getType() & ElementDeclarations.Entry.CONTAINER) == 2)
                list.add(DELIMITER_HANDLING);
            if (entry.getType() == ElementDeclarations.Entry.MIXED)
                list.add(MIXED_HANDLING);
            if (entry.getType() == ElementDeclarations.Entry.EMPTY)
                list.add(EMPTY_HANDLING);
            return list;
        }
    }
}
