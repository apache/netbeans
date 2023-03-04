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
package org.netbeans.modules.nativeexecution.pty;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.modules.nativeexecution.NativeProcessInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
public final class IOConnector {

    private static final RequestProcessor rp = new RequestProcessor("IOConnectorImpl", 2); // NOI18N
    private static final IOConnector instance = new IOConnector();

    private IOConnector() {
    }

    public static IOConnector getInstance() {
        return instance;
    }
    
    public boolean connect(InputOutput io, NativeProcess process, Runnable postConnectRunnabel) {
        if (!IOTerm.isSupported(io)) {
            return false;
        }

        if (IOResizable.isSupported(io)) {
            String tty = PtySupport.getTTY(process);
            if (tty != null) {
                try {
                    IONotifier.addPropertyChangeListener(io, new ResizeListener(process.getExecutionEnvironment(), tty));
                } catch (CancellationException ex) {
                    // TODO:CancellationException error processing
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }        

        IOTerm.connect(io, process.getOutputStream(), process.getInputStream(), 
                process.getErrorStream(), NativeProcessInfo.getCharset(process), postConnectRunnabel);
        return true;
    }

    public boolean connect(final InputOutput io, final NativeProcess process) {
        return connect(io, process, null);
    }

    public boolean connect(final InputOutput io, final Pty pty) {
        if (pty == null || io == null) {
            throw new NullPointerException();
        }

        if (!IOTerm.isSupported(io)) {
            return false;
        }

        IOTerm.connect(io, pty.getOutputStream(), pty.getInputStream(), pty.getErrorStream(), null);

        if (IOResizable.isSupported(io)) {
            try {
                IONotifier.addPropertyChangeListener(io, new ResizeListener(pty.getEnv(), pty.getSlaveName()));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                // TODO:CancellationException error processing
            }
        }

        return true;
    }

    private static class ResizeListener implements PropertyChangeListener {

        private Task task = null;
        private Dimension cells;
        private Dimension pixels;
        private final boolean pxlsAware;

        ResizeListener(final ExecutionEnvironment env, final String tty) throws IOException, CancellationException {
            final HostInfo hinfo = HostInfoUtils.getHostInfo(env);

            if (OSFamily.SUNOS.equals(hinfo.getOSFamily())) {
                pxlsAware = true;

                // See IZ 192063  - Input is duplicated in internal terminal
                // See CR 7009510 - Changing winsize (SIGWINCH) of pts causes entered text duplication

                // In case OpenSolaris/Solaris 11 will not react on window size
                // change... This causes 'problems' with, say, vi started
                // in the internal terminal... But 'solves' problems with input
                // duplication, which is more important...

                String version = hinfo.getOS().getVersion();
                if (version.contains("Solaris 11.")) { // NOI18N
                    // update for IZ 236261: in Solaris 11+ this seems to work, so
                    // will not disable listener for it...

                    // update: the same is valid for 11.2
                    // assuming it was fixed in 11 - disabling this fix for 11.*
                } else if (version.contains("OpenSolaris") || version.contains("Solaris 11")) { // NOI18N
                    return;
                }
            } else {
                pxlsAware = false;
            }

            this.task = rp.create(new Runnable() {

                @Override
                public void run() {
                    Dimension c, p;

                    synchronized (ResizeListener.this) {
                        c = new Dimension(cells);
                        p = new Dimension(pixels);
                    }

                    String cmd = pxlsAware
                            ? String.format("cols %d rows %d xpixels %d ypixels %d", c.width, c.height, p.width, p.height) // NOI18N
                            : String.format("cols %d rows %d", c.width, c.height); // NOI18N
                    
                    SttySupport.apply(env, tty, cmd);
                }
            }, true);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (task != null && IOResizable.PROP_SIZE.equals(evt.getPropertyName())) {
                IOResizable.Size newVal = (IOResizable.Size) evt.getNewValue();
                if (newVal != null) {
                    Dimension newCells = newVal.cells;
                    Dimension newPixels = newVal.pixels;
                    if (newCells == null || newPixels == null) {
                        throw new NullPointerException();
                    }

                    if (newCells.equals(this.cells) && newPixels.equals(this.pixels)) {
                        return;
                    }

                    this.cells = new Dimension(newCells);
                    this.pixels = new Dimension(newPixels);
                    task.schedule(1000);
                }
            }
        }
    }
}
