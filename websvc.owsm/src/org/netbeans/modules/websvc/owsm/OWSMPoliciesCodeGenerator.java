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
package org.netbeans.modules.websvc.owsm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport;


/**
 * @author ads
 *
 */
@ServiceProvider( service=JaxWsPoliciesCodeGenerator.class )
public class OWSMPoliciesCodeGenerator implements JaxWsPoliciesCodeGenerator {
    
    private static final String ORACLE = "oracle/";             // NOI18N

    private static final String SECURITY_POLICY_FEATURE = 
        "weblogic.wsee.jws.jaxws.owsm.SecurityPolicyFeature";   // NOI18N
    
    private static final String SECURITY_FEATURE_INIT = "private static final " +
        "SecurityPolicyFeature[] securityFeature = new SecurityPolicyFeature[]" +
        " { new SecurityPolicyFeature(\"";         // NOI18N

    @Override
    public Collection<String> getRequiredClasses( String id ) {
        Collection<String> result =  new LinkedList<String>();
        result.add("weblogic.wsee.jws.jaxws.owsm.SecurityPolicyFeature");       // NOI18N
        result.add("com.sun.xml.ws.developer.WSBindingProvider");               // NOI18N
        result.add("javax.xml.ws.BindingProvider");                             // NOI18N   
        result.add("javax.xml.ws.WebServiceFeature");                           // NOI18N   
        result.add(Map.class.getCanonicalName());
        
        OWSMPolicyCodeGenerator generator = GENERATORS.get( id );
        if ( generator != null  ){
            generator.addRequiredClassesFqns( result );
        }
        return result;
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator#generatePolicyAccessCode(java.util.Set, org.netbeans.modules.websvc.api.jaxws.project.config.Client, java.lang.StringBuilder)
     */
    @Override
    public String generatePolicyAccessCode( Set<String> policyIds,
            Client client, StringBuilder code )
    {
        if ( policyIds.isEmpty() ){
            return "";
        }
        Map<String,OWSMPolicyCodeGenerator> map = new HashMap<String, 
            OWSMPolicyCodeGenerator>( GENERATORS);
        Set<String> keySet = map.keySet();
        keySet.retainAll( policyIds );
        if ( keySet.isEmpty() ){
            String id = policyIds.iterator().next();
            generateDefaultCode( id , client , code );
            return id;
        }
        else {
            String mainId = null;
            for (Entry<String, OWSMPolicyCodeGenerator> entry : map.entrySet()) {
                String id = entry.getKey();
                OWSMPolicyCodeGenerator generator = entry.getValue();
                if ( mainId == null ){
                    mainId = id;
                    generator.generatePolicyAccessCode(code, client);
                }
                else {
                    StringBuilder builder = new StringBuilder();
                    generator.generatePolicyAccessCode(builder, client);
                    code.append("/* ");                 // NOI18N
                    code.append( builder.toString() );
                    code.append(" */");                 // NOI18N
                }
            }
            return mainId;
        }
    }

    @Override
    public boolean isApplicable( Set<String> policyIds, J2eePlatform platform,
            FileObject wsdl )
    {
        JaxWsPoliciesSupport support = JaxWsPoliciesSupport.getInstance( platform );
        if ( support == null ){
            return false;
        }
        String id = support.getId();
        boolean result = id!=null && ORACLE.startsWith( id ) && id.length() == ORACLE.length()-1;
        
        if ( result ){
            policyIds.retainAll( GENERATORS.keySet() );
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator#createSecurityFeatureType(org.netbeans.api.java.source.WorkingCopy, org.netbeans.api.java.source.TreeMaker)
     */
    @Override
    public Tree createSecurityFeatureType( WorkingCopy workingCopy,
            TreeMaker make )
    {
        return make.ArrayType( createSecurityFeatureComponentType(workingCopy,
                make) );
    }


    private Tree createSecurityFeatureComponentType( WorkingCopy workingCopy,
            TreeMaker make )
    {
        TypeElement securityType = workingCopy.getElements().getTypeElement(
                SECURITY_POLICY_FEATURE);  
        Tree securityTreeType = securityType != null ? make.Type(securityType.asType()) : 
            make.Identifier(SECURITY_POLICY_FEATURE);
        return securityTreeType;
    }
    
    
    
    private void generateDefaultCode( String id, Client client,
            StringBuilder code )
    {
        code.append("WSBindingProvider wsbp = (WSBindingProvider)port;\n"); // NOI18N
        code.append("Map<String, Object> requestContext = wsbp.getRequestContext();\n");// NOI18N
        code.append("// Override the endpoint - useful when switching target environments without regenerating the jax-ws client\n");// NOI18N
        code.append("requestContext.put(WSBindingProvider.ENDPOINT_ADDRESS_PROPERTY, \"");// NOI18N
        code.append(client.getWsdlUrl());
        code.append("\");\n");                                               // NOI18N
        code.append("// Use request context to initialize poliy access \n" );// NOI18N
        code.append("//requestContext.put( some_property_key,  some_property_value );\n");      // NOI18N  
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator#createSecurityFeatureInitializer(org.netbeans.api.java.source.WorkingCopy, org.netbeans.api.java.source.TreeMaker, java.lang.String)
     */
    @Override
    public ExpressionTree createSecurityFeatureInitializer(
            WorkingCopy workingCopy, TreeMaker make, String id )
    {
        String clientId = id;
        OWSMPolicyCodeGenerator generator = GENERATORS.get(id);
        if ( generator != null){
            clientId = generator.getClientId();
        }
        if ( !clientId.startsWith( ORACLE )){
            clientId = ORACLE+clientId;
        }
        TypeElement securityType = workingCopy.getElements().getTypeElement(
                SECURITY_POLICY_FEATURE);
        NewClassTree initClassTree = make.NewClass(null, 
                Collections.<ExpressionTree>emptyList(), 
                securityType != null ? make.Identifier( securityType): 
                    make.Identifier( SECURITY_POLICY_FEATURE), 
                    Collections.singletonList(
                            make.Literal(clientId)), null);
        return make.NewArray(
                createSecurityFeatureComponentType(workingCopy, make ), 
                Collections.<ExpressionTree>emptyList(), 
                Collections.singletonList(initClassTree) );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator#modifySecurityFeatureAttribute(com.sun.source.tree.VariableTree, org.netbeans.api.java.source.WorkingCopy, org.netbeans.api.java.source.TreeMaker, java.lang.String, java.util.Set)
     */
    @Override
    public void modifySecurityFeatureAttribute( VariableTree var,
            WorkingCopy workingCopy, TreeMaker make, String id,
            Set<String> ids )
    {
        Collection<String> clientIds = new LinkedList<String>();
        Set<String> otherIds = new HashSet<String>(ids );
        otherIds.remove( id );

        Map<String,OWSMPolicyCodeGenerator> map = new HashMap<String, 
            OWSMPolicyCodeGenerator>( GENERATORS);
        Set<String> keySet = map.keySet();
        keySet.retainAll( otherIds );
        for (OWSMPolicyCodeGenerator generator :map.values()) {
            clientIds.add(generator.getClientId());
        }
        
        OWSMPolicyCodeGenerator generator = GENERATORS.get( id );
        if ( generator != null ){
            for( String relatedId : generator.getRelatedPolicyIds() ){
                clientIds.add( relatedId );
            }
        }
        
        addComments(var, make, clientIds );      
    }
    

    private void addComments( VariableTree var , TreeMaker make , 
            Collection<String> ids)
    {
        for (String relatedId : ids) {
            if ( !relatedId.startsWith( ORACLE )){
                relatedId  = ORACLE +relatedId;
            }
            StringBuilder builder = new StringBuilder(SECURITY_FEATURE_INIT);
            builder.append(relatedId);
            builder.append("\")};\n");                      // NOI18N
            make.addComment(var,Comment.create(Comment.Style.LINE, 
                    builder.toString()),true);
        }
    }
    
    private static void initGenerators() {
        Collection<? extends OWSMPolicyCodeGenerator> generators = 
            Lookup.getDefault().lookupAll( OWSMPolicyCodeGenerator.class);
        for (OWSMPolicyCodeGenerator generator : generators) {
            GENERATORS.put( generator.getId() , generator );
        }
    }
    
    private static Map<String,OWSMPolicyCodeGenerator> GENERATORS = 
        new HashMap<String, OWSMPolicyCodeGenerator>();
    
    static {
        initGenerators();
    }
}
