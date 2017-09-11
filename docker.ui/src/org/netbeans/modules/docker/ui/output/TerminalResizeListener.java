/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
