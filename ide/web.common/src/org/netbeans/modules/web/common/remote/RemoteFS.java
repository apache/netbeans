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
package org.netbeans.modules.web.common.remote;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.common.spi.RemoteFSDecorator;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A special file system for remote files
 * 
 * @author Martin Entlicher
 */
@NbBundle.Messages("RemoteFSDisplayName=Remote Filesystem")
public class RemoteFS extends AbstractFileSystem {
    
    private static RemoteFS DEFAULT = new RemoteFS();
    private final Map<String, URL> urlCache;
    private final StatusDecorator status;

    private RemoteFS() {
        this.urlCache = new HashMap<String, URL>();
        list = new RemoteList();
        info = new RemoteInfo();
        attr = new RemoteAttributes();
        RemoteFSDecorator rfsd = Lookup.getDefault().lookup(RemoteFSDecorator.class);
        if (rfsd != null) {
            rfsd.setDefaultDecorator(new RemoteStatus());
            status = rfsd;
        } else {
            status = new RemoteStatus();
        }
    }
    
    /*
    public RemoteFS(RemoteFiles rFiles) {
        this.urlCache = DEFAULT.urlCache;
        list = new RemoteList(rFiles);
        info = DEFAULT.info;
        attr = DEFAULT.attr;
        status = DEFAULT.status;
    }
    */
    
    public static RemoteFS getDefault() {
        return DEFAULT;
    }
    
    @Override
    public String getDisplayName() {
        return Bundle.RemoteFSDisplayName();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public List getList() {
        return list;
    }

    private URL getURLforName(String name) {
        synchronized (urlCache) {
            return urlCache.get(name);
        }
    }
    
    private FileObject getDelegateFor(String name) {
        return getDelegateFor(name, true);
    }
    private FileObject getDelegateFor(String name, boolean asynchronous) {
        URL url = getURLforName(name);
        if (url == null) {
            return null;
        }
        try {
            return RemoteFilesCache.getDefault().getRemoteFile(url, asynchronous);
        } catch (IOException ex) {
            return null;
        }
    }
    
    private String getNameFrom(URL url) {
        String surl = url.toExternalForm();
        if (surl.substring(0, 7).equalsIgnoreCase("http://")) { //NOI18N
            surl = surl.substring(7);
        } else if (surl.substring(0, 8).equalsIgnoreCase("https://")) { //NOI18N
            surl = surl.substring(8);
        }
        surl = surl.replace('/', '_');
        surl = surl.replace('\\', '_');
        return surl;
    }

    public FileObject getFileForURL(URL url) {
        String surl = getNameFrom(url);
        FileObject fo = getRoot().getFileObject(surl, ""); //NOI18N
        if (fo != null) {
            return fo;
        }
        synchronized (urlCache) {
            urlCache.put(surl, url);
        }
        getRoot().refresh();
        return getRoot().getFileObject(surl, ""); //NOI18N
    }

    @Override
    public StatusDecorator getDecorator() {
        return status;
    }

    
    private final class RemoteList implements List, ChangeListener {
        
        RemoteList() {
        }

        @Override
        public String[] children(String f) {
            if (f.isEmpty()) { // root
                String[] childrenNames;
                synchronized (urlCache) {
                    childrenNames = urlCache.keySet().toArray(new String[] {});
                }
                return childrenNames;
            } else {
                return new String[] {};
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refreshResource("", false); //NOI18N
        }
        
    }
    
    private class RemoteInfo implements Info {
        
        RemoteInfo() {}
        
        @Override
        public Date lastModified(String name) {
            FileObject fo = getDelegateFor(name);
            if (fo != null) {
                return fo.lastModified();
            } else {
                return new Date(0);
            }
        }

        @Override
        public boolean folder(String name) {
            return name.isEmpty(); // Only the root is folder
        }

        @Override
        public boolean readOnly(String name) {
            return true;
        }

        @Override
        public String mimeType(String name) {
            FileObject fo = getDelegateFor(name);
            if (fo != null) {
                return fo.getMIMEType();
            } else {
                return "content/unknown";   // NOI18N
            }
        }

        @Override
        public long size(String name) {
            FileObject fo = getDelegateFor(name, false);
            if (fo != null) {
                return fo.getSize();
            } else {
                return 0;
            }
        }

        @Override
        public InputStream inputStream(String name) throws FileNotFoundException {
            FileObject fo = getDelegateFor(name, false);
            if (fo != null) {
                return fo.getInputStream();
            } else {
                throw new FileNotFoundException("Did not find '"+name+"'"); //NOI18N
            }
        }

        @Override
        public OutputStream outputStream(String name) throws IOException {
            throw new IOException("Can not write to remote files"); //NOI18N
        }

        @Override
        public void lock(String name) throws IOException {
            throw new IOException("Can not write to remote files"); //NOI18N
        }

        @Override
        public void unlock(String name) {
        }

        @Override
        public void markUnimportant(String name) {
        }
    }

    private class RemoteAttributes implements Attr {

        RemoteAttributes() {
        }

        @Override
        public Object readAttribute(String name, String attrName) {
            if ("java.io.File".equals(attrName)) {      // NOI18N
                return null;
            }
            FileObject fo = getDelegateFor(name);
            if (fo != null) {
                return fo.getAttribute(attrName);
            } else {
                return null;
            }
        }

        @Override
        public void writeAttribute(String name, String attrName, Object value) throws IOException {
            FileObject fo = getDelegateFor(name);
            if (fo != null) {
                fo.setAttribute(attrName, value);
            } else {
                throw new IOException("Did not find '"+name+"'"); //NOI18N
            }
        }

        @Override
        public Enumeration<String> attributes(String name) {
            FileObject fo = getDelegateFor(name);
            if (fo != null) {
                return fo.getAttributes();
            } else {
                return Collections.enumeration(Collections.emptyList());
            }
        }

        @Override
        public void renameAttributes(String oldName, String newName) {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public void deleteAttributes(String name) {
            FileObject fo = getDelegateFor(name);
            if (fo != null) {
                Enumeration<String> attributes = fo.getAttributes();
                for (String attr : Collections.list(attributes)) {
                    try {
                        fo.setAttribute(attr, null);
                    } catch (IOException ex) {}
                }
            }
        }
    }

    @NbBundle.Messages("LBL_RemoteFiles=Remote Files")
    private class RemoteStatus implements StatusDecorator {

        RemoteStatus() {}

        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            return null;
        }

        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            int n = files.size();
            if (n == 1) {
                FileObject fo = files.iterator().next();
                if (fo.isRoot()) {
                    return Bundle.LBL_RemoteFiles();
                }
                URL url = getURLforName(fo.getNameExt());
                String path = url.getPath();
                int index = path.lastIndexOf('/');
                int index2 = path.lastIndexOf('\\');
                if (index2 >= 0) {
                    if (index < 0) {
                        index = index2;
                    } else {
                        index = Math.max(index, index2);
                    }
                }
                if (index != -1) {
                    name = path.substring(index+1);
                }
                return name;
            } else {
                int index = name.lastIndexOf('/');
                int index2 = name.lastIndexOf('\\');
                if (index2 >= 0) {
                    if (index < 0) {
                        index = index2;
                    } else {
                        index = Math.max(index, index2);
                    }
                }
                if (index != -1) {
                    name = name.substring(index+1);
                }
                return name;
            }
        }
        
    }
    
}
