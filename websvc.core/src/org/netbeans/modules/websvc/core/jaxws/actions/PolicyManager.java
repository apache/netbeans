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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.core.jaxws.policies.JaxWsPoliciesCodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;


/**
 * @author ads
 *
 */
class PolicyManager {
    
    static final String SECURITY_FEATURE = "securityFeature";  // NOI18N 

    PolicyManager( String wsdlUrl , FileObject wsdl , Project project) {
        this.wsdlUrl = wsdlUrl;
        this.wsdl  = wsdl;
        this.project =project;
        init();
    }
    
    String getWsdlUrl(){
        return wsdlUrl;
    }
    
    void init( Client client ){
        this.client = client;
    }

    void chosePolicy(){
        if ( support == null ){
            return;
        }
        
        Collection<? extends JaxWsPoliciesCodeGenerator> generators = 
            Lookup.getDefault().lookupAll( JaxWsPoliciesCodeGenerator.class);
        JaxWsPoliciesCodeGenerator policyGenerator = null; 
        for (JaxWsPoliciesCodeGenerator gen : generators){
            if ( gen.isApplicable(policyIds, support.getPlatform(), 
                    getLocalWsdl()))
            {
                policyGenerator = gen;
                break;
            }
        }
        if ( policyGenerator == null ){
            return;
        }
        
        List<String> clientPolicyIds = support.getClientPolicyIds();
        StringBuilder builder = new StringBuilder();
        String clientPolicyId = null;
        if ( policyIds.isEmpty() ){
            clientPolicyId  = choosePolicyId(clientPolicyIds);
            if ( clientPolicyId == null ){
                return;
            }
            policyIds = Collections.singleton( clientPolicyId);
        }

        generator = policyGenerator;
        importFqns = policyGenerator.getRequiredClasses( chosenId );
        support.extendsProjectClasspath(getPorject(), importFqns);
        chosenId = policyGenerator.generatePolicyAccessCode(policyIds , client , 
                builder );     
        optionalCode = builder.toString();
    }
    
    Collection<String> getImports(){
        return importFqns==null?Collections.<String>emptyList():importFqns;
    }
    
    String getOptionalCode(){
        return optionalCode==null?"":optionalCode;
    }
    
    void modifyPortCallInitArguments( Object[]  args ){
        modifyArguments(args);
    }
    
    void modifyPortInvocationInitArguments(  Object[]  args ){
        modifyArguments(args);
    }
    
    boolean isSupported(){
        return generator != null && !policyIds.isEmpty();
    }
    
    Tree createSecurityFeatureType( WorkingCopy workingCopy , TreeMaker make){
        return generator.createSecurityFeatureType( workingCopy , make );
    }
    
    ExpressionTree createSecurityFeatureInitializer( WorkingCopy workingCopy , 
            TreeMaker make)
    {
        return generator.createSecurityFeatureInitializer( workingCopy , make , 
                chosenId );
    }
    
    void modifySecurityFeatureAttribute( VariableTree var , WorkingCopy workingCopy , 
            TreeMaker make)
    {
        generator.modifySecurityFeatureAttribute( var , workingCopy , make , 
                chosenId, policyIds);
    }
    
    private Object[] modifyArguments( Object[] args )
    {
        if ( support == null ){
            return args;
        }
        if ( args[3] == null || args[3].toString().trim().length()==0){
            args[3] = getOptionalCode();
        }
        else {
            args[3] = args[3].toString()+getOptionalCode();
        }
        args[9] = SECURITY_FEATURE;        // NOI18N
        return args;
    }
    
    private String choosePolicyId( List<String> ids ) {
        PoliciesVisualPanel policies = new PoliciesVisualPanel( ids );
        DialogDescriptor desc = new DialogDescriptor(policies, 
                NbBundle.getMessage(PolicyManager.class, 
                        "LBL_ChoosePolicy"));       // NOI18N
        if ( DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(desc) ){
            return policies.getId();
        }
        return null;
    }
    
    private FileObject getLocalWsdl(){
        return wsdl;
    }
    
    private void init() {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(
                J2eeModuleProvider.class);
        if ( moduleProvider != null ){
            String id = moduleProvider.getServerInstanceID();
            support= findPolicySupport(id);
            if ( support != null && !hasServicePolicies(wsdl, 
                    Collections.singletonList( support.getLookup(wsdl))))
            {
                support = null;
            }
        }
        else {
            String[] serverInstanceIds = Deployment.getDefault().getServerInstanceIDs();
            Collection<JaxWsPoliciesSupport> supports = 
                new ArrayList<JaxWsPoliciesSupport>(serverInstanceIds.length);
            List<Lookup> supportsLookup = new ArrayList<Lookup>( serverInstanceIds.length);
            for (String id : serverInstanceIds) {
                JaxWsPoliciesSupport foundSupport = findPolicySupport(id);
                if ( foundSupport != null ){
                    supports.add( foundSupport );
                    supportsLookup.add( foundSupport.getLookup(wsdl));
                }
            }
            if ( supports.isEmpty() ){
                return;
            }
            if ( hasServicePolicies(wsdl, supportsLookup) ){
                int i=0; 
                for( JaxWsPoliciesSupport sup : supports ){
                    if ( sup.supports( wsdl , supportsLookup.get(i))){
                        support = sup;
                        return;
                    }
                    i++;
                }
            }
        }
    }

    private JaxWsPoliciesSupport findPolicySupport( String serverInstanceId ){
        try {
            J2eePlatform j2eePlatform = Deployment.getDefault().
                getServerInstance(serverInstanceId).getJ2eePlatform();
            return JaxWsPoliciesSupport.getInstance(j2eePlatform);
        }
        catch (InstanceRemovedException e){
            Logger.getLogger( PolicyManager.class.getName()).log(Level.INFO , 
                    null, e);
        }
        return null;
        
    }
    
    private Project getPorject(){
        return project;
    }
    
    private boolean hasServicePolicies( FileObject wsdl , 
            Collection<Lookup> supportLokups) 
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            WsdlPolicyHandler handler = new WsdlPolicyHandler( wsdl, supportLokups );
            saxParser.parse(FileUtil.toFile( wsdl), handler );
            policyIds = handler.getPolicyIds();
            return handler.hasPolicy();
        }
        catch (ParserConfigurationException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, e);
        }
        catch (SAXException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, e);
        }
        catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, e);
        }
        return false;
    }
    
    private static final class WsdlPolicyHandler extends DefaultHandler {
        
        private static final String POLICY = "Policy";                  // NOI18N
        
        private static final String ID = "Id";                          // NOI18N
        
        private static final String COLON_ID = ":"+ID;                  // NOI18N
        private static final String COLON_POLICY = ":"+POLICY;          // NOI18N
        
        WsdlPolicyHandler(FileObject wsdl , Collection<Lookup> supportLookups ){
            delegates = new ArrayList<DefaultHandler>( supportLookups.size());
            for (Lookup lookup : supportLookups) {
                DefaultHandler handler = lookup.lookup( DefaultHandler.class );
                delegates.add( handler );
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement( String uri, String localName, String qName )
                throws SAXException
        {
            super.endElement(uri, localName, qName);
            for (DefaultHandler delegate : delegates) {
                delegate.endElement(uri, localName, qName);
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endDocument()
         */
        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            for (DefaultHandler delegate : delegates) {
                delegate.endDocument();
            } 
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(java.lang.String)
         */
        @Override
        public void endPrefixMapping( String prefix ) throws SAXException {
            super.endPrefixMapping(prefix);
            for (DefaultHandler delegate : delegates) {
                delegate.endPrefixMapping(prefix);
            } 
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters( char[] ch, int start, int length )
                throws SAXException
        {
            super.characters(ch, start, length);
            for (DefaultHandler delegate : delegates) {
                delegate.characters(ch, start, length );
            } 
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
         */
        @Override
        public void ignorableWhitespace( char[] ch, int start, int length )
                throws SAXException
        {
            super.ignorableWhitespace(ch, start, length);
            for (DefaultHandler delegate : delegates) {
                delegate.ignorableWhitespace(ch, start, length);
            } 
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void notationDecl( String name, String publicId, String systemId )
                throws SAXException
        {
            super.notationDecl(name, publicId, systemId);
            for (DefaultHandler delegate : delegates) {
                delegate.notationDecl(name, publicId, systemId);
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String, java.lang.String)
         */
        @Override
        public void processingInstruction( String target, String data )
                throws SAXException
        {
            super.processingInstruction(target, data);
            for (DefaultHandler delegate : delegates) {
                delegate.processingInstruction(target, data);
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
         */
        @Override
        public void skippedEntity( String name ) throws SAXException {
            super.skippedEntity(name);
            for (DefaultHandler delegate : delegates) {
                delegate.skippedEntity(name);
            }
        }        
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            for (DefaultHandler delegate : delegates) {
                delegate.startDocument();
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String, java.lang.String)
         */
        @Override
        public void startPrefixMapping( String prefix, String uri )
                throws SAXException
        {
            super.startPrefixMapping(prefix, uri);
            for (DefaultHandler delegate : delegates) {
                delegate.startPrefixMapping(prefix, uri);
            }
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void unparsedEntityDecl( String name, String publicId,
                String systemId, String notationName ) throws SAXException
        {
            super.unparsedEntityDecl(name, publicId, systemId, notationName);
            for (DefaultHandler delegate : delegates) {
                delegate.unparsedEntityDecl(name, publicId, systemId, notationName);
            }
        }
        
        
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement( String uri, String localName, String qName,
                Attributes attributes ) throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            for (DefaultHandler delegate : delegates) {
                delegate.startElement(uri, localName, qName, attributes);
            }
            boolean policy = false;
            if ( localName != null && localName.equals(POLICY)){
                policy = true;
            }
            if ( qName != null && qName.endsWith(COLON_POLICY) ) {
                policy = true;
            }
            if ( !policy ){
                return;
            }
            else {
                hasPolicy = true;
            }
            int count = attributes.getLength();
            for (int i=0; i<count ; i++) {
                String value = attributes.getValue(i);
                String attrLocalName = attributes.getLocalName(i);
                String attrQName = attributes.getQName(i);
                
                if ( (attrLocalName!=null && attrLocalName.equals(ID)) || 
                        (attrLocalName!= null && attrQName.endsWith(COLON_ID)))
                {
                    policies.add( attributes.getValue(i));
                }
            }
        }
        
        boolean hasPolicy(){
            return hasPolicy;
        }
        
        Set<String> getPolicyIds(){
            return policies;
        }
        
        private boolean hasPolicy;
        private Set<String> policies = new HashSet<String>();
        private Collection<DefaultHandler> delegates;
    }
    
    private Set<String> policyIds;
    private final String wsdlUrl;
    private final FileObject wsdl;
    private final Project project;
    
    private Client client;
    private JaxWsPoliciesSupport support;
    private String chosenId;
    private String optionalCode;
    private Collection<String> importFqns;
    private JaxWsPoliciesCodeGenerator generator;
}
