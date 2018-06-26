/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ko4j.debugging;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * 
 * @author Jan Stola
 */
@ProjectServiceProvider(
    projectType = "org-netbeans-modules-maven",
    service = LateBoundPrerequisitesChecker.class
)
public final class TurnInspectOn implements LateBoundPrerequisitesChecker {
    private static final Logger LOG = Logger.getLogger(TurnInspectOn.class.getName());
    
    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        if (
            ("debug".equals(config.getActionName()) || "run".equals(config.getActionName()))
            && isBootFXOn(config)
        ) {
            int port = Server.getInstance().acceptClient();
            config.setProperty("netbeans.inspect.port", "" + port);
        }
        Object o = config.getActionName();
        return true;
    }

    private static boolean isBootFXOn(RunConfig config) {
        try {
            Method mpMethod = config.getClass().getMethod("getMavenProject");
            Object mp = mpMethod.invoke(config);
            Method artiMethod = mp.getClass().getMethod("getArtifacts");
            Set<?> s = (Set<?>)artiMethod.invoke(mp);
            final String text = s.toString();
            return text.contains("org.apidesign.html:boot-fx:") ||
                text.contains("org.netbeans.html:net.java.html.boot.fx:") ||
                text.contains("org.apidesign.bck2brwsr:bck2brwsr-maven-plugin");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Problems obtaining list of artifacts", ex);
        }
        return false;
    }
}
