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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.rest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Modifier;
import javax.xml.bind.JAXBException;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.GuardedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.AbstractTask;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.jaxb.Authenticator;
import org.netbeans.modules.websvc.saas.model.jaxb.Params;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey;
import org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor;
import org.netbeans.modules.websvc.saas.model.jaxb.Sign;
import org.netbeans.modules.websvc.saas.model.jaxb.TemplateType;
import org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates;
import org.netbeans.modules.websvc.saas.model.oauth.Metadata;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.websvc.rest.RestUtils;

/**
 *
 * @author Milan Kuchtiak
 */
public class ClientJavaSourceHelper {

    public static void generateJerseyClient(Node resourceNode, 
            FileObject targetFo, String className) 
    {
        generateJerseyClient(resourceNode, targetFo, className, 
                new Security(false, Security.Authentication.NONE));
    }

    public static void generateJerseyClient(Node resourceNode, 
            FileObject targetFo, String className, Security security) 
    {
        /*
         *  TODO : project's classpath should be extended with Jersey 2.X in cases
         *  - not web project
         *  - REST client uses some not JAX-RS 2.0 features (but Jersey2.X) web project 
         */
        Project project = FileOwnerQuery.getOwner(targetFo);
        ClassPath cp = ClassPath.getClassPath(targetFo, ClassPath.COMPILE);
        boolean jersey1Available = cp != null &&
                cp.findResource("com/sun/jersey/api/client/WebResource.class") != null;
        boolean jersey2Available = cp != null &&
                cp.findResource("org/glassfish/jersey/spi/Contract.class") != null;
        boolean jaxRs1Available = cp != null &&
                cp.findResource("javax/ws/rs/core/Application.class") != null;
        boolean jaxRs2Available = cp != null &&
                cp.findResource("javax/ws/rs/client/Client.class") != null;
        JaxRsStackSupport support = JaxRsStackSupport.getInstance(project);
        boolean jersey1AvailableOnServer = support != null &&
                support.isBundled("com.sun.jersey.api.client.WebResource");
        boolean jersey2AvailableOnServer = support != null &&
                support.isBundled("org.glassfish.jersey.spi.Contract");
        ClientGenerationStrategy strategy = null;
        if (jersey2Available || jersey2AvailableOnServer) {
            strategy = new JaxRsGenerationStrategy();
        } else if (jersey1Available || jersey1AvailableOnServer) {
            strategy = new JerseyGenerationStrategy();
        }
        if (project != null && strategy == null) {

            if (jaxRs2Available) {
                strategy = new JaxRsGenerationStrategy();
            } else if (jaxRs1Available) {
                // JAX-RS 1.0 is on classpath but no Jersey; in this case project
                // classpath needs to be enhanced with Jersey library but IDE has
                // only Jersey 2.0. That's why JaxRsGenerationStrategy strategy is
                // going to be used here:
                strategy = new JaxRsGenerationStrategy();
            }
        }
        // if all other tests were negative then generate the code using JAX-RS 2:
        if (strategy == null) {
            strategy = new JaxRsGenerationStrategy();
        }
        ProgressHandle handle = null;
        if (support == null) {
            support = JaxRsStackSupport.getDefault();
        }
        boolean requiresJersey = strategy.requiresJersey(resourceNode, security);
        /* TODO : Jersey library should be added into classpath if requiresJersey is true
         * below is Jersey 1.X based code which requires Jersey in the project's classpath.
         * New implementation should extend project's classpth with Jersey
         * library if requiresJersey is true and for non Java EE 7 profile projects.
         * Probably two Jersey versions are required because Jersey 2.X
         * is based on JAX-RS 2.0 which is not supported by Java EE 6.... 
         */ 
        try {
            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ClientJavaSourceHelper.class, "MSG_creatingRESTClient"));
            handle.start();
            // add REST and Jersey dependencies
            if (!jaxRs2Available && !jaxRs1Available) {
                support.addJsr311Api(project);
                support.extendsJerseyProjectClasspath(project);
            }
            if (requiresJersey && !jersey1Available && !jersey2Available)
            {
                support.extendsJerseyProjectClasspath(project);
            }

            // set target project type
            // PENDING: need to consider web project as well
            String targetProjectType = null;
            if (project != null) {
                targetProjectType = Wadl2JavaHelper.getProjectType(project); //NOI18N
            } else {
                targetProjectType = Wadl2JavaHelper.PROJEC_TYPE_DESKTOP;
            }
            if (targetProjectType == Wadl2JavaHelper.PROJEC_TYPE_WEB) {
                if (jersey2Available || jersey2AvailableOnServer) {
                    targetProjectType = Wadl2JavaHelper.PROJEC_TYPE_WEB_EE7;
                }
            }
            
            security.setProjectType(targetProjectType);
            
            RestServiceDescription restServiceDesc = resourceNode.getLookup().lookup(RestServiceDescription.class);
            if (restServiceDesc != null) {
                String uriTemplate = restServiceDesc.getUriTemplate();
                if (uriTemplate != null) {

                    PathFormat pf = null;
                    if (uriTemplate.length() == 0) { // subresource locator
                        // find recursively the root resource
                        ResourcePath rootResourcePath = getResourcePath(resourceNode, restServiceDesc.getClassName(), "");
                        uriTemplate = rootResourcePath.getPath();
                        pf = rootResourcePath.getPathFormat();
                    } else {
                        pf = ClientGenerationStrategy.getPathFormat(uriTemplate);
                    }
                    // compute baseURL
                    Project prj = resourceNode.getLookup().lookup(Project.class);
                    String baseURL =
                            (prj == null ? "" : getBaseURL(prj));
                    if (baseURL.endsWith("/")) {
                        baseURL = baseURL.substring(0, baseURL.length() - 1);
                    }

                    // add inner Jersey Client class
                    addJerseyClient(
                            JavaSource.forFileObject(targetFo),
                            className,
                            baseURL,
                            restServiceDesc,
                            null,
                            pf,
                            security, strategy);
                }
            } else {
                WadlSaasResource saasResource = resourceNode.getLookup().lookup(WadlSaasResource.class);
                if (saasResource != null) {

                    addSecurityMetadata(security, saasResource);

                    if (Wadl2JavaHelper.PROJEC_TYPE_WEB.equals(security.getProjectType()) && 
                            (Security.Authentication.SESSION_KEY == security.getAuthentication() || Security.Authentication.OAUTH == security.getAuthentication())) {
                        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);

                        if (restSupport != null) {
                            security.setDeploymentDescriptor(RestUtils.getDeploymentDescriptor(project));
                        }
                    }

                    String baseUrl = saasResource.getSaas().getBaseURL();

                    ResourcePath resourcePath = getResourcePath(saasResource);
                    PathFormat pf = resourcePath.getPathFormat();
                    addJerseyClient(
                            JavaSource.forFileObject(targetFo),
                            className,
                            baseUrl,
                            null,
                            saasResource,
                            pf,
                            security, strategy );

                    // add JAXB request/response types from wadl file
                    try {
                        Wadl2JavaHelper.generateJaxb(targetFo, saasResource.getSaas());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // checking if Openide modules are on the classpath
                    if (Wadl2JavaHelper.PROJEC_TYPE_NB_MODULE.equals(targetProjectType) &&
                            (Security.Authentication.OAUTH == security.getAuthentication() ||
                            Security.Authentication.SESSION_KEY == security.getAuthentication())
                            ) {
                        if (cp == null ||
                            cp.findResource("org/openide/DialogDisplayer.class.class") == null ||
                            cp.findResource("org/openide/util/NbPreferences.class.class") == null ||
                            cp.findResource("org/openide/awt/HtmlBrowser.class") == null) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    NbBundle.getMessage(ClientJavaSourceHelper.class, "MSG_MissingOpenideModules"),
                                    NotifyDescriptor.WARNING_MESSAGE));
                        }
                    }
                }
            }
        } finally {
            handle.finish();
        }
    }

    private static void addJerseyClient (
            final JavaSource source,
            final String className,
            final String resourceUri,
            final RestServiceDescription restServiceDesc,
            final WadlSaasResource saasResource,
            final PathFormat pf,
            final Security security, final ClientGenerationStrategy strategy ) 
    {
        try {
            final Task<WorkingCopy> task = new AbstractTask<WorkingCopy>() {

                @Override
                public void run(WorkingCopy copy) throws java.io.IOException {
                    copy.toPhase(JavaSource.Phase.RESOLVED);

                    ClassTree tree = JavaSourceHelper.getTopLevelClassTree(copy);
                    ClassTree modifiedTree = null;
                    if (className == null) {
                        modifiedTree = modifyJerseyClientClass(copy, tree, 
                                resourceUri, restServiceDesc, saasResource, pf, 
                                security, strategy );
                    } else {
                        modifiedTree = addJerseyClientClass(copy, tree, 
                                className, resourceUri, restServiceDesc, 
                                saasResource, pf, security, strategy);
                    }

                    copy.rewrite(tree, modifiedTree);
                }
            };
            ModificationResult result = source.runModificationTask(task);

            if ( SourceUtils.isScanInProgress() ){
                source.runWhenScanFinished( new Task<CompilationController>(){
                    @Override
                    public void run(CompilationController controller) throws Exception {
                        source.runModificationTask(task).commit();
                    }
                }, true);
            }
            else {
                result.commit();
            }
        } catch (java.io.IOException ex) {
            if (ex.getCause() instanceof GuardedException) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(ClientJavaSourceHelper.class, 
                                    "ERR_CannotApplyGuarded"),              // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE));
                Logger.getLogger(ClientJavaSourceHelper.class.getName()).
                    log(Level.FINE, null, ex);
            }
            else {
                Logger.getLogger(ClientJavaSourceHelper.class.getName()).
                    log(Level.WARNING, null, ex);
            }
        }
    }

    private static ClassTree modifyJerseyClientClass (
            WorkingCopy copy,
            ClassTree classTree,
            String resourceURI,
            RestServiceDescription restServiceDesc,
            WadlSaasResource saasResource,
            PathFormat pf,
            Security security, ClientGenerationStrategy strategy) {

        return generateClassArtifacts(copy, classTree, resourceURI, 
                restServiceDesc, saasResource, pf, security, null, strategy );
    }

    private static ClassTree addJerseyClientClass (
            WorkingCopy copy,
            ClassTree classTree,
            String className,
            String resourceURI,
            RestServiceDescription restServiceDesc,
            WadlSaasResource saasResource,
            PathFormat pf,
            Security security, ClientGenerationStrategy strategy ) {

        TreeMaker maker = copy.getTreeMaker();
        ModifiersTree modifs = maker.Modifiers(Collections.<Modifier>singleton(Modifier.STATIC));
        ClassTree innerClass = maker.Class(
                modifs,
                className,
                Collections.<TypeParameterTree>emptyList(),
                null,
                Collections.<Tree>emptyList(),
                Collections.<Tree>emptyList());

        ClassTree modifiedInnerClass =
                generateClassArtifacts(copy, innerClass, resourceURI, 
                        restServiceDesc, saasResource, pf, security, 
                        classTree.getSimpleName().toString(), strategy );

        return maker.addClassMember(classTree, modifiedInnerClass);
    }

    private static ClassTree generateClassArtifacts (
            WorkingCopy copy,
            ClassTree classTree,
            String resourceURI,
            RestServiceDescription restServiceDesc,
            WadlSaasResource saasResource,
            PathFormat pf,
            Security security,
            String outerClassName, ClientGenerationStrategy strategy ) {

        TreeMaker maker = copy.getTreeMaker();
        ClassTree modifiedClass = strategy.generateFields(maker, copy, classTree, 
                resourceURI, security);

        // add constructor
        MethodTree ctor = strategy.generateConstructor(maker, copy , modifiedClass,
                pf , security);
        modifiedClass = maker.addClassMember(modifiedClass, ctor);

        // generate another constructor for Auth BASIC
        if (Security.Authentication.BASIC == security.getAuthentication() && pf.getArguments().length == 0) {
            ctor = strategy.generateConstructorAuthBasic(maker);
            modifiedClass = maker.addClassMember(modifiedClass, ctor);
        }
        
        // add setResourcePath() method for SubresourceLocators
        boolean isSubresource = (pf.getArguments().length>0);
        if (isSubresource) {
            MethodTree subresource = strategy.generateSubresourceMethod(maker,copy,
                    modifiedClass, pf);
            modifiedClass = maker.addClassMember(modifiedClass, subresource);
        }

        // add wrappers for http methods (GET/POST/PUT/DELETE)
        if (restServiceDesc != null) {
            List<RestMethodDescription> annotatedMethods =  restServiceDesc.getMethods();
            for (RestMethodDescription methodDesc : annotatedMethods) {
                if (methodDesc instanceof HttpMethod) {
                    List<MethodTree> httpMethods = strategy.generateHttpMethods(copy, 
                            (HttpMethod)methodDesc);
                    for (MethodTree httpMethod : httpMethods) {
                        modifiedClass = maker.addClassMember(modifiedClass, 
                                httpMethod);
                    }
                }
            }
        } else if (saasResource != null) {
            modifiedClass = generateSaasClientMethods(copy, modifiedClass , 
                    saasResource, security, strategy);
        }

        // add close()
        MethodTree close = strategy.generateClose(maker, copy);
        modifiedClass = maker.addClassMember(modifiedClass, close);

        // add security stuff
        if (Security.Authentication.BASIC == security.getAuthentication()) {
            List<VariableTree> authParams = new ArrayList<VariableTree>();
            Tree argTypeTree = maker.Identifier("String"); //NOI18N
            ModifiersTree fieldModifier = maker.Modifiers(
                    Collections.<Modifier>emptySet());
            VariableTree argFieldTree = maker.Variable(fieldModifier, 
                    "username", argTypeTree, null); //NOI18N
            authParams.add(argFieldTree);
            argFieldTree = maker.Variable(fieldModifier, 
                    "password", argTypeTree, null); //NOI18N
            authParams.add(argFieldTree);

            MethodTree authMethod = strategy.generateBasicAuth(maker,copy , 
                    authParams);
            modifiedClass = maker.addClassMember(modifiedClass, authMethod);
        } 
        else if (saasResource != null && 
                Security.Authentication.SESSION_KEY == security.getAuthentication()) 
        {
            // XXX: it seems this part doesn't depend from chosen JAX-RS client API   
            final SecurityParams securityParams = security.getSecurityParams();
            if (securityParams != null) {
                modifiedClass = Wadl2JavaHelper.addSessionAuthMethods(copy, 
                        modifiedClass, securityParams);
                if (Wadl2JavaHelper.PROJEC_TYPE_WEB.equals(security.getProjectType())) {
                    final FileObject ddFo = security.getDeploymentDescriptor();
                    if (ddFo != null) {
                        final String packageName = copy.getCompilationUnit().
                                getPackageName().toString();
                        final String className = (outerClassName==null ? "" : outerClassName+"$")+ //NOI18N
                                classTree.getSimpleName().toString();
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    addWebXmlArtifacts(ddFo, securityParams, className, packageName);
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientJavaSourceHelper.class.getName()).
                                        log(Level.INFO, "Cannot add servlet/servlet mapping to web.xml", ex);   //NOI18N
                                } 
                            }
                            
                        },1000);

                    }
                    modifiedClass = Wadl2JavaHelper.addSessionAuthServlets(copy, 
                            modifiedClass, securityParams, (ddFo == null));
                }
            }
        } 
        else if (saasResource != null) {
            try {
                Metadata oauthMetadata = saasResource.getSaas().getOauthMetadata();
                if (oauthMetadata != null) {
                    modifiedClass = strategy.generateOAuthMethods(
                            security.getProjectType(), copy, modifiedClass, 
                            oauthMetadata); 
                    if (Wadl2JavaHelper.PROJEC_TYPE_WEB.equals(security.getProjectType())) 
                    {
                        final FileObject ddFo = security.getDeploymentDescriptor();
                        if (ddFo != null) {
                            final String packageName = copy.getCompilationUnit().
                                    getPackageName().toString();
                            final String className = (outerClassName==null ? "" : outerClassName+"$")+ //NOI18N
                                    classTree.getSimpleName().toString();
                            RequestProcessor.getDefault().post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        addWebXmlOAuthArtifacts(ddFo, className, 
                                                packageName);
                                    } catch (IOException ex) {
                                        Logger.getLogger(
                                                ClientJavaSourceHelper.class.getName()).log(
                                                        Level.INFO, "Cannot add servlet/servlet mapping to web.xml", ex);//NOI18N
                                    }
                                }

                            },1000);

                        }
                        modifiedClass = OAuthHelper.addOAuthServlets(copy, 
                                modifiedClass, oauthMetadata, 
                                classTree.getSimpleName().toString(), (ddFo == null));
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(
                        Level.INFO, "Cannot get metadata for oauth", ex);
            } catch (JAXBException ex) {
                Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(
                        Level.INFO, "Cannot get metadata for oauth", ex);
            }
            // ouauth authentication
        }

        if (security.isSSL()) {
            modifiedClass = generateSslConfigMethods(maker, copy, modifiedClass);
        }

        return modifiedClass;
    }
    
    private static ClassTree generateSaasClientMethods(WorkingCopy copy, 
            ClassTree classTree , WadlSaasResource saasResource, 
            Security security, ClientGenerationStrategy strategy)
    {
        List<WadlSaasMethod> saasMethods = saasResource.getMethods();
        ClassTree modifiedInnerClass = classTree;
        TreeMaker maker = copy.getTreeMaker();
        boolean hasMultipleParamsInList = false;
        boolean hasOptionalQueryParams = false;
        boolean hasFormParams = false;
        HttpParams globalParams = new HttpParams(saasResource);
        for (WadlSaasMethod saasMethod : saasMethods) {
            HttpParams httpParams = new HttpParams(saasMethod);
            httpParams.mergeQueryandHeaderParams(globalParams);
            if (httpParams.hasMultipleParamsInList()) {
                hasMultipleParamsInList = true;
            }
            if ((httpParams.hasOptionalQueryParams() && httpParams.hasRequiredQueryParams()) ||
                    httpParams.hasDefaultQueryParams()) {
                hasOptionalQueryParams = true;
            }
            if (httpParams.hasFormParams()) {
                hasFormParams = true;
            }
            List<MethodTree> httpMethods = strategy.generateHttpMethods(copy, 
                    saasMethod, httpParams, security);
            for (MethodTree httpMethod : httpMethods) {
                modifiedInnerClass = maker.addClassMember(modifiedInnerClass, httpMethod);
            }
        }
        if (hasMultipleParamsInList || hasFormParams) {
            // add new private method to compute MultivaluedMap
            MethodTree methodTree = strategy.generateFormMethod(maker, copy);
            modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);
        }
        if (hasOptionalQueryParams) {
            // add new private method
            MethodTree methodTree = strategy.generateOptionalFormMethod(maker, copy);
            modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);
        }
        return modifiedInnerClass;
    }
    
    private static ClassTree generateSslConfigMethods(TreeMaker maker,
            WorkingCopy copy , ClassTree classTree)
    {
        ModifiersTree privateModifier = maker.Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE));
        // adding getHostnameVerifier() method
        String body =
        "{" + //NOI18N
        "   return new HostnameVerifier() {" + //NOI18N
        "       @Override" + //NOI18N
        "       public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {" + //NOI18N
        "           return true;"+ //NOI18N
        "       }"+ //NOI18N
        "   }"+ //NOI18N
        "}"; //NOI18N
        MethodTree methodTree = maker.Method (
                privateModifier,
                "getHostnameVerifier", //NOI18N
                JavaSourceHelper.createTypeTree(copy, "javax.net.ssl.HostnameVerifier"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                body,
                null);
        ClassTree modifiedClass = maker.addClassMember(classTree, methodTree);

        // adding getSSLContext() method
        body =
        "{"+ //NOI18N
        "   // for alternative implementation checkout org.glassfish.jersey.SslConfigurator\n"+ //NOI18N
        "   javax.net.ssl.TrustManager x509 = new javax.net.ssl.X509TrustManager() {"+ //NOI18N
        "       @Override"+ //NOI18N
        "       public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {"+ //NOI18N
        "           return;"+ //NOI18N
        "       }"+ //NOI18N
        "       @Override"+ //NOI18N
        "       public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {"+ //NOI18N
        "           return;"+ //NOI18N
        "       }"+ //NOI18N
        "       @Override"+ //NOI18N
        "       public java.security.cert.X509Certificate[] getAcceptedIssuers() {"+ //NOI18N
        "           return null;"+ //NOI18N
        "       }"+ //NOI18N
        "   };"+ //NOI18N
        "   SSLContext ctx = null;"+ //NOI18N
        "   try {"+ //NOI18N
        "       ctx = SSLContext.getInstance(\"SSL\");"+ //NOI18N
        "       ctx.init(null, new javax.net.ssl.TrustManager[] {x509}, null);"+ //NOI18N
        "   } catch (java.security.GeneralSecurityException ex) {}"+ //NOI18N
        "   return ctx;"+ //NOI18N
        "}";
        methodTree = maker.Method (
                privateModifier,
                "getSSLContext", //NOI18N
                JavaSourceHelper.createTypeTree(copy, "javax.net.ssl.SSLContext"), //NOI18N
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                body,
                null);
        return maker.addClassMember(modifiedClass, methodTree);
    }

    private static ResourcePath getResourcePath(WadlSaasResource saasResource) {
        String path = ClientGenerationStrategy.normalizePath(
                saasResource.getResource().getPath());
        WadlSaasResource parent = saasResource.getParent();
        while(parent != null) {
            String pathToken = ClientGenerationStrategy.normalizePath(
                    parent.getResource().getPath());
            if (pathToken.length()>0) {
                path = pathToken+"/"+path; //NOI18N
            }
            parent = parent.getParent();
        }
        return new ResourcePath(ClientGenerationStrategy.getPathFormat(path), path);
    }

    private static ResourcePath getResourcePath(Node resourceNode, String resourceClass, String uriTemplate) {
        String resourceUri = ClientGenerationStrategy.normalizePath(uriTemplate);
        Node projectNode = resourceNode.getParentNode();
        if (projectNode != null) {
            for (Node sibling : projectNode.getChildren().getNodes()) {
                if (resourceNode != sibling) {
                    RestServiceDescription desc = sibling.getLookup().lookup(RestServiceDescription.class);
                    if (desc != null) {
                        for (RestMethodDescription m : desc.getMethods()) {
                            if (m instanceof SubResourceLocator) {
                                SubResourceLocator resourceLocator = (SubResourceLocator)m;
                                if (resourceClass.equals(resourceLocator.getReturnType())) {
                                    // detect resource locator uri
                                    String resourceLocatorUri = 
                                            ClientGenerationStrategy.normalizePath(
                                                    resourceLocator.getUriTemplate());
                                    String parentResourceUri = desc.getUriTemplate();
                                    if (parentResourceUri.length() > 0) {
                                        // found root resource
                                        String subresourceUri = null;
                                        if (resourceLocatorUri.length() > 0) {
                                            if (resourceUri.length() > 0) {
                                                subresourceUri = resourceLocatorUri+"/"+resourceUri; //NOI18N
                                            } else {
                                                subresourceUri = resourceLocatorUri;
                                            }
                                        } else {
                                            subresourceUri = resourceUri;
                                        }
                                        PathFormat pf = ClientGenerationStrategy.
                                                getPathFormat(ClientGenerationStrategy.
                                                        normalizePath(parentResourceUri)+"/"+subresourceUri); //NOI18N
                                        return new ResourcePath(pf, parentResourceUri);
                                    } else {
                                        // searching recursively further
                                        return getResourcePath(sibling, desc.getClassName(), resourceLocatorUri+"/"+uriTemplate); //NOI8N
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ResourcePath(ClientGenerationStrategy.getPathFormat(uriTemplate), uriTemplate);
    }

    public static String getBaseURL(Project project) {
        
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null) {
            Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(Level.INFO, "Can not detect target J2EE server"); //NOI18N
            return "";
        }
        // getting port and host name
        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
        String portNumber = "8080"; //NOI18N
        String hostName = "localhost"; //NOI18N
        try {
            ServerInstance.Descriptor instanceDescriptor = serverInstance.getDescriptor();
            if (instanceDescriptor != null) {
                int port = instanceDescriptor.getHttpPort();
                portNumber = port == 0 ? "8080" : String.valueOf(port); //NOI18N
                String hstName = instanceDescriptor.getHostname();
                if (hstName != null) {
                    hostName = hstName;
                }
            }
        } catch (InstanceRemovedException ex) {
            Logger.getLogger(ClientJavaSourceHelper.class.getName()).log(Level.INFO, "Removed ServerInstance", ex); //NOI18N
        }

        String contextRoot = null;
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();

        if (J2eeModule.Type.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO the context root value could not be read, let the user know about it
            }
            if (contextRoot != null && contextRoot.startsWith("/")) { //NOI18N
                //NOI18N
                contextRoot = contextRoot.substring(1);
            }
        }
        String applicationPath = "webresources"; //NOI18N
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        if (restSupport != null) {
            applicationPath = restSupport.getApplicationPath();
        }
        return "http://" + hostName + ":" + portNumber + "/" + //NOI18N
                (contextRoot != null && !contextRoot.equals("") ? contextRoot : "") + //NOI18N
                "/"+applicationPath; //NOI18N
    }

    static class PathFormat {
        private static final String ARG = "arg";       // NOI18N
        
        private String pattern;
        private String[] arguments;

        public String[] getArguments() {
            return arguments;
        }

        public void setArguments(String[] arguments) {
            this.arguments = new String[arguments.length];
            for(int i=0; i<arguments.length; i++){
                this.arguments[i]=getJavaIdentifier(arguments[i]);
            }
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
        
        private String getJavaIdentifier(String arg ){
            if ( arg.length() == 0 ){
                return getUniqueArgument(ARG);
            }
            else {
                char first = arg.charAt(0);
                if ( Character.isJavaIdentifierStart(first)){
                    int index = -1;
                    for(int i=1; i<arg.length(); i++){
                        if ( !Character.isJavaIdentifierPart( arg.charAt(i))){
                            index = i;
                            break;
                        }
                    }
                    if ( index ==-1 ){
                        return getUniqueArgument(arg);
                    }
                    else {
                        String start = arg.substring(0, index);
                        String end = "";
                        if ( index <arg.length()-1){
                            end = arg.substring(index+1);
                        }
                        if ( end.length() >0){
                            end = Character.toUpperCase(end.charAt(0))+end.substring(1);
                        }
                        return getUniqueArgument( start +end);
                    }
                }
                else {
                    return getJavaIdentifier(arg.substring(1));
                }
            }
        }
        
        private String getUniqueArgument(String base ){
            String result = base;
            int count=1;
            while( javaIds.contains(result)){
                result = base+count;
                count++;
            }
            javaIds.add(result);
            return result;
        }
        
        private Set<String> javaIds = new HashSet<String>();
    }
    
    static class ResourcePath {
        private PathFormat pathFormat;
        private String path;

        public ResourcePath() {
        }

        public ResourcePath(PathFormat pathFormat, String path) {
            this.pathFormat = pathFormat;
            this.path = path;
        }

        public PathFormat getPathFormat() {
            return pathFormat;
        }

        public void setPathFormat(PathFormat pathFormat) {
            this.pathFormat = pathFormat;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    private static void addSecurityMetadata(Security security, WadlSaasResource saasResource) {
        // compute security params from METADATA
        SaasMetadata saasMetadata = saasResource.getSaas().getSaasMetadata();
        if (saasMetadata != null) {
            SaasMetadata.Authentication auth = saasMetadata.getAuthentication();
            if (auth != null && auth.getSessionKey().size()>0) {
                SecurityParams securityParams = new SecurityParams();
                SessionKey sessionKey = auth.getSessionKey().get(0);
                if (sessionKey != null) {
                    securityParams.setSignature(sessionKey.getSigId());
                    Sign sign = sessionKey.getSign();
                    if (sign != null) {
                        Params secParams = sign.getParams();
                        if (secParams != null) {
                            List<String> params = new ArrayList<String>();
                            for (Params.Param secParam : secParams.getParam()) {
                                params.add(secParam.getName());
                            }
                            securityParams.setParams(params);
                        }
                    }
                    Authenticator authenticator = sessionKey.getAuthenticator();
                    if (authenticator != null) {
                        UseTemplates useTemplates = authenticator.getUseTemplates();
                        if (useTemplates != null) {
                            if (Wadl2JavaHelper.PROJEC_TYPE_NB_MODULE.equals(security.getProjectType())) { //NOI18N
                                TemplateType tt = useTemplates.getNbModule();
                                if (tt != null) {
                                    securityParams.setFieldDescriptors(tt.getFieldDescriptor());
                                    securityParams.setMethodDescriptors(tt.getMethodDescriptor());
                                    securityParams.setServletDescriptors(tt.getServletDescriptor());
                                }
                            } else if (Wadl2JavaHelper.PROJEC_TYPE_WEB_EE7.equals(security.getProjectType())) { //NOI18N
                                TemplateType tt = useTemplates.getWebEe7();
                                if (tt == null) {
                                    tt = useTemplates.getWeb();
                                }
                                if (tt != null) {
                                    securityParams.setFieldDescriptors(tt.getFieldDescriptor());
                                    securityParams.setMethodDescriptors(tt.getMethodDescriptor());
                                    securityParams.setServletDescriptors(tt.getServletDescriptor());
                                }
                            } else if (Wadl2JavaHelper.PROJEC_TYPE_WEB.equals(security.getProjectType())) { //NOI18N
                                TemplateType tt = useTemplates.getWeb();
                                if (tt != null) {
                                    securityParams.setFieldDescriptors(tt.getFieldDescriptor());
                                    securityParams.setMethodDescriptors(tt.getMethodDescriptor());
                                    securityParams.setServletDescriptors(tt.getServletDescriptor());
                                }
                            } else {
                                TemplateType tt = useTemplates.getDesktop();
                                if (tt != null) {
                                    securityParams.setFieldDescriptors(tt.getFieldDescriptor());
                                    securityParams.setMethodDescriptors(tt.getMethodDescriptor());
                                    securityParams.setServletDescriptors(tt.getServletDescriptor());
                                }
                            }
                        }

                    }
                }
                security.setSecurityParams(securityParams);
            }
        }
    }

    private static void addWebXmlArtifacts(FileObject ddFo, 
            SecurityParams securityParams, String parentClassName, 
            String packageName) throws IOException 
    {
        WebApp webApp = DDProvider.getDefault().getDDRoot(ddFo);
        if (webApp != null) {
            for (ServletDescriptor servletDesc : securityParams.getServletDescriptors()) {
                String servletName = parentClassName+"$"+servletDesc.getClassName();
                String className = packageName+"."+servletName;
                String urlPattern = servletDesc.getServletMapping();
                try {
                    Servlet servlet = (Servlet) webApp.createBean("Servlet"); //NOI18N
                    servlet.setServletName(servletName);
                    servlet.setServletClass(className);
                    ServletMapping25 servletMapping = (ServletMapping25) webApp.createBean("ServletMapping"); //NOI18N
                    servletMapping.setServletName(servletName);
                    servletMapping.addUrlPattern(urlPattern);
                    webApp.addServlet(servlet);
                    webApp.addServletMapping(servletMapping);

                } catch (ClassNotFoundException ex) {
                }
            }
            webApp.write(ddFo);
        }
    }

    private static void addWebXmlOAuthArtifacts(FileObject ddFo, String parentClassName, String packageName) throws IOException {
        String[] servletNames = new String[] {"OAuthLoginServlet", "OAuthCallbackServlet"}; //NOI18N
        String[] urlPatterns = new String[] {"/OAuthLogin", "/OAuthCallback"}; //NOI18N
        WebApp webApp = DDProvider.getDefault().getDDRoot(ddFo);
        if (webApp != null) {
            for (int i = 0; i<servletNames.length; i++) {
                String servletName = parentClassName+"$"+servletNames[i];
                String className = packageName+"."+servletName;
                try {
                    Servlet servlet = (Servlet) webApp.createBean("Servlet"); //NOI18N
                    servlet.setServletName(servletName);
                    servlet.setServletClass(className);
                    ServletMapping25 servletMapping = (ServletMapping25) webApp.createBean("ServletMapping"); //NOI18N
                    servletMapping.setServletName(servletName);
                    servletMapping.addUrlPattern(urlPatterns[i]);
                    webApp.addServlet(servlet);
                    webApp.addServletMapping(servletMapping);

                } catch (ClassNotFoundException ex) {
                }
            }
            webApp.write(ddFo);
        }
    }

    static enum HttpMimeType {
        XML("application/xml", "javax.ws.rs.core.MediaType.APPLICATION_XML"), //NOI18N
        JSON("application/json", "javax.ws.rs.core.MediaType.APPLICATION_JSON"), //NOI18N
        TEXT("text/plain", "javax.ws.rs.core.MediaType.TEXT_PLAIN"), //NOI18N
        HTML("text/html", "javax.ws.rs.core.MediaType.TEXT_HTML"), //NOI18N
        TEXT_XML("text/xml", "javax.ws.rs.core.MediaType.TEXT_XML"), //NOI18N
        FORM("application/x-www-form-urlencoded", "javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED"); //NOI18N

        private String mimeType;
        private String mediaType;

        HttpMimeType(String mimeType, String mediaType) {
            this.mimeType = mimeType;
            this.mediaType = mediaType;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getMediaType() {
            return mediaType;
        }
    }
}
