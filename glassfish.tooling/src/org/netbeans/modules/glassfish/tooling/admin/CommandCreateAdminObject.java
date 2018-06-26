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

import java.util.Map;

/**
 * Command that creates administered object with the specified JNDI name and
 * the interface definition for a resource adapter on server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpCreateAdminObject.class)
public class CommandCreateAdminObject extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create administered object command. */
    private static final String COMMAND = "create-admin-object";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** The JNDI name of this JDBC resource. */
    final String jndiName;

    /** Resource type. */
    final String resType;

    /** The name of the resource adapter associated with this administered
     *  object. */
    final String raName;

    /** Optional properties for configuring administered object. */
    final Map<String, String> properties;

    /** If this object is enabled. */
    final boolean enabled;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server create administered object
     * command entity.
     * <p/>
     * @param jndiName   The JNDI name of this JDBC resource.
     * @param resType    Resource type.
     * @param raName     The name of the resource adapter associated with
     *                   this administered object.
     * @param properties Optional properties for configuring the pool.
     * @param enabled    If this object is enabled.
     */
    public CommandCreateAdminObject(final String jndiName,
            final String resType, final String raName,
            final Map<String, String> properties, final boolean enabled) {
        super(COMMAND);
        this.resType = resType;
        this.jndiName = jndiName;
        this.raName = raName;
        this.properties = properties;
        this.enabled = enabled;
    }

    /**
     * Constructs an instance of GlassFish server create administered object
     * command entity.
     * <p/>
     * This object will be enabled on server by default.
     * <p/>
     * @param jndiName   The JNDI name of this JDBC resource.
     * @param resType    Resource type.
     * @param raName     The name of the resource adapter associated with
     *                   this administered object.
     * @param properties Optional properties for configuring the pool.
     */
    public CommandCreateAdminObject(final String jndiName,
            final String resType, final String raName,
            final Map<String, String> properties) {
        this(jndiName, resType, raName, properties, true);
    }

}
