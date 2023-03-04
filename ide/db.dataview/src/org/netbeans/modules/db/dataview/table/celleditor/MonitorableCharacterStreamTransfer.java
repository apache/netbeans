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
package org.netbeans.modules.db.dataview.table.celleditor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.openide.util.Cancellable;

class MonitorableCharacterStreamTransfer implements ProgressRunnable<Exception>, Cancellable {

    private Reader in;
    private Writer out;
    private int transfered;
    private Integer size;
    private boolean cancel;

    public MonitorableCharacterStreamTransfer(Reader in, Writer out, Integer knownsize) {
        this.in = in;
        this.out = out;
        size = knownsize;
    }

    @Override
    public Exception run(ProgressHandle handle) {
        Exception result = null;
        if (handle != null && size != null) {
            handle.switchToDeterminate(size);
        }
        try {
            int read = 0;
            char[] buffer = new char[128 * 1024];
            while ((read = in.read(buffer)) > 0 && !cancel) {
                out.write(buffer, 0, read);
                transfered += read;
                if (handle != null && size != null) {
                    handle.progress(transfered);
                }
            }
        } catch (IOException ex) {
            try {
                in.close();
            } catch (IOException ex2) {
            }
            try {
                out.close();
            } catch (IOException ex2) {
            }
            return ex;
        }
        if (handle != null) {
            handle.finish();
        }
        return result;
    }

    @Override
    public boolean cancel() {
        if (cancel) {
            return false;
        }
        this.cancel = true;
        return true;
    }

    public boolean isCancel() {
        return cancel;
    }
}
