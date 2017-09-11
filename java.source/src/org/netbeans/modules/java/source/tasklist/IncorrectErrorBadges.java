/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.tasklist;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public class IncorrectErrorBadges implements CancellableTask<CompilationInfo> {

    private static final boolean DISABLE = Boolean.getBoolean(IncorrectErrorBadges.class.getName() + ".disable");
    
    private static final Logger LOG = Logger.getLogger(IncorrectErrorBadges.class.getName());
    
    private int invocationCount;
    private long timestamp;
    private FactoryImpl factory;

    private IncorrectErrorBadges(FactoryImpl factory) {
        this.factory = factory;
    }
    
    public void cancel() {}

    public void run(CompilationInfo info) {
        if (DISABLE) {
            LOG.fine("Disabled");
            return ;
        }
        
        if (IndexingController.getDefault().isInProtectedMode()) {
            LOG.fine("RepositoryUpdater in protected mode");
            return ;
        }
        
        LOG.log(Level.FINE, "invocationCount={0}, file={1}", new Object [] { invocationCount, info.getFileObject() });
        if (invocationCount++ > 1) {
            LOG.log(Level.FINE, "Too many invocations: {0}", invocationCount);
            return ;
        }
        
        try {
            boolean containsError = false;
            for (Diagnostic d : info.getDiagnostics()) {
                if (d.getKind() == Kind.ERROR) {
                    LOG.log(Level.FINE, "File contains errors: {0}", info.getFileObject());
                    containsError = true;
                    break;
                }
            }

            final FileObject file = info.getFileObject();
            boolean hasErrorBadge = ErrorsCache.isInError(file, false);
            if (hasErrorBadge) {
                LOG.log(Level.FINE, "Errorscache.isInError: {0}", info.getFileObject());
            }

            if (containsError == hasErrorBadge) {
                return;
            }
            
            DataObject d = DataObject.find(file);

            if (d.isModified()) {
                LOG.log(Level.FINE, "File is modified: {0}", info.getFileObject());
                return;
            }

            
            if (invocationCount == 1) {
                timestamp = file.lastModified().getTime();
                LOG.log(Level.FINE, "Capturing timestamp={0}, file={1}", new Object [] { timestamp, info.getFileObject() });
                
                //possibly incorrect badges. require to be re-run, to ensure RepositoryUpdater has finished its work:
                WORKER.post(new Runnable() {
                    public void run() {
                        factory.rescheduleImpl(file);
                    }
                }, 2 * IndexingController.getDefault().getFileLocksDelay());
                
                return ;
            }

            long lastModified = file.lastModified().getTime();
            if (timestamp != 0 && timestamp != lastModified) {
                //modified since last check, ignore
                LOG.log(Level.FINE, "File modified since last check: {0}, timestamp={1}, lastModified={2}, invocationCount={3}",
                        new Object [] { info.getFileObject(), timestamp, lastModified, invocationCount });
                return ;
            }
            
            LOG.log(Level.WARNING, "Incorrect error badges detected, file={0}.",
                    FileUtil.getFileDisplayName(file));

            ClassPath sourcePath = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
            FileObject root = sourcePath.findOwnerRoot(file);

            if (root == null) {
                LOG.log(Level.WARNING, "The file is not on its own source classpath, ignoring.");
                return;
            }

            if (!PathRegistry.getDefault().isKnownRoot(root.getURL())) {
                LOG.log(Level.WARNING, "Not PathRegistry controlled root: " + root);
                return;
            }

            LOG.log(Level.WARNING, "Going to recompute root={0}, files in error={1}.",
                    new Object[] {FileUtil.getFileDisplayName(root), ErrorsCache.getAllFilesInError(root.getURL())});

            IndexingManager.getDefault().refreshIndex(root.getURL(), null, true, true);
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
        }
    }
    
    private static final RequestProcessor WORKER = new RequestProcessor(IncorrectErrorBadges.class.getName());

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory {

        public FactoryImpl() {
            super(Phase.UP_TO_DATE, Priority.MIN);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new IncorrectErrorBadges(this);
        }
        
        void rescheduleImpl(FileObject file) {
            reschedule(file);
        }
    }
    
}
