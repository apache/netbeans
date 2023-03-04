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

package org.netbeans.insane.model;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

// TODO: provide lazy iterators

/**
 * A HeapModel based on the Insane binary heap dump.
 *
 * @author  Nenik
 */
class BinaryHeapModel implements HeapModel {

    public static HeapModel open(File file) throws Exception {
        HeapModel model = new BinaryHeapModel(file);
        return model;
    }

    private Map<Integer, Item> createdObjects = new HashMap<Integer, Item>();
    private Map<Integer, Cls> createdClasses = new HashMap<Integer, Cls>();
        
    ByteBuffer buffer;
    int refsOffset;
    int objsOffset;
        
    BinaryHeapModel(File data) throws Exception {
        // mmap it
        long len = data.length();
        buffer = new FileInputStream(data).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, len);
        System.err.println("magic=" + buffer.getInt(0));

        // prepare pointers
        refsOffset = buffer.getInt(4);
        objsOffset = buffer.getInt(8);
        System.err.println("refs=" + refsOffset);
        System.err.println("objs=" + objsOffset);

        // prescan classes?
    }
    
    public Iterator<Item> getAllItems() {
        ArrayList<Item> all = new ArrayList<Item>();

        int actOffset = objsOffset;
        while (actOffset < buffer.limit()) {
            HItem act = (HItem)getItem(actOffset);
            all.add(act);
            actOffset = act.getNextOffset();                
        }
        return all.iterator();
    }

    public Collection<Item> getObjectsOfType(String type) {
        Cls cls = getClsByName(type);
        return cls == null ? Collections.<Item>emptyList() : cls.getInstances();
    }

    public Collection<String> getRoots() {
        ArrayList<String> all = new ArrayList<String>();

        int actOffset = refsOffset;
        while (actOffset < objsOffset) {
            RefType act = new RefType(actOffset);
            if (act.isStatic()) all.add(act.getReferenceName());
            actOffset = act.getNextOffset();                
        }
        return all;
    }
        
    public Item getObjectAt(String staticRefName) {
        RefType type = getRefTypeByName(staticRefName);
        return type.getInstance();
    }
        
    public Item getItem(int id) {
        Integer key = Integer.valueOf(id);
        Item ret = createdObjects.get(key);
        if (ret == null) {
            ret = new HItem(id);
            createdObjects.put(key, ret);
        }
        return ret;
    }
        
    private Cls getClsByName(String name) {
        int actOffset = 12;
        while (actOffset < refsOffset) {
            Cls act = getCls(actOffset);
            if (name.equals(act.getClassName())) return act;
            actOffset = act.getNextOffset();
        }
        return null;
    }

    private Cls getCls(int offset) {
        Integer key = Integer.valueOf(offset);
        Cls ret = createdClasses.get(key);
        if (ret == null) {
            ret = new Cls(offset);
            createdClasses.put(key, ret);
        }
        return ret;
    }

    private class Cls {
        int offset;
        private Cls(int offset) {
            this.offset = offset;
        }

        public String getClassName() {
            ByteBuffer local = (ByteBuffer)buffer.duplicate().position(offset);
            int len = local.getInt();
            byte[] data = new byte[len];
            local.get(data);
            return new String(data);
        }

        public Collection<Item> getInstances() {
            ByteBuffer local = (ByteBuffer)buffer.duplicate().position(offset);
            local.position(local.getInt() + local.position());
            int count = local.getInt();

            ArrayList<Item> list = new ArrayList<Item>(count);
            while(--count >= 0) list.add(getItem(local.getInt()));
            return list;
        }

        private int getNextOffset() {
            ByteBuffer local = ((ByteBuffer)buffer.duplicate().position(offset)).slice();
            int strLen = local.getInt();

            return offset + 4 + strLen + 4 + 4*local.getInt(4+strLen);
        }
    }

    private RefType getRefTypeByName(String name) {
        int actOffset = refsOffset;
        while (actOffset < objsOffset) {
            RefType act = new RefType(actOffset);
            if (name.equals(act.getReferenceName())) return act;
            actOffset = act.getNextOffset();
        }
        return null;
    }


    private class RefType {
        int offset;
        private RefType(int offset) {
            this.offset = offset;
        }

        // REF_TYPE:
        //   STR referenceName
        //   INT staticOffset (0 for null static ref, -1 for nonstatic ref)
        public String getReferenceName() {
            ByteBuffer local = (ByteBuffer)buffer.duplicate().position(offset);
            int len = local.getInt();
            byte[] data = new byte[len];
            local.get(data);
            return new String(data);
        }
        
        public boolean isStatic() {
            ByteBuffer local = (ByteBuffer)buffer.duplicate().position(offset);
            local.position(local.getInt() + local.position());
            int instOffset = local.getInt();
            return (instOffset != -1);
        }

        public Item getInstance() {
            ByteBuffer local = (ByteBuffer)buffer.duplicate().position(offset);
            local.position(local.getInt() + local.position());
            int instOffset = local.getInt();
            if (instOffset > 0) return getItem(instOffset);
            return null;
        }

        private int getNextOffset() {
            ByteBuffer local = ((ByteBuffer)buffer.duplicate().position(offset));
            int strLen = local.getInt();
            return offset + 4 + strLen + 4;
        }
    }


    public class HItem implements Item {
        int offset;

        private HItem(int offset) {
            this.offset = offset;
        }

        private ByteBuffer prepareBuffer() {
            return ((ByteBuffer)buffer.duplicate().position(offset)).slice();
        }

        public String getType() {
            return getCls(prepareBuffer().getInt()).getClassName();
        }

        public int getSize() {
            return prepareBuffer().getInt(4);
        }

        public String getValue() {
            return "unknown"; // TODO: Add [C content to the file
        }

        public Enumeration<Object> incomming() {
            ByteBuffer buff = prepareBuffer();
            buff.position(8);
            int out = buff.getInt();
            int inc = buff.getInt();

            buff.position(16 + 8*out);
            Vector<Object> v = new Vector<Object>(inc);

            while (--inc >= 0) {
                int refOffset = buff.getInt();
                int objOffset = buff.getInt();

                if (objOffset != 0) { // normal ref
                    v.add(getItem(objOffset));
                } else { // static ref
                    v.add(new RefType(refOffset).getReferenceName());
                }
            }

            return v.elements(); // XXX - eager
        }

        public Enumeration<Item> outgoing() {
            ByteBuffer buff = prepareBuffer();
            int out = buff.getInt(8);
            buff.position(16);

            Vector<Item> v = new Vector<Item>(out);

            while (--out>= 0) {
                int refOffset = buff.getInt();
                int objOffset = buff.getInt();

                v.add(getItem(objOffset));
            }

            return v.elements(); // XXX - eager 
        }


        public String toString() {
            return getType() + "@" + Integer.toHexString(getId());
        }

        public int getId() {
            return offset; // XXX - different ID
        }

        private int getNextOffset() {
            ByteBuffer buff = prepareBuffer();

            return offset + 16 + 8*buff.getInt(8) + 8*buff.getInt(12);
        }
    }    
}
                                      
