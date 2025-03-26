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
package org.netbeans.lib.jshell.agent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.execution.LoaderDelegate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * NetBeans JShell agent wrapper. Handles the initial handshake between NetBeans IDE and JShell,
 * and listens for incoming connections. The initial handshake is initiated by this process, it 
 * sends back the authorization key and port number opened for listening. The IDE then connects
 * back as necessary, when the user opens or resets a JShell terminal.
 * <p/>
 * The real agent execution is a little peculiar, since the agent needs to manipulate bytecode and
 * I didn't want to do all that manually; so it uses ObjectWeb ASM library - but the lib should not
 * appear on application classpath for possible clashes with app's own libraries. A private classloader
 * is used to load the agent itself.
 * 
 * @author sdedic
 */
public class NbJShellAgent implements Runnable, ClassFileTransformer {
    /**
     * This field will be initialized at startup. The IDE will grab the value
     * using JDI to associate a debugger Session with the appropriate incoming socket.
     */
    public static volatile String debuggerKey = ""; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(NbJShellAgent.class.getName());
    
    /**
     * Address to connect to for the initial handshake
     */
    private InetAddress  address;
    private int          port;
    private List<String>    libraries = Collections.emptyList();
    

    /**
     * Name of reference class. If field | method are given, the class' static method/field
     * will be queried to get a classloader. If both field and method is null, the class
     * will be instrumented to publish its classloader.
     */
    private String  className;
    private String  field;
    private String  method;
    private String  key;
    private Instrumentation instrumentation;
    
    private ClassLoader agentClassLoader;

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }
    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    public InetAddress getAddress() {
        return address;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public String getClassName() {
        return className;
    }

    void setClassName(String className) {
        this.className = className;
    }

    public String getField() {
        return field;
    }

    void setField(String field) {
        this.field = field;
    }

    public String getMethod() {
        return method;
    }

    void setMethod(String method) {
        this.method = method;
    }
    
    public ClassLoader createClassLoader() {
        if (agentClassLoader != null) {
            return agentClassLoader;
        }
        if (libraries.isEmpty()) {
            LOG.log(Level.FINE, "Creating standard classloader"); // NOI18N
            return getClass().getClassLoader();
            
        }
        LOG.log(Level.FINE, "Creating custom classloader"); // NOI18N
        List<URL> urls = new ArrayList<URL>(libraries.size());
        for (String s : libraries) {
            try {
                URL url = new File(s).toURI().toURL(); 
                urls.add(url);
                LOG.log(Level.FINE, "Adding library: {0}", url); // NOI18N
            } catch (MalformedURLException ex) {
                // skip
                LOG.log(Level.WARNING, "Unable to add library {0}: {1}", new Object[] { s,  ex }); // NOI18N
            }
        }
        ClassLoader agentClassLoader = new URLClassLoader(
                urls.toArray(new URL[0]), 
                getClass().getClassLoader());
        try {
            agentClassLoader.loadClass("org.netbeans.lib.jshell.agent.AgentWorker"); // NOI18N
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NbJShellAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.agentClassLoader = agentClassLoader;
        return agentClassLoader;
    }
    
    public static void premain(String args, Instrumentation inst) {
        LOG.log(Level.FINE, "NbJShell agent starting, parameters: {0}", args); // NOI18N
        LOG.log(Level.FINE, "Properties: " + System.getProperties().toString().replace(",", "\n")); // NOI18N
        NbJShellAgent agent = new NbJShellAgent();

        agent.instrumentation = inst;
        
        String[] pars = args.split(",");
        for (String param : pars) {
            String[] nameVal = param.split("=");
            if (nameVal == null || nameVal.length != 2) {
                continue;
            }
            String opt = nameVal[0];
            if ("address".equals(opt)) {
                    try {
                        agent.setAddress(InetAddress.getByName(nameVal[1]));
                    } catch (UnknownHostException ex) {
                        LOG.log(Level.SEVERE, "Invalid host address: {0}", ex);
                    }
            } else if ("port".equals(opt)) {
                    agent.setPort(Integer.valueOf(nameVal[1]));
            } else if ("libraries".equals(opt)) {
                    agent.setLibraries(Arrays.asList(nameVal[1].split(";")));
            } else if ("key".equals(opt)) {
                    debuggerKey = nameVal[1];
                    LOG.log(Level.FINE, "Association key: " + debuggerKey); // NOI18N
                    agent.setKey(nameVal[1]);
            } else if ("class".equals(opt)) {
                    agent.setClassName(nameVal[1].replace('.', '/')); // NOI18N
            } else if ("field".equals(opt)) {
                    agent.setField(nameVal[1]);
            } else if ("method".equals(opt)) {
                    agent.setMethod(nameVal[1]);
            }
        }
        
        if (agent.needsInstrumentation()) {
            inst.addTransformer(agent, true);
        }
        
        try {
            ThreadGroup tg = new ThreadGroup("NetBeans JSHell agent support"); // NOI18N
            Thread t = new Thread(tg, agent, "JShell VM Agent Connector"); // NOI18N
            LOG.log(Level.INFO, "Starting JShell agent loop");
            t.setDaemon(true);
            t.setContextClassLoader(agent.createClassLoader());
            t.start();
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private Class workerClass;
    private Constructor workerCtor;
    
    public boolean needsInstrumentation() {
        return className != null && field == null && method == null;
    }
    
    public void run() {
        try {
            workerClass = Class.forName("org.netbeans.lib.jshell.agent.AgentWorker", true, createClassLoader()); //createClassLoader().loadClass("jdk.internal.jshell.remote.AgentWorker"); // NOI18N
            workerCtor = workerClass.getConstructor(NbJShellAgent.class, Socket.class);
        } catch (ClassNotFoundException ex) {        
            LOG.log(Level.WARNING, "Could not load worker class: ", ex); // NOI18N
        } catch (NoSuchMethodException ex) {
            LOG.log(Level.WARNING, "Could not load worker class: ", ex); // NOI18N
        } catch (SecurityException ex) {
            LOG.log(Level.WARNING, "Could not load worker class: ", ex); // NOI18N
        }
        ServerSocket socket;
        try {
             socket = new ServerSocket();
             socket.bind(null);
            LOG.log(Level.FINE, "NetBeans JShell agent starting at port {0}", socket.getLocalPort()); // NOI18N
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Failed to allocate callback socket: ", ex); // NOI18N
            return;
        }
        
        LOG.log(Level.FINE, "Opening socket to {0}:{1}", new Object[] { // NOI18N
            getAddress(), getPort()
        });
        Socket handshake;
        try {
            // do NOT close the handshake socket, its close be used as an indication that the 
            // agent completely terminated.
            handshake = new Socket(getAddress(), getPort());
            LOG.log(Level.FINE, "Connection to master granted, creating OS"); // NOI18N
            ObjectOutputStream ostm = new ObjectOutputStream(handshake.getOutputStream());
            // send an authorization
            LOG.log(Level.FINE, "Authorizing with key {0}, local port for callback is: {1}", new Object[] { // NOI18N
                key, socket.getLocalPort()
            });
            ostm.writeUTF(key);
            ostm.writeInt(socket.getLocalPort());
            ostm.flush();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Initial handshake failed: ", ex); // NOI18N
            return;
        }
        
        int counter = 1;
        
        try {
            while (true) {
                try {
                    Socket newConnection = socket.accept();
                    Runnable r = (Runnable) workerCtor.newInstance(this, newConnection);
                    Thread t = new Thread(r, "JShell agent #" + (counter++)); // NOI18N
                    t.setDaemon(true);
                    LOG.log(Level.FINE, "Forking JShell agent " + r.hashCode()); // NOI18N
                    t.start();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Error during accept: ", ex); // NOI18N
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "Could not initialize the worker", ex); // NOI18N
                } catch (InstantiationException ex) {
                    LOG.log(Level.WARNING, "Could not initialize the worker", ex); // NOI18N
                } catch (IllegalAccessException ex) {
                    LOG.log(Level.WARNING, "Could not initialize the worker", ex); // NOI18N
                } catch (InvocationTargetException ex) {
                    LOG.log(Level.WARNING, "Could not initialize the worker", ex); // NOI18N
                }
            }
        } finally {
            try {
                handshake.close();
            } catch (IOException ex) {
                Logger.getLogger(NbJShellAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    ////////////////////// INSTRUMENTATION PART ///////////////////////////////////
    private static final String CLASS_INIT_NAME = "<clinit>"; // NOI18N
    private static final String CLASS_INIT_DESC = "()V"; // NOI18N
    private static final String JAVA_LANG_CLASS = "java/lang/Class"; // NOI18N
    
    private static final String AGENT_CLASS = "jdk/internal/jshell/remote/AgentWorker"; // NOI18N
    private static final String AGENT_CLASSLOADER_FIELD = "referenceClassLoader"; // NOI18N
    private static final String AGENT_CLASSLOADER_DESC = "Ljava/lang/ClassLoader;"; // NOI18N
    
    private static final String CLASS_GET_CLASSLOADER_DESC = "()Ljava/lang/ClassLoader;"; // NOI18N
    private static final String CLASS_GET_CLASSLODER_METHOD = "getClassLoader"; // NOI18N
    
    /**
     * Transforms an existin &lt;clinit> method. Adds bytecode before the return, if
     * exists.
     * @param className
     * @param target 
     */
    private void transformClassInit(String className, MethodNode target) {
        Type classType = Type.getObjectType(className);
        if (target.instructions.size() > 0) {
            if (target.instructions.getLast().getOpcode() == Opcodes.RETURN) {
                target.instructions.remove(target.instructions.getLast());
            }
        }
        // ldc
        target.visitLdcInsn(classType);
        // invokevirtual --> classloader on stack
        target.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                JAVA_LANG_CLASS,
                CLASS_GET_CLASSLODER_METHOD,
                CLASS_GET_CLASSLOADER_DESC,
                false
        );
        // putstatic
        target.visitFieldInsn(
                Opcodes.PUTSTATIC,
                AGENT_CLASS,
                AGENT_CLASSLOADER_FIELD,
                AGENT_CLASSLOADER_DESC
        );
        target.visitInsn(Opcodes.RETURN);
        // ldc || classloader instance
        target.maxStack = Math.max(target.maxStack, 1);
        target.maxLocals = Math.max(target.maxLocals, 1);
        target.visitEnd();
    }
    
    /**
     * Generates a new clinit.
     * @param className
     * @param target 
     */
    private void insertClassInit(String className, ClassNode target) {
        // method void static clinit()
        MethodNode clinit = new MethodNode(Opcodes.ASM9,
            Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
            CLASS_INIT_NAME,
            CLASS_INIT_DESC,
            null,
            new String[0]
        );
        
        transformClassInit(className, clinit);
        target.methods.add(clinit);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(getClassName())) {
            return null;
        }
        
        InputStream istm = null;
        try {
            istm = new ByteArrayInputStream(classfileBuffer);
            ClassReader reader = new ClassReader(istm);
            ClassWriter wr = new ClassWriter(reader, 0);
            ClassNode clazz = new ClassNode(Opcodes.ASM9);
            reader.accept(clazz, 0);
            
            boolean found = false;
            for (MethodNode m : (Collection<MethodNode>)clazz.methods) {
                if (CLASS_INIT_NAME.equals(m.name)) {
                    transformClassInit(className, m);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                insertClassInit(className, clazz);
            }
            
            clazz.accept(wr);
//            try (FileOutputStream debug = new FileOutputStream("/tmp/transformed.class")) {
//                debug.write(wr.toByteArray());
//            }
            return wr.toByteArray();
        } catch (IOException ex) {
            // retrhrow an exception with cause filled in
            IllegalClassFormatException x = new IllegalClassFormatException("I/O error");
            x.initCause(ex);
            throw x;
        } finally {
            if (istm != null) {
                try {
                    istm.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
}
