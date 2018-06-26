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
 * GlassFish Server Start DAS Command Entity.
 * <p/>
 * Holds data for command. Objects of this class are created by API user.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerLocal.class)
@RunnerRestClass(runner=RunnerLocal.class)
public class CommandStartDAS extends CommandJavaClassPath {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** No command string is needed for Start DAS command but we may use it
     *  in logs. */
    private static final String COMMAND = "start-das";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** JVM options to be passed to java executable.
        Typically options as as <code>-D&lt;name&gt;=&lt;value&gt;</code>
        or <code>-X&lt;option&gt</code>. 
    */
    final String javaOpts;

    /** GlassFish specific arguments to be passed to
     *  bootstrap main method, e.g. <code>--domain domain_name</code>. */
    final String glassfishArgs;
    
    /** GlassFish server domain directory (full path). */
    final String domainDir;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server start DAS command entity.
     * @param javaHome      Java SE home used to select JRE for GlassFish
     *                      server.
     * @param classPath     Java SE class path.
     * @param javaOptions   JVM options to be passed to java executable.
     * @param glassfishArgs GlassFish specific arguments to be passed
     *                      to bootstrap main method.
     * @param domainDir     GlassFish server domain directory (full path).
     */
    public CommandStartDAS(final String javaHome, final String classPath,
            final String javaOptions, final String glassfishArgs,
            final String domainDir) {
        super(COMMAND, javaHome, classPath);
        this.javaOpts = javaOptions;
        this.glassfishArgs = glassfishArgs;
        this.domainDir = domainDir;
    }

}
