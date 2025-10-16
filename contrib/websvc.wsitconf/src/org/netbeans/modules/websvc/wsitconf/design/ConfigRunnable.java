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

package org.netbeans.modules.websvc.wsitconf.design;

import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class ConfigRunnable implements Runnable {

    private ProgressHandle progressHandle = ProgressHandle.createHandle(null);
    final ProgressPanel progressPanel = new ProgressPanel(
            NbBundle.getMessage(MtomConfiguration.class, "LBL_Wait")); //NOI18N

    boolean stop = false;
    boolean started = false;

    public void run() {
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
        if (!stop) {
            progressHandle.start();
            started = true;
            progressHandle.switchToIndeterminate();
            progressPanel.open(progressComponent);
        }
    }

    public void stop() {
        stop = true;
        if (started) {
            progressHandle.finish();
        }
        progressPanel.close();
    }    
}
