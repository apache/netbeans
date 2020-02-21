/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only("GPL") or the Common
 * Development and Distribution License("CDDL")(collectively, the
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs.server;

/***
 *
 */
public interface Errno {

    /** Operation not permitted */
    public static final int EPERM = 1;

    /** No such file or directory */
    public static final int ENOENT = 2;

    /** No such process */
    public static final int ESRCH = 3;
    
    /** Interrupted system call */
    public static final int EINTR = 4;
    
    /** I/O error */
    public static final int EIO = 5;
    
    /** No such device or address */
    public static final int ENXIO = 6;
    
    /** Argument list too long */
    public static final int E2BIG = 7;
    
    /** Exec format error */
    public static final int ENOEXEC = 8;
    
    /** Bad file number */
    public static final int EBADF = 9;
    
    /** No child processes */
    public static final int ECHILD = 10;
    
    /** Try again */
    public static final int EAGAIN = 11;
    
    /** Out of memory */
    public static final int ENOMEM = 12;
    
    /** Permission denied */
    public static final int EACCES = 13;
    
    /** Bad address */
    public static final int EFAULT = 14;
    
    /** Block device required */
    public static final int ENOTBLK = 15;
    
    /** Device or resource busy */
    public static final int EBUSY = 16;
    
    /** File exists */
    public static final int EEXIST = 17;
    
    /** Cross-device link */
    public static final int EXDEV = 18;
    
    /** No such device */
    public static final int ENODEV = 19;
    
    /** Not a directory */
    public static final int ENOTDIR = 20;
    
    /** Is a directory */
    public static final int EISDIR = 21;
    
    /** Invalid argument */
    public static final int EINVAL = 22;
    
    /** File table overflow */
    public static final int ENFILE = 23;
    
    /** Too many open files */
    public static final int EMFILE = 24;
    
    /** Not a typewriter */
    public static final int ENOTTY = 25;
    
    /** Text file busy */
    public static final int ETXTBSY = 26;
    
    /** File too large */
    public static final int EFBIG = 27;
    
    /** No space left on device */
    public static final int ENOSPC = 28;
    
    /** Illegal seek */
    public static final int ESPIPE = 29;
    
    /** Read-only file system */
    public static final int EROFS = 30;
    
    /** Too many links */
    public static final int EMLINK = 31;
    
    /** Broken pipe */
    public static final int EPIPE = 32;
    
    /** Math argument out of domain of func */
    public static final int EDOM = 33;
    
    /** Math result not representable */
    public static final int ERANGE = 34;

}
