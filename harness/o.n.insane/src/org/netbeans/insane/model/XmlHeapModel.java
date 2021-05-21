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

package org.netbeans.insane.model;

import java.io.*;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An implementation of the heap model that parses SimpleXmlVisitor output
 * and keeps all the data in memory.
 *
 * @author  Nenik
 */
class XmlHeapModel implements org.netbeans.insane.model.HeapModel {

    public static HeapModel parse(File file) throws Exception {
        HeapModel model = new XmlHeapModel(new InputSource(new FileInputStream(file)));
        return model;
    }

    private Map<Integer,Item> items = new HashMap<Integer,Item>();
    private Map<String,Item> roots = new HashMap<String,Item>();
    
    // HeapModel interface implementation
    public Iterator<Item> getAllItems() {
        return Collections.unmodifiableCollection(items.values()).iterator();
    }
    
    public Collection<Item> getObjectsOfType(String type) {
        Collection<Item> filter = new ArrayList<Item>();
        for (Iterator<Item> it = getAllItems(); it.hasNext();) {
            Item act = it.next();
            if (type.equals(act.getType()))filter.add(act);
        }
        return filter;
    }
    
    public Collection<String> getRoots() {
        return Collections.unmodifiableSet(roots.keySet());
    }

    
    public Item getObjectAt(String staticRefName) {
        return roots.get(staticRefName);
    }
    
    public Item getItem(int id) {
        Item itm = items.get(new java.lang.Integer(id));
        if (itm == null) throw new IllegalArgumentException("Bad ID");
        return itm;
    }

    static class MemItem implements Item {
        // a list of Items, Strings and one null
        private Object[] incommingRefs = new Object[0];
        private Item[] outgoingRefs = new Item[0];
        private int id;
        private int size;
        private String type;
        private String value;

        MemItem(int id, String type, int size, String value) {
            this.id = id;
            this.type = type.intern();
            this.size = size;
            this.value = value;
        }
            
        // Item interface implementation    
        public String getType() {
            return type;
        }
            
        public int getSize() {
            return size;
        }
        
        public String getValue() {
            return value;
        }

        public Enumeration<Object> incomming() {
            return new RefEnum<Object>(incommingRefs);
        }

        public Enumeration<Item> outgoing() {
            return new RefEnum<Item>(outgoingRefs);
        }
    
        public int getId() {
            return id;
        }
            
        // debug helper
        public String toString() {
            if (value == null) {
                return type + "@" + Integer.toHexString(id);
            } else {
                return type + "@" + Integer.toHexString(id) + ": \"" + value + '"';
            }
        }

        // parsing impl
        void addIncomming(Object incomming) {
            Object[] nr = new Object[incommingRefs.length + 1];
            nr[0] = incomming;
            System.arraycopy(incommingRefs, 0, nr, 1, incommingRefs.length);
            incommingRefs = nr;
        }

        void addOutgoing(Item outgoing) {
            Item[] nr = new Item[outgoingRefs.length+1];
            nr[outgoingRefs.length] = outgoing;
            System.arraycopy(outgoingRefs, 0, nr, 0, outgoingRefs.length);
            outgoingRefs = nr;
        }
    }

    
    //parser implementation
    
    private XmlHeapModel(InputSource is) throws Exception {
        Handler h = new Handler();
        SAXParserFactory fact = SAXParserFactory.newInstance();
        SAXParser parser = fact.newSAXParser();
        parser.getXMLReader().setContentHandler(h);
        parser.getXMLReader().parse(is);
    }

    Item createItem(int id, String type, int size, String val) {
        Item item = new MemItem(id, type, size, val);
        items.put(new Integer(id), item);
        return item;
    }

    void addReference(int from, int to) {
        MemItem f = (MemItem)getItem(from);
        MemItem t = (MemItem)getItem(to);
        f.addOutgoing(t);
        t.addIncomming(f);
    }
    
    void addReference(String stat, int to) {
        MemItem t = (MemItem)getItem(to);
        t.addIncomming(stat);
    }
        
        
    private class Handler extends DefaultHandler {
        private int depth = 0;
            
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (depth == 0) {
                if (! "insane".equals(qName)) throw new SAXException("format");
            } else if (depth != 1) {
                throw new SAXException("format");
            } else {
                if ("object".equals(qName)) {
                    String id = atts.getValue("id");
                    String type = atts.getValue("type");
                    String size = atts.getValue("size");
                    String val = atts.getValue("value");
                    createItem(getIdFromString(id), type, Integer.parseInt(size), val);
                } else if ("ref".equals(qName)) {
                    String from = atts.getValue("from");
                    String name = atts.getValue("name");
                    String to = atts.getValue("to");
//                        if (! "java.lang.ref.Reference.referent".equals(name)) {
                        if (from != null) {
                            addReference(getIdFromString(from), getIdFromString(to));
                        } else {
                            addReference(name, getIdFromString(to));
                        }
//                        }
                } else {
                    throw new SAXException("format");
                }
            }
            depth++;
        }
            

        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            depth--;
        }
    }

    int getIdFromString(String s) {
        return Integer.parseInt(s, 16);
    }

    // An enumeration over object array
    private static class RefEnum<T> implements Enumeration<T> {

        int ptr;
        T[] items;

        RefEnum(T[] data) {
            items = data;
        }

        @Override
        public boolean hasMoreElements() {
            return ptr < items.length;
        }

        @Override
        public T nextElement() {
            if (hasMoreElements()) {
                return items[ptr++];
            }
            throw new NoSuchElementException();
        }
    }
}
                                      
