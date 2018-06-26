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
package org.netbeans.modules.glassfish.tooling.data.cloud;

import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * GlassFish Cloud Entity.
 * <p/>
 * GlassFish cloud entity instance which is used when not defined externally
 * in IDE.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudEntity implements GlassFishCloud {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud name (display name in IDE). */
    protected String name;

    /** GlassFish cloud host. */
    protected String host;

    /** GlassFish cloud port. */
    protected int port;

    /** GlassFish cloud local server. */
    protected GlassFishServer localServer;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs empty class instance. No default values are set.
     */
    public GlassFishCloudEntity() {
    }

    /**
     * Constructs class instance with ALL values set.
     * <p/>
     * @param name        GlassFish cloud name to set.
     * @param host        GlassFish cloud host to set.
     * @param port        GlassFish server port to set.
     * @param localServer GlassFish cloud local server to set.
     */
    public GlassFishCloudEntity(String name, String host, int port,
            GlassFishServer localServer) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud name (display name in IDE).
     * <p/>
     * @return GlassFish cloud name (display name in IDE).
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set GlassFish cloud name (display name in IDE).
     * <p/>
     * @param name GlassFish cloud name to set (display name in IDE).
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get GlassFish cloud host.
     * <p/>
     * @return GlassFish cloud host.
     */
    @Override
    public String getHost() {
        return host;
    }

    /**
     * Set GlassFish cloud host.
     * <p/>
     * @param host GlassFish cloud host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get GlassFish server port.
     * <p/>
     * @return GlassFish server port.
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * Set GlassFish server port.
     * <p/>
     * @param port GlassFish server port to set.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get GlassFish cloud local server.
     * <p/>
     * @return GlassFish cloud local server.
     */
    @Override
    public GlassFishServer getLocalServer() {
        return localServer;
    }

    /**
     * Set GlassFish cloud local server.
     * <p/>
     * @param localServer GlassFish cloud local server to set.
     */
    public void setLocalServer(GlassFishServer localServer) {
        this.localServer = localServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * String representation of this GlassFish cloud entity.
     * <p/>
     * @return String representation of this GlassFish cloud entity.
     */
    @Override
    public String toString() {
        return name;
    }

}
