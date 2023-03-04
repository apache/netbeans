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

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.earproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.lookup.Lookups;

/**
 * Action provider of the Enterprise Application project.
 */
public class EarActionProvider implements ActionProvider {

    private static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N
    
    // Definition of commands
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
    private static final String COMMAND_VERIFY = "verify"; //NOI18N

    // Commands available from J2ee projects
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_RUN, 
        COMMAND_DEBUG, 
        COMMAND_PROFILE,
        EjbProjectConstants.COMMAND_REDEPLOY,
        COMMAND_VERIFY,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME
    };

    EarProject project;
    
    // Ant project helper of the project
    private final UpdateHelper updateHelper;
        
    /** Map from commands to ant targets */
    Map<String,String[]> commands;
    
    public EarActionProvider(EarProject project, UpdateHelper updateHelper) {
        commands = new HashMap<String, String[]>();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_PROFILE, new String[]{"profile"}); // NOI18N
        commands.put(EjbProjectConstants.COMMAND_REDEPLOY, new String[] {"run-deploy"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N
        
        this.updateHelper = updateHelper;
        this.project = project;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName ());
    }
    
    public String[] getSupportedActions() {
        return supportedActions.clone();
    }
    
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }

        String realCommand = command;
        J2eeModuleProvider.DeployOnSaveSupport support = project.getAppModule().getDeployOnSaveSupport();
        if (COMMAND_BUILD.equals(realCommand)
                && isCosEnabled() && support != null && support.containsIdeArtifacts()) {
            boolean cleanAndBuild = DeployOnSaveUtils.showBuildActionWarning(project,
                    new DeployOnSaveUtils.CustomizerPresenter() {

                public void showCustomizer(String category) {
                    CustomizerProviderImpl provider = project.getLookup().lookup(CustomizerProviderImpl.class);
                    provider.showCustomizer(category);
                }
            });
            if (cleanAndBuild) {
                realCommand = COMMAND_REBUILD;
            } else {
                return;
            }
        }
        
        final String commandToExecute = realCommand;
                Properties p = new Properties();
                String[] targetNames;

                targetNames = getTargetNames(commandToExecute, context, p);
                if (targetNames == null) {
                    return;
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (p.keySet().size() == 0) {
                    p = null;
                }
                final J2eeApplicationProvider app = EarActionProvider.this.project.getAppModule();
                final ActionProgress listener = ActionProgress.start(context);
                try {
                    Deployment.getDefault().suspendDeployOnSave(app);
                    ActionUtils.runTarget(findBuildXml(), targetNames, p).addTaskListener(new TaskListener() {

                        @Override
                        public void taskFinished(Task task) {
                            Deployment.getDefault().resumeDeployOnSave(app);
                            assert task instanceof ExecutorTask;
                            listener.finished(((ExecutorTask) task).result() == 0);
                        }
                    });
                } catch (IOException e) {
                    Deployment.getDefault().resumeDeployOnSave(app);
                    Exceptions.printStackTrace(e);
                    listener.finished(false);
                } catch (RuntimeException ex) {
                    Deployment.getDefault().resumeDeployOnSave(app);
                    listener.finished(false);
                    throw ex;
                }
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        // set context for advanced nbbrowse task:
        FileObject browserContextFile = context.lookup(FileObject.class);
        if (browserContextFile == null) {
            browserContextFile = project.getProjectDirectory();
        }
        String ctx = FileUtil.toFile(browserContextFile).getAbsolutePath();
        p.setProperty("browser.context", ctx);

        String[] targetNames = commands.get(command);
        
        //EXECUTION PART
        if (command.equals (COMMAND_RUN) || command.equals (EjbProjectConstants.COMMAND_REDEPLOY)) { //  || command.equals (COMMAND_DEBUG)) {
            if (!checkSelectedServer (false, false, false)) {
                return null;
            }
            if (command.equals (COMMAND_RUN) && isDebugged()) {
                p.setProperty("is.debugged", "true"); // NOI18N
            }
            if (command.equals (EjbProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
            setDirectoryDeploymentProperty(p);
        //DEBUGGING PART
        } else if (command.equals (COMMAND_DEBUG)) {
            if (!checkSelectedServer (true, false, false)) {
                return null;
            }
            setDirectoryDeploymentProperty(p);
            
            if (isDebugged()) {
                p.setProperty("is.debugged", "true"); // NOI18N
            }

            SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
            if (null != spp) {
                StringBuilder edbd = new StringBuilder();
                final Set s = spp.getSubprojects();
                Iterator<Project> iter = s.iterator();
                while (iter.hasNext()) {
                    Project proj = iter.next();
                    WebModuleProvider wmp = proj.getLookup().lookup(WebModuleProvider.class);
                    if (null != wmp) {
                        WebModule wm = wmp.findWebModule(proj.getProjectDirectory());
                        if (null != wm) {
                            FileObject fo = wm.getDocumentBase();
                            if (null != fo) {
                                edbd.append(FileUtil.toFile(fo).getAbsolutePath()+":"); //NOI18N
                            }
                        }
                    }
                }
                p.setProperty("ear.docbase.dirs", edbd.toString()); // NOI18N
            }
        // PROFILING PART
        } else if (command.equals (COMMAND_PROFILE)) {
            // TODO This is basically a copy of the debugging part for now. Figure out what to do here!
            
            if (!checkSelectedServer (false, true, false)) {
                return null;
            }
            setDirectoryDeploymentProperty(p);
//            
//            if (isDebugged()) {
//                p.setProperty("is.debugged", "true"); // NOI18N
//            }

            SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
            if (null != spp) {
                StringBuilder edbd = new StringBuilder();
                final Set s = spp.getSubprojects();
                Iterator<Project> iter = s.iterator();
                while (iter.hasNext()) {
                    Project proj = iter.next();
                    WebModuleProvider wmp = proj.getLookup().lookup(WebModuleProvider.class);
                    if (null != wmp) {
                        WebModule wm = wmp.findWebModule(proj.getProjectDirectory());
                        if (null != wm) {
                            FileObject fo = wm.getDocumentBase();
                            if (null != fo) {
                                edbd.append(FileUtil.toFile(fo).getAbsolutePath()+":"); //NOI18N
                            }
                        }
                    }
                }
                p.setProperty("ear.docbase.dirs", edbd.toString()); // NOI18N
            }
        //COMPILATION PART
        } else {
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        
        collectStartupExtenderArgs(p, command);

        return targetNames;
    }

     private void setDirectoryDeploymentProperty(Properties p) {
        String instance = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
            String sdi = jmp.getServerInstanceID();
            J2eeModule mod = jmp.getJ2eeModule();
            if (sdi != null && mod != null) {
                boolean cFD = Deployment.getDefault().canFileDeploy(instance, mod);
                p.setProperty(DIRECTORY_DEPLOYMENT_SUPPORTED, "" + cFD); // NOI18N
            }
        }
    }

    public boolean isActionEnabled( String command, Lookup context ) {
        if ( findBuildXml() == null ) {
            return false;
        }

        J2eeModuleProvider.DeployOnSaveSupport support = project.getAppModule().getDeployOnSaveSupport();
        if (isCosEnabled() && support != null && support.containsIdeArtifacts() && COMMAND_COMPILE_SINGLE.equals(command)) {
            return false;
        }

        if ( command.equals( COMMAND_VERIFY ) ) {
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            return provider != null && provider.hasVerifierSupport();
        } else if (command.equals(COMMAND_RUN)) {
            //see issue #92895
            //XXX - replace this method with a call to API as soon as issue 109895 will be fixed
            boolean isAppClientSelected = project.evaluator().getProperty("app.client") != null; //NOI18N
            return checkSelectedServer(false, false, true) && !(isAppClientSelected && isTargetServerRemote());
        }
        // other actions are global
        return true;
    }
    
    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (null == jmp) {
            // XXX this is a bug that I don't know about fixing yet
            return false;
        }
        
        ServerDebugInfo sdi = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    // calculate the sdi as late as possible.
                    if (null == sdi) {
                        sdi = jmp.getServerDebugInfo ();
                        if (null == sdi) {
                            return false;
                        }
                    }
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName() != null
                                && attCookie.getHostName().equalsIgnoreCase(sdi.getHost())
                                && attCookie.getPortNumber() == sdi.getPort()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean checkSelectedServer (boolean checkDebug, boolean checkProfile, boolean noMessages) {
        // XXX determine what to do with the ejb jar project properties
        return J2EEProjectProperties.checkSelectedServer(project, updateHelper.getAntProjectHelper(),
                project.getJ2eeProfile(), J2eeModule.Type.EAR, new J2EEProjectProperties.SetServerInstanceCallback() {

            @Override
            public void setServerInstance(String serverInstanceId) {
                EarProjectProperties.setServerInstance(project, updateHelper, serverInstanceId);
            }
        }, checkDebug, checkProfile, noMessages);
    }

    private boolean isTargetServerRemote() {
        J2eeModuleProvider module = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = module.getInstanceProperties();
        String domain = props.getProperty("DOMAIN"); //NOI18N
        String location = props.getProperty("LOCATION"); //NOI18N
        return "".equals(domain) && "".equals(location); //NOI18N
    }
    
    private boolean isCosEnabled() {
        return Boolean.parseBoolean(project.evaluator().getProperty(EarProjectProperties.J2EE_COMPILE_ON_SAVE));
    }
    
    private void collectStartupExtenderArgs(Map p, String command) {
        StringBuilder b = new StringBuilder();
        for (String arg : runJvmargsIde(command)) {
            b.append(' ').append(arg);
        }
        if (b.length() > 0) {
            p.put("run.jvmargs.ide", b.toString()); // NOI18N
        }
    }
    
    private List<String> runJvmargsIde(String command) {
        StartupExtender.StartMode mode;
        if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals(COMMAND_PROFILE) || command.equals(COMMAND_PROFILE_SINGLE)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (command.equals(COMMAND_TEST) || command.equals(COMMAND_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            return Collections.emptyList();
        }
        List<String> args = new ArrayList<String>();
        JavaPlatform p = getActivePlatform();
        for (StartupExtender group : StartupExtender.getExtenders(Lookups.fixed(project, p != null ? p : JavaPlatformManager.getDefault().getDefaultPlatform()), mode)) {
            args.addAll(group.getArguments());
        }
        return args;
    }
    
    private JavaPlatform getActivePlatform() {
        return CommonProjectUtils.getActivePlatform(project.evaluator().getProperty("platform.active"));
    }
}
