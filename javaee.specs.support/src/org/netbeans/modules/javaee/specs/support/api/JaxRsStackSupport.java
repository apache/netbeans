/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
