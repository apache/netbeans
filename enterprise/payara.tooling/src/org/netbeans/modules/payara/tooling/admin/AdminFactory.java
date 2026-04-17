/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.tooling.admin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.netbeans.modules.payara.tooling.data.PayaraAdminInterface;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;
import org.netbeans.modules.payara.tooling.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Abstract Server Command Factory.
 * <p/>
 * Selects correct Payara server administration functionality depending
 * on given Payara server entity object.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 * @author Gaurav Gupta
 */
public abstract class AdminFactory {

    // Class attributes                                                       //
    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(AdminFactory.class);

    // Static methods                                                         //
    /**
     * Creates specific <code>AdminFactory</code> child class instance
     * to build Payara server administration command runner and data objects
     * based on provided Payara server version.
     * <p>
     * @param version Payara server version.
     * @return Child factory class instance to work with given Payara server.
     */
    static AdminFactory getInstance(final PayaraPlatformVersionAPI version)
            throws CommandException {
        if(version.isMinimumSupportedVersion()) {
                return AdminFactoryRest.getInstance();
        } else {
            // Anything else is not unknown.
                throw new CommandException(CommandException.UNKNOWN_VERSION);
        }
    }

    /**
     * Creates specific <code>AdminFactory</code> child class instance
     * to build Payara server administration command runner and data objects
     * based on provided Payara server administration interface type.
     * <p/>
     * @param adminInterface Payara server administration interface type.
     * @return Child factory class instance to work with given Payara server.
     */
    public static AdminFactory getInstance(
            final PayaraAdminInterface adminInterface) throws CommandException {
        switch (adminInterface) {
            case REST: return AdminFactoryRest.getInstance();
            case HTTP: return AdminFactoryHttp.getInstance();
            // Anything else is unknown.
            default:
                throw new CommandException(
                        CommandException.UNKNOWN_ADMIN_INTERFACE);
        }
    }

    // Abstract methods                                                       //
    /**
     * Build runner for command interface execution and connect it with
     * provided <code>Command</code> instance.
     * <p/>
     * @param srv Target Payara server.
     * @param cmd Payara server administration command entity.
     * @return Payara server administration command execution object.
     */
    public abstract Runner getRunner(
            final PayaraServer srv, final Command cmd);

    // Methods                                                                //
    /**
     * Constructs an instance of selected <code>Runner</code> child class.
     * <p/>
     * @param srv Target Payara server.
     * @param cmd Payara server administration command entity.
     * @param runnerClass Class of newly instantiated <code>runner</code>
     * @return Payara server administration command execution object.
     * @throws <code>CommandException</code> if construction of new instance
     *         fails.
     */
    Runner newRunner(final PayaraServer srv, final Command cmd,
            final Class runnerClass) throws CommandException {
        final String METHOD = "newRunner";
        Constructor<Runner> con = null;
        Runner runner = null;
        try {
            con = runnerClass.getConstructor(PayaraServer.class, Command.class);
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
