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

package org.netbeans.progress.module;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.progress.spi.ExtractedProgressUIWorker;
import org.netbeans.modules.progress.spi.ProgressEvent;
import org.netbeans.modules.progress.spi.ProgressUIWorkerProvider;
import org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.modules.progress.spi.TaskModel;

/**
 * Fallback provider in case no GUI is registered.
 * Just enough to make unit tests run without errors, etc.
 * @author Jesse Glick
 * @see "issue #87812"
 */
public class TrivialProgressUIWorkerProvider implements ProgressUIWorkerProvider, ProgressUIWorkerWithModel, ExtractedProgressUIWorker {

    public TrivialProgressUIWorkerProvider() {}

    public ProgressUIWorkerWithModel getDefaultWorker() {
        return this;
    }

    public ExtractedProgressUIWorker getExtractedComponentWorker() {
        return this;
    }

    public void setModel(TaskModel model) {}

    public void showPopup() {}

    public void processProgressEvent(ProgressEvent event) {}

    public void processSelectedProgressEvent(ProgressEvent event) {}

    public JComponent getProgressComponent() {
        return new JPanel();
    }

    public JLabel getMainLabelComponent() {
        return new JLabel();
    }

    public JLabel getDetailLabelComponent() {
        return new JLabel();
    }

}
