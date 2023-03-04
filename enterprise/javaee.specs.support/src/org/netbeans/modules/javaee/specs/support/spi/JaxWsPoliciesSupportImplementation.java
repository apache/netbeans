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
package org.netbeans.modules.javaee.specs.support.spi;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;



/**
 * @author ads
 *
 */
public interface JaxWsPoliciesSupportImplementation {
    
    /**
     * @return support identifier
     */
    String getId();

    /**
     * Getter for all available client policy ids.
     * @return list of client related policy ids
     */
    List<String> getClientPolicyIds();
    
    /**
     * Getter for all available service policy ids.
     * @return list of service  related policy ids
     */
    List<String> getServicePolicyIds();

    /**
     * Check whether WS ( wsdl ) is supported
     * @param wsdl WS definition local file
     * @param lookup lookup associated with wsdl file
     * @return <code>true</code> if wsdl file is supported 
     */
    boolean supports( FileObject wsdl , Lookup lookup );

    /**
     * Extends <code>projects</code>'s classpath with classes given by their 
     * <code>fqns</code>.
     * @param porject project which classpath should be extended
     * @param fqns full qualified name of classes 
     */
    void extendsProjectClasspath( Project project, Collection<String> fqns );

    /**
     * Access to lookup associated with WSDL.
     * One can put f.e. DefaultHandler into this lookup. 
     * As result WSDL file will be parsed with this handler ( only one parsing 
     * session instead of doing this by each support. )
     * @param wsdl WS definition local file
     * @return related lookup
     */
    Lookup getLookup( FileObject wsdl );
    
    /**
     * Getter for pair collection of policy identifier ( as a key )
     * and full policy description ( as a value ).  
     * @return map of identifier of policy and associated description 
     */
    Map<String, String> getPolicyDescriptions();

}
