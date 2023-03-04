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
package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import java.io.InvalidObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.PopException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
        SourcePosition sourcePosition = truffleFrame.getSourcePosition();
        if (sourcePosition == null) {
            return null;
        }
        Source source = sourcePosition.getSource();
        URL url = source.getUrl();
        URI uri;
        if (url != null) {
            try {
                uri = url.toURI();
            } catch (URISyntaxException ex) {
                uri = source.getURI();
            }
        } else {
            uri = source.getURI();
        }
        return uri;
    }

    @Override
    public String getSourceMimeType() {
        SourcePosition sourcePosition = truffleFrame.getSourcePosition();
        if (sourcePosition != null) {
            Source source = sourcePosition.getSource();
            return source.getMimeType();
        } else {
            return null;
        }
    }

    @Override
    public int getLine() {
        SourcePosition sourcePosition = truffleFrame.getSourcePosition();
        if (sourcePosition != null) {
            return sourcePosition.getStartLine();
        } else {
            return -1;
        }
    }

    @Override
    public int getColumn() {
        SourcePosition sourcePosition = truffleFrame.getSourcePosition();
        if (sourcePosition != null) {
            return sourcePosition.getStartColumn();
        } else {
            return -1;
        }
    }

    @Override
    @NbBundle.Messages("MSG_FramePopFailed=Pop of the stack frame has failed.")
    public void popOff() throws PopException {
        JPDADebugger debugger = truffleFrame.getDebugger();
        JPDAThread tr = truffleFrame.getThread();
        int depth = truffleFrame.getDepth();
        boolean unwindScheduled = TruffleAccess.unwind(debugger, tr, depth);
        if (unwindScheduled) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentGuestPCInfo(tr);
            try {
                currentPCInfo.getStepCommandVar().setFromMirrorObject(-1);
                tr.resume();
            } catch (InvalidObjectException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            throw new PopException(Bundle.MSG_FramePopFailed());
        }
    }

}
