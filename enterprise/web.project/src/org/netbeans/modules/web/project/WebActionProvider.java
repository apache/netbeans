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
package org.netbeans.modules.web.project;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.project.ui.SetExecutionUriAction;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import java.util.HashSet;
import java.util.LinkedList;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.javaee.project.api.WhiteListUpdater;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.api.webmodule.RequestParametersQuery;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.ui.ServletScanObserver;
import org.netbeans.modules.web.project.ui.ServletUriPanel;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
public class WebActionProvider extends BaseActionProvider {

    // property definitions
    private static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N

    // Definition of commands
    private static final String COMMAND_VERIFY = "verify"; //NOI18N
    
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        WebProjectConstants.COMMAND_REDEPLOY,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_VERIFY,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME
    };

    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
    };

    /**Set of commands which are affected by background scanning*/
    private Set<String> bkgScanSensitiveActions;

    /**Set of commands which need java model up to date*/
    private Set<String> needJavaModelActions;

    private static final String[] actionsDisabledForQuickRun = {
        COMMAND_COMPILE_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    /** Map from commands to ant targets */
    Map<String,String[]> commands;

    public WebActionProvider(WebProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator) {
        super(project, updateHelper, evaluator, project.getSourceRoots(), project.getTestSourceRoots(), 
                project.getAntProjectHelper(), new CallbackImpl(project.getClassPathProvider(), project.getWebModule()));
        commands = new HashMap<String, String[]>();
        commands.put(COMMAND_BUILD, new String[]{"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[]{"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[]{"clean", "dist"}); // NOI18N
        // the target name is compile-single, except for JSPs, where it is compile-single-jsp
        commands.put(COMMAND_COMPILE_SINGLE, new String[]{"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[]{"run"}); // NOI18N
        // the target name is run, except for Java files with main method, where it is run-main
        commands.put(COMMAND_RUN_SINGLE, new String[]{"run-main"}); // NOI18N
        commands.put(WebProjectConstants.COMMAND_REDEPLOY, new String[]{"run-deploy"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[]{"debug"}); // NOI18N
        // the target name is debug, except for Java files with main method, where it is debug-single-main
        commands.put(COMMAND_DEBUG_SINGLE, new String[]{"debug-single-main"}); // NOI18N
        commands.put(COMMAND_PROFILE, new String[]{"profile"}); // NOI18N
        commands.put(COMMAND_PROFILE_SINGLE, new String[]{"profile-single-main"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[]{"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[]{"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[]{"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[]{"debug-test"}); // NOI18N
        commands.put(COMMAND_PROFILE_TEST_SINGLE, new String[]{"profile-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[]{"debug-fix"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[]{"verify"}); // NOI18N
        commands.put(SingleMethod.COMMAND_RUN_SINGLE_METHOD, new String[] {"test-single-method"}); // NOI18N
        commands.put(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD, new String[] {"debug-single-method"}); // NOI18N
        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
            COMMAND_RUN_SINGLE
        ));

        this.needJavaModelActions = new HashSet<String>(Arrays.asList(
            JavaProjectConstants.COMMAND_DEBUG_FIX
        ));
        setServerExecution(true);
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return actionsDisabledForQuickRun;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        return Boolean.parseBoolean(getEvaluator().getProperty(WebProjectProperties.J2EE_COMPILE_ON_SAVE));
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions.clone();
    }

    @Override
    protected void updateJavaRunnerClasspath(String command, Map<String, Object> execProperties) {
        if (COMMAND_TEST_SINGLE.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command) || COMMAND_PROFILE_TEST_SINGLE.equals(command) ||
            SingleMethod.COMMAND_DEBUG_SINGLE_METHOD.equals(command) || SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command) ||
            COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_PROFILE_SINGLE.equals(command)) {
            FileObject fo = (FileObject)execProperties.get(JavaRunner.PROP_EXECUTE_FILE);
            ClassPath cp = getCallback().findClassPath(fo, ClassPath.EXECUTE);
            ClassPath cp2 = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(getProject().getProjectDirectory()), getEvaluator(),
                    new String[]{"j2ee.platform.classpath", "j2ee.platform.embeddableejb.classpath"}));
            cp = ClassPathSupport.createProxyClassPath(cp, cp2);
            execProperties.put(JavaRunner.PROP_EXECUTE_CLASSPATH, cp);
            Collection<String> coll = (Collection<String>)execProperties.get(JavaRunner.PROP_RUN_JVMARGS);
            if (coll == null) {
                coll = new LinkedList<String>();
                execProperties.put(JavaRunner.PROP_RUN_JVMARGS, coll);
            }
            String s = getEvaluator().getProperty(WebProjectProperties.RUNMAIN_JVM_ARGS);
            if (s != null && s.trim().length() > 0) {
                coll.add(s);
            }
            s = getEvaluator().getProperty(ProjectProperties.ENDORSED_CLASSPATH);
            if (s != null && s.trim().length() > 0) {
                ClassPath ecp = ClassPathFactory.createClassPath(
                        ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                        FileUtil.toFile(getProject().getProjectDirectory()), getEvaluator(),
                        new String[]{ProjectProperties.ENDORSED_CLASSPATH}));
                coll.add("-Xbootclasspath/p:\""+ecp.toString(ClassPath.PathConversionMode.WARN) +"\"");
            }
        }
    }

    @Override
    protected boolean handleJavaClass(Properties p, FileObject javaFile, String command, List<String> targetNames) {
        return runServlet(p, javaFile, "LBL_RunAction", COMMAND_DEBUG_SINGLE.equals(command), COMMAND_PROFILE_SINGLE.equals(command), targetNames);
    }

    @Override
    public String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        // set context for advanced nbbrowse task:
        FileObject fo = context.lookup(FileObject.class);
        if (fo == null) {
            fo = getProject().getProjectDirectory();
        }
        String ctx = FileUtil.toFile(fo).getAbsolutePath();
        p.setProperty("browser.context", ctx);

        if (command.equals(COMMAND_RUN_SINGLE) ||command.equals(COMMAND_RUN) ||
            command.equals(WebProjectConstants.COMMAND_REDEPLOY) ||command.equals(COMMAND_DEBUG) ||
            command.equals(COMMAND_DEBUG_SINGLE) || command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX) ||
            command.equals(COMMAND_PROFILE) || command.equals(COMMAND_PROFILE_SINGLE) ||
            command.equals( COMMAND_TEST_SINGLE) || command.equals(COMMAND_DEBUG_TEST_SINGLE) || command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            setDirectoryDeploymentProperty(p);
        }

        if (command.equals(WebProjectConstants.COMMAND_REDEPLOY)) {
            p.setProperty("forceRedeploy", "true"); //NOI18N
        } else {
            p.setProperty("forceRedeploy", "false"); //NOI18N
        }

        if (isDebugged()) {
            p.setProperty("is.debugged", "true");
        }
        if (command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_PROFILE_SINGLE)) {
            String res[] = super.getTargetNames(command, context, p, doJavaChecks);
            if (res != null) {
                return res;
            }
            if (!checkSelectedServer(
                    command.equals(COMMAND_DEBUG_SINGLE), command.equals(COMMAND_PROFILE_SINGLE))) {
                return null;
            }
            String targetNames[];
            if (command.equals(COMMAND_DEBUG_SINGLE)) {
                targetNames = new String[]{"debug"};
            } else if (command.equals(COMMAND_PROFILE_SINGLE)) {
                targetNames = new String[]{"profile"};
            } else {
                targetNames = new String[]{"run"};
            }
            // run a JSP
            FileObject files[] = findJsps(context);
            if (files != null && files.length > 0) {
                // possibly compile the JSP, if we are not compiling all of them
                String raw = getAntProjectHelper().getStandardPropertyEvaluator().getProperty(WebProjectProperties.COMPILE_JSPS);
                boolean compile = decodeBoolean(raw);
                if (!compile) {
                    setAllPropertiesForSingleJSPCompilation(p, files);
                }
                String requestParams = RequestParametersQuery.getFileAndParameters(files[0]);
                if (requestParams != null) {
                    p.setProperty("client.urlPart", requestParams); //NOI18N
                    p.setProperty(BaseActionProvider.PROPERTY_RUN_SINGLE_ON_SERVER, "yes");
                    return targetNames;
                } else {
                    return null;
                }
            } else {
                // run HTML file
                FileObject[] htmlFiles = findHtml(context);
                if ((htmlFiles != null) && (htmlFiles.length > 0)) {
                    String requestParams = RequestParametersQuery.getFileAndParameters(htmlFiles[0]);
                    if (requestParams == null) {
                        requestParams = FileUtil.getRelativePath(WebModule.getWebModule(htmlFiles[0]).getDocumentBase(), htmlFiles[0]); // NOI18N
                        if (requestParams != null) {
                            requestParams = "/" + requestParams.replace(" ", "%20"); // NOI18N
                        }
                    }
                    if (requestParams != null) {
                        p.setProperty("client.urlPart", requestParams); //NOI18N
                        p.setProperty(BaseActionProvider.PROPERTY_RUN_SINGLE_ON_SERVER, "yes"); // NOI18N
                        return targetNames;
                    } else {
                        return null;
                    }
                }
            }
            return null;
        } else if (command.equals(COMMAND_RUN) || command.equals(WebProjectConstants.COMMAND_REDEPLOY)) {
            if (!checkSelectedServer(false, false)) {
                return null;
            }
            if (WhiteListUpdater.isWhitelistViolated(getProject())) {
                return null;
            }
            return commands.get(command);
        } else if (command.equals(COMMAND_PROFILE)) {
            if (!checkSelectedServer(false, true)) {
                return null;
            }
            initWebServiceProperties(p);
        } else if (command.equals(COMMAND_DEBUG)) {
            if (!checkSelectedServer(true, false)) {
                return null;
            }
            initWebServiceProperties(p);
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            String res[] = super.getTargetNames(command, context, p, doJavaChecks);
            if (res != null) {
                return res;
            }
            FileObject[] files = findJsps(context);
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    FileObject jsp = files[i];
                    if (areIncludesModified(jsp)) {
                        invalidateClassFile(jsp);
                    }
                }
                setAllPropertiesForSingleJSPCompilation(p, files);
                return new String[]{"compile-single-jsp"};
            } else {
                return null;
            }
        }
        return super.getTargetNames(command, context, p, doJavaChecks);
    }

    private void initWebServiceProperties(Properties p) {
        WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(getProject().getProjectDirectory());
        if (wscs != null) { //project contains ws reference
            List serviceClients = wscs.getServiceClients();
            //we store all ws client names into hash set for later fast searching
            HashSet<String> scNames = new HashSet<String>();
            for (Iterator scIt = serviceClients.iterator(); scIt.hasNext();) {
                WsCompileClientEditorSupport.ServiceSettings serviceClientSettings =
                        (WsCompileClientEditorSupport.ServiceSettings) scIt.next();
                scNames.add(serviceClientSettings.getServiceName());
            }

            StringBuffer clientDCP = new StringBuffer();//additional debug.classpath
            StringBuffer clientWDD = new StringBuffer();//additional web.docbase.dir

            //we find all projects containg a web service
            Set<FileObject> globalPath = GlobalPathRegistry.getDefault().getSourceRoots();
            HashSet<String> serverNames = new HashSet<String>();
            //iteration through all source roots
            for (FileObject sourceRoot : globalPath) {
                Project serverProject = FileOwnerQuery.getOwner(sourceRoot);
                if (serverProject != null) {
                    if (!serverNames.add(serverProject.getProjectDirectory().getName())) //project was already visited
                    {
                        continue;
                    }

                    WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(serverProject.getProjectDirectory());
                    if (wss != null) { //project contains ws
                        List<WsCompileEditorSupport.ServiceSettings> services = wss.getServices();
                        boolean match = false;
                        for (Iterator sIt = services.iterator(); sIt.hasNext();) {
                            WsCompileEditorSupport.ServiceSettings serviceSettings =
                                    (WsCompileEditorSupport.ServiceSettings) sIt.next();
                            String serviceName = serviceSettings.getServiceName();
                            if (scNames.contains(serviceName)) { //matching ws name found
                                match = true;
                                break; //no need to continue
                            }
                        }
                        if (match) { //matching ws name found in project
                            //we need to add project's source folders onto a debugger's search path
                            AntProjectHelper serverHelper = wss.getAntProjectHelper();
                            String dcp = serverHelper.getStandardPropertyEvaluator().getProperty(WebProjectProperties.DEBUG_CLASSPATH);
                            if (dcp != null) {
                                String[] pathTokens = PropertyUtils.tokenizePath(dcp);
                                for (int i = 0; i <
                                        pathTokens.length; i++) {
                                    File f = new File(pathTokens[i]);
                                    if (!f.isAbsolute()) {
                                        pathTokens[i] = serverProject.getProjectDirectory().getPath() + "/" + pathTokens[i];
                                    }
                                    clientDCP.append(pathTokens[i] + ":");
                                }
                            }

                            String wdd = serverHelper.getStandardPropertyEvaluator().getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
                            if (wdd != null) {
                                String[] pathTokens = PropertyUtils.tokenizePath(wdd);
                                for (int i = 0; i <
                                        pathTokens.length; i++) {
                                    File f = new File(pathTokens[i]);
                                    if (!f.isAbsolute()) {
                                        pathTokens[i] = serverProject.getProjectDirectory().getPath() + "/" + pathTokens[i];
                                    }
                                    clientWDD.append(pathTokens[i] + ":");
                                }
                            }
                        }
                    }
                }
            }
            if (clientDCP.length()>0) {
                p.setProperty(WebProjectProperties.WS_DEBUG_CLASSPATHS, clientDCP.toString());
            }
            if (clientWDD.length() > 0) {
                p.setProperty(WebProjectProperties.WS_WEB_DOCBASE_DIRS, clientWDD.toString());
            }
        }
    }

    // Fix for IZ#170419 - Invoking Run took 29110 ms.
    private boolean runServlet( Properties p, FileObject javaFile, String
            actionName , boolean debug, boolean profile, List<String> targetNames) 
    {
        assert !(debug && profile);
        
        // run servlet
        // PENDING - what about servlets with main method? servlet should take
        // precedence
        WebModule webModule = WebModule.getWebModule(javaFile);
        final Dialog[] waitDialog = new Dialog[1];
        final boolean[] cancel = new boolean[1];
        if (SetExecutionUriAction.isScanInProgress(webModule, javaFile,
                new ServletScanObserver() {

                    @Override
                    public void scanFinished() {
                        SwingUtilities.invokeLater( new Runnable(){
                            @Override
                            public void run(){
                                if ( waitDialog[0]!=null ){
                                    waitDialog[0].setVisible(false);
                                    waitDialog[0].dispose();
                                }
                            }
                        });
                    }
                }))
        {
            JLabel label = new JLabel(
                    NbBundle
                            .getMessage(WebActionProvider.class, "MSG_WaitScan"),
                    javax.swing.UIManager.getIcon("OptionPane.informationIcon"),
                    SwingConstants.LEFT); // NOI18N
            label.setBorder(new EmptyBorder(12, 12, 11, 11));
            DialogDescriptor dd = new DialogDescriptor(label, NbBundle
                    .getMessage(WebActionProvider.class, actionName).
                    replace("&", ""),true, new Object[] { NbBundle.getMessage(
                            WebActionProvider.class,"LBL_CancelAction", 
                            new Object[] { NbBundle
                                    .getMessage(WebActionProvider.class,
                                            actionName) }) }, null, 0,
                    null, new ActionListener() {
                        
                        @Override
                        public void actionPerformed( ActionEvent arg0 ) {
                            if ( waitDialog[0]!=null ){
                                waitDialog[0].setVisible(false);
                                waitDialog[0].dispose();
                                cancel[0] = true;
                            }                            
                        }
                    });        //NOI8N
            waitDialog[0] = DialogDisplayer.getDefault().createDialog(dd);
            waitDialog[0].pack();
            waitDialog[0].setVisible(true);
            
            if ( cancel[0] ){
                return true;
            }
        }
        String[] urlPatterns = SetExecutionUriAction.getServletMappings(
                webModule, javaFile);
        if (urlPatterns != null && urlPatterns.length > 0) {
            ServletUriPanel uriPanel = new ServletUriPanel(
                    urlPatterns,
                    (String) javaFile
                            .getAttribute(SetExecutionUriAction.ATTR_EXECUTION_URI),
                    false);
            DialogDescriptor desc = new DialogDescriptor(uriPanel, NbBundle
                    .getMessage(WebActionProvider.class,
                            "TTL_setServletExecutionUri"));
            Object res = DialogDisplayer.getDefault().notify(desc);
            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                p.setProperty("client.urlPart", uriPanel.getServletUri()); // NOI18N
                try {
                    javaFile.setAttribute(
                            SetExecutionUriAction.ATTR_EXECUTION_URI, uriPanel
                                    .getServletUri());
                }
                catch (IOException ex) {
                }
            }
            else {
                return true;
            }
        }
        else if (debug ){
            return debugEmptyMapping(javaFile);
        }
        else {
            return runEmptyMapping(javaFile);
        }
        p.setProperty(BaseActionProvider.PROPERTY_RUN_SINGLE_ON_SERVER, "yes");
        if (profile) {
            targetNames.add("profile"); // NOI18N
        } else if (debug) {
            targetNames.add("debug"); // NOI18N
        } else {
            targetNames.add("run"); // NOI18N
        }
        return true;
    }

    private boolean runEmptyMapping( FileObject javaFile ) {
        String mes = java.text.MessageFormat.format(NbBundle.getMessage(
                WebActionProvider.class, "TXT_noExecutableClass"),
                new Object[] { javaFile.getName() });
        NotifyDescriptor desc = new NotifyDescriptor.Message(mes,
                NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
        return true;
    }
    
    private boolean debugEmptyMapping( FileObject javaFile ){
        JavaSource js = JavaSource.forFileObject(javaFile);
        if (isWebService(js)) {  //cannot debug web service implementation file
            String mes = java.text.MessageFormat.format(
                    NbBundle.getMessage(WebActionProvider.class, 
                            "TXT_cannotDebugWebservice"),
                    new Object[]{javaFile.getName()});      // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                    NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return true;
        } else {
            String mes = java.text.MessageFormat.format(
                    NbBundle.getMessage(WebActionProvider.class, 
                            "TXT_missingServletMappings"),
                    new Object[]{javaFile.getName()});      // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                    NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return true;
        }
    }

    /* Deletes translated class/java file to force recompilation of the page with all includes
     */
    public void invalidateClassFile(FileObject jsp) {
        String dir = getAntProjectHelper().getStandardPropertyEvaluator().getProperty(WebProjectProperties.BUILD_GENERATED_DIR);
        if (dir == null) {
            return;
        }

        dir = dir + "/src"; //NOI18N
        WebModule wm = WebModule.getWebModule(jsp);
        if (wm == null) {
            return;
        }

        String name = Utils.getServletName(wm.getDocumentBase(), jsp);
        String filePath = name.substring(0, name.lastIndexOf('.')).replace('.', '/');

        String fileClass = dir + '/' + filePath + ".class"; //NOI18N
        String fileJava = dir + '/' + filePath + ".java"; //NOI18N

        FileObject fC = FileUtil.toFileObject(getAntProjectHelper().resolveFile(fileClass));
        FileObject fJ = FileUtil.toFileObject(getAntProjectHelper().resolveFile(fileJava));
        try {
            if ((fJ != null) && (fJ.isValid())) {
                fJ.delete();
            }

            if ((fC != null) && (fC.isValid())) {
                fC.delete();
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    /* checks if timestamp of any of the included pages is higher than the top page
     */
    public boolean areIncludesModified(FileObject jsp) {
        boolean modified = false;
        WebModule wm = WebModule.getWebModule(jsp);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(jsp, wm, JspParserAPI.ERROR_IGNORE);
        if (!result.isParsingSuccess()) {
            modified = true;
        } else {
            PageInfo pi = result.getPageInfo();
            if (pi != null) {
                List includes = pi.getDependants();
                if ((includes != null) && (includes.size() > 0)) {
                    long jspTS = jsp.lastModified().getTime();
                    int size = includes.size();
                    for (int i = 0; i < size; i++) {
                        String filename = (String) includes.get(i);
                        filename = FileUtil.toFile(wm.getDocumentBase()).getPath() + filename;
                        File f = new File(filename);
                        long incTS = f.lastModified();
                        if (incTS > jspTS) {
                            modified = true;
                            break;
                        }
                    }
                }
            }
        }
        return modified;
    }

// PENDING - should not this be in some kind of an API?
    private boolean decodeBoolean(String raw) {
        if (raw != null) {
            String lowecaseRaw = raw.toLowerCase();

            if (lowecaseRaw.equals("true") || // NOI18N
                    lowecaseRaw.equals("yes") || // NOI18N
                    lowecaseRaw.equals("enabled")) // NOI18N
            {
                return true;
            }
        }
        return false;
    }

    private void setAllPropertiesForSingleJSPCompilation(Properties p, FileObject[] files) {
        p.setProperty("jsp.includes", getBuiltJspFileNamesAsPath(files)); // NOI18N
         /*ActionUtils.antIncludesList(files, project.getWebModule ().getDocumentBase ())*/

        p.setProperty("javac.jsp.includes", getCommaSeparatedGeneratedJavaFiles(files)); // NOI18N

    }

    public String getCommaSeparatedGeneratedJavaFiles(FileObject[] jspFiles) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < jspFiles.length; i++) {
            String jspRes = getJspResource(jspFiles[i]);
            if (i > 0) {
                b.append(',');
            }
            b.append(Utils.getGeneratedJavaResource(jspRes));
        }
        return b.toString();
    }

    /** Returns a resource name for a given JSP separated by / (does not start with a /).
     */
    private String getJspResource(FileObject jsp) {
        ProjectWebModule pwm = getWebProject().getWebModule();
        FileObject webDir = pwm.getDocumentBase();
        return FileUtil.getRelativePath(webDir, jsp);
    }

    public File getBuiltJsp(FileObject jsp) {
        ProjectWebModule pwm = getWebProject().getWebModule();
        FileObject webDir = pwm.getDocumentBase();
        String relFile = FileUtil.getRelativePath(webDir, jsp).replace('/', File.separatorChar);
        File webBuildDir = pwm.getContentDirectoryAsFile();
        return new File(webBuildDir, relFile);
    }

    public String getBuiltJspFileNamesAsPath(FileObject[] files) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            String path = getBuiltJsp(files[i]).getAbsolutePath();
            if (i > 0) {
                b.append(File.pathSeparator);
            }
            b.append(path);
        }
        return b.toString();
    }

    private WebProject getWebProject() {
        return (WebProject)getProject();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        if (command.equals(COMMAND_VERIFY)) {
            return getWebProject().getWebModule().hasVerifierSupport();
        }
        if (super.isActionEnabled(command, context)) {
            return true;
        }
        if (command.equals(COMMAND_COMPILE_SINGLE) ||
            command.equals(COMMAND_DEBUG_SINGLE) ||
            command.equals(COMMAND_RUN_SINGLE) ||
            command.equals(COMMAND_PROFILE_SINGLE)) {
            if (findJsps(context) != null || findHtml(context) != null ) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // Private methods -----------------------------------------------------

    /*
     * copied from ActionUtils and reworked so that it checks for mimeType of files, and DOES NOT include files with suffix 'suffix'
     */
    private static FileObject[] findSelectedFilesByMimeType(Lookup context, FileObject dir, String mimeType, String suffix, boolean strict) {
        if (dir != null && !dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }

        List<FileObject> files = new ArrayList<FileObject>();
        for (DataObject d : context.lookupAll(DataObject.class)) {
            FileObject f = d.getPrimaryFile();
            boolean matches = FileUtil.toFile(f) != null;
            if (dir != null) {
                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
            }
            if (mimeType != null) {
                matches &= f.getMIMEType().equals(mimeType);
            }
            if (suffix != null) {
                matches &= !f.getNameExt().endsWith(suffix);
            }
            // Generally only files from one project will make sense.
            // Currently the action UI infrastructure (PlaceHolderAction)
            // checks for that itself. Should there be another check here?
            if (matches) {
                files.add(f);
            } else if (strict) {
                return null;
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        return files.toArray(new FileObject[0]);
    }

    private FileObject[] findHtml(Lookup context) {
        FileObject webDir = getWebProject().getWebModule().getDocumentBase();
        FileObject[] files = null;
        if (webDir != null) {
            files = findSelectedFilesByMimeType(context, webDir, "text/html", null, true);
            if(files == null) {
                files = findSelectedFilesByMimeType(context, webDir, "text/xhtml", null, true);
            }
        }
        return files;
    }

    /** Find selected jsps
     */
    private FileObject[] findJsps(Lookup context) {
        FileObject webDir = getWebProject().getWebModule().getDocumentBase();
        FileObject[] files = null;
        if (webDir != null) {
            files = findSelectedFilesByMimeType(context, webDir, "text/x-jsp", ".jspf", true);
        }
        return files;
    }

    private boolean isDebugged() {
        J2eeModuleProvider jmp = (J2eeModuleProvider) getWebProject().getLookup().lookup(J2eeModuleProvider.class);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        ServerDebugInfo sdi = null;
        if (sessions != null && sessions.length > 0) {
            sdi = jmp.getServerDebugInfo();
            if (sdi == null) {
                return false;
            }
        }
        if (sessions != null) {
            for (int i = 0; i < sessions.length; i++) {
                Session s = sessions[i];
                if (s != null) {
                    Object o = s.lookupFirst(null, AttachingDICookie.class);
                    if (o != null) {
                        AttachingDICookie attCookie = (AttachingDICookie) o;
                        if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                            String shmem = attCookie.getSharedMemoryName();
                            if (shmem == null) {
                                continue;
                            }
                            if (shmem.equalsIgnoreCase(sdi.getShmemName())) {
                                return true;
                            }
                        } else {
                            String hostname = attCookie.getHostName();
                            if (hostname == null) {
                                continue;
                            }
                            if (hostname.equalsIgnoreCase(sdi.getHost())) {
                                if (attCookie.getPortNumber() == sdi.getPort()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkSelectedServer(boolean checkDebug, boolean checkProfile) {
        final PropertyEvaluator eval = getAntProjectHelper().getStandardPropertyEvaluator();
        if ("false".equals(eval.getProperty(WebProjectProperties.J2EE_SERVER_CHECK))) { // NOI18N
            return true;
        }
        return J2EEProjectProperties.checkSelectedServer(getProject(), getAntProjectHelper(),
                ((WebProject) getProject()).getAPIWebModule().getJ2eeProfile(), J2eeModule.Type.WAR, new J2EEProjectProperties.SetServerInstanceCallback() {

            @Override
            public void setServerInstance(String serverInstanceId) {
                WebActionProvider.this.setServerInstance(serverInstanceId);
            }
        }, checkDebug, checkProfile, false);
    }

    private void setServerInstance(String serverInstanceId) {
        WebProjectProperties.setServerInstance((WebProject)getProject(), getUpdateHelper(), serverInstanceId);
    }

    private boolean isDDServlet(Lookup context, FileObject javaClass) {
//        FileObject webDir = project.getWebModule ().getDocumentBase ();
//        if (webDir==null) return false;
//        FileObject fo = webDir.getFileObject("WEB-INF/web.xml"); //NOI18N

        FileObject webInfDir = getWebProject().getWebModule().getWebInf();
        if (webInfDir == null) {
            return false;
        }

        FileObject fo = webInfDir.getFileObject(ProjectWebModule.FILE_DD);
        if (fo == null) {
            return false;
        }

        String relPath = FileUtil.getRelativePath(getRoot(getWebProject().getSourceRoots().getRoots(), javaClass), javaClass);
        // #117888
        String className = relPath.replace('/', '.').replaceFirst("\\.java$", ""); // is there a better way how to do it?
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
            Servlet servlet = (Servlet) webApp.findBeanByName("Servlet", "ServletClass", className); //NOI18N
            if (servlet != null) {
                return true;
            } else {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
    }

    private FileObject getRoot(FileObject[] roots, FileObject file) {
        FileObject srcDir = null;
        for (int i = 0; i < roots.length; i++) {
            if (FileUtil.isParentOf(roots[i], file) || roots[i].equals(file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }

    private void setDirectoryDeploymentProperty(Properties p) {
        String instance = getAntProjectHelper().getStandardPropertyEvaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            J2eeModuleProvider jmp = getProject().getLookup().lookup(J2eeModuleProvider.class);
            String sdi = jmp.getServerInstanceID();
            J2eeModule mod = jmp.getJ2eeModule();
            if (sdi != null && mod != null) {
                boolean cFD = Deployment.getDefault().canFileDeploy(instance, mod);
                p.setProperty(DIRECTORY_DEPLOYMENT_SUPPORTED, "" + cFD); // NOI18N
            }
        }
    }

    private boolean isWebService(JavaSource js) {
        final boolean[] foundWebServiceAnnotation = {false};
        if (js != null) {
            try {
                js.runUserActionTask(new org.netbeans.api.java.source.Task<CompilationController>() {
                    @Override
                    public void run(CompilationController ci) throws Exception {
                        ci.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement classEl = org.netbeans.modules.j2ee.core.api.support.java.SourceUtils.getPublicTopLevelElement(ci);
                        if (classEl != null) {
                            TypeElement wsElement = ci.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                            if (wsElement != null) {
                                List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();
                                for (AnnotationMirror anMirror : annotations) {
                                    if (ci.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                                        foundWebServiceAnnotation[0] = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }, true);
            } catch (java.io.IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
        }
        return foundWebServiceAnnotation[0];
    }

    private static class CallbackImpl implements BaseActionProvider.Callback2 {

        private final J2eeModuleProvider provider;

        // be aware: there are two ClassPathProviderImpl: one in java.api.common and second in web.project
        private final ClassPathProviderImpl cp;

        public CallbackImpl(ClassPathProviderImpl cp, J2eeModuleProvider provider) {
            this.cp = cp;
            this.provider = provider;
        }

        @Override
        public ClassPath getProjectSourcesClassPath(String type) {
            return cp.getProjectSourcesClassPath(type);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            return cp.findClassPath(file, type);
        }

        @Override
        public void antTargetInvocationFailed(String command, Lookup context) {
            Deployment.getDefault().resumeDeployOnSave(provider);
        }

        @Override
        public void antTargetInvocationFinished(String command, Lookup context, int result) {
            Deployment.getDefault().resumeDeployOnSave(provider);
        }

        @Override
        public void antTargetInvocationStarted(String command, Lookup context) {
            Deployment.getDefault().suspendDeployOnSave(provider);
        }
    }

    @ProjectServiceProvider(
            service = ActionProvider.class,
            projectTypes = {@LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-project", position=1)})
    public static WebActionProvider create(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        final WebProject project = lkp.lookup(WebProject.class);
        final WebActionProvider webActionProvider = new WebActionProvider(project, project.getUpdateHelper(), project.evaluator());
        webActionProvider.startFSListener();
        return webActionProvider;
    }
}
