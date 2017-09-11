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
                                      
