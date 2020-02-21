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
