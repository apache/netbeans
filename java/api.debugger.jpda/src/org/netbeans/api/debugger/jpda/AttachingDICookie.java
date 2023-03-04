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
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Attaches to some already running JDK and returns VirtualMachine for it.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My Attaching First Debugger Info", 
 *        new Object [] {
 *            AttachingDICookie.create (
 *                "localhost",
 *                1234
 *            )
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @author Jan Jancura
 */
public final class AttachingDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-AttachingDICookie";
    
    private static final Logger logger = Logger.getLogger(AttachingDICookie.class.getName());

    private final AttachingConnector attachingConnector;
    private final Map<String,? extends Argument> args;

    
    private AttachingDICookie (
        AttachingConnector attachingConnector,
        Map<String,? extends Argument> args
    ) {
        this.attachingConnector = attachingConnector;
        this.args = args;
    }

    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param attachingConnector a connector to be used
     * @param args map of arguments
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingDICookie create (
        AttachingConnector attachingConnector,
        Map<String,? extends Argument> args
    ) {
        return new AttachingDICookie (
            attachingConnector, 
            args
        );
    }

    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a potr number
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingDICookie create (
        String hostName,
        int portNumber
    ) {
        return new AttachingDICookie (
            findAttachingConnector ("socket"),
            getArgs (
                findAttachingConnector ("socket"), 
                hostName, 
                portNumber
            )
        );
    }

    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param name a name of shared memory block
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingDICookie create (
        String name
    ) {
        return new AttachingDICookie (
            findAttachingConnector ("shmem"),
            getArgs (
                findAttachingConnector ("shmem"), 
                name
            )
        );
    }

    /** 
     * Returns instance of AttachingDICookie.
     *
     * @return instance of AttachingDICookie
     */
    public AttachingConnector getAttachingConnector () {
        return attachingConnector;
    }

    /**
     * Returns map of arguments.
     *
     * @return map of arguments
     */
    public Map<String,? extends Argument> getArgs () {
        return args;
    }

    /**
     * Returns port number.
     *
     * @return port number
     */
    public int getPortNumber () {
        Argument a = args.get ("port");
        if (a == null) return -1;
        String pn = a.value ();
        if (pn == null) return -1;
        return Integer.parseInt (pn);
    }

    /**
     * Returns name of computer.
     *
     * @return name of computer
     */
    public String getHostName () {
        Argument a = args.get ("hostname");
        if (a == null) return null;
        return a.value ();
    }

    /**
     * Returns shared memory block name.
     *
     * @return shared memory block name
     */
    public String getSharedMemoryName () {
        Argument a = args.get ("name");
        if (a == null) return null;
        return a.value ();
    }

    /**
     * Returns process ID.
     *
     * @return the process ID
     * @since 2.16
     */
    public String getProcessID () {
        Argument a = args.get ("pid");
        if (a == null) return null;
        return a.value ();
    }

    /**
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     * @throws java.io.IOException when unable to attach.
     * @throws IllegalConnectorArgumentsException when some connector argument is invalid.
     */
    @Override
    public VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException {
        try {
            return attachingConnector.attach (args);
        } catch (IOException ioex) {
            String msg = "Attaching Connector = "+attachingConnector+", arguments = "+args; // NOI18N
            logger.log(Level.INFO, msg, ioex);
            throw ioex;
        }
    }
    
    
    // private helper methods ..................................................

    private static Map<String,? extends Argument> getArgs (
        AttachingConnector attachingConnector,
        String hostName,
        int portNumber
    ) {
        Map<String,? extends Argument> args = attachingConnector.defaultArguments ();
        args.get ("hostname").setValue (hostName);
        args.get ("port").setValue ("" + portNumber);
        return args;
    }

    private static Map<String,? extends Argument> getArgs (
        AttachingConnector attachingConnector,
        String name
    ) {
        Map<String,? extends Argument> args = attachingConnector.defaultArguments ();
        args.get ("name").setValue (name);
        return args;
    }
    
    private static AttachingConnector findAttachingConnector (String s) {
        Iterator<AttachingConnector> iter = Bootstrap.virtualMachineManager ().
            attachingConnectors ().iterator ();
        while (iter.hasNext ()) {
            AttachingConnector ac = iter.next ();
            if (ac.transport() != null && ac.transport ().name ().toLowerCase ().indexOf (s) > -1)
                return ac;
        }
        return null;
    }
}
