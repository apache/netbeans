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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.Connector.IntegerArgument;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Listens on given port for some connection of remotely running JDK
 * and returns VirtualMachine for it.
 *
 * <br><br>
 * <b>How to use it:</b>
 * {@codesnippet org.netbeans.api.debugger.jpda.StartListeningTest}
 * 
 * @author Jan Jancura
 */
public final class ListeningDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-ListeningDICookie";

    private final ListeningConnector listeningConnector;
    private Map<String, ? extends Argument> args;
    private boolean isListening = false;

    private ListeningDICookie (
        ListeningConnector listeningConnector,
        Map<String, ? extends Argument> args
    ) {
        this.listeningConnector = listeningConnector;
        this.args = args;
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param listeningConnector a instance of ListeningConnector
     * @param args arguments to be used
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static ListeningDICookie create (
        ListeningConnector listeningConnector,
        Map<String, ? extends Argument> args
    ) {
        return new ListeningDICookie (
            listeningConnector,
            args
        );
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters. Example
     * showing how to tell the IDE to start listening on a random port:
     * 
     * {@codesnippet org.netbeans.api.debugger.jpda.StartListeningTest}
     *
     * @param portNumber a number of port to listen on, use {@code -1} to
     *    let the system select a random port since 3.12
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static ListeningDICookie create (
        int portNumber
    ) {
        return new ListeningDICookie (
            findListeningConnector ("socket"),
            getArgs (
                findListeningConnector ("socket"),
                portNumber
            )
        );
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param name a name of shared memory block to listen on
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static ListeningDICookie create (
        String name
    ) {
        return new ListeningDICookie (
            findListeningConnector ("socket"),
            getArgs (
                findListeningConnector ("socket"),
                name
            )
        );
    }

    private static ListeningConnector findListeningConnector (String s) {
        Iterator iter = Bootstrap.virtualMachineManager ().
            listeningConnectors ().iterator ();
        while (iter.hasNext ()) {
            ListeningConnector ac = (ListeningConnector) iter.next ();
            if (ac.transport() != null && ac.transport ().name ().toLowerCase ().indexOf (s) > -1)
                return ac;
        }
        return null;
    }

    private static Map<String, ? extends Argument> getArgs (
        ListeningConnector listeningConnector,
        int portNumber
    ) {
        Map<String, ? extends Argument> args = listeningConnector.defaultArguments ();
        args.get ("port").setValue (portNumber > 0 ? "" + portNumber : "");
        return args;
    }

    private static Map<String, ? extends Argument> getArgs (
        ListeningConnector listeningConnector,
        String name
    ) {
        Map<String, ? extends Argument> args = listeningConnector.defaultArguments ();
        args.get ("name").setValue (name);
        return args;
    }

    /**
     * Returns instance of ListeningConnector.
     *
     * @return instance of ListeningConnector
     */
    public ListeningConnector getListeningConnector () {
        return listeningConnector;
    }

    /**
     * Returns map of arguments to be used.
     *
     * @return map of arguments to be used
     */
    public Map<String, ? extends Argument> getArgs () {
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
        if (pn == null || pn.length() == 0) {
            // default to system chosen port when no port is specified:
            try {
                String address = listeningConnector.startListening(args);
                isListening = true;
                int splitIndex = address.indexOf(':');
                String localaddr = null;
                if (splitIndex >= 0) {
                    localaddr = address.substring(0, splitIndex);
                    address = address.substring(splitIndex+1);
                }
                a.setValue(address);
                pn = address;
            } catch (IOException ex) {
            } catch (IllegalConnectorArgumentsException ex) {
            }
        } else if (a instanceof IntegerArgument) {
            return ((IntegerArgument) a).intValue();
        }
        try {
            return Integer.parseInt (pn);
        } catch (NumberFormatException e) {
            return -1;
        }
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
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     */
    public VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException {
        try {
            if (!isListening) {
            try {
                listeningConnector.startListening(args);
            } catch (Exception e) {
                // most probably already listening
            }
            }
            return listeningConnector.accept (args);
        } finally {
            try {
                listeningConnector.stopListening(args);
            } catch (Exception e) {
                // most probably not listening anymore                
            }
        }
    }
}
