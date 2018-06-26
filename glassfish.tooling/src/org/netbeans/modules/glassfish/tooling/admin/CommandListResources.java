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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Command that retrieves list of JDBC resources defined on server.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpListResources.class)
@RunnerRestClass(runner=RunnerRestListResources.class)
public class CommandListResources extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandListResources.class);

    /** Command string prefix used to construct list JDBC resources
     *  HTTP command. */
    private static final String COMMAND_PREFIX = "list-";

    /** Command string suffix used to construct list JDBC resources
     *  HTTP command. */
    private static final String COMMAND_SUFFIX = "s";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add resource to target server.
     * <p/>
     * @param server    GlassFish server entity.
     * @param cmdSuffix Resource command suffix. Value should not be null.
     * @param target              GlassFish server target.
     * @return Add resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultList<String> listResources(final GlassFishServer server,
            final String cmdSuffix, final String target)
            throws GlassFishIdeException {
        final String METHOD = "listResources";
        Command command = new CommandListResources(command(cmdSuffix), target);
        Future<ResultList<String>> future =
                ServerAdmin.<ResultList<String>>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        }
    }

    /**
     * Constructs command string for provided resource command suffix.
     * <p/>
     * @param resourceCmdSuffix Resource command suffix. Value should not
     *                          be null.
     */
    public static String command(String resourceCmdSuffix) {
        StringBuilder sb = new StringBuilder(COMMAND_PREFIX.length()
                + COMMAND_SUFFIX.length() + resourceCmdSuffix.length());
        sb.append(COMMAND_PREFIX);
        sb.append(resourceCmdSuffix);
        sb.append(COMMAND_SUFFIX);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server list JDBC resources
     * command entity.
     * <p/>
     * Command string is supplied as an argument.
     * <p/>
     * @param command Server command represented by this object. Use
     *                <code>command</code> static method to build this string
     *                using resource command suffix.
     * @param target  Target GlassFish instance or cluster.
     */
    public CommandListResources(final String command, final String target) {
        super(command, target);
    }
}
