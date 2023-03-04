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
