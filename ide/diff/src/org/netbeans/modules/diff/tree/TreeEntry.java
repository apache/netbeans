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

package org.netbeans.modules.diff.tree;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.SwingUtilities;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

public class TreeEntry implements Comparable<TreeEntry> {
    static final byte[] DIR = "DIRECTORY".getBytes(StandardCharsets.UTF_8);

    private final LookupListener ll = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            Object source = ev.getSource();
            if (source instanceof Lookup.Result result) {
                boolean saveCookiesPresent = !result.allInstances().isEmpty();
                SwingUtilities.invokeLater(() -> {
                    boolean old = modified;
                    modified = saveCookiesPresent;
                    pcs.firePropertyChange("modified", old, modified);
                });
            }
        }
    };

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private TreeEntry parent;
    private final FileObject file1;
    private final FileObject file2;
    private final FileObject basePath1;
    private final FileObject basePath2;
    private final byte[] checksum1;
    private final byte[] checksum2;
    private final List<TreeEntry> children;
    private boolean modified;

    @SuppressWarnings({"LeakingThisInConstructor", "AccessingNonPublicFieldOfAnotherObject"})
    public TreeEntry(FileObject file1, FileObject file2, FileObject basePath1, FileObject basePath2, byte[] checksum1, byte[] checksum2, List<TreeEntry> children) {
        this.file1 = file1;
        this.file2 = file2;
        this.basePath1 = basePath1;
        this.basePath2 = basePath2;
        this.checksum1 = checksum1;
        this.checksum2 = checksum2;
        this.children = new ArrayList<>(children);
        if (this.file2 != null) {
            Lookup.Result<SaveCookie> saveCookieResult = this.file2.getLookup().lookupResult(SaveCookie.class);
            saveCookieResult.addLookupListener(WeakListeners.create(LookupListener.class, ll, saveCookieResult));
            modified = !saveCookieResult.allInstances().isEmpty();
        }
        for(TreeEntry cte: children) {
            cte.parent = this;
        }
    }

    public TreeEntry getParent() {
        return parent;
    }

    public FileObject getFile1() {
        return file1;
    }

    public FileObject getFile2() {
        return file2;
    }

    public FileObject getBasePath1() {
        return basePath1;
    }

    public FileObject getBasePath2() {
        return basePath2;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public byte[] getChecksum1() {
        return checksum1;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public byte[] getChecksum2() {
        return checksum2;
    }

    public boolean isFilesIdentical() {
        return Arrays.equals(checksum1, checksum2);
    }

    public String getRelativePath() {
        String basePath;
        String filePath;
        if(file1 != null) {
            filePath = file1.getPath();
            basePath = basePath1.getPath();
        } else {
            filePath = file2.getPath();
            basePath = basePath2.getPath();
        }
        return filePath.substring(Math.min(filePath.length(), basePath.length() + 1));
    }

    public String getRelativeParent() {
        String basePath;
        String filePath;
        if(file1 != null) {
            filePath = file1.getParent().getPath();
            basePath = basePath1.getPath();
        } else {
            filePath = file2.getParent().getPath();
            basePath = basePath2.getPath();
        }
        return filePath.substring(Math.min(filePath.length(), basePath.length() + 1));
    }

    public String getName() {
        if(file1 != null) {
            return file1.getNameExt();
        } else {
            return file2.getNameExt();
        }
    }

    public List<TreeEntry> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean isModified() {
        return modified;
    }

    public void removeChild(TreeEntry treeEntry) {
        children.remove(treeEntry);
    }

    @Override
    public int compareTo(TreeEntry t) {
        return getRelativePath().compareToIgnoreCase(t.getRelativePath());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.file1);
        hash = 29 * hash + Objects.hashCode(this.file2);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TreeEntry other = (TreeEntry) obj;
        if (!Objects.equals(this.file1, other.file1)) {
            return false;
        }
        if (!Objects.equals(this.file2, other.file2)) {
            return false;
        }
        if (!Arrays.equals(this.checksum1, other.checksum1)) {
            return false;
        }
        if (!Arrays.equals(this.checksum2, other.checksum2)) {
            return false;
        }
        return Objects.equals(this.children, other.children);
    }

}
