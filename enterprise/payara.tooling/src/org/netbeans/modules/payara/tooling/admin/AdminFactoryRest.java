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

import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Payara Server REST Command Factory.
 * <p>
 * Selects correct Payara server administration functionality using REST
 * command interface.
 * <p>
 * Factory is implemented as singleton.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class AdminFactoryRest extends AdminFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Singleton object instance. */
    private static volatile AdminFactoryRest instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return <code>AdminFactoryRest</code> singleton instance.
     */
    static AdminFactoryRest getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (AdminFactoryRest.class) {
            if (instance == null) {
                instance = new AdminFactoryRest();
            }
        }
        return instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build runner for REST command interface execution and connect it with
     * provided <code>Command</code> instance.
     * <p>
     * @param srv Payara server entity object.
     * @param cmd Payara server administration command entity.
     * @return Payara server administration command execution object.
     */
    @Override
    public Runner getRunner(final PayaraServer srv, final Command cmd) {
        Runner runner;
        Class cmcClass = cmd.getClass();
        RunnerRestClass rc = (RunnerRestClass)cmcClass.getAnnotation(
                RunnerRestClass.class);
        if (rc != null) {
            Class runnerClass = rc.runner();
            String command = rc.command();
            runner = newRunner(srv, cmd, runnerClass);
            if (command != null && command.length() > 0) {
                cmd.command = command;
            }
        }
        else {
            runner = new RunnerRest(srv, cmd);
        }
        return runner;
    }

}
