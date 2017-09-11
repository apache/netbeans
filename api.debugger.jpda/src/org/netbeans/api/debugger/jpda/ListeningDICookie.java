/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My First Listening Debugger Info",
 *        new Object [] {
 *            ListeningDICookie.create (
 *                1234
 *            )
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @author Jan Jancura
 */
public final class ListeningDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-ListeningDICookie";

    private ListeningConnector listeningConnector;
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
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param portNumber a number of port to listen on
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
        args.get ("port").setValue ("" + portNumber);
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
