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
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 * Builder/controller for a diff between two folders
 */
public class RecursiveDiffer {

    private static final Logger LOG = Logger.getLogger(RecursiveDiffer.class.getName());

    private static final RequestProcessor requestProcessor = new RequestProcessor("RecursiveDiffer", 1, false, false);
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private FileObject dir1;
    private FileObject dir2;
    private TreeEntry filteredResult;
    private TreeEntry scanResult;
    private final Map<TreeEntry,TreeEntry> filteredResultMap = new WeakHashMap<>();
    private boolean flatten = true;
    private boolean scanning = false;
    private volatile boolean cancel = false;
    private List<Pattern> exclusionPatterns = Collections.emptyList();

    public RecursiveDiffer(FileObject dir1, FileObject dir2) {
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    public FileObject getDir1() {
        return dir1;
    }

    public void setDir1(FileObject dir1) {
        this.dir1 = dir1;
    }

    public FileObject getDir2() {
        return dir2;
    }

    public void setDir2(FileObject dir2) {
        this.dir2 = dir2;
    }

    public List<Pattern> getExclusionPatterns() {
        return Collections.unmodifiableList(exclusionPatterns);
    }

    public void setExclusionPatterns(List<Pattern> exclusionPatterns) {
        if(exclusionPatterns != null) {
            this.exclusionPatterns = new ArrayList<>(exclusionPatterns);
        } else {
            this.exclusionPatterns = Collections.emptyList();
        }
    }

    public TreeEntry getScanResult() {
        return scanResult;
    }

    public TreeEntry getFilteredResult() {
        return filteredResult;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        boolean old = this.flatten;
        this.flatten = flatten;
        pcs.firePropertyChange("flatten", old, this.flatten);
        requestProcessor.post(() -> updateFilteredResult());
    }

    public boolean isScanning() {
        return scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    public void cancelScan() {
        this.cancel = true;
    }

    public void startScan() {
        cancel = false;
        assert SwingUtilities.isEventDispatchThread();
        if(scanning) {
            return;
        }
        scanning = true;
        pcs.firePropertyChange("scanning", false, true);
        requestProcessor.post(() -> {
            try {
                TreeEntry te = scan(dir1, dir2, dir1, dir2);
                SwingUtilities.invokeLater(() -> {
                    TreeEntry oldResult = scanResult;
                    scanResult = te;
                    pcs.firePropertyChange("scanResult", oldResult, scanning);
                    requestProcessor.post(() -> updateFilteredResult());
                });
            } catch (StopScanningException ex) {
                if(scanResult == null) {
                    SwingUtilities.invokeLater(() -> {
                        TreeEntry oldResult = scanResult;
                        scanResult = new TreeEntry(dir1, dir2, dir1, dir2, TreeEntry.DIR, TreeEntry.DIR, Collections.emptyList());
                        pcs.firePropertyChange("scanResult", oldResult, scanning);
                        requestProcessor.post(() -> updateFilteredResult());
                    });
                }
            } finally {
                SwingUtilities.invokeLater(() -> {
                    scanning = false;
                    pcs.firePropertyChange("scanning", true, false);
                });
            }
        });
    }

    private void updateFilteredResult() {
        Map<TreeEntry,TreeEntry> filteredMap = new HashMap<>();
        TreeEntry localInput = filterEntry(scanResult, filteredMap);
        SwingUtilities.invokeLater(() -> {
            filteredResultMap.clear();
            filteredResultMap.putAll(filteredMap);
            TreeEntry old = filteredResult;
            filteredResult = localInput;
            pcs.firePropertyChange("filteredResult", old, filteredResult);
        });
    }

    private TreeEntry filterEntry(TreeEntry te, Map<TreeEntry,TreeEntry> filteredMap) {
        if(te == null) {
            return null;
        }

        List<TreeEntry> newChildren = new ArrayList<>();

        if(flatten) {
            List<TreeEntry> queue = new ArrayList<>();
            queue.addAll(te.getChildren());
            while(! queue.isEmpty()) {
                TreeEntry cte = queue.remove(0);
                if(! cte.isFilesIdentical()) {
                    newChildren.add(new TreeEntry(
                        cte.getFile1(), cte.getFile2(),
                        cte.getBasePath1(), cte.getBasePath2(),
                        cte.getChecksum1(), cte.getChecksum2(),
                        Collections.emptyList()
                    ));
                }
                queue.addAll(cte.getChildren());
            }
        } else {
            for (TreeEntry child : te.getChildren()) {
                TreeEntry filteredEntry = filterEntry(child, filteredMap);
                if ((!filteredEntry.isFilesIdentical()) || (!filteredEntry.getChildren().isEmpty())) {
                    newChildren.add(filteredEntry);
                }
            }

        }

        TreeEntry filteredEntry = new TreeEntry(
            te.getFile1(), te.getFile2(),
            te.getBasePath1(), te.getBasePath2(),
            te.getChecksum1(), te.getChecksum2(),
            newChildren);
        filteredMap.put(filteredEntry, te);
        return filteredEntry;
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

    private TreeEntry scan(FileObject fo1, FileObject fo2, FileObject baseDir1, FileObject baseDir2) throws StopScanningException {
        checkCanceled();

        byte[] checksum1 = getChecksum(fo1);
        byte[] checksum2 = getChecksum(fo2);
        Map<String,FileObject> dir1Children = new HashMap<>();
        Map<String,FileObject> dir2Children = new HashMap<>();

        if(fo1 != null) {
            for (FileObject fo : fo1.getChildren()) {
                dir1Children.put(fo.getNameExt(), fo);
            }
        }

        if(fo2 != null) {
            for (FileObject fo : fo2.getChildren()) {
                dir2Children.put(fo.getNameExt(), fo);
            }
        }

        Set<String> childNames = new TreeSet<>();
        childNames.addAll(dir1Children.keySet());
        childNames.addAll(dir2Children.keySet());

        List<TreeEntry> children = new ArrayList<>();

        CHILD: for (String childName : childNames) {
            FileObject child1 = dir1Children.get(childName);
            FileObject child2 = dir2Children.get(childName);

            if(child1 != null && ! child1.isValid()) {
                child1 = null;
            }
            if(child2 != null && ! child2.isValid()) {
                child2 = null;
            }

            String relativePath;
            if (child1 != null) {
                relativePath = child1.getPath().substring(baseDir1.getPath().length() + 1);
            } else {
                relativePath = child2.getPath().substring(baseDir2.getPath().length() + 1);
            }

            for(Pattern p: exclusionPatterns) {
                if(p.matcher(relativePath).matches()) {
                    continue CHILD;
                }
            }

            children.add(scan(child1, child2, baseDir1, baseDir2));
        }

        Collections.sort(children);

        return new TreeEntry(fo1, fo2, baseDir1, baseDir2, checksum1, checksum2, children);
    }

    @SuppressWarnings("NestedAssignment")
    private byte[] getChecksum(FileObject fo) throws StopScanningException {
        checkCanceled();
        if(fo == null) {
            return new byte[0];
        } else if(fo.isFolder()) {
            return TreeEntry.DIR;
        } else {
            try(InputStream is = fo.getInputStream()) {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] buffer = new byte[16 * 1024];
                int read;
                while((read = is.read(buffer)) >= 0) {
                    checkCanceled();
                    md.update(buffer, 0, read);
                }
                return md.digest();
            } catch (IOException | NoSuchAlgorithmException ex) {
                LOG.log(Level.INFO, null, ex);
                return new byte[0];
            }
        }
    }

    private void checkCanceled() throws StopScanningException {
        if(cancel) {
            throw new StopScanningException();
        }
    }

    public void removeTreeEntry(TreeEntry target) {
        TreeEntry original = filteredResultMap.getOrDefault(target, target);
        TreeEntry parent = original.getParent();
        boolean contentIdentical = parent.isFilesIdentical();
        ArrayList newChildren = new ArrayList<>(parent.getChildren());
        newChildren.remove(original);
        if (newChildren.isEmpty() && contentIdentical) {
            removeTreeEntry(original);
        } else {
            parent.removeChild(original);
        }
        updateFilteredResult();
    }

    private static final class StopScanningException extends Exception {

    }
}
