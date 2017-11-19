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

package org.netbeans.lib.richexecution;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

// public because it's needed by "Term Driver".
public class CLibrary {
    private final GenericCLibrary delegate;
    private final GenericConstants constants;

    private interface GenericCLibrary extends Library {
	// signals
	public int kill(int pid, int sig);

	// pty support stuff
	public int posix_openpt(int flags);
	public int getpt();
	public int grantpt(int master_fd);
	public int unlockpt(int master_fd);
	public String ptsname(int master_fd);

	// termios stuff
	public int tcgetattr(int fd, LinuxTermios termios);
	public int tcsetattr(int fd, int optionalActions, final LinuxTermios termios);
	public void cfmakeraw(LinuxTermios termios);

	// generic unix support stuff
	public int close(int fd);
	public int open(String pathname, int flags);
	public int ioctl(int fd, int op, String str_arg);
	public int ioctl(int fd, int op, WinSize winsize);
	public String strerror(int errno);
    }

    private static interface DefaultCLibrary
	    extends GenericCLibrary {
    }

    private abstract static class GenericConstants {

	protected GenericConstants() {
	}

	public final int SIGHUP = 1;
	public final int SIGTERM = 15;

	// Linux: /bits/fcntl.h
	// Mac: /usr/include/sys/fcntl.h
	// Solaris: /usr/include/sys/fcntl.h
	public final int O_RDWR = 2;

	public final int TCSANOW = 0;        // for tcsetattr()
	public final int TCSADRAIN = 1;
	public final int TCSAFLUSH = 2;

	public abstract int I_PUSH();

	public abstract int NCCS();

	public abstract int TIOCSWINSZ();
    }

    private static class DefaultConstants extends GenericConstants {
	public int TIOCSWINSZ() {
	    throw new UnsupportedOperationException("TIOCSWINSZ not supported");
	}

	public int I_PUSH() {
	    throw new UnsupportedOperationException("I_PUSH not supported");
	}

	public int NCCS() {
	    throw new UnsupportedOperationException("NCCS not supported");
	}
    }

    private static class SolarisConstants extends GenericConstants {
	public int TIOCSWINSZ() {
	    // Solaris: /sys/termios.h
	    final int _TIOC = ('T' << 8);
	    return (_TIOC | 103);
	}

	public int I_PUSH() {
	    // Solaris: /sys/stropts.h
	    final int STR = ('S' << 8);
	    return (STR | 02);
	}

	public int NCCS() {
	    // Solaris: /sys/termios.h
	    final int NCCS = 19;
	    return NCCS;
	}
    }
    private static class LinuxConstants extends GenericConstants {
	public int TIOCSWINSZ() {
	    // Linux: /ioctls.h
	    return 0x5414;
	}

	public int I_PUSH() {
	    // Linux: /bits/stropts.h
	    final int __SID = ('S' << 8);
	    return (__SID | 2);
	}

	public int NCCS() {
	    // Linux: /bits/termios.h
	    final int NCCS = 32;
	    return NCCS;
	}
    }

    private static class MacConstants extends GenericConstants {
	public int TIOCSWINSZ() {
	    // Mac: /sys/ttycom.h
	    return 0x80087467;
	}

	public int I_PUSH() {
	    throw new UnsupportedOperationException("I_PUSH not supported");
	}

	public int NCCS() {
	    // Used for termios and not used on the Mac
	    throw new UnsupportedOperationException("I_PUSH not supported");
	}
    }

    public CLibrary() {
	switch (Platform.get()) {
	    case LinuxIntel32:
	    case LinuxIntel64:
		delegate = (GenericCLibrary) Native.loadLibrary("c", DefaultCLibrary.class);
		constants = new LinuxConstants();
		break;
	    case MacosIntel32:
		delegate = (GenericCLibrary) Native.loadLibrary("c", DefaultCLibrary.class);
		constants = new MacConstants();
		break;
	    case SolarisIntel32:
	    case SolarisIntel64:
	    case SolarisSparc32:
	    case SolarisSparc64:
		delegate = (GenericCLibrary) Native.loadLibrary("c", DefaultCLibrary.class);
		constants = new SolarisConstants();
		break;
	    case WindowsIntel32:
	    case Other:
	    default:
		delegate = (GenericCLibrary) Native.loadLibrary("c", DefaultCLibrary.class);
		constants = new DefaultConstants();
		break;
	}
    }

    // struct winsize
    // Solaris: sys/termios.h
    public static class WinSize extends Structure {
        // JNA cannot figure sizeof structure if members aren't public
        public short ws_row;
        public short ws_col;
        public short ws_xpixel;
        public short ws_ypixel;

        public WinSize(int rows, int cols, int height, int width) {
            this.ws_row = (short) rows;
            this.ws_col = (short) cols;
            this.ws_xpixel = (short) width;
            this.ws_ypixel = (short) height;
        }

        protected List getFieldOrder() {
            return Arrays.asList(new String[] {"ws_row", "ws_col", "ws_xpixel", "ws_ypixel"});
        }
    }

    // struct termios
    // Not used on the mac

    static public class SolarisTermios extends Structure {
        public int c_iflag;     // input modes
        public int c_oflag;     // output modes
        public int c_cflag;     // control modes
        public int c_lflag;     // local modes
        public byte c_cc[];     // control characters

        public SolarisTermios() {
            c_cc = new byte[INSTANCE.NCCS()];
        }

        protected List getFieldOrder() {
            return Arrays.asList(new String[] {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc"});
        }
    }

    // Not used on the mac
    static public class LinuxTermios extends Structure {
        public int c_iflag;     // input modes
        public int c_oflag;     // output modes
        public int c_cflag;     // control modes
        public int c_lflag;     // local modes
        public byte c_line;     // line discipline
        public byte c_cc[];     // control characters
        public int c_ispeed;    // input speed
        public int c_ospeed;    // output speed

        public LinuxTermios() {
            c_cc = new byte[INSTANCE.NCCS()];
        }

        protected List getFieldOrder() {
            return Arrays.asList(new String[] {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_line", "c_cc", "c_ispeed", "c_ospeed"});
        }
    }

    public static CLibrary INSTANCE = Factory.makeInstance();

    // signals
    public final int kill(int pid, int sig) {
	return delegate.kill(pid, sig);
    }

    // pty support stuff
    public final int posix_openpt(int flags) {
	return delegate.posix_openpt(flags);
    }
    public final int getpt() {
	return delegate.getpt();
    }
    public final int grantpt(int master_fd) {
	return delegate.grantpt(master_fd);
    }
    public final int unlockpt(int master_fd) {
	return delegate.unlockpt(master_fd);
    }
    public final String ptsname(int master_fd) {
	return delegate.ptsname(master_fd);
    }

    // termios stuff
    public final int tcgetattr(int fd, LinuxTermios termios) {
	switch (Platform.get()) {
	    case LinuxIntel32:
	    case LinuxIntel64:
		return delegate.tcgetattr(fd, termios);
	    default:
		throw new UnsupportedOperationException("tcgetattr not supported");
	}
    }
    public final int tcsetattr(int fd, int optionalActions, final LinuxTermios termios) {
	switch (Platform.get()) {
	    case LinuxIntel32:
	    case LinuxIntel64:
		return delegate.tcsetattr(fd, optionalActions, termios);
	    default:
		throw new UnsupportedOperationException("tcsetattr not supported");
	}
    }
    public final void cfmakeraw(LinuxTermios termios) {
	switch (Platform.get()) {
	    case LinuxIntel32:
	    case LinuxIntel64:
		delegate.cfmakeraw(termios);
		break;
	    default:
		throw new UnsupportedOperationException("cfmakeraw not supported");
	}
    }

    // generic unix support stuff
    public final int close(int fd) {
	return delegate.close(fd);
    }
    public final int open(String pathname, int flags) {
	return delegate.open(pathname, flags);
    }
    public final int ioctl(int fd, int op, String str_arg) {
	return delegate.ioctl(fd, op, str_arg);
    }
    public final int ioctl(int fd, int op, WinSize winsize) {
	return delegate.ioctl(fd, op, winsize);
    }
    public final String strerror(int errno) {
	return delegate.strerror(errno);
    }

    public final int SIGHUP() { return constants.SIGHUP; }
    public final int SIGTERM() { return constants.SIGTERM; }

    public final int NCCS() { return constants.NCCS(); }

    public final int TCSANOW() { return constants.TCSANOW; }
    public final int TCSADRAIN() { return constants.TCSADRAIN; }
    public final int TCSAFLUSH() { return constants.TCSAFLUSH; }

    public final int O_RDWR() { return constants.O_RDWR; }

    public final int I_PUSH() { return constants.I_PUSH(); }

    public final int TIOCSWINSZ() { return constants.TIOCSWINSZ(); }

    private static class Factory {
	public static CLibrary makeInstance() {
	    return new CLibrary();
	}
    }
}

