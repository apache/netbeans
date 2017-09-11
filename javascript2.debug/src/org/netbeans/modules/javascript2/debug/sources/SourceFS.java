/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.debug.sources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.io.ReaderInputStream;

/**
 *
 * @author Martin
 */
@NbBundle.Messages("SourceFSDisplayName=JavaScript Sources")
final class SourceFS extends AbstractFileSystem {
    
//    private static SourceFS DEFAULT = new SourceFS();
    
    private static final AtomicLong COUNT = new AtomicLong();
    private final long id = COUNT.incrementAndGet();
    
    //private final Map<String, URL> urlCache;
    private final Map<String, Item> items;
    //private final Status status;

    SourceFS() {
        //this.urlCache = new HashMap<String, URL>();
        this.items = new LinkedHashMap<>();
        list = new SourceList();
        info = new SourceInfo();
        attr = new SourceAttributes();
        //status = new SourceStatus();
    }
    
//    public static SourceFS getDefault() {
//        return DEFAULT;
//    }
    
    @Override
    public String getDisplayName() {
        return Bundle.SourceFSDisplayName();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    public List getList() {
        return list;
    }
    
    public long getID() {
        return id;
    }
    
    public FileObject createFile(String path, SourceContent content) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        int pl;
        do {
            pl = path.length();
            path = path.replace("//", "/");
        } while (path.length() < pl);
        int i = path.lastIndexOf('/');
        String name = (i >= 0) ? path.substring(i + 1) : path;
        Item item = new Item(name, content);
        FileObject parent;
        synchronized (items) {
            items.put(path, item);
            parent = getExistingParent(path);
        }
        parent.refresh(true);
        FileObject fo = findResource(path);
        if (fo == null && i > 0) {
            // check refreshes of intermediate folders
            String pp = parent.getPath();
            String parentPath = path.substring(0, i);
            while (parentPath.startsWith(pp) && pp.length() < parentPath.length()) {
                i = path.indexOf('/', pp.length() + 1);
                if (i < 0) {
                    break;
                }
                pp = path.substring(0, i);
                parent = findResource(pp);
                if (parent != null) {
                    parent.refresh();
                } else {
                    break;
                }
            }
            fo = findResource(path);
        }
        if (fo == null) {
            throw new IllegalStateException("Can not create file '"+path+"' parent = "+parent);
        }
        return fo;
    }
    
    private FileObject getExistingParent(String path) {
        FileObject parent = findResource(path);
        if (parent != null) {
            return parent.getParent();
        }
        while (parent == null && !path.isEmpty()) {
            int i = path.lastIndexOf('/');
            if (i > 0) {
                path = path.substring(0, i);
            } else {
                break;
            }
            parent = findResource(path);
        }
        if (parent == null) {
            parent = getRoot();
        }
        return parent;
    }
    
    private Item getItem(String path) {
        synchronized (items) {
            return items.get(path);
        }
    }

    /*
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
            return SourceFilesCache.getDefault().getRemoteFile(url, asynchronous);
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
    */

    /*@Override
    public Status getStatus() {
        return status;
    }*/

    
    private final class SourceList implements List, ChangeListener {
        
        SourceList() {
        }

        @Override
        public String[] children(String f) {
            while (f.startsWith("/")) {
                f = f.substring(1);
            }
            if (!f.isEmpty() && !f.endsWith("/")) {
                f = f + '/';
            }
            java.util.List<String> children = new ArrayList<>();
            int fl = f.length();
//            if (fl > 0) {
//                fl++; // The slash
//            }
            synchronized (items) {
                for (String name : items.keySet()) {
                    if (name.startsWith(f)) {
                        int end = name.indexOf('/', fl);
                        String ch;
                        if (end > 0) {
                            ch = name.substring(fl, end);
                        } else {
                            ch = name.substring(fl);
                        }
                        children.add(ch);
                    }
                }
            }
            return children.toArray(new String[]{});
            /*
            if (f.isEmpty()) { // root
                String[] childrenNames;
                synchronized (urlCache) {
                    childrenNames = urlCache.keySet().toArray(new String[] {});
                }
                return childrenNames;
            } else {
                return new String[] {};
            }
            */
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refreshResource("", false); //NOI18N
        }
        
    }
    
    private class SourceInfo implements Info {
        
        SourceInfo() {}
        
        @Override
        public Date lastModified(String name) {
            Item item = getItem(name);
            if (item != null) {
                return item.date;
            } else {
                return new Date(0);
            }
        }

        @Override
        public boolean folder(String name) {
            if (name.isEmpty()) {
                return true;     // The root is folder
            }
            synchronized (items) {
                for (String namePath : items.keySet()) {
                    if (namePath.startsWith(name)) {
                        return name.length() < namePath.length();
                    }
                }
            }
            return false;   // Unknown
        }

        @Override
        public boolean readOnly(String name) {
            return true;
        }

        @Override
        public String mimeType(String name) {
            return null;
        }

        @Override
        public long size(String name) {
            Item item = getItem(name);
            if (item != null) {
                return item.getSize();
            } else {
                return 0;
            }
        }

        @Override
        public InputStream inputStream(String name) throws FileNotFoundException {
            Item item = getItem(name);
            if (item != null) {
                return item.getInputStream();
            } else {
                throw new FileNotFoundException("Did not find '"+name+"'"); //NOI18N
            }
        }

        @Override
        public OutputStream outputStream(String name) throws IOException {
            throw new IOException("Can not write to source files"); //NOI18N
        }

        @Override
        public void lock(String name) throws IOException {
            throw new IOException("Can not write to source files"); //NOI18N
        }

        @Override
        public void unlock(String name) {
        }

        @Override
        public void markUnimportant(String name) {
        }
    }

    private class SourceAttributes implements Attr {

        SourceAttributes() {
        }

        @Override
        public Object readAttribute(String name, String attrName) {
            if ("java.io.File".equals(attrName)) {      // NOI18N
                return null;
            }
            Item item = getItem(name);
            if (item != null) {
                return item.getAttribute(attrName);
            } else {
                return null;
            }
        }

        @Override
        public void writeAttribute(String name, String attrName, Object value) throws IOException {
            Item item = getItem(name);
            if (item != null) {
                item.setAttribute(attrName, value);
            } else {
                throw new IOException("Did not find '"+name+"'"); //NOI18N
            }
        }

        @Override
        public Enumeration<String> attributes(String name) {
            Item item = getItem(name);
            if (item != null) {
                return item.getAttributes();
            } else {
                return Collections.emptyEnumeration();
            }
        }

        @Override
        public void renameAttributes(String oldName, String newName) {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public void deleteAttributes(String name) {
            Item item = getItem(name);
            if (item != null) {
                item.deleteAttributes();
            }
        }
    }

    
    private static class Item {
        
        public final String name;
        public final SourceContent content;
        public final Date date;
        private final Map<String, Object> attrs = new HashMap<>();
        
        public Item(String name, SourceContent content) {
            this.name = name;
            this.content = content;
            this.date = new Date();
        }

        private long getSize() {
            if (content != null) {
                return content.getLength();
            } else {
                return 0l;
            }
        }

        private InputStream getInputStream() throws FileNotFoundException {
            try {
                return new ReaderInputStream(new StringReader(content.getContent()));
            } catch (IOException ex) {
                throw new FileNotFoundException(ex.getLocalizedMessage());
            }
        }

        public Object getAttribute(String attrName) {
            synchronized (attrs) {
                return attrs.get(attrName);
            }
        }

        public void setAttribute(String attrName, Object value) {
            synchronized (attrs) {
                attrs.put(attrName, value);
            }
        }

        public Enumeration<String> getAttributes() {
            synchronized (attrs) {
                return Collections.enumeration(attrs.keySet());
            }
        }

        public void deleteAttributes() {
            synchronized (attrs) {
                attrs.clear();
            }
        }
        
    }
    
}
