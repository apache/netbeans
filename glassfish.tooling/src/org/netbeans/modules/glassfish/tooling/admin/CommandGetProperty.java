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

import java.util.concurrent.*;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Command that retrieves property (properties) from server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpGetProperty.class)
@RunnerRestClass(runner=RunnerRestGetProperty.class)
public class CommandGetProperty extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandGetProperty.class);

    /** Command string for create-cluster command. */
    private static final String COMMAND = "get";
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Pattern that defines properties to retrieve. */
    String propertyPattern;
    

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve properties from server.
     * <p/>
     * @param server          GlassFish server entity.
     * @param propertyPattern Pattern that defines properties to retrieve.
     * @return GlassFish command result containing map with key-value pairs
     *         returned by server.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultMap<String, String> getProperties(
            final GlassFishServer server, final String propertyPattern)
            throws GlassFishIdeException {
        final String METHOD = "getProperties";
        Future<ResultMap<String, String>> future =
                ServerAdmin.<ResultMap<String, String>>exec(
                server, new CommandGetProperty(propertyPattern));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception", propertyPattern), ee);
        }
    }
    
    /**
     * Retrieve properties from server with timeout.
     * <p/>
     * @param server          GlassFish server entity.
     * @param propertyPattern Pattern that defines properties to retrieve.
     * @param timeout         Administration command execution timeout [ms].
     * @return GlassFish command result containing map with key-value pairs
     *         returned by server.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultMap<String, String> getProperties(
            final GlassFishServer server, final String propertyPattern,
            final long timeout)
            throws GlassFishIdeException {
        final String METHOD = "getProperties";
        Future<ResultMap<String, String>> future =
                ServerAdmin.<ResultMap<String, String>>exec(
                server, new CommandGetProperty(propertyPattern));
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception", propertyPattern), ee);
        } catch (TimeoutException te) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exceptionWithTimeout",
                    propertyPattern, Long.toString(timeout)), te);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server get property command entity.
     * <p/>
     * @param property Pattern that defines property tor retrieve.
     */
    public CommandGetProperty(final String property) {
        super(COMMAND);
        propertyPattern = property;
    }
}
