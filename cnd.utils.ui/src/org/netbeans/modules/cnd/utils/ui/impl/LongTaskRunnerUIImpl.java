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
package org.netbeans.modules.cnd.utils.ui.impl;

import java.awt.Frame;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.spi.utils.LongTaskRunner;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.openide.util.Cancellable;
import org.openide.windows.WindowManager;

/**
 *
 */
public class LongTaskRunnerUIImpl extends LongTaskRunner {

    LongTaskRunnerUIImpl(Runnable offEDTTask, Runnable postEDTTask) {
        super(offEDTTask, postEDTTask);
    }

    @Override
    public void runLongTask(String title, String message, Cancellable canceller) {
        if (SwingUtilities.isEventDispatchThread()) {
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            ModalMessageDlg.runLongTask(mainWindow, title, message, new ModalMessageDlg.LongWorker() {
                @Override
                public void doWork() {
                    getOffEDTRunner().run();
                }

                @Override
                public void doPostRunInEDT() {
                    getPostEDTRunner().run();
                }
            }, null);
        } else {
            getOffEDTRunner().run();
            getPostEDTRunner().run();
            
        }
    }

}
