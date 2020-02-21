/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.remote.impl.RemoteLogger;

/**
 *
 *
 */
final class CachedRemoteInputStream extends InputStream {

    private static final int BUFFER_SIZE;
    private final RemotePlainFile remoteFile;
    private final byte[] buffer;
    private int position;
    private final Writer writer = new PrintWriter(System.err);
    private Object delegate;

    static {
        int prefferedBufferSize = 8192;
        int defaultBufferSize = -1;
        try {
            Field field = BufferedInputStream.class.getDeclaredField("defaultBufferSize"); // NOI18N
            if (field != null) {
                field.setAccessible(true);
                defaultBufferSize = field.getInt(BufferedInputStream.class);
            }
        } catch (IllegalArgumentException | IllegalAccessException | 
                NoSuchFieldException | SecurityException ex) {
        }
        if (defaultBufferSize > prefferedBufferSize) {
            BUFFER_SIZE = defaultBufferSize;
        } else {
            BUFFER_SIZE = prefferedBufferSize;
        }
    }

    CachedRemoteInputStream(RemotePlainFile remoteFile, ExecutionEnvironment srcExecEnv) {
        this.remoteFile = remoteFile;
        position = 0;
        buffer = CommonTasksSupport.readFile(remoteFile.getPath(), srcExecEnv, 0, BUFFER_SIZE, writer);
    }

    private CachedRemoteInputStream(CachedRemoteInputStream master) {
        this.remoteFile = master.remoteFile;
        position = 0;
        buffer = master.buffer;
    }

    @Override
    public int read() throws IOException {
        if (delegate != null) {
            if (delegate instanceof FileInputStream) {
                return ((FileInputStream) delegate).read();
            } else {
                return -1;
            }
        }
        if (position < buffer.length) {
            return 0xFF & buffer[position++];
        } else {
            try {
                if (buffer.length < BUFFER_SIZE) {
                    RemoteFileSystemUtils.getCanonicalParent(remoteFile).ensureChildSync(remoteFile);
                    return -1;
                } else {
                    RemoteFileSystemUtils.getCanonicalParent(remoteFile).ensureChildSync(remoteFile);
                    delegate = new FileInputStream(remoteFile.getCache());
                    if (remoteFile.getCache().length() > 1024*1024) {
                        boolean debug = false;
                        assert (debug = true);
                        if (debug) {
                            new Exception("Too long remote file "+remoteFile.getPath()).printStackTrace(System.err); // NOI18N
                        }
                    }
                    while (position > 0) {
                        ((FileInputStream) delegate).read();
                        position--;
                    }
                    return ((FileInputStream) delegate).read();
                }
            } catch (ConnectException | InterruptedException | TimeoutException ex) {
                return -1;
            } catch (ExecutionException ex) {
                RemoteLogger.finest(ex);
                return -1;
            } finally {
                if (delegate == null) {
                    delegate = Integer.valueOf(-1);
                }
            }
        }
    }

    CachedRemoteInputStream reuse() {
        if (delegate != null) {
            return null;
        }
        return new CachedRemoteInputStream(this);
    }
}
