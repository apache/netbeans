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
