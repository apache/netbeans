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
package org.netbeans.modules.java.source.remoteapi;

import java.io.DataInputStream;
import java.io.DataOutputStream;


import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.EnumSet;
import com.google.gson.Gson;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.source.remote.api.RemoteParserTask;
import org.netbeans.modules.parsing.impl.indexing.DefaultCacheFolderProvider;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class Server {

    public static void main(String... args) throws Exception {
        System.setProperty("netbeans.user", args[1]);
        Class<?> main = Class.forName("org.netbeans.core.startup.Main");
        main.getDeclaredMethod("initializeURLFactory").invoke(null);
        DefaultCacheFolderProvider.getInstance().setCacheFolder(FileUtil.toFileObject(new File(args[2], "index")));
        CacheFolderProvider.getCacheFolderForRoot(Places.getUserDirectory().toURI().toURL(), EnumSet.noneOf(CacheFolderProvider.Kind.class), CacheFolderProvider.Mode.EXISTENT);
        startImpl(Integer.parseInt(args[0]));
    }

    public static void start(int reportPort) {
        try {
            startImpl(reportPort);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static final Gson gson = new Gson();
    private static final RequestProcessor WORKER = new RequestProcessor(Server.class.getName(), 1, false, false);

    private static void startImpl(int reportPort) throws UnknownHostException, IOException {
        try (Socket report = new Socket(InetAddress.getLocalHost(), reportPort);
             OutputStream out = report.getOutputStream();
             InputStream in = report.getInputStream();
             DataOutputStream dos = new DataOutputStream(out);
             DataInputStream dis = new DataInputStream(in)) {
            Map<Long, Future<?>> id2Task = new HashMap<>();
            Set<Long> cancelledTasks = new HashSet<>();
            while (true) {
                try {
                String command = dis.readUTF();
                switch (command) {
                    case "run-remote": {
                        long id = dis.readLong();
                        String config = dis.readUTF();
                        String task = dis.readUTF();
                        String additionalParams = dis.readUTF();
                        WORKER.post(() -> {
                            try {
                                String value;
                                try {
                                    Parser.Config conf = gson.fromJson(config, Parser.Config.class);
                                    value = Parser.runControllerTask(conf, cc -> {
                                        for (RemoteParserTask t : Lookup.getDefault().lookupAll(RemoteParserTask.class)) {
                                            if (cancelledTasks.contains(id)) return ""; //XXX: what return here?
                                            if (t.getClass().getName().equals(task)) {
                                                ParameterizedType pt = Arrays.stream(t.getClass().getGenericInterfaces())
                                                                             .filter(type -> type instanceof ParameterizedType)
                                                                             .map(type -> ((ParameterizedType) type))
                                                                             .findAny()
                                                                             .get();
                                                boolean hasCC = pt.getActualTypeArguments()[1].getTypeName().equals(CompilationController.class.getName());
                                                if (!hasCC) {
                                                    cc.toPhase(Phase.RESOLVED);
                                                }
                                                Class<?> paramClass = Class.forName(pt.getActualTypeArguments()[2].getTypeName(), false, t.getClass().getClassLoader());
                                                Future<?> futureResult = t.computeResult(cc, gson.fromJson(additionalParams, paramClass));
                                                if (cancelledTasks.contains(id)) {
                                                    futureResult.cancel(true);
                                                    return ""; //XXX: what return here?
                                                }
                                                id2Task.put(id, futureResult);
                                                return gson.toJson(futureResult.get());
                                            }
                                        }
                                        return ""; //XXX: what do here?
                                    });
                                } catch (Throwable t) {
                                    dos.writeLong(id);
                                    dos.writeUTF("exception");
                                    writeException(dos, t);
                                    dos.flush();
                                    return ;
                                } finally {
                                    id2Task.remove(id);
                                    cancelledTasks.remove(id);
                                }
                                dos.writeLong(id);
                                dos.writeUTF("success");
                                dos.writeUTF(value);
                                dos.flush();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        });
                        break;
                    }
                    case "cancel": {
                        long id = dis.readLong();
                        Future<?> future = id2Task.get(id);
                        if (future != null) {
                            future.cancel(true);
                        }
                        cancelledTasks.add(id);
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown command!");
                }
            } catch (Throwable t) {
                    t.printStackTrace();
            }
            }
        }
    }

    private static void writeException(DataOutputStream dos, Throwable t) throws IOException {
        dos.writeUTF(t.getClass().getName() + ":" + t.getMessage());
        StackTraceElement[] stackTrace = t.getStackTrace();
        dos.writeInt(stackTrace.length);
        for (StackTraceElement el : stackTrace) {
            dos.writeUTF(el.getClassName());
            dos.writeUTF(el.getMethodName());
            dos.writeUTF(el.getFileName());
            dos.writeInt(el.getLineNumber());
        }
        Throwable cause = t.getCause();
        if (cause != null) {
            dos.writeBoolean(true);
            writeException(dos, cause);
        } else {
            dos.writeBoolean(false);
        }
        Throwable[] suppressed = t.getSuppressed();
        dos.writeInt(suppressed.length);
        for (Throwable s : suppressed) {
            writeException(dos, s);
        }
    }

}
