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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.error;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
    @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
    @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
})
public final class HighlightProviderTaskFactory extends TaskFactory {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N

    @Override
    public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        return Collections.singletonList(new ErrorsHighlighter());
    }

    private static final class ErrorsHighlighter extends IndexingAwareParserResultTask<Parser.Result> {
        private final CancelSupport cancel = CancelSupport.create(this);
        private InterrupterImpl interrupter = new InterrupterImpl();
        private Parser.Result lastParserResult;

        public ErrorsHighlighter() {
            super(TaskIndexingMode.ALLOWED_DURING_SCAN);
        }

        @Override
        public void run(Parser.Result result, SchedulerEvent event) {
            synchronized(this) {
                if (lastParserResult == result) {
                    return;
                }
                interrupter.cancel();
                this.interrupter = new InterrupterImpl();
                if (cancel.isCancelled()) {
                    lastParserResult = null;
                    return;
                } else {
                    this.lastParserResult = result;
                }
            }
            long time = 0;
            try {
                final FileObject fo = result.getSnapshot().getSource().getFileObject();
                if (fo == null) {
                    return;
                }
                final CsmFile csmFile = CsmFileInfoQuery.getDefault().getCsmFile(result);
                if (csmFile == null) {
                    return;
                }
                final Document doc = result.getSnapshot().getSource().getDocument(false);
                if (doc == null) {
                    return;
                }
                DataObject dobj = DataObject.find(fo);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "HighlightProviderTaskFactory started"); //NOI18N
                    time = System.currentTimeMillis();
                }
                HighlightProvider.getInstance().update(csmFile, doc, dobj, interrupter);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "HighlightProviderTaskFactory finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
            }
        }

        @Override
        public void cancel() {
            synchronized(this) {
                interrupter.cancel();
                lastParserResult = null;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "HighlightProviderTaskFactory canceled"); //NOI18N
            }
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public int getPriority() {return 3000;}
    }
}
