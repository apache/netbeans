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
package org.netbeans.modules.subversion.remote.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Handles the credential or property files used by Subversion.
 *
 * 
 *
 */
public class KVFile {

    /** a Map holding the entries*/
    private Map<Key, byte[]> map;
    /** a Map holding the keys*/
    private Map<String, Key> keyMap;
    /** the credential or property file */
    private final VCSFileProxy file;            

    /**
     * Creates a new instance
     * 
     * @parameter file the credential or property file
     */
    public KVFile(VCSFileProxy file) {
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
        return getMap().get(key);
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
            return new String(value, "UTF8"); //NOI18N
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
            map = new TreeMap<>();
        }
        return map;
    }

    public Map<String, byte[]> getNormalizedMap() {
        Map<Key, byte[]> keyValue = getMap();
        Map<String, byte[]> stringValue = new HashMap<>(keyValue.size());
        Iterator<Map.Entry<Key, byte[]>> it = keyValue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Key, byte[]> next = it.next();
            // getKey().toString() == the normalization
            stringValue.put(next.getKey().toString(), next.getValue());
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
            keyMap = new HashMap<>();
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
            is = new BufferedInputStream(file.getInputStream(false));
            int keyIdx = 0;
            while(!checkEOF(is)) {                      
               int keyLength = readEntryLength(is);     // key length
               byte[] keyName = new byte[keyLength];
               is.read(keyName);
               is.read(); // skip '\n'
               int valueLength = readEntryLength(is);   // value length
               byte[] value = new byte[valueLength];
               is.read(value);
               Key key = new Key(keyIdx, new String(keyName, "UTF8")); //NOI18N
               setKey(key);
               getMap().put(key, value);
               is.read(); // skip '\n'
               keyIdx++;
            }
        } catch (EOFException eofe) {
            if(getMap().size() > 0) {
                // there are already some key-value pairs ->
                // something in the file structure seems to be wrong
                throw new EOFException(file.getPath());
            }
            // otherwise skip the exception, could be just an empty file
        } catch (NumberFormatException nfe) {
            throw new IOException(file.getPath(), nfe);
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
        String line = baos.toString("UTF-8"); //NOI18N
        return Integer.decode(line.substring(2));
    }          

    public void store() throws IOException {        
        store(file);
    }
    
    public void store(VCSFileProxy file) throws IOException {
        OutputStream os = null; 
        try {
            VCSFileProxy parent = file.getParentFile();
            if(parent!=null && !parent.exists()) {
                VCSFileProxySupport.mkdirs(parent);
            }
            os = VCSFileProxySupport.getOutputStream(file);
            for (Iterator<Key> it = getMap().keySet().iterator(); it.hasNext();) {
                Key key = it.next();
                byte[] value = getMap().get(key);                
                
                StringBuffer sb = new StringBuffer();
                sb.append("K "); // NOI18N
                sb.append(key.getName().length());
                sb.append("\n"); // NOI18N
                sb.append(key.getName());
                sb.append("\n"); // NOI18N
                sb.append("V "); // NOI18N
                sb.append(value.length);
                sb.append("\n"); // NOI18N
                os.write(sb.toString().getBytes("UTF8")); //NOI18N
                os.write(value);            
                os.write("\n".getBytes("UTF8")); //NOI18N
            }
            os.write("END\n".getBytes("UTF8")); //NOI18N
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
    
    protected VCSFileProxy getFile() {
        return file;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    void setValue(Key key, String value) {
        try {
            setValue(key, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            setValue(key, value.getBytes());
        }
    }

    /**
     * Represents a key
     */
    protected static class Key implements Comparable<Key> {
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
        @Override
        public boolean equals(Object obj) {
            if( !(obj instanceof Key) ) {
                return false;
            }
            Key key = (Key) obj;
            return key.getIndex() == getIndex() && key.getName().equals(getName());
        }      
        @Override
        public int hashCode() {
            StringBuilder sb = new StringBuilder();
            sb.append(getName());            
            sb.append(getIndex());
            return sb.toString().hashCode();
        }
        @Override
        public int compareTo(Key key) {
            if (key.getIndex() < getIndex()) {
                return 1;
            } else if (key.getIndex() > getIndex()) {
                return -1;
            }    
            return 0;
        }
        @Override
        public String toString() {
            return name;
        }
    }    
   
}
