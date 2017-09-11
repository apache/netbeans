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
