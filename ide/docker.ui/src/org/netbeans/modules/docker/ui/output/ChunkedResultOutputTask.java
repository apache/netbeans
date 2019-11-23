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
package org.netbeans.modules.docker.ui.output;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.api.ActionChunkedResult;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class ChunkedResultOutputTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ChunkedResultOutputTask.class.getName());

    private final RequestProcessor requestProcessor = new RequestProcessor(ChunkedResultOutputTask.class);

    private final InputOutput io;

    private final ActionChunkedResult logResult;

    public ChunkedResultOutputTask(InputOutput io, ActionChunkedResult logResult) {
        this.io = io;
        this.logResult = logResult;
    }

    public Future start() {
        return requestProcessor.submit(this);
    }

    @Override
    public void run() {
        ActionChunkedResult.Chunk r;
        try {
            while ((r = logResult.fetchChunk()) != null) {
                if (r.isError()) {
                    io.getErr().print(r.getData());
                } else {
                    io.getOut().print(r.getData());
                }
            }
        } finally {
            close();
        }
    }

    private void close() {
        io.getOut().close();
        try {
            logResult.close();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }
}
