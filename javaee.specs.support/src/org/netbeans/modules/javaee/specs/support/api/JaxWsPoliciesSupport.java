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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.spi.JaxWsPoliciesSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;



/**
 * @author ads
 *
 */
public final class JaxWsPoliciesSupport {

    private final JaxWsPoliciesSupportImplementation impl;
    private final J2eePlatform platform;

    private JaxWsPoliciesSupport(JaxWsPoliciesSupportImplementation impl,
            J2eePlatform platform ) 
    {
        this.impl = impl;
        this.platform = platform;
    }
    
    @NonNull
    public static JaxWsPoliciesSupport getInstance(@NonNull J2eePlatform platform) {
        Parameters.notNull("platform", platform);
        JaxWsPoliciesSupportImplementation impl = platform.getLookup().
            lookup(JaxWsPoliciesSupportImplementation.class);
        if (impl != null) {
            return new JaxWsPoliciesSupport(impl, platform );
        }
        return null;
    }
    
    
    /**
     * Identifier allows to distinguish supports
     * @return identifier of support
     */
    public String getId(){
        return impl.getId();
    }
    
    
    /**
     * Getter for J2EE Platform support target
     * @return platform which has the support
     */
    public J2eePlatform getPlatform(){
        return platform;
    }
    
    /**
     * Getter for all available client policy ids.
     * @return list of client related policy ids
     */
    public List<String> getClientPolicyIds(){
        return impl.getClientPolicyIds();
    }
    
    /**
     * Getter for pair collection of policy identifier ( as a key )
     * and full policy description ( as a value ).  
     * @return map of identifier of policy and associated description 
     */
    public Map<String,String> getPolicyDescriptions(){
        return impl.getPolicyDescriptions();
    }
    
    /**
     * Getter for all available service policy ids.
     * @return list of service  related policy ids
     */
    public List<String> getServicePolicyIds(){
        return impl.getServicePolicyIds();
    }

    /**
     * Check whether WS ( wsdl ) is supported
     * @param wsdl WS definition local file
     * @param lookup lookup associated with wsdl file
     * @return <code>true</code> if wsdl file is supported 
     */
    public boolean supports( FileObject wsdl , Lookup lookup) {
        return impl.supports(wsdl, lookup);
    }

    /**
     * Extends <code>projects</code>'s classpath with classes given by their 
     * <code>fqns</code>.
     * @param porject project which classpath should be extended
     * @param fqns full qualified name of classes 
     */
    public void extendsProjectClasspath( Project project,
            Collection<String> fqns )
    {
        impl.extendsProjectClasspath(project, fqns );        
    }
    
    /**
     * Access to lookup associated with WSDL.
     * One can put f.e. DefaultHandler into this lookup. 
     * As result WSDL file will be parsed with this handler ( only one parsing 
     * session instead of doing this by each support. )
     * @param wsdl WS definition local file
     * @return related lookup
     */
    public Lookup getLookup(FileObject wsdl){
        return impl.getLookup(wsdl );      
    }

}
