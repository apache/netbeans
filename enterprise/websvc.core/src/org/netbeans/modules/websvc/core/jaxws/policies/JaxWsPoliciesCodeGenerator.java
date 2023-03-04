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
package org.netbeans.modules.websvc.core.jaxws.policies;

import java.util.Collection;
import java.util.Set;

import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.openide.filesystems.FileObject;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;


/**
 * @author ads
 *
 */
public interface JaxWsPoliciesCodeGenerator {
    
    /**
     * Check whether generator is applicable to some id from <code>policyIds</code>
     * for target J2EE <code>platform</code> and given WS ( defined by WSDL file ).
     * @param policyIds set of policies which generator check against of
     * @param platform target <code>platform</code>
     * @param wsdl WS definition file
     * @return true if generator is applicable to some of policy id for target platform and WS 
     */
    boolean isApplicable( Set<String> policyIds , J2eePlatform platform , 
            FileObject wsdl );

    /**
     * Getter of classes FQN from which depends generator. 
     * This FQNs will be added as imports into source ocde and 
     * project classpath will be extended with required dependencies.
     * @param id main policy id which is used for client code generation
     * @return FQNs of required classes
     */
    Collection<String> getRequiredClasses( String id );

    /**
     * Policy access code generator.
     * This code will be inserted after WS reference port creation code in 
     * the client and before port method call.
     * Code should contain only one policy id usage. All other could be ignored
     * or put into comments of generated code.
     * @param policyIds set of policy ids found in the WS definition 
     * @param client client information
     * @param code storage for generated java source code which should be added to use the policies from the list
     * @return chosen policy id from the set of <code>policyIds</code>
     */
    String generatePolicyAccessCode( Set<String> policyIds , Client client, StringBuilder code);

    /**
     * Creates type tree for <code>securityFeature</code> attribute declaration.
     * It should be subtype of  javax.xml.ws.WebServiceFeature[].
     * @param workingCopy modification compilation controller
     * @param make tree maker
     * @return declaration type tree
     */
    Tree createSecurityFeatureType( WorkingCopy workingCopy, TreeMaker make );

    /**
     * Creates initializer for <code>securityFeature</code> attribute declaration.
     * <code>id</code> could be either service id or client id.
     * Initializer should contains client id ( which can be identified by service id 
     * if <code>id</code> is service ).
     * @param workingCopy modification compilation controller
     * @param make tree maker
     * @param id main policy id 
     * @return initializer for <code>securityFeature</code> attribute
     */
    ExpressionTree createSecurityFeatureInitializer( WorkingCopy workingCopy,
            TreeMaker make, String id);

    /**
     * Modifies  <code>securityFeature</code> attribute declaration.
     * F.e. add additional comments.
     * Main <code>id</code> could have number of related client ids.
     * This method could add declaration for related and other <code>policyIds</code>
     * in comments for <code>var</code>.
     * @param var <code>securityFeature</code> attribute
     * @param workingCopy  modification compilation controller
     * @param make tree maker
     * @param id main policy id which is used to generate <code>securityFeature</code> attribute
     * @param policyIds policy ids which are found in WS
     */
    void modifySecurityFeatureAttribute( VariableTree var,
            WorkingCopy workingCopy, TreeMaker make, String id, 
            Set<String> policyIds );

}
