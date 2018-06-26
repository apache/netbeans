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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
@ServiceProvider(service=WSEditorProvider.class)
public class OWSMEditorProvider implements WSEditorProvider {

    private static final String ORACLE = "oracle";          // NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider#enable(org.openide.nodes.Node)
     */
    @Override
    public boolean enable( Node node ) {
        Service service = node.getLookup().lookup(Service.class);
        if ( service == null ){
            JaxWsService jaxWsService = node.getLookup().lookup(JaxWsService.class);
            if ( jaxWsService == null ){
                return false;
            }
        }
                
        JaxWsPoliciesSupport support = getPoliciesSupport(node.getLookup());
        if ( support != null ){
            return getSecurityPolicies(support).size()!=0;
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider#createWSEditor(org.openide.util.Lookup)
     */
    @Override
    public WSEditor createWSEditor( Lookup lookup ) {
        JaxWsPoliciesSupport support = getPoliciesSupport(lookup);
        return new OWSMPoliciesEditor( support , lookup, getSecurityPolicies(support));
    }
    
    private List<String> getSecurityPolicies( JaxWsPoliciesSupport support ) {
        List<String> list = support.getServicePolicyIds();
        Set<String> set = new LinkedHashSet<String>( SECURITY_POLICIES );
        set.retainAll( list );
        
        List<String> result = new ArrayList<String>( list.size() );
        for (String id : set) {
            result.add( ORACLE + id);
        }
        return result;
    }
    
    private JaxWsPoliciesSupport getPoliciesSupport(Lookup lookup ){
        FileObject srcFile = lookup.lookup(FileObject.class);
        if (srcFile != null) {
            Project prj = FileOwnerQuery.getOwner(srcFile);
            J2eeModuleProvider moduleProvider = prj.getLookup().lookup(
                    J2eeModuleProvider.class);
            if ( moduleProvider != null ){
                String id = moduleProvider.getServerInstanceID();
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().
                        getServerInstance(id).getJ2eePlatform();
                    JaxWsPoliciesSupport support = 
                        JaxWsPoliciesSupport.getInstance(j2eePlatform);
                    if ( support != null && ORACLE.equals(support.getId())){
                        return support;
                    }
                }
                catch (InstanceRemovedException e){
                    Logger.getLogger( OWSMEditorProvider.class.getName()).log(
                            Level.INFO ,null, e);
                }
            }
        }
        return null;
    }
    
    private static Set<String> SECURITY_POLICIES = new LinkedHashSet<String>();
    
    static {
        SECURITY_POLICIES.add("binding_authorization_denyall_policy");      // NOI18N
        SECURITY_POLICIES.add("binding_authorization_permitall_policy");  // NOI18N
        SECURITY_POLICIES.add("binding_permission_authorization_policy");  // NOI18N
        SECURITY_POLICIES.add("no_authentication_service_policy");  // NOI18N
        SECURITY_POLICIES.add("no_authorization_service_policy");  // NOI18N
        SECURITY_POLICIES.add("no_messageprotection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("sts_trust_config_service_policy");  // NOI18N
        SECURITY_POLICIES.add("whitelist_authorization_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_http_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_http_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_or_username_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_or_username_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_token_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml20_token_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml20_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_sts_issued_saml_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_username_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_username_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_hok_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_integrity_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_protection_ski_basic256_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml20_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml20_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_id_propagation_with_msg_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_token_with_message_protection_ski_basic256_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_x509_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_with_message_protection_basic128_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml_or_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml20_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_sts_issued_saml_hok_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_x509_token_with_message_protection_service_policy");  // NOI18N
    }

}
