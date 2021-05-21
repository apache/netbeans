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

package org.openide.filesystems;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Simple implementation of memory file system.
 * @author Jaroslav Tulach
 */
final class MemoryFileSystem extends AbstractFileSystem implements AbstractFileSystem.Info, AbstractFileSystem.Change, AbstractFileSystem.List, AbstractFileSystem.Attr {
    private static final Logger ERR = Logger.getLogger(MemoryFileSystem.class.getName());

    private static final AtomicLong COUNT = new AtomicLong();
    private final long id = COUNT.incrementAndGet();
    
    /** time when the filesystem was created. It is supposed to be the default
     * time of modification for all resources that has not been modified yet
     */
    private java.util.Date created = new java.util.Date();

    /** maps String to Entry */
    private final Map<String, Entry> entries = initEntry();
    
    @SuppressWarnings("deprecation") // need to set it for compat
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    /** Creates new MemoryFS */
    public MemoryFileSystem() {
        attr = this;
        list = this;
        change = this;
        info = this;

        
        try {
            _setSystemName("MemoryFileSystem" + String.valueOf(id));
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

    /** Creates MemoryFS with data */
    public MemoryFileSystem(String[] resources) {
        this();

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < resources.length; i++) {
            sb.append(resources[i]);

            if (resources[i].endsWith("/")) {
                // folder
                getOrCreateEntry(resources[i]).data = null;
            } else {
                getOrCreateEntry(resources[i]).data = new byte[0];
            }
        }
    }

    /** finds entry for given name */
    private Entry getOrCreateEntry(String n) {
        if ((n.length() > 0) && (n.charAt(0) == '/')) {
            n = n.substring(1);
        }

        boolean isValidEntry = isValidEntry(n);
        synchronized(entries) {
            Entry x = entries.get(n);

            if (x == null || !isValidEntry) {
                x = new Entry(n);
                entries.put(n, x);
            }
        
            return x;
        }
    }

	

    private boolean isValidEntry(String n) {
	return isValidEntry(n, null);
    }
    
    /** finds whether there already is this name */
    private boolean isValidEntry(String n, Boolean expectedResult) {
        boolean retval = (n.length() == 0) ? true : false;
        
        if ((n.length() > 0) && (n.charAt(0) == '/')) {
            n = n.substring(1);
        }

        Entry x = entries.get(n);
	FileObject fo = null;
        
        if (x != null) {
            Reference<? extends FileObject> ref = findReference(n);
            if (ref != null) {
                fo = ref.get();
                retval = (fo != null) ? fo.isValid() : true;
            }   
        }

	if (ERR.isLoggable(Level.FINE) && expectedResult != null && retval != expectedResult.booleanValue()) {
	    logMessage("entry: " + x +  " isValidReference.fo: " + ((fo == null) ? "null" : //NOI18N
		(fo.isValid() ? "valid" : "invalid")));//NOI18N
	}
	
        return (retval);
    }

    public String getDisplayName() {
        return "MemoryFileSystem";
    }

    public boolean isReadOnly() {
        return false;
    }

    public Enumeration<String> attributes(String name) {
        if (!isValidEntry(name)) {
            return org.openide.util.Enumerations.empty();
        }
        return Collections.enumeration(getOrCreateEntry(name).attrs.keySet());
    }

    public String[] children(String f) {
        if ((f.length() > 0) && (f.charAt(0) == '/')) {
            f = f.substring(1);
        }

        if ((f.length() > 0) && !f.endsWith("/")) {
            f = f + "/";
        }

        Set<String> l = new HashSet<String>();

        //System.out.println("Folder: " + f);
        synchronized(entries) {
            for (String name : entries.keySet()) {
                if (name.startsWith(f) || (f.trim().length() == 0)) {
                    int i = name.indexOf('/', f.length());
                    String child = null;

                    if (i > 0) {
                        child = name.substring(f.length(), i);
                    } else {
                        child = name.substring(f.length());
                    }

                    if (child.trim().length() > 0) {
                        l.add(child);
                    }
                }
            }

            return l.toArray(new String[0]);
        }
    }

    public void createData(String name) throws IOException {
        if (isValidEntry(name, Boolean.FALSE)) {
	    StringBuffer message = new StringBuffer();
	    message.append("File already exists: ").append(name);
            throw new IOException(message.toString());//NOI18N
        }

        getOrCreateEntry(name).data = new byte[0];
    }

    public void createFolder(String name) throws java.io.IOException {
        if (isValidEntry(name, Boolean.FALSE)) {
	    StringBuffer message = new StringBuffer();
	    message.append("Folder already exists: ").append(name);
            throw new IOException(message.toString());//NOI18N
        }

        getOrCreateEntry(name).data = null;
    }

    public void delete(String name) throws IOException {
        if (entries.remove(name) == null) {
            throw new IOException("No file to delete: " + name); // NOI18N
        }
    }

    public void deleteAttributes(String name) {
    }

    public boolean folder(String name) {
        return getOrCreateEntry(name).data == null;
    }

    public InputStream inputStream(String name) throws java.io.FileNotFoundException {
        byte[] arr = getOrCreateEntry(name).data;

        if (arr == null) {
            arr = new byte[0];
        }

        return new ByteArrayInputStream(arr);
    }

    public java.util.Date lastModified(String name) {
        java.util.Date d = getOrCreateEntry(name).last;

        return (d == null) ? created : d;
    }

    public void lock(String name) throws IOException {
    }

    public void markUnimportant(String name) {
    }

    public String mimeType(String name) {
        return (String) getOrCreateEntry(name).attrs.get("mimeType");
    }

    public OutputStream outputStream(final String name)
    throws java.io.IOException {
        class Out extends ByteArrayOutputStream {
            public void close() throws IOException {
                super.close();

                getOrCreateEntry(name).data = toByteArray();
                getOrCreateEntry(name).last = new Date();
            }
        }

        return new Out();
    }

    public Object readAttribute(String name, String attrName) {
        return isValidEntry(name) ? getOrCreateEntry(name).attrs.get(attrName) : null;
    }

    public boolean readOnly(String name) {
        return false;
    }

    @Override
    public void rename(String oldName, String newName)
    throws IOException {
        if (!isValidEntry(oldName)) {
            throw new IOException("The file to rename does not exist.");
        }

        if (isValidEntry(newName)) {
            throw new IOException("Cannot rename to existing file");
        }

        if ((newName.length() > 0) && (newName.charAt(0) == '/')) {
            newName = newName.substring(1);
        }
        
        ArrayList<Map.Entry<String, Entry>> clone = new ArrayList<Map.Entry<String, Entry>>(entries.entrySet());
        for (Map.Entry<String, Entry> each : clone) {
            if (each.getKey().startsWith(oldName)) {
                entries.remove(each.getKey());
                String n = newName + each.getKey().substring(oldName.length());
                entries.put(n, each.getValue());
            }
        }
    }

    public void renameAttributes(String oldName, String newName) {
    }

    public long size(String name) {
        byte[] d = getOrCreateEntry(name).data;

        return (d == null) ? 0 : d.length;
    }

    public void unlock(String name) {
    }

    public void writeAttribute(String name, String attrName, Object value)
    throws IOException {
        getOrCreateEntry(name).attrs.put(attrName, value);
    }

    private Map<String, Entry> initEntry() {
        if (!ERR.isLoggable(Level.FINE)) {
            return new ConcurrentHashMap<String, MemoryFileSystem.Entry>();
        }

	return new ConcurrentHashMap<String, MemoryFileSystem.Entry>() {
	    public MemoryFileSystem.Entry get(String key) {
		MemoryFileSystem.Entry retval = super.get(key);
		logMessage("called: GET" + " key: "+key + " result: " + retval);//NOI18N    		
		return retval;
	    }

	    public MemoryFileSystem.Entry put(String key, MemoryFileSystem.Entry value) {
		MemoryFileSystem.Entry retval = super.put(key, value);
		logMessage("called: PUT" + " key: "+key  + " value: "+value+ " result: " + retval);//NOI18N		
		return retval;            
	    }        

	    public MemoryFileSystem.Entry remove(String key) {
		MemoryFileSystem.Entry retval = super.remove(key);
		logMessage("called: REMOVE" + " key: "+key + " result: " + retval);//NOI18N		
		return retval;
	    }
	};
    }
    
    static final class Entry {
        /** String, Object */
        public Map<String, Object> attrs = Collections.synchronizedMap(new HashMap<String, Object>());
        public byte[] data;
        public java.util.Date last;
	private final String entryName;

	Entry(String entryName) {
	    this.entryName = entryName;
	}
	

	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append(" [").append(entryName);//NOI18N
	    sb.append(" -> ").append(super.toString());//NOI18N
	    sb.append("] ");
	    return sb.toString();
	}
    }
    
    
    private static void logMessage(final String message) {
        StringBuffer sb = new StringBuffer();
        sb.append(" -> ").append(message);

        //ucomment if necessary
        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bos);
        new Exception().printStackTrace(pw);
        pw.close();
        sb.append(bos.toString());
         */
        ERR.fine(sb.toString());
    }    

    // Support for URLs of the form memory://fs23/folder/file
    @ServiceProvider(service=URLMapper.class)
    public static final class Mapper extends URLMapper {
        private static final Map<Long,Reference<FileSystem>> filesystems = new HashMap<Long,Reference<FileSystem>>(); // i.e. a sparse array by id
        public @Override URL getURL(FileObject fo, int type) {
            if (type != URLMapper.INTERNAL) {
                return null;
            }
            try {
                FileSystem fs = fo.getFileSystem();
                if (fs instanceof MemoryFileSystem) {
                    String path = fo.getPath();
                    if (fo.isFolder() && !fo.isRoot()) {
                        path += '/';
                    }
                    return url((MemoryFileSystem) fs, path);
                }
            } catch (FileStateInvalidException x) {
                // ignore
            }
            return null;
        }
        // keep as separate method to avoid linking Handler until needed
        private static synchronized URL url(MemoryFileSystem fs, String path) {
            synchronized (filesystems) {
                Reference<FileSystem> r = filesystems.get(fs.id);
                if (r == null || r.get() == null) {
                    r = new WeakReference<FileSystem>(fs);
                    filesystems.put(fs.id, r);
                }
            }
            try {
                return new URL(null, Handler.PROTOCOL + "://fs" + fs.id + "/" + path, new Handler());
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
        }
        private static final Pattern HOST = Pattern.compile("fs(\\d+)"); // NOI18N
        static FileObject find(URL url) {
            if (!Handler.PROTOCOL.equals(url.getProtocol())) {
                return null;
            }
            Matcher m = HOST.matcher(url.getHost());
            if (!m.matches()) {
                return null;
            }
            Reference<FileSystem> r;
            synchronized (filesystems) {
                r = filesystems.get(Long.parseLong(m.group(1)));
            }
            if (r == null) {
                return null;
            }
            FileSystem fs = r.get();
            if (fs == null) {
                return null;
            }
            return fs.findResource(url.getPath().substring(1));
        }
        public @Override FileObject[] getFileObjects(URL url) {
            FileObject f = find(url);
            return f != null ? new FileObject[] {f} : null;
        }
    }
    @URLStreamHandlerRegistration(protocol=Handler.PROTOCOL)
    public static final class Handler extends URLStreamHandler {
        static final String PROTOCOL = "memory"; // NOI18N
        protected @Override URLConnection openConnection(URL u) throws IOException {
            return new MemoryConnection(u);
        }


        @Override
        protected int hashCode(URL u) {
            int h = 0;
            String host = u.getHost();
            if (host != null)
                h += host.toLowerCase().hashCode();
            // Generate the file part.
            String file = u.getFile();
            if (file != null)
                h += file.hashCode();
            // Generate the port part.
            return h;
        }

        @Override
        protected boolean equals(URL u1, URL u2) {
            return
                    Objects.equals(u1.getHost(), u2.getHost()) &&
                    Objects.equals(u1.getFile(), u2.getFile());
        }
        
        private static class MemoryConnection extends FileURL {
            MemoryConnection(URL u) {
                super(u);
            }
            public @Override synchronized void connect() throws IOException {
                if (fo == null) {
                    fo = Mapper.find(url);
                }
                if (fo == null) {
                    throw new FileNotFoundException(url.toString());
                }
            }
        }
    }

}
