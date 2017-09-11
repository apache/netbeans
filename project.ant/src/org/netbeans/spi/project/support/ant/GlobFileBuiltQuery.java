/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Not a disk file: {0}", file);
            }
            return null;
        }
        String source = sourceF.getAbsolutePath();
        for (int i = 0; i < fromPrefixes.length; i++) {
            String prefixEval = eval.evaluate(fromPrefixes[i]);
            if (prefixEval == null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "{0} evaluates to null", fromPrefixes[i]);
                }
                continue;
            }
            String suffixEval = eval.evaluate(fromSuffixes[i]);
            if (suffixEval == null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "{0} evaluates to null", fromSuffixes[i]);
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
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "{0} evaluates to null", toPrefixes[i]);
                }
                continue;
            }
            String toSuffixEval = eval.evaluate(toSuffixes[i]);
            if (toSuffixEval == null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "{0} evaluates to null", toSuffixes[i]);
                }
                continue;
            }
            File target = helper.resolveFile(toPrefixEval + particular + toSuffixEval);
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Found target for {0}: {1}", new Object[]{source, target});
            }
            return target;
        }
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "No match for path {0} among {1} {2}", new Object[]{source, Arrays.asList(fromPrefixes), Arrays.asList(fromSuffixes)});
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
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "isBuilt: {0} from {1}", new Object[]{b, this});
                }
            }
            if (doFire) {
                cs.fireChange();
            }
            return b;
        }
        
        private boolean isReallyBuilt() {
            if (!source.isValid()) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "invalid: {0}", this);
                }
                return false; // whatever
            }
            if (source.isModified()) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "modified: {0}", this);
                }
                return false;
            }
            if (target == null) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "no target matching {0}", this);
                }
                return false;
            }
            long targetTime = target.lastModified();
            long sourceTime = source.getFileObject().lastModified().getTime();
            if (targetTime >= sourceTime) {
                return true;
            } else {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "out of date (target: {0} vs. source: {1}): {2}", new Object[]{targetTime, sourceTime, this});
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
