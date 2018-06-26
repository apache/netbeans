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
package org.netbeans.modules.glassfish.tooling.server.state;

import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServerStatus;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * GlassFish server status entity.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishStatusEntity implements GlassFishServerStatus {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(GlassFishStatusEntity.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server entity. */
    private GlassFishServer server;

    /** Current GlassFish server status. */
    private GlassFishStatus status;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server status entity.
     * <p/>
     * Initial server status value is set as unknown.
     * <p/>
     * @param server GlassFish server entity.
     */
    public GlassFishStatusEntity(final GlassFishServer server) {
        this.server = server;
        this.status = GlassFishStatus.UNKNOWN;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish server entity.
     * <p/>
     * @return GlassFish server entity.
     */
    @Override
    public GlassFishServer getServer() {
        return server;
    }

    /**
     * Set GlassFish server entity.
     * <p/>
     * @param server GlassFish server entity.
     */
    void setServer(final GlassFishServer server) {
        this.server = server;
    }

    /**
     * Get current GlassFish server status.
     * <p/>
     * @return Current GlassFish server status.
     */
    @Override
    public GlassFishStatus getStatus() {
        return status;
    }

    /**
     * Set current GlassFish server status.
     * <p/>
     * @param status Current GlassFish server status.
     */
    void setStatus(final GlassFishStatus status) {
        this.status = status;
    }

}
