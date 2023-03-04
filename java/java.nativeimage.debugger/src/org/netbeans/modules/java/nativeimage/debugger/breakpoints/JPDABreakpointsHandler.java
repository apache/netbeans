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
package org.netbeans.modules.java.nativeimage.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.nativeimage.api.debug.NIDebugger;
import org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Gathers JPDA breakpoints and submits them to the native debugger.
 */
public class JPDABreakpointsHandler extends DebuggerManagerAdapter implements PropertyChangeListener {

    private static final String SOURCES_FOLDER = "sources"; // NOI18N

    private final File niFileSources;
    private final NIDebugger debugger;
    private final Set<JPDABreakpoint> attachedBreakpoints = new HashSet<>();

    public JPDABreakpointsHandler(File niFile, NIDebugger debugger) {
        this.niFileSources = getNativeSources(niFile);
        this.debugger = debugger;
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        dm.addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
        Breakpoint[] bs = dm.getBreakpoints();
        for (Breakpoint b : bs) {
            add(b);
        }
    }

    private static File getNativeSources(File niFile) {
        File sources = new File(niFile.getParentFile(), SOURCES_FOLDER);
        if (sources.isDirectory()) {
            return sources;
        } else {
            return null;
        }
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        add(breakpoint);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            breakpoint.removePropertyChangeListener(this);
            debugger.removeBreakpoint(breakpoint);
            synchronized (attachedBreakpoints) {
                attachedBreakpoints.remove(breakpoint);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();
        if (source instanceof JPDABreakpoint && !(Breakpoint.PROP_DISPOSED.equals(propertyName) || Breakpoint.PROP_VALIDITY.equals(propertyName))) {
            // Change of breakpoint  properties
            added((JPDABreakpoint) source);
        }
    }

    private void add(Breakpoint b) {
        if (b instanceof JPDABreakpoint && !((JPDABreakpoint) b).isHidden()) {
            JPDABreakpoint jb = (JPDABreakpoint) b;
            jb.addPropertyChangeListener(this);
            synchronized (attachedBreakpoints) {
                attachedBreakpoints.add(jb);
            }
            Breakpoint nativeBreakpoint = added(jb);
            if (nativeBreakpoint != null) {
                nativeBreakpoint.addPropertyChangeListener(Breakpoint.PROP_VALIDITY, e -> {
                    Breakpoint.VALIDITY validity = nativeBreakpoint.getValidity();
                    String validityMessage = nativeBreakpoint.getValidityMessage();
                    ((ChangeListener) jb).stateChanged(new ValidityChanger(validity, validityMessage));
                });
            }
        }
    }

    private Breakpoint added(JPDABreakpoint b) {
        if (b instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) b;
            URL url;
            try {
                url = new URL(lb.getURL());
            } catch (MalformedURLException ex) {
                return null;
            }
            String filePath = null;
            FileObject fo = URLMapper.findFileObject(url);
            for (FileObject root : GlobalPathRegistry.getDefault().getSourceRoots()) {
                if (FileUtil.isParentOf(root, fo)) {
                    String path = FileUtil.getRelativePath(root, fo);
                    filePath = SOURCES_FOLDER + File.separator + path;
                    break;
                }
            }
            if (filePath == null) {
                try {
                    filePath = new File(url.toURI()).getAbsolutePath();
                } catch (URISyntaxException ex) {
                    return null;
                }
            }
            NILineBreakpointDescriptor niBreakpointDescriptor = NILineBreakpointDescriptor.newBuilder(filePath, lb.getLineNumber())
                    .condition(lb.getCondition())
                    .enabled(lb.isEnabled())
                    .hidden(true)
                    .build();
            Object nativeBreakpoint = debugger.addLineBreakpoint(lb, niBreakpointDescriptor);
            return (Breakpoint) nativeBreakpoint;
        }
        return null;
    }

    public void dispose() {
        synchronized (attachedBreakpoints) {
            for (JPDABreakpoint jb : attachedBreakpoints) {
                jb.removePropertyChangeListener(this);
                debugger.removeBreakpoint(jb);
                ((ChangeListener) jb).stateChanged(new ValidityChanger(Breakpoint.VALIDITY.UNKNOWN, null));
            }
            attachedBreakpoints.clear();
        }
        DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
    }

    private static class ValidityChanger extends ChangeEvent {

        private final String validityMessage;

        ValidityChanger(Breakpoint.VALIDITY validity, String validityMessage) {
            super(validity);
            this.validityMessage = validityMessage;
        }

        @Override
        public String toString() {
            return validityMessage;
        }
    }
}
