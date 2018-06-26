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

import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * GlassFish restart DAS administration command with
 * <code></code> query execution using HTTP interface.
 * <p/>
 * Contains code for command that is called with
 * <code>debug=true|false&force=true|false&kill=true|false</code> query string.
 * <p/>
 * Class implements GlassFish server administration functionality trough HTTP
 * interface.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpRestartDAS extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Restart DAS command <code>debug</code> parameter's name. */
    private static final String DEBUG_PARAM = "debug";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Builds restart DAS query string for given command.
     * <p/>
     * <code>debug=true|false&force=true|false&kill=true|false</code>
     * <p/>
     * @param command GlassFish Server Administration Command Entity.
     *                <code>CommandRestartDAS</code> instance is expected.
     * @return Restart DAS query string for given command.
     */
    static String query(final Command command) {
        if (command instanceof CommandRestartDAS) {
            boolean debug = ((CommandRestartDAS)command).debug;
            int boolValSize = FALSE_VALUE.length() > TRUE_VALUE.length()
                    ? FALSE_VALUE.length() : TRUE_VALUE.length();
            StringBuilder sb = new StringBuilder(DEBUG_PARAM.length()
                    + boolValSize + 1);
            sb.append(DEBUG_PARAM);
            sb.append(PARAM_ASSIGN_VALUE);
            sb.append(debug ? TRUE_VALUE : FALSE_VALUE);
            return sb.toString();
        }
        else {
            throw new CommandException(
                    CommandException.ILLEGAL_COMAND_INSTANCE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerHttpRestartDAS(final GlassFishServer server,
            final Command command) {
        super(server, command, query(command));
    }

}
