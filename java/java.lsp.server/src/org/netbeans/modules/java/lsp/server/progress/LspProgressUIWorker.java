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
package org.netbeans.modules.java.lsp.server.progress;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.modules.progress.spi.TaskModel;

/**
 *
 * @author sdedic
 */
public class LspProgressUIWorker implements ProgressUIWorkerWithModel{
    private static final Logger LOG = Logger.getLogger(LspProgressUIWorker.class.getName());
    
    private TaskModel taskModel;
    
    public LspProgressUIWorker() {
    }

    @Override
    public void setModel(TaskModel model) {
        this.taskModel = model;
    }

    @Override
    public void showPopup() {
        // ??
    }

    @Override
    public void processProgressEvent(ProgressEvent event) {
        InternalHandle h = event.getSource();
        if (!(h instanceof LspInternalHandle)) {
            // sorry ...
            return;
        }
        LspInternalHandle lsHandle = (LspInternalHandle)h;
        
        switch (event.getType()) {
            case ProgressEvent.TYPE_START:
                lsHandle.sendStartMessage(event);
                break;
                
            case ProgressEvent.TYPE_SWITCH:
            case ProgressEvent.TYPE_PROGRESS:
                lsHandle.sendProgress(event);
                break;
                
            case ProgressEvent.TYPE_SILENT:
            case ProgressEvent.TYPE_REQUEST_STOP:
                // ignore
                break;
                
            case ProgressEvent.TYPE_FINISH:
                lsHandle.sendFinish(event);
                break;
            default:
                LOG.log(Level.INFO, "Unexpected progress event type for {0}: {1}", new Object[] {
                    lsHandle, event.getType()
                });
                break;
        }
    }

    @Override
    public void processSelectedProgressEvent(ProgressEvent event) {
        // No support from LSP / VScode UI. Most probably can be handled
        // on the client side, if it stacks progresses somehow.
    }
    
}
