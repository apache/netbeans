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
package org.netbeans.modules.docker.ui.output;

import org.netbeans.modules.docker.api.StatusEvent;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class StatusOutputListener implements StatusEvent.Listener {

    private final InputOutput io;

    public StatusOutputListener(InputOutput io) {
        this.io = io;
    }

    @Override
    public void onEvent(StatusEvent event) {
        StringBuilder sb = new StringBuilder();
        if (event.getId() != null) {
            sb.append(event.getId()).append(": ");
        }
        sb.append(event.getMessage());
        if (event.getProgress() != null) {
            sb.append(" ").append(event.getProgress());
        }
        if (event.isError()) {
            io.getErr().println(sb.toString());
        } else {
            io.getOut().println(sb.toString());
        }
    }

}
