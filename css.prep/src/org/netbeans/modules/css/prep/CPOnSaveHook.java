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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.css.prep;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

@MimeRegistrations({
    @MimeRegistration(mimeType = "text/scss", service = OnSaveTask.Factory.class, position = 2000),
    @MimeRegistration(mimeType = "text/sass", service = OnSaveTask.Factory.class, position = 2000),
    @MimeRegistration(mimeType = "text/less", service = OnSaveTask.Factory.class, position = 2000),})
public class CPOnSaveHook implements OnSaveTask.Factory {

    private static final RequestProcessor RP = new RequestProcessor(CPOnSaveHook.class);
    private static final Logger LOG = Logger.getLogger(CPOnSaveHook.class.getSimpleName());
    
    @Override
    public OnSaveTask createTask(OnSaveTask.Context context) {
        Document doc = context.getDocument();
        final FileObject fileObject = DataLoadersBridge.getDefault().getFileObject(doc);
        final Project project = fileObject == null ? null : FileOwnerQuery.getOwner(fileObject);

        return project == null ? null : new OnSaveTask() {
            private boolean cancelled;

            @Override
            public void performTask() {
                if (!cancelled) {
                    //run the CssPreprocessorUtils.processSavedFile() out of the original thread
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            String mimeType = fileObject.getMIMEType();
                            CssPreprocessorType type = CssPreprocessorType.find(mimeType);
                            if (type == null) {
                                // #244470
                                LOG.log(Level.WARNING, "Cannot find CssPreprocessorType for MIME type {0} (filename: {1})", new Object[] {mimeType, fileObject.getNameExt()});
                                return;
                            }
                            CssPreprocessorUtils.processSavedFile(project, type);
                            LOG.log(Level.INFO, "processSavedFile called for {0} type on project {1}.", new Object[]{type.getDisplayName(), project.getProjectDirectory().getPath()});
                        }
                        
                    });
                }
            }

            @Override
            public void runLocked(Runnable run) {
                run.run(); //better keep running in the original thread as the tasks may be nested
            }

            @Override
            public boolean cancel() {
                return cancelled = true;
            }
        };
    }
}
