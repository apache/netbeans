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
