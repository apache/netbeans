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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.data.GlassFishAdminInterface;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * GlassFish Abstract Server Command Factory.
 * <p/>
 * Selects correct GlassFish server administration functionality depending
 * on given GlassFish server entity object.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class AdminFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AdminFactory.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates specific <code>AdminFactory</code> child class instance
     * to build GlassFish server administration command runner and data objects
     * based on provided GlassFish server version.
     * <p>
     * @param version GlassFish server version.
     * @return Child factory class instance to work with given GlassFish server.
     */
    static AdminFactory getInstance(final GlassFishVersion version)
            throws CommandException {
        switch (version) {
            // Use HTTP interface for any GlassFish older than 3.
            case GF_1:
                throw new CommandException(
                        CommandException.UNSUPPORTED_VERSION);
            case GF_2:
            case GF_2_1:
            case GF_2_1_1:
                return AdminFactoryHttp.getInstance();
            // Use REST interface for GlassFish 3 and 4.
            case GF_3:
            case GF_3_0_1:
            case GF_3_1:
            case GF_3_1_1:
            case GF_3_1_2:
            case GF_4:
                return AdminFactoryRest.getInstance();
            // Anything else is not unknown.
            default:
                throw new CommandException(CommandException.UNKNOWN_VERSION);
        }
    }

    /**
     * Creates specific <code>AdminFactory</code> child class instance
     * to build GlassFish server administration command runner and data objects
     * based on provided GlassFish server administration interface type.
     * <p/>
     * @param adminInterface GlassFish server administration interface type.
     * @return Child factory class instance to work with given GlassFish server.
     */
    public static AdminFactory getInstance(
            final GlassFishAdminInterface adminInterface) throws CommandException {
        switch (adminInterface) {
            case REST: return AdminFactoryRest.getInstance();
            case HTTP: return AdminFactoryHttp.getInstance();
            // Anything else is unknown.
            default:
                throw new CommandException(
                        CommandException.UNKNOWN_ADMIN_INTERFACE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build runner for command interface execution and connect it with
     * provided <code>Command</code> instance.
     * <p/>
     * @param srv Target GlassFish server.
     * @param cmd GlassFish server administration command entity.
     * @return GlassFish server administration command execution object.
     */
    public abstract Runner getRunner(
            final GlassFishServer srv, final Command cmd);

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of selected <code>Runner</code> child class.
     * <p/>
     * @param srv Target GlassFish server.
     * @param cmd GlassFish server administration command entity.
     * @param runnerClass Class of newly instantiated <code>runner</code>
     * @return GlassFish server administration command execution object.
     * @throws <code>CommandException</code> if construction of new instance
     *         fails.
     */
    Runner newRunner(final GlassFishServer srv, final Command cmd,
            final Class runnerClass) throws CommandException {
        final String METHOD = "newRunner";
        Constructor<Runner> con = null;
        Runner runner = null;
        try {
            con = runnerClass.getConstructor(
                    GlassFishServer.class, Command.class);
        } catch (NoSuchMethodException | SecurityException nsme) {
            throw new CommandException(CommandException.RUNNER_INIT, nsme);
        }
        if (con == null) {
            return runner;
        }
        try {
            runner = con.newInstance(srv, cmd);
        } catch (InstantiationException | IllegalAccessException ie) {
            throw new CommandException(CommandException.RUNNER_INIT, ie);
        } catch (InvocationTargetException ite) {
            LOGGER.log(Level.WARNING, "exceptionMsg", ite.getMessage());
            Throwable t = ite.getCause();
            if (t != null) {
                LOGGER.log(Level.WARNING, "cause", t.getMessage());
            }
            throw new CommandException(CommandException.RUNNER_INIT, ite);
        }
        return runner;
    }

}
