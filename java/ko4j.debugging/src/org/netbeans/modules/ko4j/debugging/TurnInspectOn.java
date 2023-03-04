/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
