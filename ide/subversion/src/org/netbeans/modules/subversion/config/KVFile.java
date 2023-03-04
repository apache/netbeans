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
package org.netbeans.modules.subversion.config;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.versioning.util.FileUtils;

/**
 * Handles the credential or property files used by Subversion.
 *
 * @author Tomas Stupka
 *
 */
public class KVFile {

    /** a Map holding the entries*/
    private Map<Key, byte[]> map;
    /** a Map holding the keys*/
    private Map<String, Key> keyMap;
    /** the credential or property file */
    private final File file;            

    /**
     * Creates a new instance
     * 
     * @parameter file the credential or property file
     */
    public KVFile(File file) {
        this.file = file;
        try {
            if(file.exists()) {
                parse();
            }
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        }
    }
        
    /**
     * Returns the value for the given Key
     *
     * @param key 
     * @return the value stored under the given Key
     */
    protected byte[] getValue(Key key) {
        return (byte[]) getMap().get(key);
    }

    /**
     * Returns the value for the given Key as a String
     *
     * @param key 
     * @return the value stored under the given Key as a String
     */
    protected String getStringValue(Key key) {
        try {
            byte[] value = getValue(key);

            if (value == null) {
                return null;
            }
            return new String(value, "UTF8");
        } catch (UnsupportedEncodingException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);            
            return null;
        }              
    }
    
    /**
     * Stores the given value under the given Key
     *
     */
    protected void setValue(Key key, byte[] value) {
        getMap().put(key, value);
    }
 
    /**
     * Returns the Map holding the Key and value pairs
     *
     * @return map
     */
    private Map<Key, byte[]> getMap() {
        if(map == null) {
            map = new TreeMap<Key, byte[]>();
        }
        return map;
    }

    public Map<String, byte[]> getNormalizedMap() {
        Map<Key, byte[]> keyValue = getMap();
        Map<String, byte[]> stringValue = new HashMap<String, byte[]>(keyValue.size());
        Iterator<Map.Entry<Key, byte[]>> it = keyValue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry next = it.next();
            // getKey().toString() == the normalization
            stringValue.put(next.getKey().toString(), (byte[]) next.getValue());
        }
        return stringValue;
    }
    
    /**
     * Returns the Map holding the Keys
     *
     * @return map
     */
    private Map<String, Key> getKeyMap() {
        if(keyMap == null) {
            keyMap = new HashMap<String, Key>();
        }
        return keyMap;
    }

    protected Key getKey(Key key) {
        Key storedKey = getKey(key.getName());
        if(storedKey == null) {
            setKey(key);
            return key;
        }
        return storedKey;
    }
    
    private Key getKey(String name) {
        return getKeyMap().get(name);
    }
    
    protected void setKey(Key key) {
        getKeyMap().put(key.getName(), key);
    }

    /**
     * Parses the instances file.
     *
     */
    private void parse() throws IOException {        
        InputStream is = null;        
        try {            
            is = FileUtils.createInputStream(file);                                    
            int keyIdx = 0;
            while(!checkEOF(is)) {                      
               int keyLength = readEntryLength(is);     // key length
               byte[] keyName = new byte[keyLength];
               is.read(keyName);
               is.read(); // skip '\n'
               int valueLength = readEntryLength(is);   // value length
               byte[] value = new byte[valueLength];
               is.read(value);
               Key key = new Key(keyIdx, new String(keyName, "UTF8"));
               setKey(key);
               getMap().put(key, value);
               is.read(); // skip '\n'
               keyIdx++;
            }
        } catch (EOFException eofe) {
            if(getMap().size() > 0) {
                // there are already some key-value pairs ->
                // something in the file structure seems to be wrong
                throw new EOFException(file.getAbsolutePath());
            }
            // otherwise skip the exception, could be just an empty file
        } catch (NumberFormatException nfe) {
            throw new IOException(file.getAbsolutePath(), nfe);
        } finally {
            try {                 
                if (is != null) {        
                    is.close();
                }
            } catch (IOException e) {
                Subversion.LOG.log(Level.INFO, null, e); // should not happen
            }                              
        }
    }  
    
    private boolean checkEOF(InputStream is) throws IOException {
        is.mark(3);
        byte[] end = new byte[3];
        is.read(end);
        is.reset();
        if(end[0] == -1 || end[1] == -1 || end[2] == -1) {
            throw new EOFException();
        }
        return end[0] == 'E' && end[1] == 'N' && end[2] == 'D';
    }
    
    private int readEntryLength(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte b = (byte) is.read();
        while ( b != '\n')  {
            if(b == -1) {
                throw new EOFException();
            }
            baos.write(b);
            b = (byte) is.read();
        }
        String line = baos.toString();
        return Integer.decode(line.substring(2)).intValue();
    }          

    public void store() throws IOException {        
        store(file);
    }
    
    public void store(File file) throws IOException {
        OutputStream os = null; 
        try {
            File parent = file.getParentFile();
            if(parent!=null && !parent.exists()) {
                parent.mkdirs();
            }
            os = FileUtils.createOutputStream(file);            
            for (Iterator it = getMap().keySet().iterator(); it.hasNext();) {
                Key key = (Key) it.next();
                byte[] value = (byte[]) getMap().get(key);                
                
                StringBuffer sb = new StringBuffer();
                sb.append("K "); // NOI18N
                sb.append(key.getName().length());
                sb.append("\n"); // NOI18N
                sb.append(key.getName());
                sb.append("\n"); // NOI18N
                sb.append("V "); // NOI18N
                sb.append(value.length);
                sb.append("\n"); // NOI18N
                os.write(sb.toString().getBytes("UTF8"));    
                os.write(value);            
                os.write("\n".getBytes()); // NOI18N
            }
            os.write("END\n".getBytes()); // NOI18N
            os.flush();
            
        } finally {
            if(os != null) {
                try {
                    os.close();    
                } catch (IOException ex) {
                    Subversion.LOG.log(Level.INFO, null, ex);
                }                
            }            
        }        
    }    
    
    protected File getFile() {
        return file;
    }

    void setValue(Key key, String value) {
        setValue(key, value.getBytes());
    }

    /**
     * Represents a key
     */
    protected static class Key implements Comparable {
        /** the key index */
        private final int idx;
        /** the keys name */
        private final String name;        
        /** creates a new instance */
        protected Key(int idx, String name) {
            this.name = name;
            this.idx = idx;
        }
        public int getIndex() {
            return idx;
        }       
        public String getName() {
            return name;
        }                
        public boolean equals(Object obj) {
            if( !(obj instanceof Key) ) {
                return false;
            }
            Key key = (Key) obj;
            return key.getIndex() == getIndex() && key.getName().equals(getName());
        }      
        public int hashCode() {
            StringBuffer sb = new StringBuffer();
            sb.append(getName());            
            sb.append(getIndex());
            return sb.toString().hashCode();
        }
        public int compareTo(Object obj) {
            if( !(obj instanceof Key) ) {
                return 0;
            }
            Key key = (Key) obj;
            if (key.getIndex() < getIndex()) {
                return 1;
            } else if (key.getIndex() > getIndex()) {
                return -1;
            }    
            return 0;
        }
        public String toString() {
            return name;
        }
    }    
   
}
