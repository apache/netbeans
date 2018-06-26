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
 * GlassFish server administration command entity.
 * <p/>
 * Holds common data for administration command.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class Command {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Server command represented by this object. */
    protected String command;

    /** Indicate whether we shall retry command execution. */
    protected boolean retry = false;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server administration command entity
     * with specified server command.
     * <p/>
     * @param command Server command represented by this object.
     */
    protected Command(final String command) {
        this.command = command;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns server command represented by this object.  Set in constructor.
     * e.g. "deploy", "list-applications", etc.
     * <p/>
     * @return command string represented by this object.
     */
    public String getCommand() {
        return command;
    }

    // This is also kind of getter.
    /**
     * Sometimes (e.g. during startup), the server does not accept commands.  In
     * such cases, it will block for 20 seconds and then return with the message
     * "V3 cannot process this command at this time, please wait".
     * <p/>
     * In such cases, we set a flag and have the option to reissue the command.
     * <p/>
     * @return true if server responded with it's "please wait" message.
     */
    public boolean retry() {
        return retry;
    }

}
