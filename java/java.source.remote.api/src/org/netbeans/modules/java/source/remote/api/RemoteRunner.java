/**
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
package org.netbeans.modules.java.source.remote.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.remoteapi.Parser.Config;
import org.netbeans.modules.java.source.remoteapi.RemoteProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class RemoteRunner {
    public static @CheckForNull RemoteRunner create(FileObject source) {//TODO: rename to get?
        return RemoteProvider.getRunner(source, RemoteRunner::new);
    }

    private static final Gson gson = new Gson();

    private final DataOutputStream out;
    private final Map<Long, Consumer<DataInputStream>> handlers;
    private final AtomicLong id = new AtomicLong();

    private RemoteRunner(DataInputStream dis, DataOutputStream dos) {
        this.out = dos;
        handlers = new ConcurrentHashMap<>();
        new RequestProcessor(RemoteRunner.class.getName(), 1, false, false).post(() -> {
            try {
                while (true) {
                    long id = dis.readLong();

                    Consumer<DataInputStream> handler = handlers.remove(id);

                    handler.accept(dis);
                }
            } catch (IOException ex) {
                //OK
            }
        });
    }
    
    public <T, P> @NonNull Future<T> readAndDecode(CompilationInfo info, Class<? extends RemoteParserTask<T, CompilationInfo, P>> remoteTask, Class<T> decodeType, P additionalParam) throws IOException {
        return readAndDecodeInternal(Config.create(info), remoteTask, decodeType, additionalParam);
    }

    public <T, P> @NonNull Future<T> readAndDecode(FileObject file, Class<? extends RemoteParserTask<T, CompilationController, P>> remoteTask, Class<T> decodeType, P additionalParam) throws IOException {
        return readAndDecodeInternal(Config.create(file), remoteTask, decodeType, additionalParam);
    }

    private <T, I extends CompilationInfo, P> @NonNull Future<T> readAndDecodeInternal(Config conf, Class<? extends RemoteParserTask<T, I, P>> remoteTask, Class<T> decodeType, P additionalParam) throws IOException {
        long thisRequestId = id.getAndIncrement();
        CompletableFuture<T> result = new CompletableFuture<T>() {
            @Override
            public boolean cancel(boolean intr) {
                super.cancel(intr);
                try {
                    out.writeUTF("cancel");
                    out.writeLong(thisRequestId);
                    out.flush();
                    return true;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        };
        handlers.put(thisRequestId, is -> {
            try {
                switch (is.readUTF()) {
                    case "success":
                        result.complete(gson.fromJson(is.readUTF(), decodeType));
                        break;
                    case "exception":
                        result.completeExceptionally(readException(is));
                        break;
                    default:
                        result.completeExceptionally(new IllegalStateException("Unknown response!"));
                        break;
                }
            } catch (IOException ex) {
                result.completeExceptionally(ex);
            }
        });
        out.writeUTF("run-remote");
        out.writeLong(thisRequestId);
        String confSer = gson.toJson(conf);
        byte[] confBytes = confSer.getBytes("UTF-8");
        out.writeInt(confBytes.length);
        out.write(confBytes);
        out.writeUTF(remoteTask.getName());
        out.writeUTF(gson.toJson(additionalParam));
        out.flush();
        return result;
    }

    private static RemoteException readException(DataInputStream dis) throws IOException {
        RemoteException ex = new RemoteException(dis.readUTF());
        StackTraceElement[] stackTrace = new StackTraceElement[dis.readInt()];

        for (int i = 0; i < stackTrace.length; i++) {
            stackTrace[i] = new StackTraceElement(dis.readUTF(), dis.readUTF(), dis.readUTF(), dis.readInt());
        }

        ex.setStackTrace(stackTrace);

        if (dis.readBoolean())
            ex.initCause(readException(dis));

        int suppressedCount = dis.readInt();

        for (int i = 0; i < suppressedCount; i++) {
            ex.addSuppressed(readException(dis));
        }

        return ex;
    }
    
    private static final class RemoteException extends IOException {

        public RemoteException(String message) {
            super(message);
        }
        
    }
}
