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

package org.netbeans.core.startup.layers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.core.startup.preferences.RelPaths;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Partial implementation of the cache manager using BinaryFS as the layer
 * implementation. Still not fully working because current LayerCacheManager
 * don't support replacing of the layer.
 * Not optimalized yet!
 *
 * @author Petr Nejedly
 */
final class BinaryCacheManager extends ParsingLayerCacheManager {
    private final String cacheLocation;

    BinaryCacheManager() {
        this("all-layers.dat"); // NOI18N
    }

    BinaryCacheManager(String cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

    @Override
    public FileSystem createEmptyFileSystem() throws IOException {
        return FileUtil.createMemoryFileSystem();
    }
    
    @Override
    public FileSystem load(FileSystem previous, ByteBuffer bb) throws IOException {
        try {
            FileSystem fs = new BinaryFS(cacheLocation(), bb);
            return fs;
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String cacheLocation() {
        return cacheLocation;
    }

    @Override
    protected boolean openURLs() {
        return false;
    }

    @Override
    protected void store(FileSystem fs, final MemFolder root, OutputStream os) throws IOException {
        try {
            sizes = new HashMap<MemFileOrFolder,Integer>(1000);
            Map<String,int[]> strings = new HashMap<String, int[]>();
            int fsSize = computeSize(root, strings);
            LayerCacheManager.err.log(Level.FINE, "Writing binary layer cache of length {0} to {1}", new Object[]{fsSize + BinaryFS.MAGIC.length, cacheLocation()});
            os.write(BinaryFS.MAGIC);
            BinaryWriter bw = new BinaryWriter (os, root, fsSize, strings);
            writeFolder(bw, root, true);
        } finally {
            sizes = null; // free the cache
            os.close();
        }
    }
    
    void writeFolder(BinaryWriter bw, MemFolder folder) throws IOException {
        writeFolder(bw, folder, false);
    }
    void writeFolder(BinaryWriter bw, MemFolder folder, boolean emptyURLsAllowed) throws IOException {
        writeBaseURLs(folder, bw, emptyURLsAllowed);
        
        if (folder.attrs != null) {
            bw.writeInt(folder.attrs.size()); // attr count
            for (MemAttr attr : folder.attrs) {
                writeAttribute(bw, attr); // write attrs
            }
        } else {
            bw.writeInt(0); // no attrs
        }
        
        if (folder.children != null) {
            bw.writeInt(folder.children.size()); // file count
            // compute len of all FileRefs
            int baseOffset = bw.getPosition();
            for (MemFileOrFolder item : folder.children) {
                baseOffset += computeHeaderSize(item, null);
            }
            // baseOffset now contains the offset of the first file content

            // write file headers
            for (MemFileOrFolder item : folder.children) {
                bw.writeString(item.name); //    String name
                bw.writeByte((item instanceof MemFile) ? (byte)0 : (byte)1); //boolean isFolder
                bw.writeInt(baseOffset); //  int contentRef

                baseOffset += computeSize(item, null);
                // baseOffset now contains the offset of the next file content
            }

            // write file/folder contents
            for (MemFileOrFolder item : folder.children) {
                // TODO: can check the correctenss of the offsets now
                if (item instanceof MemFile) {
                    writeFile(bw, (MemFile)item);
                } else {
                    writeFolder(bw, (MemFolder)item);
                }
            }
            
        } else {
            bw.writeInt(0); // no files
        }
    }

    private void writeBaseURLs(MemFileOrFolder folder, BinaryWriter bw, boolean emptyURLsAllowed) throws IOException {
        List<URL> urls = folder.getURLs();
        if (urls.size() > 0) {
            int last = urls.size() - 1;
            for (int i = 0; i < last; i++) {
                URL u = urls.get(i);
                bw.writeBaseURL(u);
            }
            bw.writeBaseURL(urls.get(last), true);
        } else {
            assert emptyURLsAllowed;
        }
    }
    
    private void writeFile(BinaryWriter bw, MemFile file) throws IOException {
        writeBaseURLs(file, bw, false);

        if (file.attrs != null) {
            bw.writeInt(file.attrs.size()); // attr count
            for (MemAttr attr : file.attrs) {
                writeAttribute(bw, attr); // write attrs
            }
        } else {
            bw.writeInt(0); // no attrs
        }
        
        //    int contentLength | -1, byte[contentLength] content | String URL
        if (file.ref != null) {
            bw.writeInt(-1); // uri
            bw.writeString(toRelativeURL(file.ref));
        } else if (file.contents != null) {
            bw.writeInt(file.contents.length);
            bw.writeBytes(file.contents);
        } else {
            bw.writeInt(0); // empty file
        }
    }

    private static final String[] ATTR_TYPES = {
        "bytevalue", // NOI18N
        "shortvalue", // NOI18N
        "intvalue", // NOI18N
        "longvalue", // NOI18N
        "floatvalue", // NOI18N
        "doublevalue", // NOI18N
        "boolvalue", // NOI18N
        "charvalue", // NOI18N
        "stringvalue", // NOI18N
        "urlvalue", // NOI18N
        "methodvalue", // NOI18N
        "newvalue", // NOI18N
        "serialvalue", // NOI18N
        "bundlevalue", // NOI18N
    };

    private void writeAttribute(BinaryWriter bw, MemAttr attr) throws IOException {
        bw.writeString(attr.name);
        for (int i = 0; i < ATTR_TYPES.length; i++) {
            if(ATTR_TYPES[i].equals(attr.type)) {
                bw.writeByte((byte)i);
                if (i == 9) {
                    bw.writeString(toRelativeURL(attr.data));
                } else {
                    bw.writeString(attr.data);
                }
                return;
            }
        }
        throw new IOException("Wrong type: " + attr);
    }
    
    // this map is actually valid only during BFS regeneration, null otherwise
    private HashMap<MemFileOrFolder,Integer> sizes;
    
    private int computeSize(MemFileOrFolder mf, Map<String,int[]> text) {
        Integer i = sizes.get(mf);
        if (i != null) return i;

        // base urls
        int size = mf.getURLs().size() * 4;

        size += 4; // int attrCount
        if (mf.attrs != null) {
            for (MemAttr attr : mf.attrs) {
                size += computeSize(attr, text); // Attribute[attrCount] attrs
            }
        }

        if (mf instanceof MemFile) {
             MemFile file = (MemFile)mf;
             size += 4; //    int contentLength
             if (file.ref != null) {
                 size += computeSize(toRelativeURL(file.ref), text); // String uri
             } else if (file.contents != null) {
                 size += file.contents.length;
             } // else size += 0; // no content, no uri
        } else { // mf instanceof MemFolder
            MemFolder folder = (MemFolder)mf;
            size += 4; // int fileCount
            if (folder.children != null) {
                for (MemFileOrFolder item : folder.children) {
                    size += computeHeaderSize(item, text); // File[fileCount] references    
                    size += computeSize(item, text); // File/FolderContent[fileCount] contents
                }
            }
        }
        sizes.put(mf, size);
        return size;
    }
    
    private int computeHeaderSize(MemFileOrFolder mof, Map<String,int[]> text) {
        // String name, boolean isFolder, int contentRef
        return computeSize(mof.name, text) + 1 + 4;
    }

    private static int computeSize(String s, Map<String,int[]> text) { // int len, byte[len] utf8
        if (text != null) {
            int[] count = text.get(s);
            if (count == null) {
                count = new int[1];
                text.put(s, count);
            }
            count[0]++;
        }
        return 4;
    }
    
    private int computeSize(MemAttr attr, Map<String,int[]> text) { //String name, byte type, String value
        return computeSize(attr.name, text) + 1 + computeSize(attr.data, text);
    }

    private static final class BinaryWriter {
        private OutputStream os;
        private int position;
        /** map from base URL to int[1] value */
        private final Map<String,Object> urls;
        private final Map<String,Integer> strings;
        BinaryWriter(OutputStream os, MemFolder root, int fsSize, Map<String,int[]> strings) throws IOException {
            this.os = os;
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            this.strings = Collections.unmodifiableMap(map);
            urls = writeBaseUrls (root, fsSize, strings, map);
            position = 0;
        }

        int getPosition() {
            return position;
        }
        
        void writeByte(byte b) throws IOException {
            os.write(b);
            position ++;
        }

        void writeBytes(byte[] bytes) throws IOException {
            os.write(bytes);
            position += bytes.length;
        }
        
        void writeInt(int num) throws IOException {
            byte[] data = new byte[4];
            data[0] = (byte)num;
            data[1] = (byte)(num >> 8);
            data[2] = (byte)(num >> 16);
            data[3] = (byte)(num >> 24);
            writeBytes(data);
        }
        
        void writeString(String str) throws IOException {
            Integer offset = strings.get(str);
            assert offset != null : "Found " + str;
            writeInt(offset);
        }
        
        void writeBaseURL (java.net.URL url) throws IOException {
            writeBaseURL(url, false);
        }
        void writeBaseURL (java.net.URL url, boolean negative) throws IOException {
            String relUrl = toRelativeURL(url);
            int[] number = (int[])urls.get (relUrl);
            assert number != null : "Should not be null, because it was collected: " + url + " map: " + urls;
            int index = number[0];
            if (negative) {
                index = -10 - index;
            }
            writeInt (index);
        }
        
        private Map<String,Object> writeBaseUrls(
            MemFileOrFolder root, int fsSize, Map<String,int[]> texts, Map<String,Integer> fillIn
        ) throws IOException {
            java.util.LinkedHashMap<String,Object> map = new java.util.LinkedHashMap<String,Object> ();
            int[] counter = new int[1];
            
            collectBaseUrls (root, map, counter);
            
            int size = 0;
            Iterator<Entry<String, Object>> it = map.entrySet ().iterator ();
            for (int i = 0; i < counter[0]; i++) {
                Entry<String, Object> entry = it.next ();
                String u = entry.getKey ();
                
                assert ((int[])entry.getValue ())[0] == i : i + "th key should be it " + ((int[])entry.getValue ())[0];
                
                size += computeSize (u, texts);
            }
            
            ByteArrayOutputStream arr = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(arr);
            for (String txt : sort(texts.entrySet())) {
                fillIn.put(txt, dos.size());
                dos.writeUTF(txt);
            }
            dos.flush();
            
            int textSize = dos.size();
            writeInt(BinaryFS.MAGIC.length + 4 + 4 + textSize + 4 + size + fsSize); // size of the whole image
            
            writeInt(textSize);
            os.write(arr.toByteArray());
            
            writeInt (size); // the size of urls part
            
            it = map.entrySet ().iterator ();
            for (int i = 0; i < counter[0]; i++) {
                Entry<String, Object> entry = it.next ();
                writeString (entry.getKey());
            }
            return map;
        }
        
        private void collectBaseUrls (MemFileOrFolder f, Map<String,Object/*int[]*/> map, int[] counter) {
            for (URL u : f.getURLs()) {
                String tmp = toRelativeURL(u);
                int[] exists = (int[])map.get(tmp);
                if (exists == null) {
                    map.put (tmp, counter.clone ());
                    counter[0]++;
                }
            }
            if (f instanceof MemFolder && ((MemFolder)f).children != null) {
                for (MemFileOrFolder item : ((MemFolder)f).children) {
                    collectBaseUrls (item, map, counter);
                }
            }
        }

    private List<String> sort(Set<Entry<String, int[]>> entrySet) {
            List<Entry<String, int[]>> lst = new ArrayList<Entry<String, int[]>>(entrySet);
            class C implements Comparator<Entry<String, int[]>> {
                @Override
                public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
                    return o2.getValue()[0] - o1.getValue()[0];
                }
            }
            lst.sort(new C());
            List<String> res = new ArrayList<String>();
            for (Entry<String, int[]> entry : lst) {
                res.add(entry.getKey());
            }
            return res;
            
        }
    }

    private static String toRelativeURL(URL u) {
        return toRelativeURL(u.toExternalForm());
    }
    private static String toRelativeURL(String tmp) {
        if (tmp.startsWith("jar:file:")) {
            final String[] relPath = RelPaths.findRelativePath(tmp.substring(9));
            if (relPath != null) {
                tmp = relPath[0] + '@' + relPath[1];
            }
        }
        return tmp;
    }
    
}
