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

package org.netbeans.modules.debugger.jpda.ant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import org.openide.util.RequestProcessor;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;


/**
 * Ant task to attach the NetBeans JPDA debugger to a remote process.
 * @see "#18708"
 * @author Jesse Glick
 */
public class JPDAConnect extends Task {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N
    
    private RequestProcessor rp = new RequestProcessor("JPDAConnect", 1);
    
    private String host = "localhost"; // NOI18N

    private String address;
    
    /** Explicit sourcepath of the debugged process. */
    private JPDAStart.Sourcepath sourcepath = null;
    
    /** Explicit modulepath of the debugged process. */
    private Path modulepath = null;

    /** Explicit classpath of the debugged process. */
    private Path classpath = null;
    
    /** Explicit bootclasspath of the debugged process. */
    private Path bootclasspath = null;
        
    private String listeningCP = null;
    
    /** Name which will represent this debugging session in debugger UI.
     * If known in advance it should be name of the app which will be debugged.
     */
    private String name;

    /** Default transport is socket*/
    private String transport = "dt_socket"; // NOI18N
    
    
    /**
     * Host to connect to.
     * By default, localhost.
     */
    public void setHost (String h) {
        host = h;
    }
    
    public void setAddress (String address) {
        this.address = address;
    }
    
    private String getAddress () {
        return address;
    }

    public void addModulepath (Path path) {
        logger.log(Level.FINE, "addModlepath({0})", path);
        if (modulepath != null)
            throw new BuildException ("Only one modulepath subelement is supported");
        modulepath = path;
    }
    
    public void addClasspath (Path path) {
        logger.fine("addClasspath("+path+")");
        if (classpath != null)
            throw new BuildException ("Only one classpath subelement is supported");
        classpath = path;
    }
    
    public void addBootclasspath (Path path) {
        logger.fine("addBootclasspath("+path+")");
        if (bootclasspath != null)
            throw new BuildException ("Only one bootclasspath subelement is supported");
        bootclasspath = path;
    }
    
    public void addSourcepath (JPDAStart.Sourcepath path) {
        logger.fine("addSourcepath("+path+")");
        if (sourcepath != null)
            throw new BuildException ("Only one sourcepath subelement is supported");
        sourcepath = path;
    }
    
    public void setListeningcp(String listeningCP) {
        this.listeningCP = listeningCP;
    }

    public void setTransport (String transport) {
        this.transport = transport;
    }
    
    private String getTransport () {
        return transport;
    }
    
    public void setName (String name) {
        this.name = name;
    }
    
    private String getName () {
        return name;
    }
    
    @Override
    public void execute () throws BuildException {
        logger.fine("JPDAConnect.execute ()"); // NOI18N
        Path plainSourcepath = null;
        boolean isSourcePathExclusive = false;
        if (sourcepath != null) {
            isSourcePathExclusive = sourcepath.isExclusive();
            plainSourcepath = sourcepath.getPlainPath();
        }

        JPDAStart.verifyPaths(getProject(), classpath);
        JPDAStart.verifyPaths(getProject(), modulepath);
        //JPDAStart.verifyPaths(getProject(), bootclasspath); Do not check the paths on bootclasspath (see issue #70930).
        JPDAStart.verifyPaths(getProject(), plainSourcepath);
        
        if (name == null)
            throw new BuildException (
                "name attribute must specify name of this debugging session", 
                getLocation ()
            );
        if (address == null)
            throw new BuildException (
                "address attribute must specify port number or memory " +
                "allocation unit name of connection", 
                getLocation ()
            );
        if (transport == null)
            transport = "dt_socket"; // NOI18N

        final Object[] lock = new Object [1];

        ClassPath sourcePath = JPDAStart.createSourcePath (
            getProject (),
            modulepath,
            classpath,
            plainSourcepath,
            isSourcePathExclusive
        );
        ClassPath jdkSourcePath = JPDAStart.createJDKSourcePath (
            getProject (),
            bootclasspath
        );
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Create sourcepath:"); // NOI18N
            logger.fine("    modulepath : " + modulepath); // NOI18N
            logger.fine("    classpath : " + classpath); // NOI18N
            logger.fine("    sourcepath : " + plainSourcepath); // NOI18N
            logger.fine("    bootclasspath : " + bootclasspath); // NOI18N
            logger.fine("    >> sourcePath : " + sourcePath); // NOI18N
            logger.fine("    >> jdkSourcePath : " + jdkSourcePath); // NOI18N
        }
        final Map properties = new HashMap ();
        properties.put ("sourcepath", sourcePath); // NOI18N
        properties.put ("name", getName ()); // NOI18N
        properties.put ("jdksources", jdkSourcePath); // NOI18N
        properties.put ("listeningCP", listeningCP); // NOI18N
        String workDir = getProject().getProperty("work.dir");
        File baseDir;
        if (workDir != null) {
            baseDir = new File(workDir);
        } else {
            baseDir = getProject().getBaseDir();
        }
        properties.put ("baseDir", baseDir); // NOI18N

        logger.fine("JPDAConnect: properties = "+properties);
        

        synchronized(lock) {
            rp.post (new Runnable () {
                public void run() {
                    synchronized(lock) {
                        try {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(
                                    "JPDAConnect.execute ().synchronized: "  // NOI18N
                                    + "host = " + host + " port = " + address + // NOI18N
                                    " transport = " + transport // NOI18N
                                );
                            }
                            // VirtualMachineManagerImpl can be initialized 
                            // here, so needs to be inside RP thread.
                            if (transport.equals ("dt_socket")) // NOI18N
                                try {
                                    JPDADebugger.attach (
                                        host, 
                                        Integer.parseInt (address), 
                                        new Object[] {properties}
                                    );
                                } catch (NumberFormatException e) {
                                    throw new BuildException (
                                        "address attribute must specify port " +
                                        "number for dt_socket connection", 
                                        getLocation ()
                                    );
                                }
                            else
                                JPDADebugger.attach (
                                    address, 
                                    new Object[] {properties}
                                );
                            logger.fine(
                                    "JPDAConnect.execute ().synchronized " + // NOI18N
                                    "end: success" // NOI18N
                                );
                        } catch (Throwable e) {
                            logger.fine(
                                    "JPDAConnect.execute().synchronized " + // NOI18N
                                    "end: exception " + e // NOI18N
                                );
                            lock[0] = e;
                        } finally {
                            lock.notify();
                        }
                    }
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                logger.fine("JPDAConnect.execute() " + "end: exception " + e); // NOI18N
                throw new BuildException(e);
            }
            if (lock[0] != null)  {
                logger.fine("JPDAConnect.execute() " + "end: exception " + lock[0]); // NOI18N
                throw new BuildException((Throwable) lock[0]);
            }

        }
        if (host == null)
            log ("Attached JPDA debugger to " + address);
        else
            log ("Attached JPDA debugger to " + host + ":" + address);
        logger.fine("JPDAConnect.execute () " + "end: success"); // NOI18N
    }
}
