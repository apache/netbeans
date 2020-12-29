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
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.Native;

/**
 * An implementation of Pty based on JNA.
 */

class JNAPty extends Pty {

    JNAPty(Mode mode) throws PtyException {
        super(mode);
    }

    private String strerror(int errno) {
        return PtyLibrary.INSTANCE.strerror(errno);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void masterTIOCSWINSZ(int rows, int cols, int height, int width) {
        if (master_fd == null)
            return;
        PtyLibrary.WinSize winsize = new PtyLibrary.WinSize(rows, cols, height, width);
        PtyLibrary.INSTANCE.ioctl(Util.getFd(master_fd), PtyLibrary.TIOCSWINSZ, winsize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void slaveTIOCSWINSZ(int rows, int cols, int height, int width) {
        if (slave_fd == null)
            return;
        PtyLibrary.WinSize winsize = new PtyLibrary.WinSize(rows, cols, height, width);
        PtyLibrary.INSTANCE.ioctl(Util.getFd(slave_fd), PtyLibrary.TIOCSWINSZ, winsize);
    }

    /**
     * {@inheritDoc}
     * @throws pty.PtyException
     */
    @Override
    public void setup() throws PtyException {
	int mfd = -1;
	int sfd = -1;

	try {
	    // The most common sequence of creating pty's 
	    //	getpt()/grantpt()/unlockpt()/pts_name(),
	    // is supported on Solaris and linux, except that Solaris doesn't
	    // define getpt(). So we do our own:
	    if (OS.get() == OS.SOLARIS || OS.get() == OS.MACOS) {
		mfd = PtyLibrary.INSTANCE.open("/dev/ptmx", PtyLibrary.O_RDWR);
		if (mfd == -1)
		    throw new PtyException("open(\"/dev/ptmx\") failed -- " + strerror(Native.getLastError()));
	    } else {
		mfd = PtyLibrary.INSTANCE.getpt();
		if (mfd == -1)
		    throw new PtyException("getpt() failed -- " + strerror(Native.getLastError()));
	    }
	    System.out.printf("Pty.setup(): getpt() returns %d\n", mfd);

	    if (PtyLibrary.INSTANCE.grantpt(mfd) == -1) {
		throw new PtyException("grantpt() failed -- " + strerror(Native.getLastError()));
	    }

	    if (PtyLibrary.INSTANCE.unlockpt(mfd) == -1) {
		throw new PtyException("unlockpt() failed -- " + strerror(Native.getLastError()));
	    }

	    // SHOULD mutex access to ptsname()s return value.
	    // Or use the _r version

	    slave_name = PtyLibrary.INSTANCE.ptsname(mfd);
	    System.out.printf("Pty.setup(): ptsname() returns '%s'\n", slave_name);

	    sfd = PtyLibrary.INSTANCE.open(slave_name, PtyLibrary.O_RDWR);
	    if (sfd == -1) {
		throw new PtyException("open(\"" + slave_name + "\") failed -- " + strerror(Native.getLastError()));
	    }

            if (mode() != Mode.RAW) {
                if (OS.get() == OS.SOLARIS) {

                    // pseudo-terminal hardware emulation module
                    if (PtyLibrary.INSTANCE.ioctl(sfd, PtyLibrary.I_PUSH, "ptem") == -1) {
                        throw new PtyException("ioctl(\"" + slave_name + "\", I_PUSH, \"ptem\") failed -- " + strerror(Native.getLastError()));
                    }

                    // standard terminal line discipline
                    if (PtyLibrary.INSTANCE.ioctl(sfd, PtyLibrary.I_PUSH, "ldterm") == -1) {
                        throw new PtyException("ioctl(\"" + slave_name + "\", I_PUSH, \"ldterm\") failed -- " + strerror(Native.getLastError()));
                    }

                    // not sure but both xterm and DtTerm do it
                    if (PtyLibrary.INSTANCE.ioctl(sfd, PtyLibrary.I_PUSH, "ttcompat") == -1) {
                        throw new PtyException("ioctl(\"" + slave_name + "\", I_PUSH, \"ttcompact\") failed -- " + strerror(Native.getLastError()));
                    }
                }
            } else {
                if (OS.get() == OS.LINUX) {
                    PtyLibrary.Termios termios = new PtyLibrary.Termios();

                    // check existing settings
                    // If we don't do this tcssetattr() will return EINVAL.
                    if (PtyLibrary.INSTANCE.tcgetattr(sfd, termios) == -1) {
                        throw new PtyException("tcgetattr(\"" + slave_name + "\", <termios>) failed -- " + strerror(Native.getLastError()));
                    }

                    // System.out.printf("tcgetattr() gives %s\n", termios);

                    // initialize values relevant for raw mode
                    PtyLibrary.INSTANCE.cfmakeraw(termios);

                    // System.out.printf("cfmakeraw() gives %s\n", termios);

                    // apply them
                    if (PtyLibrary.INSTANCE.tcsetattr(sfd, PtyLibrary.TCSANOW, termios) == -1) {
                        throw new PtyException("tcsetattr(\"" + slave_name + "\", TCSANOW, <termios>) failed -- " + strerror(Native.getLastError()));
                    }
                }
            }

	    // Success ... assign fd's to FileObjects
	    Util.assignFd(mfd, master_fd);
	    Util.assignFd(sfd, slave_fd);

	} catch (PtyException x) {
	    PtyLibrary.INSTANCE.close(mfd);
	    PtyLibrary.INSTANCE.close(sfd);
            throw x;
	}
    }
}
