/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 * Parser task, which listens on css parsing and tries to update running webKit
 * browser.
 * @author Jan Becicka
 */
class CSSUpdaterTask extends ParserResultTask<ParserResult> {
    
    @Override
    public void run(ParserResult result, SchedulerEvent event) {
        final CSSUpdater updater = CSSUpdater.getDefault();
        if (!updater.isStarted()) {
            return;
        }
        String sourceMimeType = result.getSnapshot().getSource().getMimeType();
        if (!hasFatalErrors(result) && "text/css".equals(sourceMimeType)) { // NOI18N
            updater.update(result.getSnapshot().getSource().getFileObject(), result.getSnapshot().getText().toString());
        }
    }

    @Override
    public int getPriority() {
        //Everything else has higher priority
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    private boolean hasFatalErrors(ParserResult result) {
        for (org.netbeans.modules.csl.api.Error err : result.getDiagnostics()) {
            if (err.getSeverity() == Severity.FATAL) {
                return true;
            }
        }
        return false;
    }

    @MimeRegistration(mimeType = "text/css", service = TaskFactory.class) // NOI18N
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();
            if (mimeType.equals("text/css")) { // NOI18N
                return Collections.singletonList(new CSSUpdaterTask());
            } else {
                return Collections.emptyList();
            }
        }
    }
    
}
