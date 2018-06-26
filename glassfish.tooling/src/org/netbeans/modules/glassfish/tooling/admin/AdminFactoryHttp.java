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
 * GlassFish Server HTTP Command Factory.
 * <p>
 * Selects correct GlassFish server administration functionality using HTTP
 * command interface.
 * <p>
 * Factory is implemented as singleton.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class AdminFactoryHttp extends AdminFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Singleton object instance. */
    private static volatile AdminFactoryHttp instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return <code>AdminFactoryHttp</code> singleton instance.
     */
    static AdminFactoryHttp getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (AdminFactoryHttp.class) {
            if (instance == null) {
                instance = new AdminFactoryHttp();
            }
        }
        return instance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build runner for HTTP command interface execution and connect it with
     * provided <code>Command</code> instance.
     * <p>
     * @param srv GlassFish server entity object.
     * @param cmd GlassFish server administration command entity.
     * @return GlassFish server administration command execution object.
     */
    @Override
    public Runner getRunner(final GlassFishServer srv, final Command cmd) {
        Runner runner;
        Class cmcClass = cmd.getClass();
        RunnerHttpClass rc = (RunnerHttpClass)cmcClass.getAnnotation(
                RunnerHttpClass.class);
        if (rc != null) {
            Class runnerClass = rc.runner();
            String command = rc.command();
            runner = newRunner(srv, cmd, runnerClass);
            if (command != null && command.length() > 0) {
                cmd.command = command;
            }
        }
        else {
            runner = new RunnerHttp(srv, cmd);
        }
        return runner;
    }
    
}
