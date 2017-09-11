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

package org.netbeans.modules.java.source.usages;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavaSourceSupportAccessor;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position=90)
public class AllRefactoringsPluginFactory implements RefactoringPluginFactory {

    private static final Logger LOGGER = Logger.getLogger(AllRefactoringsPluginFactory.class.getName());
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        return new RefactoringPluginImpl();
    }
    
    private static final class RefactoringPluginImpl implements RefactoringPlugin {

        public Problem preCheck() {
            return null;
        }

        public Problem checkParameters() {
            return null;
        }

        public Problem fastCheckParameters() {
            return null;
        }

        public void cancelRequest() {}

        public Problem prepare(RefactoringElementsBag refactoringElements) {
            refactoringElements.getSession().addProgressListener(new ProgressListener() {
                public void start(ProgressEvent event) {
                    LOGGER.log(Level.FINE, "Refactoring started, locking RepositoryUpdater");
//                    RepositoryUpdater.getDefault().lockRU();
                    IndexingController.getDefault().enterProtectedMode();
                }
                public void step(ProgressEvent event) {}
                public void stop(ProgressEvent event) {
                    LOGGER.log(Level.FINE, "Refactoring finished, unlocking RepositoryUpdater");
//                    RepositoryUpdater.getDefault().unlockRU(new Runnable() {
                    IndexingController.getDefault().exitProtectedMode(new Runnable() {
                        public void run() {
                            LOGGER.log(Level.FINE, "Refreshing editor panes:");
                            for (FileObject f : JavaSourceSupportAccessor.ACCESSOR.getVisibleEditorsFiles()) {
                                JavaSource source = JavaSource.forFileObject(f);
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    LOGGER.log(Level.FINE, "Refreshing file={0}, JavaSource={1}", new Object[] {f, source});
                                }
                                if (source != null) {
                                    JavaSourceAccessor.getINSTANCE().revalidate(source);
                                }
                            }
                            LOGGER.log(Level.FINE, "done.");
                        }
                    });
                }
            });
            
            return null;
        }
        
    }

}
