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
package org.netbeans.modules.bugtracking.tasks;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;

/**
 *
 * @author jpeska
 */
public class DashboardTransferable implements Transferable {

    private static final String FLAVOR_NAME = "TaskNode";
    private TaskNode[] taskNodes;
    protected static final DataFlavor taskFlavor = new DataFlavor(TaskNode.class, FLAVOR_NAME);

    public DashboardTransferable(TaskNode... taskNodes) {
        this.taskNodes = taskNodes;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{taskFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(FLAVOR_NAME);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.getHumanPresentableName().equals(FLAVOR_NAME)) {
            return taskNodes;
        }
        return null;
    }
}
