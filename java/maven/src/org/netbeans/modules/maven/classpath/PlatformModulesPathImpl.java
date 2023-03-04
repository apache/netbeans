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
package org.netbeans.modules.maven.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;       

/**
 * 
 * Even though an extremely naive impl, it seems to be sufficient..
 * 
 * @author Tomas Stupka
 */
public class PlatformModulesPathImpl extends AbstractBootPathImpl {
    private static final String PROTOCOL_NBJRT = "nbjrt";   //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PlatformModulesPathImpl.class.getName());
    
    public PlatformModulesPathImpl(NbMavenProjectImpl project) {
        super(project);
    }

    @Override
    protected List<PathResourceImplementation> createResources() {
        final List<PathResourceImplementation> res = new ArrayList<>();
        JavaPlatform pf = findActivePlatform();
        Arrays.stream(new JavaPlatform[] {findActivePlatform()})
            .flatMap((plat)->plat.getBootstrapLibraries().entries().stream())
            .map((entry) -> entry.getURL())
            .filter((root) -> (PROTOCOL_NBJRT.equals(root.getProtocol())))
            .forEach((root)->{res.add(ClassPathSupport.createResource(root));});
        if(LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "PlatformModulesPath for project {0} and platform {1}", new Object[] {project.getProjectDirectory().getPath(), pf.getDisplayName()});
        }
        if(LOGGER.isLoggable(Level.FINER)) {            
            StringBuilder sb = new StringBuilder();
            Iterator<PathResourceImplementation> it = res.iterator();
            while(it.hasNext()) {
                sb.append(it.next());
                if(it.hasNext()) sb.append("\n");
            }
            LOGGER.log(Level.FINER, sb.toString());
        }
        return res;
    }
    
}
