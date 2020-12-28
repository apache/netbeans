/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.python.qshell.richexecution;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Pseudo Terminal.
 * <br>
 * See <a href="http://wiki.netbeans.org/TerminalEmulatorPty">
 * TerminalEmulatorPty</a> for a full description of
 * what a pty is.
 * @author Ivan Soleimanipour
 */
public abstract class Pty {
    /**
     * Describes the kind of pty.
     */
    public static enum Mode {
        /**
         * To be used by client to signify that no pty shall be used.
         */
        NONE,

        /**
         * <b>NOT IMPLEMENTED YET</b>.
         * <br>
         * A raw pty is effectively the same as a pipe ... and as useless.
         */
        RAW,

        /**
         * A standard pty.
         * <br>
         * A regular pty provides:
         * <ul>
         * <li> Line buffering.
         * <li> BS and TAB handling.
         * <li> Control character handling (^C, ^Z, ^D etc).
         * <li> Terminal size propagation.
         * </ul>
         */
        REGULAR,

        /**
         * <b>NOT IMPLEMENTED YET</b>.
         * <br>
         * An extended functionality pty.
         */
        PACKET
    };

    protected FileDescriptor master_fd = new FileDescriptor();
    protected FileDescriptor slave_fd = new FileDescriptor();
    protected String slave_name = null;
    InputStream inputStream;
    OutputStream outputStream;

    private Mode mode;

    public static Pty create(Mode mode) throws PtyException {
        return new JNAPty(mode);
    }

    /**
     * Create a Pty in the given mode.
     * <p>
     * Pty's are a kind of enhanced "named fifos/pipes". The enhancement
     * has to do with terminal related stuff, line discipline and
     * all that. Otherwise they are just conduits of information.
     * In fact that is just what RAW Pty's are.
     * <p>
     * Now there's gotta be something similar to "named fifos" on
     * Windows ... SHOULD look for it.
     * <br>
     * Until then, even if mode is NONE or RAW, we really can't create
     * Ptys on Windows.
     * <p>
     * Note that {@link PtyProcess} and all the higher level stuff that is
     * based on it still works on Windows ... it uses direct Process streams
     * rather than Pty's.
     */
    protected Pty(Mode mode) throws PtyException {
	if (OS.get() == OS.WINDOWS) {
	    throw new UnsupportedOperationException("Pty's not supported on windows");
	}
        this.mode = mode;
        setup();
    }

    /**
     * Construct the actual physical pty.
     * @throws pty.PtyException
     */
    protected abstract void setup() throws PtyException;

    /**
     * Declare the new size of the "terminal" on the master side of this pty.
     * <br>
     * This sends a SIGWINCH to the process on the master side of the pty.
     * The process may then issue ioctl(TIOCGWINSZ, ...) and update it's 
     * idea of the terminal size.
     */
    public abstract void masterTIOCSWINSZ(int rows, int cols, int height, int width);

    /**
     * Declare the new size of the "terminal" on the slave side of this pty.
     * <br>
     * This sends a SIGWINCH to the process on the slave side of the pty.
     * The process may then issue ioctl(TIOCGWINSZ, ...) and update it's 
     * idea of the terminal size.
     */
    public abstract void slaveTIOCSWINSZ(int rows, int cols, int height, int width);

    /**
     * Return the mode the pty was created in.
     * @return the mode the pty was created in.
     */
    protected Mode mode() {
        return mode;
    }

    /**
     * Return the slave end of the pty which should be opened for reading and
     * writing by the child process.
     * <br>
     * The pty slave is a file like <code>/dev/pts14</code>.
     * @return The slave end of the pty which should be opened for reading and
     * writing by the child process.
     */
    public String slaveName() {
        return slave_name;
    }

    /**
     * Close the slave file descriptor.
     * <br>
     * We don't close the master fd; it usually gets closed when it's
     * reading InputStream gets closed.
     * <br>
     * Until we call this the reader of the master side (a terminal) will
     * not see an EOF, even if the child process has exited. The reason
     * for this is that we maintain a file desriptor for the slave in
     * addition to the one dup'ed for the child process.
     * <br>
     * And the reason for _that_ is that it allows us to manipulate terminal
     * characteristics.
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        // On linux it seems one can manipulate terminal characteristics
        // through the master fd, hence masterTIOCSWINSZ(). So perhaps keeping
        // a slave fd around is pointless.
        // On Solaris it used to be that you could manipulate only
        // from the slave end. Needs more experimentation.

        // Can't close fd's directly so create a temporary stream
        (new FileOutputStream(slave_fd)).close();
    }

    /**
     * Return an InputStream to be connected to a terminal.
     * @return An InputStream to be connected to a terminal.
     */
    public InputStream getInputStream() {
        openMasterSide();
        return inputStream;
    }

    /**
     * Return an OutputStream to be connected to a terminal.
     * @return An OutputStream to be connected to a terminal.
     */
    public OutputStream getOutputStream() {
        openMasterSide();
        return outputStream;
    }

    private void openMasterSide() {
        if (inputStream == null) {
            outputStream = new FileOutputStream(master_fd);
            inputStream = new FileInputStream(master_fd);
        }
    }
}
