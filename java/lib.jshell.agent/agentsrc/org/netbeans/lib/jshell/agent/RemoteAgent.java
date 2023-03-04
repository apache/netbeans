/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.jshell.agent;

import jdk.jshell.spi.SPIResolutionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.netbeans.lib.jshell.agent.RemoteCodes.*;

/**
 * The remote agent runs in the execution process (separate from the main JShell
 * process.  This agent loads code over a socket from the main JShell process,
 * executes the code, and other misc,
 * @author Robert Field
 */
class RemoteAgent {

    protected RemoteClassLoader loader = new RemoteClassLoader();
    private final Map<String, Class<?>> klasses = new TreeMap<String, Class<?>>();

    public static void main(String[] args) throws Exception {
        String loopBack = null;
        Socket socket = new Socket(loopBack, Integer.parseInt(args[0]));
        (new RemoteAgent()).commandLoop(socket);
    }

    void commandLoop(Socket socket) throws IOException {
        // in before out -- so we don't hang the controlling process
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        OutputStream socketOut = socket.getOutputStream();
        System.setOut(new PrintStream(new MultiplexingOutputStream("out", socketOut), true));
        System.setErr(new PrintStream(new MultiplexingOutputStream("err", socketOut), true));
        ObjectOutputStream out = new ObjectOutputStream(new MultiplexingOutputStream("command", socketOut));
        while (true) {
            int cmd = in.readInt();
            performCommand(cmd, in, out);
        }
    }
    
    protected void performCommand(int cmd, ObjectInputStream in, ObjectOutputStream out) throws IOException {
            switch (cmd) {
            case CMD_EXIT:
                // Terminate this process
                return;
            case CMD_LOAD:
                // Load a generated class file over the wire
                try {
                    int count = in.readInt();
                    List<String> names = new ArrayList<String>(count);
                    for (int i = 0; i < count; ++i) {
                        String name = in.readUTF();
                        byte[] kb = (byte[]) in.readObject();
                        loader.delare(name, kb);
                        names.add(name);
                    }
                    for (String name : names) {
                        Class<?> klass = loader.loadClass(name);
                        klasses.put(name, klass);
                        // Get class loaded to the point of, at least, preparation
                        klass.getDeclaredMethods();
                    }
                    out.writeInt(RESULT_SUCCESS);
                    out.flush();
                } catch (IOException  ex) {
                    handleLoadFailure(ex, out);
                } catch (ClassNotFoundException ex) {
                    handleLoadFailure(ex, out);
                } catch (ClassCastException ex) {
                    handleLoadFailure(ex, out);
                }
                break;
            case CMD_INVOKE: {
                // Invoke executable entry point in loaded code
                String name = in.readUTF();
                Class<?> klass = klasses.get(name);
                if (klass == null) {
                    debug("*** Invoke failure: no such class loaded %s\n", name);
                    out.writeInt(RESULT_FAIL);
                    out.writeUTF("no such class loaded: " + name);
                    out.flush();
                    break;
                }
                String methodName = in.readUTF();
                Method doitMethod;
                try {
                    //this.getClass().getModule().addExports(SPIResolutionException.class.getPackage().getName(), klass.getModule());
                    doitMethod = klass.getDeclaredMethod(methodName, new Class<?>[0]);
                    doitMethod.setAccessible(true);
                    Object res;
                    try {
                        clientCodeEnter();
                        res = doitMethod.invoke(null, new Object[0]);
                    } catch (InvocationTargetException ex) {
                        if (ex.getCause() instanceof ThreadDeath) {
                            expectingStop = false;
                            throw (ThreadDeath) ex.getCause();
                        }
                        throw ex;
                    } catch (StopExecutionException ex) {
                        expectingStop = false;
                        throw ex;
                    } finally {
                        clientCodeLeave();
                    }
                    out.writeInt(RESULT_SUCCESS);
                    out.writeUTF(valueString(res));
                    out.flush();
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    StackTraceElement[] elems = cause.getStackTrace();
                    if (cause instanceof SPIResolutionException) {
                        out.writeInt(RESULT_CORRALLED);
                        out.writeInt(((SPIResolutionException) cause).id());
                    } else {
                        out.writeInt(RESULT_EXCEPTION);
                        out.writeUTF(cause.getClass().getName());
                        out.writeUTF(cause.getMessage() == null ? "<none>" : cause.getMessage());
                    }
                    out.writeInt(elems.length);
                    for (StackTraceElement ste : elems) {
                        out.writeUTF(ste.getClassName());
                        out.writeUTF(ste.getMethodName());
                        out.writeUTF(ste.getFileName() == null ? "<none>" : ste.getFileName());
                        out.writeInt(ste.getLineNumber());
                    }
                    out.flush();
                } catch (NoSuchMethodException ex) {
                    handleInvocationFailure(ex, out);
                } catch (IllegalAccessException ex) {
                    handleInvocationFailure(ex, out);
                } catch (StopExecutionException ex) {
                    try {
                        out.writeInt(RESULT_KILLED);
                        out.flush();
                    } catch (IOException err) {
                        debug("*** Error writing killed result: %s -- %s\n", ex, ex.getCause());
                    }
                }
                System.out.flush();
                break;
            }
            case CMD_VARVALUE: {
                // Retrieve a variable value
                String classname = in.readUTF();
                String varname = in.readUTF();
                Class<?> klass = klasses.get(classname);
                if (klass == null) {
                    debug("*** Var value failure: no such class loaded %s\n", classname);
                    out.writeInt(RESULT_FAIL);
                    out.writeUTF("no such class loaded: " + classname);
                    out.flush();
                    break;
                }
                try {
                    Field var = klass.getDeclaredField(varname);
                    var.setAccessible(true);
                    Object res = var.get(null);
                    out.writeInt(RESULT_SUCCESS);
                    out.writeUTF(valueString(res));
                    out.flush();
                } catch (Exception ex) {
                    debug("*** Var value failure: no such field %s.%s\n", classname, varname);
                    out.writeInt(RESULT_FAIL);
                    out.writeUTF("no such field loaded: " + varname + " in class: " + classname);
                    out.flush();
                }
                break;
            }
            case CMD_CLASSPATH: {
                // Append to the claspath
                String cp = in.readUTF();
                for (String path : cp.split(File.pathSeparator)) {
                    loader.addURL(new File(path).toURI().toURL());
                }
                out.writeInt(RESULT_SUCCESS);
                out.flush();
                break;
            }
            default:
                debug("*** Bad command code: %d\n", cmd);
                break;
        }
    }
    
    private void handleLoadFailure(Throwable ex, ObjectOutputStream out) throws IOException {
        debug("*** Load failure: %s\n", ex);
        out.writeInt(RESULT_FAIL);
        out.writeUTF(ex.toString());
        out.flush();
    }
    
    private void handleInvocationFailure(Throwable ex, ObjectOutputStream out) throws IOException {
        debug("*** Invoke failure: %s -- %s\n", ex, ex.getCause());
        out.writeInt(RESULT_FAIL);
        out.writeUTF(ex.toString());
        out.flush();
    }
    
    protected void handleUnknownCommand(int cmd, ObjectInputStream i, ObjectOutputStream o) throws IOException {
        debug("*** Bad command code: %d\n", cmd);
    }

    // These three variables are used by the main JShell process in interrupting
    // the running process.  Access is via JDI, so the reference is not visible
    // to code inspection.
    private boolean inClientCode; // Queried by the main process
    private boolean expectingStop; // Set by the main process

    // thrown by the main process via JDI:
    private final StopExecutionException stopException = new StopExecutionException();

    @SuppressWarnings("serial")             // serialVersionUID intentionally omitted
    private class StopExecutionException extends ThreadDeath {
        @Override public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    void clientCodeEnter() {
        expectingStop = false;
        inClientCode = true;
    }

    void clientCodeLeave() {
        inClientCode = false;
        while (expectingStop) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                debug("*** Sleep interrupted while waiting for stop exception: %s\n", ex);
            }
        }
    }

    private void debug(String format, Object... args) {
        System.err.printf("REMOTE: "+format, args);
    }

    static String valueString(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + (String)value + "\"";
        } else if (value instanceof Character) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }

    static final class MultiplexingOutputStream extends OutputStream {

        private static final int PACKET_SIZE = 127;

        private final byte[] name;
        private final OutputStream delegate;

        public MultiplexingOutputStream(String name, OutputStream delegate) {
            this.name = name.getBytes(StandardCharsets.UTF_8);
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            synchronized (delegate) {
                delegate.write(name.length); //assuming the len is small enough to fit into byte
                delegate.write(name);
                delegate.write(1);
                delegate.write(b);
                delegate.flush();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            synchronized (delegate) {
                int i = 0;
                while (len > 0) {
                    int size = Math.min(PACKET_SIZE, len);

                    delegate.write(name.length); //assuming the len is small enough to fit into byte
                    delegate.write(name);
                    delegate.write(size);
                    delegate.write(b, off + i, size);
                    i += size;
                    len -= size;
                }

                delegate.flush();
            }
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            super.close();
            delegate.close();
        }

    }
}
