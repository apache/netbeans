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

package org.netbeans.modules.csl.navigation;

import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.*;

/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 * <p>
 * This task is called every time the caret position changes in a GSF editor.
 * It delegates to the navigator to show the current selection.
 */
public final class CaretListeningTask extends IndexingAwareParserResultTask<ParserResult> {
    
    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
    
    CaretListeningTask() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {

        boolean navigatorShouldUpdate = ClassMemberPanel.getInstance() != null; // XXX set by navigator visible
        if (cancel.isCancelled() || (!navigatorShouldUpdate) || !(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }

        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            int offset = ((CursorMovedSchedulerEvent) event).getCaretOffset();
            if (offset != -1) {
                ClassMemberPanel.getInstance().selectElement(result, offset);
            }
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
    }


    @Override
    public final synchronized void cancel() {
    }

    public @Override int getPriority() {
        return Integer.MAX_VALUE;
    }

    public @Override Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }
}
