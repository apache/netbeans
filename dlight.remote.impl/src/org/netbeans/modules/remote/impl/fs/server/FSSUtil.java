/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.RemoteExceptions;

/**
 *
 */
/*package*/ class FSSUtil  {

    private FSSUtil() {
    }
    
    
    public interface FSSException {
        int getErrno();
    }    
    
    public static final class FSSIOException extends IOException implements FSSException {
        
        private final int errno;

        private FSSIOException(int errno, String message) {
            super(message);
            this.errno = errno;
        }

        @Override
        public int getErrno() {
            return errno;
        }
    }
    
    public static final class FSSFileNotFoundException extends FileNotFoundException implements FSSException {
        
        private final int errno;

        private FSSFileNotFoundException(int errno, String message) {
            super(message);
            this.errno = errno;
        }

        @Override
        public int getErrno() {
            return errno;
        }
    }

    public static final class FSSGenericException extends Exception implements FSSException {
        
        private final int errno;

        private FSSGenericException(int errno, String message) {
            super(message);
            this.errno = errno;
        }

        @Override
        public int getErrno() {
            return errno;
        }
    }
    
    public static IOException createIOException(int errno, String emsg, ExecutionEnvironment execEnv) {
        switch (errno) {
            case Errno.EACCES:
            case Errno.ENOENT:
                return RemoteExceptions.annotateException(new FSSFileNotFoundException(errno, emsg));
            case 0:
                RemoteLogger.info("fs_server [{0}] reports zero errno; treating as 'file not found': {1}", execEnv, emsg);
                return RemoteExceptions.annotateException(new FSSFileNotFoundException(errno, emsg));
            default:
                return RemoteExceptions.annotateException(new FSSIOException(errno, emsg));
        }
    }
    
    public static String unescape(String line) {
        if (line.indexOf('\\') == -1) {
            return line;
        } else {
            return  line.replace("\\n", "\n").replace("\\\\", "\\"); // NOI18N
        }
    }
    
    public static String escape(String line) {
        if (line.indexOf('\n') == -1 && line.indexOf('\\') == -1) {
            return line;
        } else {
            return  line.replace("\n", "\\n").replace("\\", "\\\\"); // NOI18N
        }
    }    
}
