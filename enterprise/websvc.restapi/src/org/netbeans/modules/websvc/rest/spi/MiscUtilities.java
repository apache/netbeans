/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.rest.spi;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.MiscPrivateUtilities;
import org.netbeans.modules.websvc.rest.WebXmlUpdater;
import static org.netbeans.modules.websvc.rest.WebXmlUpdater.getRestServletAdaptorByName;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import static org.netbeans.modules.websvc.rest.spi.RestSupport.REST_SERVLET_ADAPTOR;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * The purpose of this class is to trim down RestSupport and WebRestSupport down and
 * as such it contains bunch of random static utility methods which previously
 * were polluting and bloating RestSupport. They should be further refactored and
 * moved to right places.
 */
public class MiscUtilities {
    
    public static FileObject findSourceRoot(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups != null && sourceGroups.length > 0) {
            return sourceGroups[0].getRootFolder();
        }
        return null;
    }

    /*
     * Copy File only
     */
    public static void copyFile(File testdir, String name) throws IOException {
        String path = "resources/" + name;
        File df = new File(testdir, name);
        if (!df.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = RestSupport.class.getResourceAsStream(path);
                os = new FileOutputStream(df);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    /*
     * Copy File, as well as replace tokens, overwrite if specified
     */
    public static FileObject copyFile(File testdir, String name, String[] replaceKeys, boolean overwrite) throws IOException {
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject fo = dir.getFileObject(name);
        if (fo == null) {
            fo = dir.createData(name);
        } else {
            if (!overwrite) {
                return fo;
            }
        }
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
            InputStream is = RestSupport.class.getResourceAsStream("resources/" + name);
            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            String lineSep = "\n";
            if (File.separatorChar == '\\') {
                lineSep = "\r\n";
            }
            String[] replaceValues = null;
            if (replaceKeys != null) {
                replaceValues = new String[replaceKeys.length];
                for (int i = 0; i < replaceKeys.length; i++) {
                    replaceValues[i] = NbBundle.getMessage(RestSupport.class, replaceKeys[i]);
                }
            }
            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < replaceKeys.length; i++) {
                    line = line.replaceAll(replaceKeys[i], replaceValues[i]);
                }
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (lock != null) {
                lock.releaseLock();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return fo;
    }

    public static FileObject modifyFile(FileObject fo, Map<String, String> replace) throws IOException {
        StringWriter content = new StringWriter();
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            writer = new BufferedWriter(content);
            InputStream is = fo.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            String lineSep = "\n";
            if (File.separatorChar == '\\') {
                lineSep = "\r\n";
            }
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry : replace.entrySet()) {
                    line = line.replaceAll(entry.getKey(), entry.getValue());
                }
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            StringBuffer buffer = content.getBuffer();
            FileLock lock = fo.lock();
            try {
                OutputStream outputStream = fo.getOutputStream(lock);
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.forName("UTF-8")));
                writer.write(buffer.toString());
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
                if (writer != null) {
                    writer.close();
                }
            }
        }
        return fo;
    }

    public static FileObject getApplicationContextXml(Project project) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().
            lookup(J2eeModuleProvider.class);
        FileObject[] fobjs = provider.getSourceRoots();

        if (fobjs.length > 0) {
            FileObject configRoot = fobjs[0];
            FileObject webInf = configRoot.getFileObject("WEB-INF");        //NOI18N

            if (webInf != null) {
                return webInf.getFileObject("applicationContext", "xml");      //NOI18N
            }
        }

        return null;
    }

    public static String getContextRootURL(Project project) {
        String portNumber = "8080"; //NOI18N
        String host = "localhost"; //NOI18N
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null || MiscPrivateUtilities.DEVNULL.equals(serverInstanceID)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(RestSupport.class, "MSG_MissingServer"),
                    NotifyDescriptor.ERROR_MESSAGE));
        } else {
            // getting port and host name
            ServerInstance serverInstance = Deployment.getDefault().
                getServerInstance(serverInstanceID);
            try {
                ServerInstance.Descriptor instanceDescriptor =
                    serverInstance.getDescriptor();
                if (instanceDescriptor != null) {
                    int port = instanceDescriptor.getHttpPort();
                    if (port>0) {
                        portNumber = String.valueOf(port);
                    }
                    String hostName = instanceDescriptor.getHostname();
                    if (hostName != null) {
                        host = hostName;
                    }
                }
            }
            catch (InstanceRemovedException ex) {

            }
        }
        J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
        String contextRoot = null;
        try {
            contextRoot = configSupport.getWebContextRoot();
        } catch (ConfigurationException e) {
            // TODO the context root value could not be read, let the user know about it
        }
        if (contextRoot == null) {
            contextRoot = ""; // NOI18N
        }
        if (contextRoot.length() > 0 && contextRoot.startsWith("/")) { //NOI18N
            contextRoot = contextRoot.substring(1);
        }
        return "http://"+host+":"+portNumber+"/"+ //NOI18N
                (contextRoot.length()>0 ? contextRoot+"/" : ""); //NOI18N
    }

    public static boolean hasApplicationResourceClass(RestSupport restSupport, final String fqn){
        return MiscPrivateUtilities.hasApplicationResourceClass(restSupport, fqn);
    }

    public static FileObject generateTestClient(File testdir) throws IOException {
        return MiscPrivateUtilities.generateTestClient(testdir);
    }

    public static void addInitParam( RestSupport restSupport, String paramName, String value ) {
        try {
            FileObject ddFO = restSupport.getWebXmlUpdater().getWebXml(false);
            WebApp webApp = restSupport.getWebXmlUpdater().findWebApp();
            if (ddFO == null || webApp == null) {
                return;
            }
            Servlet adaptorServlet = getRestServletAdaptorByName(webApp,
                    REST_SERVLET_ADAPTOR);
            if (adaptorServlet == null) {
                // servlet is missing in the web.xml and so parameter cannot be added
                return;
            }
            InitParam initParam = (InitParam) adaptorServlet.findBeanByName(
                    "InitParam", // NOI18N
                    "ParamName", // NOI18N
                    paramName);
            if (initParam == null) {
                try {
                    initParam = (InitParam) adaptorServlet
                            .createBean("InitParam"); // NOI18N
                    adaptorServlet.addInitParam(initParam);
                }
                catch (ClassNotFoundException ex) {
                }
            }
            initParam.setParamName(paramName);
            initParam.setParamValue(value);

            webApp.write(ddFO);
        }
        catch (IOException e) {
            Logger.getLogger(RestSupport.class.getName()).log(Level.WARNING,  null , e);
        }
    }

    /** Add servlet(3.0) element to web.xml, representing JAX-RS Application with CORS filter
     * 
     * @param restSupport RestSupport instance
     * @param applicationClassName application class name
     * @param corsFilterName corse filter name
     */
    public static void addCORSFilter( RestSupport restSupport, String applicationClassName, String corsFilterName ) {
        try {
            assert applicationClassName != null;
            WebXmlUpdater webXmlUpdater = restSupport.getWebXmlUpdater();
            FileObject ddFO = webXmlUpdater.getWebXml(true);
            WebApp webApp = webXmlUpdater.findWebApp();
            if (ddFO == null || webApp == null) {
                return;
            }
            
            boolean changed = false;
            Servlet applicationConfigServlet = getRestServletAdaptorByName(webApp,
                    applicationClassName);
            if (applicationConfigServlet == null) {
                // servlet is missing in the web.xml and so parameter cannot be added
                try {
                    applicationConfigServlet = (Servlet) webApp.createBean("Servlet"); //NOI18N
                    applicationConfigServlet.setServletName(applicationClassName);
                    webApp.addServlet(applicationConfigServlet);
                    changed = true;
                } catch (ClassNotFoundException ex) {}
                
            }
            if (applicationConfigServlet != null) {
                InitParam initParam = (InitParam) applicationConfigServlet.findBeanByName(
                        "InitParam", // NOI18N
                        "ParamName", // NOI18N
                        RestSupport.CONTAINER_RESPONSE_FILTER);
                if (initParam == null) {
                    try {
                        initParam = (InitParam) applicationConfigServlet
                                .createBean("InitParam"); // NOI18N
                        initParam.setParamName(RestSupport.CONTAINER_RESPONSE_FILTER);
                        initParam.setParamValue(corsFilterName);
                        applicationConfigServlet.addInitParam(initParam);
                        changed = true;
                    }
                    catch (ClassNotFoundException ex) {
                    }
                }

            }
            if (changed) {
                webApp.write(ddFO);
            }
        }
        catch (IOException e) {
            Logger.getLogger(RestSupport.class.getName()).log(Level.WARNING,  null , e);
        }
    }

    public static ServletMapping25 getRestServletMapping(WebApp webApp) {
        return WebXmlUpdater.getRestServletMapping(webApp);
    }

    public static String getApplicationPathFromDD(WebApp webApp) {
        if (webApp != null) {
            ServletMapping25 sm = getRestServletMapping(webApp);
            if (sm != null) {
                String urlPattern = null;
                String[] urlPatterns = sm.getUrlPatterns();
                if (urlPatterns.length > 0) {
                    urlPattern = urlPatterns[0];
                }
                if (urlPattern != null) {
                    if (urlPattern.endsWith("*")) { //NOI18N
                        urlPattern = urlPattern.substring(0, urlPattern.length()-1);
                    }
                    if (urlPattern.endsWith("/")) { //NOI18N
                        urlPattern = urlPattern.substring(0, urlPattern.length()-1);
                    }
                    if (urlPattern.startsWith("/")) { //NOI18N
                        urlPattern = urlPattern.substring(1);
                    }
                    return urlPattern;
                }

            }
        }
        return null;
    }


    public static Datasource getDatasource(Project p, String jndiName) {
        J2eeModuleProvider provider = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);

        try {
            return provider.getConfigSupport().findDatasource(jndiName);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }
    
    /** Creates body for javax.ws.rs.core.Application subclass getClasses method
     * 
     * @param restSupport
     * @return 
     */
    public static String createBodyForGetClassesMethod(RestSupport restSupport) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        if (isSourceLevel17orHigher(restSupport.getProject())) {
            builder.append("Set<Class<?>> resources = new java.util.HashSet<>();");// NOI18N
        } else {
            builder.append("Set<Class<?>> resources = new java.util.HashSet<Class<?>>();");// NOI18N
        }
        if (!restSupport.hasJersey2(true)) {
            builder.append(MiscPrivateUtilities.getJacksonProviderSnippet(restSupport));
        }
        builder.append(RestConstants.GET_REST_RESOURCE_CLASSES2+"(resources);");
        builder.append("return resources;}");
        return builder.toString();
    }

    /** creates addResourceClasses method
     * 
     * @param maker tree maker
     * @param classTree class tree
     * @param controller compilation controller
     * @param methodBody method body
     * @param addComment add comment or not
     * @return modified class tree
     * @throws IOException 
     */
    public static ClassTree createAddResourceClasses(TreeMaker maker,
            ClassTree classTree, CompilationController controller,
            String methodBody, boolean addComment) throws IOException
    {
        WildcardTree wildCard = maker.Wildcard(Tree.Kind.UNBOUNDED_WILDCARD,
                null);
        ParameterizedTypeTree wildClass = maker.ParameterizedType(
                maker.QualIdent(Class.class.getCanonicalName()),
                Collections.singletonList(wildCard));
        ParameterizedTypeTree wildSet = maker.ParameterizedType(
                maker.QualIdent(Set.class.getCanonicalName()),
                Collections.singletonList(wildClass));
        ModifiersTree modifiersTree = maker.Modifiers(EnumSet
                .of(Modifier.PRIVATE));
        VariableTree newParam = maker.Variable(
                maker.Modifiers(Collections.<Modifier>emptySet()),
                "resources", wildSet, null);
        MethodTree methodTree = maker.Method(modifiersTree,
                RestConstants.GET_REST_RESOURCE_CLASSES2, maker.Type("void"),
                Collections.<TypeParameterTree> emptyList(),
                Arrays.asList(newParam),
                Collections.<ExpressionTree> emptyList(), methodBody,
                null);
        if (addComment) {
            Comment comment = Comment.create(Comment.Style.JAVADOC,// -2, -2, -2,
                    "Do not modify "+RestConstants.GET_REST_RESOURCE_CLASSES2+"() method.\n"
                    + "It is automatically populated with\n"
                    + "all resources defined in the project.\n"
                    + "If required, comment out calling this method in getClasses()."); // NOI18N
            maker.addComment(methodTree, comment, true);
        }
        return maker.addClassMember(classTree, methodTree);
    }
    
    /**
     * Is source level of a given project 1.7 or higher?
     *
     * @param project Project
     * @return true if source level is 1.7 or higher; otherwise false
     */
    private static boolean isSourceLevel17orHigher(Project project) {
        String srcLevel = SourceLevelQuery.getSourceLevel(project.getProjectDirectory());
        if (srcLevel != null) {
            double sourceLevel = Double.parseDouble(srcLevel);
            return (sourceLevel >= 1.7);
        } else {
            return false;
        }
    }
    
    /** Check if project is of Java EE 6 project type or higher
     * 
     * @param project project instance
     * @return true or false
     */
    public static boolean isJavaEE6AndHigher(Project project) {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            Profile profile = webModule.getJ2eeProfile();
            if (Profile.JAVA_EE_6_WEB == profile || 
                    Profile.JAVA_EE_6_FULL == profile ||
                        Profile.JAVA_EE_7_WEB == profile ||
                                Profile.JAVA_EE_7_FULL == profile ||
                                    Profile.JAVA_EE_8_WEB == profile ||
                                            Profile.JAVA_EE_8_FULL == profile )
            {
                return true;
            }
        }
        return false;
    }

}
