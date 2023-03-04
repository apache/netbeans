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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class TerminalResizeListener implements PropertyChangeListener, Closeable {

    private static final Logger LOGGER = Logger.getLogger(TerminalResizeListener.class.getName());

    private static final int RESIZE_DELAY = 500;

    private static final RequestProcessor RP = new RequestProcessor(TerminalResizeListener.class);

    private final InputOutput io;

    private final DockerContainer container;

    private final RequestProcessor.Task task;

    // GuardedBy("this")
    private Dimension value;

    private boolean initial = true;

    public TerminalResizeListener(InputOutput io, DockerContainer container) {
        this.io = io;
        this.container = container;
        this.task = RP.create(new Runnable() {
            @Override
            public void run() {
                Dimension newValue;
                synchronized (TerminalResizeListener.this) {
                    newValue = value;
                }
                DockerAction remote = new DockerAction(TerminalResizeListener.this.container.getInstance());
                try {
                    remote.resizeTerminal(TerminalResizeListener.this.container, newValue.height, newValue.width);
                } catch (DockerException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (IOResizable.PROP_SIZE.equals(evt.getPropertyName())) {
            IOResizable.Size newVal = (IOResizable.Size) evt.getNewValue();
            synchronized (this) {
                value = newVal.cells;
            }
            if (initial) {
                initial = false;
                task.schedule(0);
            } else {
                task.schedule(RESIZE_DELAY);
            }
        }
    }

    @Override
    public void close() throws IOException {
        task.cancel();
        if (IONotifier.isSupported(io)) {
            IONotifier.removePropertyChangeListener(io, this);
        }
    }

}
