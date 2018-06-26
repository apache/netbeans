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

import java.io.File;
import java.util.concurrent.*;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Command registers resources defined in provided xml file on specified target.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
@RunnerHttpClass(runner=RunnerHttpAddResources.class)
@RunnerRestClass(runner=RunnerRestAddResources.class)
public class CommandAddResources extends CommandTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(CommandAddResources.class);

    /** Command string for create-cluster command. */
    private static final String COMMAND = "add-resources";
    
    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add resource to target server.
     * <p/>
     * @param server              GlassFish server entity.
     * @param xmlResourceFile     File object pointing to XML file containing
     *                            resources to be added.
     * @param target              GlassFish server target.
     * @return Add resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString addResource(final GlassFishServer server,
            final File xmlResourceFile, final String target)
            throws GlassFishIdeException {
        final String METHOD = "addResource";
        Command command = new CommandAddResources(xmlResourceFile, target);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        }
    }

    /**
     * Add resource to target server.
     * <p/>
     * @param server              GlassFish server entity.
     * @param xmlResourceFile     File object pointing to XML file containing
     *                            resources to be added.
     * @param target              GlassFish server target.
     * @param timeout             Administration command execution timeout [ms].
     * @return Add resource task response.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString addResource(final GlassFishServer server,
            final File xmlResourceFile, final String target, final long timeout)
            throws GlassFishIdeException {
        final String METHOD = "addResource";
        Command command = new CommandAddResources(xmlResourceFile, target);
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException
                | CancellationException ie) {
            throw new GlassFishIdeException(
                    LOGGER.excMsg(METHOD, "exception"), ie);
        } catch (TimeoutException te) {
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD,
                    "exceptionWithTimeout", Long.toString(timeout)), te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** File object pointing to xml file that contains resources to be added. */
    File xmlResFile;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server add-resources command entity.
     * <p/>
     * @param xmlResourceFile     File object pointing to XML file containing
     *                            resources to be added.
     * @param target              GlassFish server target.
     */
    public CommandAddResources(
            final File xmlResourceFile, final String target) {
        super(COMMAND, target);
        xmlResFile = xmlResourceFile;
    }
}
