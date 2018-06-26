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
import java.net.URI;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.utils.ServerUtils;

/**
 * Locations command used to determine locations (installation, domain etc.)
 * where the DAS is running.
 * <p/>
 * Result of the command will be in the form of <code>Map<String, String></code>
 * object. The keys to particular locations are as followed:
 * Installation root - "Base-Root_value"
 * Domain root - "Domain-Root_value"
 * <p/>
 * Minimal <code>__locations</code> command support exists since GlassFish
 * 3.0.1 where both Base-Root and Domain-Root values are returned.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpLocation.class)
@RunnerRestClass(runner=RunnerRestLocation.class)
public class CommandLocation extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for location command. */
    private static final String COMMAND = "__locations";

    /** Result key to retrieve <code>Domain-Root</code> value. */
    public static final String DOMAIN_ROOT_RESULT_KEY = "Domain-Root_value";

    /** Result key to retrieve <code>Basic-Root</code> value. */
    public static final String BASIC_ROOT_RESULT_KEY = "Base-Root_value";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Verifies if domain directory returned by location command result matches
     * domain directory of provided GlassFish server entity.
     * <p/>
     * @param resultMap Locations command result.
     * @param server    GlassFish server entity.
     * @return For local server value of <code>true</code> means that domain
     *         directory returned by location command result matches domain
     *         directory of provided GlassFish server entity and value
     *         of <code>false</code> that they differs. For remote serve this
     *         test makes no sense and value of <code>true</code> is always
     *         returned.
     */
    public static boolean verifyResult(
            final ResultMap<String, String> resultMap,
            final GlassFishServer server) {
        if (!server.isRemote()) {
            boolean result = false;
            String domainRootResult
                    = resultMap.getValue().get(DOMAIN_ROOT_RESULT_KEY);
            String domainRootServer = ServerUtils.getDomainPath(server);
            if (domainRootResult != null && domainRootServer != null) {
                URI rootResult = new File(domainRootResult).toURI().normalize();
                URI rootServer = new File(domainRootServer).toURI().normalize();
                if (rootResult != null && rootServer != null) {
                    result = rootServer.equals(rootResult);
                } 
            }
            return result;
        } else {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server location command entity.
     */
    public CommandLocation() {
        super(COMMAND);
    }
    
}
