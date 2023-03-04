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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.Connector.Argument;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * Launches a new JVM in debug mode and returns VirtualMachine for it.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My First Launching Debugger Info", 
 *        new Object [] {
 *            LaunchingDICookie.create (
 *                "examples.texteditor.Ted",
 *                new String [] {},
 *                "c:\\nb\\settings\\sampledir",
 *                true
 *            )
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @author Jan Jancura
 */
public final class LaunchingDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-LaunchingDICookie";

    private LaunchingConnector  launchingConnector;
    private Map<String, ? extends Argument> args;

    private String              mainClassName;
    private boolean             suspend;


    private LaunchingDICookie (
        LaunchingConnector launchingConnector,
        Map<String, ? extends Argument> args,
        String mainClassName,
        boolean suspend
    ) {
        this.launchingConnector = launchingConnector;
        this.args = args;
        this.mainClassName = mainClassName;
        this.suspend = suspend;
    }

    /**
     * Creates a new instance of LaunchingDICookie for given parameters.
     *
     * @param mainClassName a name or main class
     * @param commandLine command line of debugged JVM
     * @param address a address to listen on
     * @param suspend if true session will be suspended
     * @return a new instance of LaunchingDICookie for given parameters
     */
    public static LaunchingDICookie create (
        String          mainClassName,
        String          commandLine,
        String          address,
        boolean         suspend
    ) {
        return new LaunchingDICookie (
            findLaunchingConnector (),
            getArgs (commandLine, address),
            mainClassName,
            suspend
        );
    }

    /**
     * Creates a new instance of LaunchingDICookie for given parameters.
     *
     * @param mainClassName a name or main class
     * @param args command line arguments
     * @param classPath a classPath
     * @param suspend if true session will be suspended
     * @return a new instance of LaunchingDICookie for given parameters
     */
    public static LaunchingDICookie create (
        String          mainClassName,
        String[]        args,
        String          classPath,
        boolean         suspend
    ) {
        StringBuilder argss = new StringBuilder();
        int i, k = args.length;
        for (i = 0; i < k; i++) {
            argss.append(" \"").append(args [i]).append("\"");
        }
        // TODO: This code is likely wrong, we need to run debuggee on JDK that
        //       is set on the project.
        // XXX: This method is likely not called from anywhere.
        //      But it's an impl. of API method JPDADebugger.launch().
        String commandLine = System.getProperty ("java.home") + 
            "\\bin\\java -agentlib:jdwp=transport=" +
            getTransportName () + 
            ",address=name,suspend=" + 
            (suspend ? "y" : "n") +
            " -classpath \"" + 
            classPath + 
            "\" " +
            mainClassName + 
            argss;
        String address = "name";
        return new LaunchingDICookie (
            findLaunchingConnector (),
            getArgs (commandLine, address),
            mainClassName,
            suspend
        );
    }
    
    /**
     * Returns type of transport to be used.
     *
     * @return type of transport to be used
     */
    public static String getTransportName () {
        return findLaunchingConnector ().transport ().name ();
    }


    // main methods ............................................................

    /**
     * Returns main class name.
     *
     * @return main class name
     */
    public String getClassName () {
        return mainClassName;
    }

    /**
     * Returns suspended state.
     *
     * @return suspended state
     */
    public boolean getSuspend () {
        return suspend;
    }

    /**
     * Returns command line to be used.
     *
     * @return command line to be used
     */
    public String getCommandLine () {
        Argument a = args.get ("command");
        if (a == null) return null;
        return a.value ();
    }
    
    /**
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     */
    public VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException, VMStartException {
        return launchingConnector.launch (args);
    }
    
    
    // private helper methods ..................................................

    private static Map<String, ? extends Argument> getArgs (
        String commandLine,
        String address
    ) {
        Map<String, ? extends Argument> args = findLaunchingConnector ().defaultArguments ();
        args.get ("command").setValue (commandLine);
        args.get ("address").setValue (address);
        return args;
    }
    
    private static LaunchingConnector findLaunchingConnector () {
        Iterator<LaunchingConnector> iter = Bootstrap.virtualMachineManager ().
            launchingConnectors ().iterator ();
        while (iter.hasNext ()) {
            LaunchingConnector lc = iter.next ();
            if (lc.name ().indexOf ("RawCommandLineLaunch") > -1)
                return lc;
        }
        return null;
    }
}
