/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        private Object[] refs = new Object[] {null};
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
            return new RefEnum(true, refs);
        }

        public Enumeration<Item> outgoing() {
            return new RefEnum(false, refs);
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
            Object[] nr = new Object[refs.length+1];
            nr[0] = incomming;
            System.arraycopy(refs, 0, nr, 1, refs.length);
            refs = nr;
        }

        void addOutgoing(Object outgoing) {
            Object[] nr = new Object[refs.length+1];
            nr[refs.length] = outgoing;
            System.arraycopy(refs, 0, nr, 0, refs.length);
            refs = nr;
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

    // An enumeration over object array, enumeration either pre-null items
    // or post-null items
    private static class RefEnum implements Enumeration {
        int ptr;
        Object[] items;
        RefEnum(boolean first, Object[] data) {
            items = data;
            if (!first) while (data[ptr++] != null);
        }

        public boolean hasMoreElements() {
            return ptr < items.length && items[ptr] != null;
        }

        public Object nextElement() {
            if (hasMoreElements()) return items[ptr++];
            throw new NoSuchElementException();
        }
    }


}
                                      
