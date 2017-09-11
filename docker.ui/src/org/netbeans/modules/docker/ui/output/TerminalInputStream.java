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

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.terminal.api.IOConnect;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class TerminalInputStream extends FilterInputStream {

    private static final Logger LOGGER = Logger.getLogger(TerminalInputStream.class.getName());

    private final InputOutput io;

    private final Closeable[] close;

    public TerminalInputStream(InputOutput io, InputStream in, Closeable... close) {
        super(in);
        this.io = io;
        this.close = close;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int i = super.read(b, off, len);
            if (i < 0) {
                closeTerminal();
            }
            return i;
        } catch (IOException ex) {
            closeTerminal();
            throw ex;
        }
    }

    @Override
    public int read() throws IOException {
        try {
            int i = super.read();
            if (i < 0) {
                closeTerminal();
            }
            return i;
        } catch (IOException ex) {
            closeTerminal();
            throw ex;
        }
    }

    private void closeTerminal() {
        for (Closeable c : close) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        // disconnect all is needed as we call getOut().reset()
        // because of that getOut()
        if (IOConnect.isSupported(io)) {
            IOConnect.disconnectAll(io, null);
        }
        //IOTerm.disconnect(io, null);
        //LOGGER.log(Level.INFO, "Closing terminal", new Exception());
    }

}
