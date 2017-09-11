/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core.spi;

/**
 * Represents an Eclipse launch configuration (*.launch file).
 */
public final class LaunchConfiguration {

    /** {@link #getType} for running the plain Java launcher. */
    public static final String TYPE_LOCAL_JAVA_APPLICATION = "org.eclipse.jdt.launching.localJavaApplication";

    private final String name;
    private final String type;
    private final String projectName;
    private final String mainType;
    private final String programArguments;
    private final String vmArguments;
    // XXX should support classpath, and map to NB run.classpath; but format is too tricky to deal with for now

    public LaunchConfiguration(String name, String type, String projectName, String mainType, String programArguments, String vmArguments) {
        this.name = name;
        this.type = type;
        this.projectName = projectName;
        this.mainType = mainType;
        this.programArguments = programArguments;
        this.vmArguments = vmArguments;
    }

    /**
     * Gets the name of the configuration.
     * @return an identifier
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of configuration.
     * @return a classification, e.g. {@link #TYPE_LOCAL_JAVA_APPLICATION}
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the project for which the configuration applies.
     * @return the project name (could be null for a general configuration)
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the main class run to run.
     * @return the main type (as a Java FQN), if set; else null
     */
    public String getMainType() {
        return mainType;
    }

    /**
     * Gets a list of program arguments.
     * @return a (space-separated) list of arguments, if set; else null
     */
    public String getProgramArguments() {
        return programArguments;
    }

    /**
     * Gets a list of (J)VM arguments.
     * @return a (space-separated) list of arguments, if set; else null
     */
    public String getVmArguments() {
        return vmArguments;
    }

}
