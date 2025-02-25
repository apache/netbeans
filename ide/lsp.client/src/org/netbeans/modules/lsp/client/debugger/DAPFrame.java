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
package org.netbeans.modules.lsp.client.debugger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.eclipse.lsp4j.debug.StackFrame;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.lsp.client.debugger.DAPDebugger.URIPathConvertor;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFrame;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;

import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.Line;

public final class DAPFrame implements DVFrame {

    private static final Logger LOGGER = Logger.getLogger(DAPFrame.class.getName());

    private final URIPathConvertor fileConvertor;
    private final DAPThread thread;
    private final StackFrame frame;

    public DAPFrame(URIPathConvertor fileConvertor, DAPThread thread, StackFrame frame) {
        this.fileConvertor = fileConvertor;
        this.thread = thread;
        this.frame = frame;
    }

    @Override
    public String getName() {
        String name = frame.getName();

        if (name.length() > 100) {
            name = name.substring(0, 100) + "...";
        }

        return name;
    }

    @Override
    public DVThread getThread() {
        return thread;
    }

    @Override
    public void makeCurrent() {
        thread.setCurrentFrame(this);
    }

    @Override
    public URI getSourceURI() {
        //XXX: frame.getSource().getPath() may not work(!)
        if (frame.getSource() == null || frame.getSource().getPath() == null) {
            return null;
        }
        return fileConvertor.toURI(frame.getSource().getPath());
    }

    @Override
    public int getLine() {
        return frame.getLine();
    }

    @Override
    public int getColumn() {
        return -1;//TODO
    }

    @CheckForNull
    public Line location() {
        if (frame.getLine() == 0) {
            return null;
        }

        URI sourceURI = getSourceURI();
        if (sourceURI == null) {
            return null;
        }
        FileObject file;
        try {
            if (!sourceURI.isAbsolute()) {
                return null;
            }

            file = URLMapper.findFileObject(sourceURI.toURL());
        } catch (MalformedURLException ex) {
            return null;
        }
        if (file == null) {
            return null;
        }
        LineCookie lc = file.getLookup().lookup(LineCookie.class);
        return lc.getLineSet().getOriginal(frame.getLine() - 1);
    }

    public int getId() {
        return frame.getId();
    }

    public String getDescription() {
        return getName(); //TODO!!
    }

}
