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

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class WLIncrementalDeployment extends IncrementalDeployment implements IncrementalDeployment2 {
    
    private static final Logger LOGGER = Logger.getLogger(WLIncrementalDeployment.class.getName());
    
    private static RequestProcessor DEPLOYMENT_RP = new RequestProcessor("Weblogic Incremental Deployment", 5); // NOI18N

    private static final boolean FORBID_DIRECTORY_DEPLOYMENT = Boolean.getBoolean(WLIncrementalDeployment.class.getName() + ".forbidDirectoryDeployment");

    private static final int FAST_SWAP_TIMEOUT = 3000;
    
    private static final int FAST_SWAP_POLLING = 100;

    private static final String WLDEPLOY = "wldeploy"; // NOI18N

    private final WLDeploymentManager dm;

    public WLIncrementalDeployment(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public boolean canFileDeploy(Target target, J2eeModule deployable) {
        if (FORBID_DIRECTORY_DEPLOYMENT || dm.isRemote()) {
            return false;
        }
        return deployable != null && !J2eeModule.Type.CAR.equals(deployable.getType())
                && !J2eeModule.Type.RAR.equals(deployable.getType());
    }

    @Override
    public File getDirectoryForModule(final TargetModuleID module) {
        if (module.getParentTargetModuleID() == null) {
            return null;
        }

        File file = null;
        // this won't happen currently just being defensive
        if (module instanceof WLTargetModuleID) {
            file = ((WLTargetModuleID) module).getDir();
        }

        // the following code should work even for standalone apps (not just in
        // EAR) but the condition above prevents such call to be defensive
        if (file == null) {
            WLConnectionSupport support = dm.getConnectionSupport();
            try {
                file = support.executeAction(new WLConnectionSupport.JMXRuntimeAction<File>() {

                    @Override
                    public File call(MBeanServerConnection con, ObjectName service) throws Exception {
                        Object uri = null;
                        Object path = null;

                        TargetModuleID parent = module.getParentTargetModuleID() == null ? module : module.getParentTargetModuleID();

                        ObjectName testPattern = new ObjectName("com.bea:Name=" + parent.getModuleID() + ",Location="
                                + parent.getTarget().getName() + ",Type=AppDeployment,*"); // NOI18N
                        Set<ObjectName> deployments = con.queryNames(testPattern, null);
                        if (!deployments.isEmpty()) {
                            ObjectName appItem = (ObjectName) con.getAttribute(
                                    deployments.iterator().next(), "AppMBean");
                            if (appItem == null) {
                                return null;
                            }

                            ObjectName[] comps = (ObjectName[]) con.getAttribute(appItem, "Components");
                            for (ObjectName comp : comps) {
                                String name = (String) con.getAttribute(comp, "Name");
                                if (module.getModuleID().equals(name)) {
                                    uri = (String) con.getAttribute(comp, "URI");
                                    if (uri != null) {
                                        break;
                                    }
                                }
                            }
                            // FullPath attribute is sometimes wrong :(
                            // TODO resolve relative paths - is it possible ?
                            path = con.getAttribute(appItem, "Path");
                        }

                        if (path == null || (uri == null && module.getParentTargetModuleID() != null)) {
                            return null;
                        }

                        return (uri != null) ? new File(path.toString() + File.separator + uri.toString()) : new File(path.toString());
                    }
                });
            } catch (Exception ex) {
                // pass through
            }
        }

        if (null != file && !file.isDirectory()) {
            throw new IllegalStateException(NbBundle.getMessage(WLIncrementalDeployment.class,
                    "ERR_UndeployAndRedeploy"));
        }
        return file;
    }

    @Override
    public File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration) {
        // FIXME more or less copied from GlassFish
        // 1) should be in common place
        // 2) the logic is inverted - this should be in project
        File dest = null;
        if (app.getType() == J2eeModule.Type.EAR) {
            File tmp = getProjectDir(app);
            if (null == tmp) {
               return dest;
            }
            dest = new File(tmp, "target");  // NOI18N
            if (!dest.exists()) {
                // the app wasn't a maven project
                dest = new File(tmp, "dist");  // NOI18N
            }
            if (dest.isFile() || (dest.isDirectory() && !dest.canWrite())) {
               throw new IllegalStateException();
            }
            String moduleName = sanitizeName(computeModuleID(app));
            String dirName = WLDEPLOY;
            if (null != moduleName) {
                dirName += "/"+moduleName; // NOI18N
            }
            dest = new File(dest, dirName);
            boolean retval = true;
            if (!dest.exists()) {
                retval = dest.mkdirs();
            }
            if (!retval || !dest.isDirectory()) {
               dest = null;
            }
        }
        return dest;
    }

    @Override
    public File getDirectoryForNewModule(File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        //return new File(appDir, transform(removeLeadSlash(uri)));
        return new File(appDir, removeLeadSlash(uri));
    }

    @Override
    public ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes) {
        boolean redeploy = changes.classesChanged() || changes.descriptorChanged()
                || changes.ejbsChanged() || changes.manifestChanged() || changes.serverDescriptorChanged();
        if (changes instanceof DeploymentChangeDescriptor) {
            DeploymentChangeDescriptor deploymentChanges = (DeploymentChangeDescriptor) changes;
            redeploy = redeploy || deploymentChanges.serverResourcesChanged();
        }

        if (!redeploy) {
            WLProgressObject progress = new WLProgressObject(module);
            progress.fireProgressEvent(module, new WLDeploymentStatus(
                    ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                    NbBundle.getMessage(WLIncrementalDeployment.class, "MSG_Deployment_Completed")));
            return progress;
        }

        return dm.redeploy(module);
//        CommandBasedDeployer deployer = new CommandBasedDeployer(dm);
//        return deployer.directoryRedeploy(module, dm.getDeployTargets());
    }

    @Override
    public ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration,
            File dir) {

        String name = dir.getName();
        // FIXME this needs more fine tuning (escape chars)
        try {
            FileObject content = app.getContentDirectory();
            if (content != null) {
                Project project = FileOwnerQuery.getOwner(content);
                if (project != null) {
                    name = ProjectUtils.getInformation(project).getName();
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        CommandBasedDeployer deployer = new CommandBasedDeployer(dm);
        return deployer.directoryDeploy(target, name, dir, dm.getHost(), dm.getPort(),
                dm.getCommonConfiguration().isSecured(), app.getType());
    }

    @Override
    public ProgressObject incrementalDeploy(TargetModuleID module, DeploymentContext context) {
        dm.deployOptionalPackages(context.getRequiredLibraries());
        return incrementalDeploy(module, context.getChanges());
    }

    @Override
    public ProgressObject initialDeploy(Target target, DeploymentContext context) {
        dm.deployOptionalPackages(context.getRequiredLibraries());
        return initialDeploy(target, context.getModule(), null, context.getModuleFile());
    }

    @Override
    public boolean isDeployOnSaveSupported() {
        return !dm.isRemote();
    }

    @Override
    public ProgressObject deployOnSave(final TargetModuleID module,
            final DeploymentChangeDescriptor desc) {

        if (desc.classesChanged() && !desc.descriptorChanged()
                && !desc.ejbsChanged() && !desc.manifestChanged()
                && !desc.serverDescriptorChanged() && !desc.serverResourcesChanged()) {
            final BridgingProgressObject progress = new BridgingProgressObject(module);
            progress.fireProgressEvent(null, new WLDeploymentStatus(
                    ActionType.EXECUTE, CommandType.REDEPLOY, StateType.RUNNING,
                    NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Started")));
        
            DEPLOYMENT_RP.submit(new Runnable() {

                @Override
                public void run() {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                            NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeploying", module.getModuleID())));
// DISABLED SINCE 7.1.1 see #206798
//                    if (deployFastSwap(module)) {
//                        LOGGER.log(Level.FINE, "Fast swap successful");
//                        progress.fireProgressEvent(null, new WLDeploymentStatus(
//                                ActionType.EXECUTE, CommandType.REDEPLOY, StateType.COMPLETED,
//                                NbBundle.getMessage(CommandBasedDeployer.class, "MSG_Redeployment_Completed")));
//                        return;
//                    }
//                    LOGGER.log(Level.FINE, "Fast swap failed doing incremental deploy");
                    progress.startListening(module, incrementalDeploy(module, desc));
                }
            });
            return progress;
        }
        return incrementalDeploy(module, desc);
    }
    
    @Override
    public String getModuleUrl(final TargetModuleID module) {
        assert module != null;

        if (module.getWebURL() == null) {
            String url = module.getModuleID();
            return url.startsWith("/") ? url : "/" + url;
        }

        // TODO is this hack ?
        // looks like weblogic (TargetModulesIDs returned by server)
        // is using weburl as moduleID for war in ear
        // and ejb jar name for ejb in ear, we need moduleURI
        final String id = module.getModuleID();
        final String slashId = id.startsWith("/") ? id : "/" + id;
        WLConnectionSupport support = dm.getConnectionSupport();
        String url = null;
        try {
            url = support.executeAction(new WLConnectionSupport.JMXRuntimeAction<String>() {

                @Override
                public String call(MBeanServerConnection con, ObjectName service) throws Exception {
                    ObjectName pattern = new ObjectName(
                            "com.bea:Type=WebAppComponentRuntime,*"); // NOI18N

                    Set<ObjectName> runtimes = con.queryNames(pattern, null);
                    for (ObjectName runtime : runtimes) {
                        String moduleId = (String) con.getAttribute(runtime, "ModuleId"); // NOI18N
                        if (id.equals(moduleId)
                                || slashId.equals(moduleId)) {
                            return (String) con.getAttribute(runtime, "ModuleURI"); // NOI18N
                        }
                    }

                    TargetModuleID parent = module.getParentTargetModuleID();
                    ObjectName testPattern = new ObjectName("com.bea:Name=" + module.getModuleID() + ",Location="
                            + module.getTarget().getName() + ",Type=WebAppComponent"
                            + (parent != null ? ",Application=" + parent.getModuleID() + ",*" : ",*")); // NOI18N
                    Set<ObjectName> deployments = con.queryNames(testPattern, null);
                    if (!deployments.isEmpty()) {
                        return (String) con.getAttribute(deployments.iterator().next(), "URI"); // NOI18N
                    }

                    return null;
                }
            });
        } catch (Exception ex) {
            // pass through
        }
        if (url != null) {
            return url.startsWith("/") ? url : "/" + url; // NOI18N
        }
        // will fail probably
        return id.startsWith("/") ? id : "/" + id; // NOI18N
    }
    
    private boolean deployFastSwap(TargetModuleID module) {
        final String server = module.getTarget().getName();
        final String application = module.getModuleID();

        WLConnectionSupport support = dm.getConnectionSupport();
        try {
            Boolean ret = support.executeAction(new WLConnectionSupport.JMXAction<Boolean>() {

                @Override
                public Boolean call(MBeanServerConnection connection) throws Exception {
                    ObjectName appRuntime = new ObjectName(
                            "com.bea:ServerRuntime=" + server + ",Name=" + application + ",Type=ApplicationRuntime"); // NOI18N
                    ObjectName redefinitionRuntime = (ObjectName) connection.getAttribute(
                            appRuntime, "ClassRedefinitionRuntime"); // NOI18N

                    if (redefinitionRuntime == null) {
                        return false;
                    }

                    ObjectName redef = (ObjectName) connection.invoke(
                            redefinitionRuntime, "redefineClasses", new Object[] {null, null}, // NOI18N
                            new String[] {String.class.getName(), String[].class.getName()});
                    
                    long start = System.currentTimeMillis();
                    String str = (String) connection.getAttribute(redef, "Status"); // NOI18N
                    while ((System.currentTimeMillis() - start) < FAST_SWAP_TIMEOUT && isRunning(str)) {
                        Thread.sleep(FAST_SWAP_POLLING);
                        str = (String) connection.getAttribute(redef, "Status"); // NOI18N
                    }

                    Integer candidates = (Integer) connection.getAttribute(redef, "CandidateClassesCount"); // NOI18N
                    if (LOGGER.isLoggable(Level.FINE)) {
                        Integer processed = (Integer) connection.getAttribute(redef, "ProcessedClassesCount"); // NOI18N
                        LOGGER.log(Level.FINE, "Processed {0} from {1} candidate classes", // NOI18N
                                new Object[] {processed, candidates});
                    }

                    if (isRunning(str)) {
                        connection.invoke(redef, "cancel", new Object[0], new String[0]); // NOI18N
                        return false;
                    }

                    if (!("FINISHED".equals(str)) // NOI18N
                            || (candidates != null && candidates.intValue() == 0)) {
                        return false;
                    }
                    return true;
                }

                @Override
                public String getPath() {
                    return "/jndi/weblogic.management.mbeanservers.runtime"; // NOI18N
                }
            });
            return ret.booleanValue();
        }catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
    }
    
    private static boolean isRunning(String status) {
        return "RUNNING".equals(status) || "SCHEDULED".equals(status); // NOI18N
    }

    // FIXME copied from GF
    // try to get the Project Directory as a File
    // use a couple different stratgies, since the resource.dir is in a user-
    // editable file -- but it is quicker to access, if it is there....
    //
    private File getProjectDir(J2eeModule app) {
        try {
            FileObject fo = app.getContentDirectory();
            Project p = FileOwnerQuery.getOwner(fo);
            if (null != p) {
                fo = p.getProjectDirectory();
                return FileUtil.toFile(fo);
            }
        } catch (IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.FINER,    // NOI18N
                    null,ex);
        }
        java.io.File tmp = app.getResourceDirectory();

        if (tmp != null) {
            return tmp.getParentFile();
        }
        return null;
    }

    // FIXME copied from GF
    public static String sanitizeName(String name) {
        if (null == name || name.matches("[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#:]*")) {
            return name;
        }
        // the string is bad...
        return "_" + name.replaceAll("[^\\p{L}\\p{N}\\-_./;#:]", "_");
    }

    // FIXME copied from GF
    public static String computeModuleID(J2eeModule module) {
        String moduleID = null;
        FileObject fo = null;
        try {
            fo = module.getContentDirectory();
            if (null != fo) {
                moduleID = ProjectUtils.getInformation(FileOwnerQuery.getOwner(fo)).getDisplayName();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINER, null, ex);
        }

//        if (null == moduleID || moduleID.trim().length() < 1) {
//            J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getSunDDModuleHelper(module.getType());
//            if(j2eeModuleHelper != null) {
//                RootInterface rootDD = j2eeModuleHelper.getStandardRootDD(module);
//                if(rootDD != null) {
//                    try {
//                        moduleID = rootDD.getDisplayName(null);
//                    } catch (VersionNotSupportedException ex) {
//                        // ignore, handle as null below.
//                    }
//                }
//            }
//        }

        return moduleID;
    }

    // FIXME copied from GF
//    private static String transform(String s) {
//        int len = s.length();
//        if (len > 4) {
//            StringBuilder sb = new StringBuilder(s);
//            char tmp = sb.charAt(len - 4);
//            if (tmp == '.') {
//                sb.setCharAt(len-4, '_');
//                return sb.toString();
//            }
//        }
//        return s;
//    }

    // FIXME copied from GF
    private static String removeLeadSlash(String s) {
        if (null == s) {
            return s;
        }
        if (s.length() < 1) {
            return s;
        }
        if (!s.startsWith("/")) {
            return s;
        }
        return s.substring(1);
    }

    private static class BridgingProgressObject extends WLProgressObject implements ProgressListener {

        public BridgingProgressObject(TargetModuleID... moduleIds) {
            super(moduleIds);
        }
        
        public void startListening(TargetModuleID moduleID, ProgressObject po) {
            po.addProgressListener(this);
            if (!po.getDeploymentStatus().isRunning()) {
                po.removeProgressListener(this);
            }
            fireProgressEvent(moduleID, po.getDeploymentStatus());
        }
        
        @Override
        public void handleProgressEvent(ProgressEvent pe) {
            if (!getDeploymentStatus().isRunning() && pe.getDeploymentStatus().isRunning()) {
                // prevent wrong ordering
                return;
            }
            fireProgressEvent(pe.getTargetModuleID(), pe.getDeploymentStatus());
        }
        
    }
}
