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
package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import java.net.URI;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;

/**
 *
 * @author martin
 */
public final class TruffleDVFrame implements DVFrame {

    private final DVThread thread;
    private final TruffleStackFrame truffleFrame;

    TruffleDVFrame(DVThread thread, TruffleStackFrame truffleFrame) {
        this.thread = thread;
        this.truffleFrame = truffleFrame;
    }

    TruffleStackFrame getTruffleFrame() {
        return truffleFrame;
    }

    @Override
    public String getName() {
        return truffleFrame.getMethodName();
    }

    @Override
    public DVThread getThread() {
        return thread;
    }

    @Override
    public void makeCurrent() {
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(truffleFrame.getThread());
        if (currentPCInfo != null) {
            currentPCInfo.setSelectedStackFrame(truffleFrame);
        }
    }

    @Override
    public URI getSourceURI() {
        return truffleFrame.getSourcePosition().getSource().getURI();
    }

    @Override
    public int getLine() {
        return truffleFrame.getSourcePosition().getStartLine();
    }

    @Override
    public int getColumn() {
        return truffleFrame.getSourcePosition().getStartColumn();
    }

}
