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
package org.netbeans.modules.web.inspect.webkit;

import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.actions.Closable;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Cache of temporary read-only copies of remote CSS style sheets.
 *
 * @author Jan Stola
 */
public class RemoteStyleSheetCache {
    /** The default instance of this class. */
    private static final RemoteStyleSheetCache DEFAULT = new RemoteStyleSheetCache();
    /**
     * The cache itself: a mapping between {@code StyleSheetBody}
     * and the corresponding temporary read-only copy of the stylesheet.
     */
    private final Map<StyleSheetBody, FileObject> cache = new HashMap<StyleSheetBody, FileObject>();

    /**
     * Returns the default instance of this class.
     * 
     * @return the default instance of this class.
     */
    public static RemoteStyleSheetCache getDefault() {
        return DEFAULT;
    }

    /**
     * Creates a new {@code RemoteStyleSheetCache}.
     */
    private RemoteStyleSheetCache() {
    }

    /**
     * Returns a temporary read-only copy of the stylesheet that corresponds
     * to the given {@code StyleSheetBody}.
     * 
     * @param body identification of the stylesheet.
     * @return temporary read-only copy of the stylesheet that corresponds
     * to the given {@code StyleSheetBody}.
     */
    public FileObject getFileObject(StyleSheetBody body) {
        synchronized (this) {
            FileObject fob = cache.get(body);
            if (fob == null) {
                fob = new StyleSheetFileObject(body);
                cache.put(body, fob);
            }
            return fob;
        }
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        synchronized (this) {
            Collection<FileObject> files = cache.values();
            closeFiles(new ArrayList<FileObject>(files));
            cache.clear();
        }
    }

    /**
     * Closes the given collection of files.
     * 
     * @param files files to close.
     */
    private static void closeFiles(final Collection<FileObject> files) {
        if (EventQueue.isDispatchThread()) {
            for (FileObject file : files) {
                try {
                    DataObject dob = DataObject.find(file);
                    Closable close = dob.getLookup().lookup(Closable.class);
                    if (close != null) {
                        close.close();
                    }
                } catch (DataObjectNotFoundException dnfex) {
                    Logger.getLogger(RemoteStyleSheetCache.class.getName()).log(Level.INFO, null, dnfex);
                }
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    closeFiles(files);
                }
            });
        }
    }

    /**
     * In-memory file object that represents a content of a style sheet.
     */
    static class StyleSheetFileObject extends FileObject {
        /**
         * Style-sheet represented by this file object or {@code null}
         * when the file object is a folder.
         */
        private transient StyleSheetBody body;
        /**
         * Style-sheet in this folder or {@code null} when this
         * file object is not a folder.
         */
        private StyleSheetFileObject child;
        /**
         * Parent folder of the style-sheet or {@code null} when
         * this file object is a folder.
         */
        private StyleSheetFileObject parent;
        /** File system where this file object resides. */
        private FileSystem fs;
        /** Attributes of this file object. */
        private final Map<String,Object> attributes = new HashMap<String,Object>();

        /**
         * Creates a new {@code StyleSheetFileObject} that represents
         * the given style-sheet.
         * 
         * @param body stylesheet to represent.
         */
        StyleSheetFileObject(StyleSheetBody body) {
            this.body = body;
            this.parent = new StyleSheetFileObject(this);
            this.fs = this.parent.fs;
        }

        /**
         * Creates a new folder {@code StyleSheetFileObject} that
         * contains the given file object.
         * 
         * @param child file object to include in the folder.
         */
        StyleSheetFileObject(StyleSheetFileObject child) {
            this.child = child;
            this.fs = new DummyFileSystem(this);
        }

        /**
         * Returns the name specified in the style-sheet meta-data.
         * 
         * @return name specified in the style-sheet meta-data or {@code null}.
         */
        private String getSpecifiedName() {
            String name = null;
            if (body != null) {
                List<Rule> rules = body.getRules();
                if (rules.isEmpty()) {
                    StyleSheetHeader header = body.getHeader();
                    if (header != null) {
                        name = header.getSourceURL();
                    }
                } else {
                    Rule rule = rules.get(0);
                    name = rule.getSourceURL();
                }
                if (name == null) {
                    name = body.getStyleSheetId();
                } else {
                    int index = name.lastIndexOf('/'); // NOI18N
                    name = name.substring(index+1);
                    if (name.endsWith(".css")) { // NOI18N
                        name = name.substring(0, name.length()-4);
                    }
                }
            }
            return name;
        }

        @Override
        @NbBundle.Messages({
            "RemoteStyleSheetCache.generatedStylesheet=Generated Style Sheet" // NOI18N
        })
        public String getName() {
            String name = getSpecifiedName();
            if (name == null || name.isEmpty()) {
                name = Bundle.RemoteStyleSheetCache_generatedStylesheet();
            }
            return name;
        }

        @Override
        public String getExt() {
            String name = getSpecifiedName();
            return (name == null || name.isEmpty()) ? "" : "css"; // NOI18N
        }

        @Override
        public String getMIMEType() {
            return "text/css"; // NOI18N
        }

        @Override
        public String getMIMEType(String... withinMIMETypes) {
            return getMIMEType();
        }

        @Override
        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new IOException();
        }

        @Override
        public FileSystem getFileSystem() throws FileStateInvalidException {
            return fs;
        }

        @Override
        public FileObject getParent() {
            return parent;
        }

        @Override
        public boolean isFolder() {
            return (body == null);
        }

        @Override
        public Date lastModified() {
            return new Date();
        }

        @Override
        public boolean isRoot() {
            return isFolder();
        }

        @Override
        public boolean isData() {
            return !isFolder();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void delete(FileLock lock) throws IOException {
            throw new IOException();
        }

        @Override
        public Object getAttribute(String attrName) {
            return attributes.get(attrName);
        }

        @Override
        public void setAttribute(String attrName, Object value) throws IOException {
            attributes.put(attrName, value);
        }

        @Override
        public Enumeration<String> getAttributes() {
            return Collections.enumeration(attributes.keySet());
        }

        @Override
        public void addFileChangeListener(FileChangeListener fcl) {
        }

        @Override
        public void removeFileChangeListener(FileChangeListener fcl) {
        }

        @Override
        public long getSize() {
            return body.getText().length();
        }

        @Override
        public InputStream getInputStream() throws FileNotFoundException {
            return new ByteArrayInputStream(body.getText().getBytes());
        }

        @Override
        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new IOException();
        }

        @Override
        public FileLock lock() throws IOException {
            return FileLock.NONE;
        }

        @Override
        public void setImportant(boolean b) {
        }

        @Override
        public FileObject[] getChildren() {
            FileObject[] children;
            if (child == null) {
                children = new FileObject[0];
            } else {
                children = new FileObject[] { child };
            }
            return children;
        }

        @Override
        public FileObject getFileObject(String name, String ext) {
            return null;
        }

        @Override
        public FileObject createFolder(String name) throws IOException {
            throw new IOException();
        }

        @Override
        public FileObject createData(String name, String ext) throws IOException {
            throw new IOException();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
        
    }

    /**
     * Dummy file-system.
     */
    static class DummyFileSystem extends FileSystem {
        /** Root of the file-system. */
        private final FileObject root;

        /**
         * Creates a new {@code DummyFileSystem} with the specified root.
         * 
         * @param root root of the file-system.
         */
        DummyFileSystem(FileObject root) {
            this.root = root;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DummyFileSystem.class, "RemoteStyleSheetCache.downloadedStyleSheets"); // NOI18N
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public FileObject getRoot() {
            return root;
        }

        @Override
        public FileObject findResource(String name) {
            return null;
        }

        public SystemAction[] getActions() {
            return new SystemAction[0];
        }
        
    }
    
}
