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
