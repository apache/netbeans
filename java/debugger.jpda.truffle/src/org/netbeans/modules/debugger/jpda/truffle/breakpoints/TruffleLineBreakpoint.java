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

package org.netbeans.modules.debugger.jpda.truffle.breakpoints;

import java.beans.PropertyChangeListener;
import java.net.URL;

import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

public class TruffleLineBreakpoint extends JSLineBreakpoint {

    private volatile boolean suspend = true;
    private volatile String printText;

    public TruffleLineBreakpoint(EditorLineHandler lineHandler) {
        super(lineHandler);
    }

    public TruffleLineBreakpoint(URL url, int lineNumber) {
        this(getEditorLineHandler(url, lineNumber));
    }

    /**
     * Test whether the breakpoint suspends execution when hit.
     */
    public final boolean isSuspend() {
        return suspend;
    }

    /**
     * Set whether the breakpoint should suspend execution when hit.
     */
    public final void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    /**
     * Get a logging text that is printed to an output console when the breakpoint is hit.
     */
    public final String getPrintText() {
        return printText;
    }

    /**
     * Set a logging text that is printed to an output console when the breakpoint is hit.
     */
    public final void setPrintText(String printText) {
        this.printText = printText;
    }

    private static EditorLineHandler getEditorLineHandler(URL url, int lineNumber) {
        EditorLineHandler handler;
        if (Lookup.getDefault().lookup(EditorLineHandlerFactory.class) != null) {
            handler = EditorLineHandlerFactory.getHandler(url, lineNumber);
        } else {
            handler = new FixedLineHandler(url, lineNumber);
        }
        return handler;
    }

    private static final class FixedLineHandler implements EditorLineHandler {

        private final URL url;
        private int lineNumber;

        FixedLineHandler(URL url, int lineNumber) {
            this.url = url;
            this.lineNumber = lineNumber;
        }

        @Override
        public FileObject getFileObject() {
            return URLMapper.findFileObject(url);
        }

        @Override
        public URL getURL() {
            return url;
        }

        @Override
        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pchl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pchl) {
        }
    }
}
