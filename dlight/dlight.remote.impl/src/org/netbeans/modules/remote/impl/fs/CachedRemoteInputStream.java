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
