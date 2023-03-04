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
