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

package org.netbeans;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A shared startup-needed resource archive.
 * File format of the archive:
 *  [Header]
 *  ([Source entry]|[File entry])*
 * 
 * Header:
 *   8B [Magic]
 *   8B [timestamp]
 * 
 * Source entry (describes a data source for following entries):
 *   1B 0x01 type identifier
 *   xB id   utf8 String identifier of the source (file name)
 *
 * File entry (keeps content of a file with name and source ref):
 *   1B 0x02 type identifier (0x03 for general)
 *   2B src  number of the source (sources are counted in file from 0)
 *   4B len  length of the data (or -1 for no such file for source)
 *   xB name utf8 String name of the file
 * lenB data file content
 * 
 * Utf8 string
 *   2B len  length of the following String in bytes
 * lenB data utf8 encoded string
 *
 * @author nenik
 */
class Archive implements Stamps.Updater {
    // increment on format change
    private static final long magic = 6836742066851800321l;
    private static final Logger LOG = Logger.getLogger(Archive.class.getName());
    
    private volatile boolean saved;
    private final boolean prepopulated;
    
    private volatile boolean gathering;
    private final Object gatheringLock = new Object();
    // these two collections are guarded either by the gatheringLock
    // or by the "gathering" volatile flag transitions
    private Map<String,Boolean> requests = new LinkedHashMap<String, Boolean>();
    private Map<String,ArchiveResources> knownSources = new HashMap<String,ArchiveResources>();

    private volatile boolean active;
    // These two collections are guarded by the "active" volatile flag transition.
    // They are modified from a single thread only, when "active" flag is false
    private Map<String,Integer> sources = new HashMap<String,Integer>();
    private Map<Entry, Entry> entries = new HashMap<Entry,Entry>();
    
    public Archive() {
        gathering = false;
        active = false;
        prepopulated = false;
    }

    Archive(boolean prep) {
        gathering = false;
        active = false;
        prepopulated = prep;
    }
    
    /** Creates a new instance of Archive that reads data from given cache
     */
    Archive(Stamps cache) {
        ByteBuffer master = cache.asByteBuffer("all-resources.dat");
        if (master != null) {
            try {
                parse(master, cache.lastModified());
            } catch (Exception e) {
                sources.clear();
                entries.clear();
            }
        } else {
            sources.clear();
            entries.clear();
        }
        prepopulated = entries.size() > 0;
 
        active = true;
        gathering = true;
    }

    final boolean isActive() {
        return active;
    }

    /**
     * Sweep through the master buffer and remember all the entries
     */
    private void parse(ByteBuffer master, long after) throws Exception {
        if (master.remaining() < 16) throw new IllegalStateException("Cache invalid");
        if (master.getLong() != magic) throw new IllegalStateException("Wrong format");
        if (master.getLong() < after) throw new IllegalStateException("Cache outdated");
        
        int srcCounter = 0;

        while (master.remaining() > 0) {
            int type = master.get();
            switch (type) {
                case 1: // source header
                    String name = parseString(master);
                    sources.put(name, srcCounter++);
                    break;
                case 2:
                    Entry en = new Entry(master); // shifts the buffer
                    entries.put(en, en);
                    break;
                default:
                    throw new IllegalStateException("Cache invalid");
            }
        }
        master.rewind();
    }
    
    private static String parseString(ByteBuffer src) {
        int len = src.getChar();
        byte data[] = new byte[len];
        src.get(data);
        try {
            return new String(data, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            throw new InternalError(); // UTF8 must be supported
        }
    }
    
    private static void writeString(DataOutputStream dos, String str) throws UnsupportedEncodingException, IOException {
        byte[] data = str.getBytes("UTF8");
        dos.writeChar(data.length);
        dos.write(data);
    }
    
    @SuppressWarnings("element-type-mismatch")
    public byte[] getData(ArchiveResources source, String name) throws IOException {
        Entry e = null;
        String srcId = source.getIdentifier();
        Map<Entry, Entry> ents = entries;
        if (active) {
            Integer src = sources.get(srcId);
            if (src == null) {
                e = null;
            } else {
                e = ents.get(new Template(src, name)); // or null
            }
            if (e == null && gathering) {
                StringBuilder sb = new StringBuilder(srcId.length() + name.length());
                String key = sb.append(srcId).append(name).toString();

                synchronized(gatheringLock) {
                    if (gathering) {
                        if (!knownSources.containsKey(srcId)) knownSources.put(srcId, source);
                        if (!requests.containsKey(key)) requests.put(key, Boolean.TRUE);
                    }
                }
            }
        }
        if (e == null) {
            byte[] data = source.resource(name);
            // maybe store it now? No.
            return data;
        }

        return e.getContent();
    }
    
    public void stopGathering() {
        synchronized (gatheringLock) {
            gathering = false;
        }
    }
    
    public void stopServing() {
        active = false;
        // thread-safe, the only place using the field after
        // construction is guarded by above-cleared volatile flag
        // and this free happens-after clearing the flag
        entries = null;
    }
    
    public void save(Stamps cache) throws IOException {
        if (saved) {
            return;
        }
        saved = true;
        cache.scheduleSave(this, "all-resources.dat", prepopulated);
    }

    @Override
    public void flushCaches(DataOutputStream dos) throws IOException {
        stopGathering();
        stopServing();
        
        assert !gathering;
        assert !active;
       
        if (!prepopulated) { // write header
            dos.writeLong(magic);
            dos.writeLong(System.currentTimeMillis());
        }
        
        // no need to really synchronize on this collection, gathering flag
        // is already cleared
        for (String s:requests.keySet()) {
            String[] parts = s.split("(?<=!/)", 2);
            String name = parts.length == 2 ? parts[1] : "";
            ArchiveResources src = knownSources.get(parts[0]);
            assert src != null : "Could not find " + s + " in " + knownSources;
            byte[] data = src.resource(name);
            Integer srcId = sources.get(parts[0]);
            if (srcId == null) {
                srcId = sources.size();
                sources.put(parts[0], srcId);
                dos.write(1);
                writeString(dos, parts[0]);
            }
            
            dos.write(2);
            dos.writeChar(srcId);
            dos.writeInt(data == null ? -1 : data.length); // store a marker to avoid openning
            writeString(dos, name);
            if (data != null) {
                dos.write(data);
            }
        }
        dos.close();

        if (LOG.isLoggable(Level.FINER)) {
            for (Object r : requests.keySet()) {
                LOG.log(Level.FINER, "archiving: {0}", r);
            }
        }

        // clean up
        requests = null;
        knownSources = null;
        sources = null;
    }

    @Override
    public void cacheReady() {
        // nothing needs to be done
    }

    final boolean isPopulated() {
        return prepopulated;
    }

    /* Entry layout in the buffer:
     * -1    1B 0x02 type identifier (0x03 for general)
     *  0 -> 2B src  number of the source (sources are counted in file from 0)
     *  2    4B len  length of the data
     *  6    2B x    Length of name (in bytes)
     *  8    xB name utf8 String name of the file
     *x+8    yB data file content
     */
    
    private static class Entry {
        private final int offset;
        private final ByteBuffer master;
        
        Entry(ByteBuffer m) {
            master = m;
            offset = master.position();
            int fLen = master.getInt(offset+2);
            int nLen = master.getChar(offset+6);
            if (fLen < 0) fLen = 0;
            master.position(offset+8+nLen+fLen);
        }

        String getName() {
            ByteBuffer my = master.duplicate();
            my.position(offset+6);
            return parseString(my);
        }
        
        int getSource() {
            return master.getChar(offset);
        }
        
        byte[] getContent() {
            int fLen = master.getInt(offset+2);
            int nLen = master.getChar(offset+6);
            if (fLen < 0) return null;

            ByteBuffer clone = master.duplicate();
            clone.position(offset+8+nLen);
            byte[] content = new byte[fLen];
            clone.get(content);
            return content;
        }
        
        public @Override int hashCode() {
            ByteBuffer clone = master.duplicate();
            clone.position(offset+8);
            clone.limit(offset+8+master.getChar(offset+6));

            int code = 53*master.getChar(offset);
            while (clone.hasRemaining()) code = code*53 + clone.get();
            return code;
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof Template) return obj.equals(this);
            return obj == this;
        }
        
        public @Override String toString() {
            return "#" + getSource() + ":" + getName() + "=[" + offset + "]";
        }
    }
    
    // template
    private static class Template {
        private int source;
        private byte[] utf;
        
        Template(int src, String name) { 
            try {
                this.source = src;
                utf = name.getBytes("UTF8");
            } catch (UnsupportedEncodingException ex) {
                throw new InternalError();
            }
        }
        
        public @Override boolean equals(Object o) {
            if (! (o instanceof Entry)) return false;
            Entry e = (Entry)o;
            
            if (source != e.master.getChar(e.offset)) return false;
            if (utf.length != e.master.getChar(e.offset+6)) return false;

            ByteBuffer clone = e.master.duplicate();
            clone.position(((Entry)o).offset+8);
            
            
            for (byte b : utf) if (b != clone.get()) return false;

            return true;
        }
        
        public @Override int hashCode() {
            int code = 53*source;
            for (byte b : utf) code = code*53 + b;
            return code;
        }
    }
}
