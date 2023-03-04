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
package org.netbeans.modules.debugger.jpda.ui.debugging;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.VMCannotBeModifiedException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.ui.models.CallStackNodeModel;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
public final class JPDADVFrame implements DVFrame {

    private final DVThread thread;
    private final CallStackFrame stackFrame;

    JPDADVFrame(DVThread thread, CallStackFrame stackFrame) {
        this.thread = thread;
        this.stackFrame = stackFrame;
    }

    public CallStackFrame getCallStackFrame() {
        return stackFrame;
    }

    @Override
    public String getName() {
        String name = CallStackNodeModel.getCSFName(((JPDAThreadImpl) stackFrame.getThread()).getDebugger().getSession(), stackFrame, false);
        int colon = name.lastIndexOf(':');
        if (colon > 0 && hasDigitsOnly(name.substring(colon + 1))) {
            name = name.substring(0, colon);
        }
        name += "()";
        return name;
    }

    private static boolean hasDigitsOnly(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return !string.isEmpty();
    }

    @Override
    public DVThread getThread() {
        return thread;
    }

    @Override
    public void makeCurrent() {
        stackFrame.makeCurrent();
    }

    @Override
    public URI getSourceURI() {
        JPDADebuggerImpl debugger = ((JPDAThreadImpl) stackFrame.getThread()).getDebugger();
        Session session = debugger.getSession();
        String language = session.getCurrentLanguage();
        SourcePath sourcePath = debugger.getEngineContext();
        String url = null;
        try {
            url = sourcePath.getURL(stackFrame, language);
        } catch (InternalExceptionWrapper | InvalidStackFrameExceptionWrapper | ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper e) {
            // url stays null
        }
        if (url != null) {
            try {
                return new URI(url);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public int getLine() {
        return stackFrame.getLineNumber(stackFrame.getDefaultStratum());
    }

    @Override
    public int getColumn() {
        return -1;
    }

    @Override
    public void popOff() throws DebuggingView.PopException {
        try {
            stackFrame.popFrame();
        } catch (VMCannotBeModifiedException ex) {
            throw new UnsupportedOperationException(ex.getLocalizedMessage());
        } catch (InvalidStackFrameException | IllegalArgumentException | NativeMethodException ex) {
            throw new DebuggingView.PopException(ex.getLocalizedMessage());
        }
    }
}
