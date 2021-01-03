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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import java.util.List;

public interface PtyLibrary extends Library {

    // struct winsize
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

        @Override
        protected List getFieldOrder() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    // struct termios
    public static class Termios extends Structure {
        public int c_iflag;     // input modes
        public int c_oflag;     // output modes
        public int c_cflag;     // control modes
        public int c_lflag;     // local modes
        public byte c_line;     // line discipline  (+linux -solaris)
        public byte c_cc[];     // control characters
        public int c_ispeed;    // input speed      (+linux -solaris)
        public int c_ospeed;    // output speed     (+linux - solaris)

        public Termios() {
            c_cc = new byte[NCCS];
        }

        @Override
        protected List getFieldOrder() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public PtyLibrary INSTANCE = (PtyLibrary) Native.loadLibrary("c", PtyLibrary.class);

    // pty support stuff
    public int getpt();
    public int grantpt(int master_fd);
    public int unlockpt(int master_fd);
    public String ptsname(int master_fd);

    // termios stuff
    public int tcgetattr(int fd, Termios termios);
    public int tcsetattr(int fd, int optionalActions, final Termios termios);
    public void cfmakeraw(Termios termios);

    // generic unix support stuff
    public int close(int fd);
    public int open(String pathname, int flags);
    public int ioctl(int fd, int op, String str_arg);
    public int ioctl(int fd, int op, WinSize winsize);
    public String strerror(int errno);

    // Linux: /bits/termios.h
    public static final int NCCS = 32;          // 19 on solaris

    public static final int TCSANOW = 0;        // for tcsetattr()
    public static final int TCSADRAIN = 1;
    public static final int TCSAFLUSH = 2;

    // Linux: /bits/fcntl.h
    public static final int O_RDWR = 2;

    // Linux: /bits/stropts.h
    static final int __SID = ('S' << 8);
    public static final int I_PUSH = (__SID | 2);

    // Linux: /ioctls.h
    public static final int TIOCSWINSZ = 0x5414;
}
