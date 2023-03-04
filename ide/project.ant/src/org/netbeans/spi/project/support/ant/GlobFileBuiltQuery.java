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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Simple file built query based on glob patterns.
 * @see AntProjectHelper#createGlobFileBuiltQuery
 * @author Jesse Glick
 */
final class GlobFileBuiltQuery implements FileBuiltQueryImplementation {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.spi.project.support.ant.GlobFileBuiltQuery"); // NOI18N
        
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final String[] fromPrefixes;
    private final String[] fromSuffixes;
    private final String[] toPrefixes;
    private final String[] toSuffixes;
    private static final Reference<StatusImpl> NONE = new WeakReference<StatusImpl>(null);
    private final Map<FileObject,Reference<StatusImpl>> statuses = new WeakHashMap<FileObject,Reference<StatusImpl>>();

    /**
     * Create a new query implementation based on an Ant-based project.
     * @see AntProjectHelper#createGlobFileBuiltQuery
     */
    public GlobFileBuiltQuery(AntProjectHelper helper, PropertyEvaluator eval, String[] from, String[] to) throws IllegalArgumentException {
        this.helper = helper;
        this.eval = eval;
        int l = from.length;
        if (to.length != l) {
            throw new IllegalArgumentException("Non-matching lengths"); // NOI18N
        }
        fromPrefixes = new String[l];
        fromSuffixes = new String[l];
        toPrefixes = new String[l];
        toSuffixes = new String[l];
        for (int i = 0; i < l; i++) {
            int idx = from[i].indexOf('*');
            if (idx == -1 || idx != from[i].lastIndexOf('*')) {
                throw new IllegalArgumentException("Zero or multiple asterisks in " + from[i]); // NOI18N
            }
            fromPrefixes[i] = from[i].substring(0, idx);
            fromSuffixes[i] = from[i].substring(idx + 1);
            idx = to[i].indexOf('*');
            if (idx == -1 || idx != to[i].lastIndexOf('*')) {
                throw new IllegalArgumentException("Zero or multiple asterisks in " + to[i]); // NOI18N
            }
            toPrefixes[i] = to[i].substring(0, idx);
            toSuffixes[i] = to[i].substring(idx + 1);
            // XXX check that none of the pieces contain two slashes in a row, and
            // the path does not start with or end with a slash, etc.
        }
        // XXX add properties listener to evaluator... if anything changes, refresh all
        // status objects and clear the status cache; can then also keep a cache of
        // evaluated path prefixes & suffixes
    }
    
    public @Override synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        Reference<StatusImpl> r = statuses.get(file);
        if (r == NONE) {
            return null;
        }
        StatusImpl status = (r != null) ? r.get() : null;
        if (status == null) {
            status = createStatus(file);
            if (status != null) {
                statuses.put(file, new WeakReference<StatusImpl>(status));
            } else {
                statuses.put(file, NONE);
            }
        }
        return status;
    }
    
    private File findTarget(FileObject file) {
        File sourceF = FileUtil.toFile(file);
        if (sourceF == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Not a disk file: {0}", file);
            }
            return null;
        }
        String source = sourceF.getAbsolutePath();
        for (int i = 0; i < fromPrefixes.length; i++) {
            String prefixEval = eval.evaluate(fromPrefixes[i]);
            if (prefixEval == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0} evaluates to null", fromPrefixes[i]);
                }
                continue;
            }
            String suffixEval = eval.evaluate(fromSuffixes[i]);
            if (suffixEval == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0} evaluates to null", fromSuffixes[i]);
                }
                continue;
            }
            boolean endsWithSlash = prefixEval.endsWith("/"); // NOI18N
            String prefixF = helper.resolveFile(prefixEval).getAbsolutePath();
            if (endsWithSlash && !prefixF.endsWith(File.separator)) {
                prefixF += File.separatorChar;
            }
            if (!source.startsWith(prefixF)) {
                continue;
            }
            String remainder = source.substring(prefixF.length());
            if (!remainder.endsWith(suffixEval.replace('/', File.separatorChar))) {
                continue;
            }
            String particular = remainder.substring(0, remainder.length() - suffixEval.length());
            String toPrefixEval = eval.evaluate(toPrefixes[i]);
            if (toPrefixEval == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0} evaluates to null", toPrefixes[i]);
                }
                continue;
            }
            String toSuffixEval = eval.evaluate(toSuffixes[i]);
            if (toSuffixEval == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "{0} evaluates to null", toSuffixes[i]);
                }
                continue;
            }
            File target = helper.resolveFile(toPrefixEval + particular + toSuffixEval);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Found target for {0}: {1}", new Object[]{source, target});
            }
            return target;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "No match for path {0} among {1} {2}", new Object[]{source, Arrays.asList(fromPrefixes), Arrays.asList(fromSuffixes)});
        }
        return null;
    }
    
    private StatusImpl createStatus(FileObject file) {
        File target = findTarget(file);
        if (target != null) {
            ProjectIDEServicesImplementation.FileBuiltQuerySource source = ProjectIDEServices.createFileBuiltQuerySource(file);
            return source != null ? new StatusImpl(source, file, target) : null;
        } else {
            return null;
        }
    }

    private static final RequestProcessor RP = new RequestProcessor(StatusImpl.class.getName());
    
    private final class StatusImpl implements FileBuiltQuery.Status, PropertyChangeListener/*<DataObject>*/, FileChangeListener, Runnable {
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private Boolean built = null;
        private final ProjectIDEServicesImplementation.FileBuiltQuerySource source;
        private File target;
        private final FileChangeListener targetListener;
        
        @SuppressWarnings("LeakingThisInConstructor")
        StatusImpl(ProjectIDEServicesImplementation.FileBuiltQuerySource source, FileObject sourceFO, File target) {
            this.source = source;
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(this, this.source));
            sourceFO.addFileChangeListener(FileUtil.weakFileChangeListener(this, sourceFO));
            this.target = target;
            targetListener = new FileChangeListener() {
                public @Override void fileFolderCreated(FileEvent fe) {
                    // N/A for file
                }
                public @Override void fileDataCreated(FileEvent fe) {
                    update();
                }
                public @Override void fileChanged(FileEvent fe) {
                    update();
                }
                public @Override void fileDeleted(FileEvent fe) {
                    recalcTarget();
                    update();
                }
                public @Override void fileRenamed(FileRenameEvent fe) {
                    update();
                }
                public @Override void fileAttributeChanged(FileAttributeEvent fe) {
                    update();
                }
            };
            FileUtil.addFileChangeListener(targetListener, target);
        }
        
        // Side effect is to update its cache and maybe fire changes.
        public @Override boolean isBuilt() {
            boolean doFire = false;
            boolean b;
            synchronized (GlobFileBuiltQuery.this) {
                b = isReallyBuilt();
                if (built != null && built.booleanValue() != b) {
                    doFire = true;
                }
                built = Boolean.valueOf(b);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "isBuilt: {0} from {1}", new Object[]{b, this});
                }
            }
            if (doFire) {
                cs.fireChange();
            }
            return b;
        }
        
        private boolean isReallyBuilt() {
            if (!source.isValid()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "invalid: {0}", this);
                }
                return false; // whatever
            }
            if (source.isModified()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "modified: {0}", this);
                }
                return false;
            }
            if (target == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "no target matching {0}", this);
                }
                return false;
            }
            long targetTime = target.lastModified();
            long sourceTime = source.getFileObject().lastModified().getTime();
            if (targetTime >= sourceTime) {
                return true;
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "out of date (target: {0} vs. source: {1}): {2}", new Object[]{targetTime, sourceTime, this});
                }
                return false;
            }
        }
        
        public @Override void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }
        
        public @Override void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        private void update() {
            // XXX should this maintain a single Task and schedule() it?
            RP.post(this);
        }
        
        public @Override void run() {
            isBuilt();
        }
        
        public @Override void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource() instanceof ProjectIDEServicesImplementation.FileBuiltQuerySource;
            if (ProjectIDEServicesImplementation.FileBuiltQuerySource.PROP_MODIFIED.equals(evt.getPropertyName())) {
                update();
            }
        }
        
        public @Override void fileChanged(FileEvent fe) {
            update();
        }
        
        public @Override void fileDeleted(FileEvent fe) {
            update();
        }
        
        public @Override void fileRenamed(FileRenameEvent fe) {
            recalcTarget();
            update();
        }
        
        public @Override void fileDataCreated(FileEvent fe) {
            // ignore
        }
        
        public @Override void fileFolderCreated(FileEvent fe) {
            // ignore
        }
        
        public @Override void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }

        private void recalcTarget() {
            File target2 = findTarget(source.getFileObject());
            if (!BaseUtilities.compareObjects(target, target2)) {
                synchronized(targetListener) {
                    // #45694: source file moved, recalculate target.
                    if (target != null) {
                        FileUtil.removeFileChangeListener(targetListener, target);
                    }
                    if (target2 != null) {
                        FileUtil.addFileChangeListener(targetListener, target2);
                    }
                }
                target = target2;
            }
        }
        
        @Override
        public String toString() {
            return "GFBQ.StatusImpl[" + source.getFileObject()+ " -> " + target + "]"; // NOI18N
        }

    }
    
}
