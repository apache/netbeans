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
package org.netbeans.modules.javaee.specs.support.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.javaee.specs.support.bridge.IdeJaxRsSupportImpl;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.openide.util.Parameters;


/**
 * @author ads
 *
 */
public final class JaxRsStackSupport {
    
    private final JaxRsStackSupportImplementation impl;
    
    private JaxRsStackSupport(JaxRsStackSupportImplementation impl){
        this.impl = impl;
    }
    

    @CheckForNull
    private static JaxRsStackSupport getInstance(@NonNull J2eePlatform platform){
        Parameters.notNull("platform", platform);
        JaxRsStackSupportImplementation support = platform.getLookup().lookup(
                JaxRsStackSupportImplementation.class);
        if ( support == null ){
            return null;
        }
        return new JaxRsStackSupport( support );
    }


    /**
     * Returns JaxRsStackSupport based on project's application server. It is
     * possible that server does not have any JaxRsStackSupport implementation
     * in which case this method returns null and API client can fallback on default
     * JaxRsStackSupport provided by method {@link #getDefault()}
     */
    @CheckForNull
    public static JaxRsStackSupport getInstance(@NonNull Project project) {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(
                J2eeModuleProvider.class);
        if (moduleProvider != null) {
            try {
                String id = moduleProvider.getServerInstanceID();
                if ( id == null ){
                    return null;
                }
                J2eePlatform j2eePlatform = Deployment.getDefault().
                    getServerInstance(id).getJ2eePlatform();
                JaxRsStackSupportImplementation support = j2eePlatform.getLookup().lookup(
                        JaxRsStackSupportImplementation.class);
                if ( support != null ){
                    return new JaxRsStackSupport( support );
                }
            } catch (InstanceRemovedException ex) {
                // ignore
            }
        }
        return null;
    }
    
    public static JaxRsStackSupport getDefault(){
        return new JaxRsStackSupport(new IdeJaxRsSupportImpl());
    }
    
    /**
     * Adds JSR311 API into project's classpath if it supported.
     * Returns <code>false</code> if jsr311 is  not added ( not supported).
     * @param project project which classpath should be extended
     * @return <code>true</code> if project's classpath is extended with jsr311
     */
    public boolean addJsr311Api(Project project){
        return impl.addJsr311Api( project );
    }

    /**
     * Extends project's classpath  with Jersey libraries
     * @param project project which classpath should be extended
     * @return  <code>true</code> if project's classpath is extended
     */
    public boolean extendsJerseyProjectClasspath( Project project  ){
        return impl.extendsJerseyProjectClasspath( project );
    }

    /**
     * Clear project classapth .
     * I.e. removes all JaxRs libraries ( both JSR311 API and Jersey ) from project classpath .
     * @param project project which classpath should be cleared
     */
    public void removeJaxRsLibraries(Project project) {
        impl.removeJaxRsLibraries(project); 
    }
    
    /**
     * If custom Jersey library is chosen ( f.e. NB bundled ) then 
     * some JEE servers require additional project configuration.
     * Otherwise collision could happen between bundled server Jersey and custom library.
     */
    public void configureCustomJersey( Project project ){
        impl.configureCustomJersey( project );
    }
    
    /**
     * Checks whether class with <code>classFqn</code> FQN is bundled with
     * JEE server distribution. This information allows to use classes for REST
     * service configuration.  
     * @param classFqn class FQN 
     * @return true if JEE server bundled with <code>classFqn</code> 
     */
    public boolean isBundled(String classFqn ){
        return impl.isBundled( classFqn );
    }
}
