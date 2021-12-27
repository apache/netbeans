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
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import javax.imageio.stream.FileImageInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses a SimpleXMLVisitor XML output and converts it into simple binary
 * format suitable for mmap usage.
 * This version does a two-pass over the XML dump to spare memory during
 * conversion. Estimated memory consumption during the scan is about objects*30B
 *
 * The format (version 'SBIH'): <pre>
        INT version
        INT refsOffset
        INT objsOffset
        CLASS_TYPEs
        REF_TYPEs (static refs come first)
        OBJECTs
 
 
 
        OBJECT:
            INT classTypeOffset
            INT heapSize
            INT numOutgoing
            INT numIncomming
            REFs outgoing
            REFs incomming
 
        REF:
            INT refTypeOffset (or negative iff array index)
            INT objectOffset
 
        REF_TYPE:
            STR referenceName
            INT staticOffset (0 for null static ref, -1 for nonstatic ref)
        
        STR:
            INT len
            UTF8 text
 
        CLASS_TYPE:
            STR  className
            INT  numInstances
            INTs objectOffsets
  </pre>
 
 *
 * @author  Nenik
 */
// XXX use UTF8
final class InsaneConverter {
    
    private File from;
    private File to;
    
    // parsing intermediate data
    private Map<String, ClassInfo> classInfo = new LinkedHashMap<String, ClassInfo>();
    private Map<String, RefInfo> refInfo = new LinkedHashMap<String, RefInfo>();
//    private Map instanceInfo = new HashMap(); //new LinkedHashMap(); // Map<String id, InstanceInfo>
  
    private ObjectSet instanceInfo = new ObjectSet();
    
    private int refsOffset;
    private int objsOffset;
    private int totalOffset;
    
    boolean prescan = true;
    
    MappedByteBuffer store;

    private InsaneConverter(File from, File to) {
        this.from = from;
        this.to = to;
    }
    
    ByteBuffer getByteBuffer(int offset) {
        if (offset < 12) throw new IllegalArgumentException("bad offset " + offset );
        return ((ByteBuffer)store.duplicate().position(offset)).slice();
    }
    
    // Holder for info about each object or array type
    private class ClassInfo {
        String className;
        int offset;
        int countOrOffset;
        
        ClassInfo(String className) {
            this.className = className;
        }
        
        void register(InstanceInfo inst) { // {throws IOException {
            if (prescan) {
                countOrOffset++;
            } else {
                ByteBuffer buff = getByteBuffer(countOrOffset);
                inst.storeOffset(buff);
                countOrOffset += 4;
            }
        }
        
        int computeNextOffset(int currentOffset) {
            offset = currentOffset;
            return currentOffset + computeStringSize(className) + 4 + 4*countOrOffset;
        }

        void storeHeader() {
            ByteBuffer buff = getByteBuffer(offset);
            storeString(buff, className);
            buff.putInt(countOrOffset);
            countOrOffset = buff.position() + offset;
        }
        
        void storeOffset(ByteBuffer buff) { // throws IOException {
            buff.putInt(offset);
        }
    }
    
    private ClassInfo getClassInfo(String className) {
        ClassInfo ret = classInfo.get(className);
        if (ret == null) {
            ret = new ClassInfo(className);
            classInfo.put(className, ret);
        }
        return ret;
    }
    
    private static Object[] EMPTY = new Object[0];
    
    private static class InstanceInfo {
        private int id1;
        private int offset;
        private int incommingCountOrPtr;
        private int outgoingCountOrPtr;
        
        InstanceInfo(String str) {
                id1 = Integer.parseInt(str, 16);
        }
        
         void process(InsaneConverter converter, ClassInfo type, int size) { // store the header
            if (converter.prescan) {
                // do nothing here
            } else {
                // store header
                ByteBuffer buff = converter.getByteBuffer(offset);
                type.storeOffset(buff);
                buff.putInt(size);
                buff.putInt(outgoingCountOrPtr);
                buff.putInt(incommingCountOrPtr);

                // recompute offsets
                int outCount = outgoingCountOrPtr;
                outgoingCountOrPtr = offset + buff.position();
                incommingCountOrPtr = outgoingCountOrPtr + 8*outCount;                
            }
        }
        
        void registerIncommingReference(InsaneConverter converter, RefInfo ref, InstanceInfo inst) {
            incommingCountOrPtr = registerReference(converter, incommingCountOrPtr, ref, inst);
        }
        
        void registerOutgoingReference(InsaneConverter converter, RefInfo ref, InstanceInfo inst) {
            outgoingCountOrPtr = registerReference(converter, outgoingCountOrPtr, ref, inst);
        }
        
        private int registerReference(InsaneConverter converter, int ptr, RefInfo ref, InstanceInfo inst) {
            if (converter.prescan) {
                return ptr+1;
            } else {
                // XXX
                ByteBuffer buff = converter.getByteBuffer(ptr);
                ref.storeOffset(buff);
                if (inst == null) {
                    buff.putInt(0);
                } else {
                    inst.storeOffset(buff);
                }
                return ptr+8;
            }
        }

        int computeNextOffset(int currentOffset) {
            offset = currentOffset;
            return currentOffset + 4 + 4 + 4 + 4 + 8*outgoingCountOrPtr + 8*incommingCountOrPtr;
        }
        
        void storeOffset(ByteBuffer buff) {//  throws IOException {
            buff.putInt(offset);
        }
        
        public boolean equals(Object o) {
            if (o instanceof InstanceInfo) {
                return id1 == ((InstanceInfo)o).id1;
            }
            return false;
        }
        
        public int hashCode() {
            return 61315*id1;
        }

    }

    void createInstanceInfo(String strId, String type, int size, String val) {
        InstanceInfo template = new InstanceInfo(strId);
        
        InstanceInfo ii = (InstanceInfo)instanceInfo.get(template);
        if (ii == null) {
            if (!prescan) throw new IllegalArgumentException("Unknown element during second pass:" + strId);
            ii = template;
            instanceInfo.put(ii);
        }
        ClassInfo cls = getClassInfo(type);
        ii.process(this, cls, size);
        cls.register(ii);
    }
    
    InstanceInfo getInstance(String strId) {
        InstanceInfo template = new InstanceInfo(strId);
        return (InstanceInfo)instanceInfo.get(template);
    }
        
    private class RefInfo {
        int offset;
        String refName;
        InstanceInfo instance;
        
        RefInfo(String name, InstanceInfo inst) {
            refName = name;
            instance = inst;
        }

//        REF_TYPE:
//            STR referenceName
//            INT staticOffset (0 for null static ref, -1 for nonstatic ref)

        int computeNextOffset(int currentOffset) {
            offset = currentOffset;
            return currentOffset + computeStringSize(refName) + 4;
        }
        
        void storeHeader()  throws IOException {
            ByteBuffer buff = getByteBuffer(offset);
            storeString(buff, refName);
            if (instance == null) {
                buff.putInt(-1);
            } else {
                instance.storeOffset(buff);
            }
        }

        void storeOffset(ByteBuffer buff)  { // throws IOException {
            buff.putInt(offset);
        }
    }
    
    void registerReference(String type, String fromId, String toId) {
        InstanceInfo from = fromId == null ? null : getInstance(fromId);
        InstanceInfo to = getInstance(toId);
        
        RefInfo ref = refInfo.get(type);
        if (ref == null) {
            if (from == null) { // static
                ref = new RefInfo(type, to);
            } else {
                ref = new RefInfo(type, null);
            }
            refInfo.put(type, ref);
        }
        
        if (from != null) from.registerOutgoingReference(this, ref, to);
        to.registerIncommingReference(this, ref, from);
    }

   
    
    
    private void process()  throws Exception {
        FileInputStream fis = new FileInputStream(from);
        // parse
        try {
            parse(fis);
        } finally {
            fis.close();
        }
        // compute offsets
        compute();
        // store headers
        
        RandomAccessFile raf = new RandomAccessFile(to, "rw"); 
        store = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, totalOffset);
        raf.close();
        storeHeaders();
        
        // second pass
        prescan = false;
        fis = new FileInputStream(from);
        try {
            parse(fis);
        } finally {
            fis.close();
        }
        store.force();
    }

    private void compute() {
        int currentOffset = 4+4+4;

        // compute offset of classes
        for (ClassInfo info : classInfo.values()) {
            currentOffset = info.computeNextOffset(currentOffset);
        }
        
        refsOffset = currentOffset;
        
        // compute offsets of refs
        for (RefInfo info : refInfo.values()) {
            currentOffset = info.computeNextOffset(currentOffset);
        }

        objsOffset = currentOffset;
        
        // compute offsets of instances
        for (Iterator<InstanceInfo> it = instanceInfo.iterator(); it.hasNext(); ) {
            InstanceInfo info = it.next();
            currentOffset = info.computeNextOffset(currentOffset);
        }
        totalOffset = currentOffset;
    }
    
    private void storeHeaders()  throws IOException {
        // store header
        store.put("SBIH".getBytes());
        store.putInt(refsOffset);
        store.putInt(objsOffset);
        
        // store classes
        for (ClassInfo info : classInfo.values()) {
            info.storeHeader();
        }
        
        // store refs
        for (RefInfo info : refInfo.values()) {
            info.storeHeader();
        }
        
/*        // store instances
        for (Iterator it = instanceInfo.values().iterator(); it.hasNext(); ) {
            InstanceInfo info = (InstanceInfo)it.next();
            info.store(out);
        }
  */      
    }
    
    private static void storeString(ByteBuffer buff, String str)  { //throws IOException {
        byte[] data = str.getBytes();
        buff.putInt(data.length);
        buff.put(data);
    }
    
    private static int computeStringSize(String str) {
        return 4 + str.getBytes().length;
    }
    
    private void parse(InputStream source) throws Exception {
        class Handler extends DefaultHandler {
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
                        createInstanceInfo(id, type, Integer.parseInt(size), val);
                    } else if ("ref".equals(qName)) {
                        String from = atts.getValue("from");
                        String name = atts.getValue("name");
                        String to = atts.getValue("to");
                        registerReference(name, from, to);
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
        
        Handler h = new Handler();
        SAXParserFactory fact = SAXParserFactory.newInstance();
        SAXParser parser = fact.newSAXParser();
        parser.getXMLReader().setContentHandler(h);
        parser.getXMLReader().parse(new InputSource(source));
    }        

    public static void convert(File from, File to) throws Exception {
        InsaneConverter conv = new InsaneConverter(from, to);
        conv.process();
    }
    
}
                                      
