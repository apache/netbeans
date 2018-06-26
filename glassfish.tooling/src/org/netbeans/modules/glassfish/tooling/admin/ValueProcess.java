/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.glassfish.tooling.admin;

/**
 * GlassFish server process.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ValueProcess {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** The name of the executable to run. */
    private String processName;

    /** Arguments passed to the executable. */
    private String arguments;

    /** Process information. */
    private Process process;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server process entity.
     * <p/>
     * Entity is initialized in <code>RunnerLocal</code> method
     * <code>call</code>.
     * method.
     * <p/>
     * @param processName The name of the executable to run..
     * @param arguments Arguments passed to the executable.
     */
    ValueProcess(String processName, String arguments, Process process) {
        this.processName = processName;        
        this.arguments = arguments;
        this.process = process;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get name of the executable to run.
     * <p/>
     * @return Name of the executable to run.
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Get arguments passed to the executable.
     * <p/>
     * @return Arguments passed to the executable.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Get process information.
     * <p/>
     * @return Process information.
     */
    public Process getProcess() {
        return process;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert stored process information to <code>String</code>.
     * <p>
     * @return A <code>String</code> representation of the value of this object.
     */
    @Override
    public String toString() {
        int length = (processName != null ? processName.length() : 0) +
                (arguments != null ? arguments.length() : 0) + 1;
        StringBuilder sb = new StringBuilder(length);
        if (processName != null) {
            sb.append(processName);
        }
        sb.append(' ');
        if (arguments != null) {
            sb.append(arguments);
        }
        return sb.toString();
    }

}
