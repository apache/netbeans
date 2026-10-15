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
package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.openide.filesystems.FileUtil;

import javax.enterprise.deploy.shared.ModuleType;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.config.J2eeModuleAccessor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import org.netbeans.modules.j2ee.deployment.impl.projects.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Encapsulates a set of ServerTarget(s), provides a wrapper for deployment
 * help.  This is a throw away object, that get created and used within
 * scope of one deployment execution.
 *
 * Typical user are ServerExecutor and Debugger code, with the following general sequence:
 *
 *      TargetServer ts = new TargetServer(deploymentTarget);
 *      ts.startTargets(deployProgressUI);
 *      TargetModule[] tms = ts.deploy(deployProgressUI);
 *      deploymentTarget.setTargetModules(tms);
 */
public class TargetServer {

    private static final Logger LOGGER = Logger.getLogger(TargetServer.class.getName());
    private Target[] targets;
    private final ServerInstance instance;
    private final DeploymentTarget dtarget;
    private IncrementalDeployment incremental; //null value signifies don't do incremental
    private Map availablesMap = null;
    private Set deployedRootTMIDs = new HashSet(); // type TargetModule
    private Set undeployTMIDs = new HashSet(); // TMID
    private Set distributeTargets = new HashSet(); //Target
    private TargetModule[] redeployTargetModules = null;
    private File application = null;
    private File currentContentDir = null;
    private String contextRoot = null;

    public TargetServer(DeploymentTarget target) {
        this.dtarget = target;
        this.instance = dtarget.getServer().getServerInstance();
    }

    private void init(ProgressUI ui, boolean start, boolean processLast) throws ServerException {
        if (targets == null) {
            if (start) {
                instance.start(ui);
                targets = dtarget.getServer().toTargets();
            } else {
                Set<Target> tempTargets = new HashSet<Target>(Arrays.asList(dtarget.getServer().toTargets()));
                for (Iterator<Target> it = tempTargets.iterator(); it.hasNext();) {
                    Target target = it.next();
                    if (!instance.getStartServer().isRunning(target)) {
                        it.remove();
                    }
                }
                targets = tempTargets.toArray(new Target[0]);
            }
        }

        incremental = instance.getIncrementalDeployment();
        if (incremental != null && !checkServiceImplementations())
            incremental = null;

        try {
            FileObject contentFO = dtarget.getModule().getContentDirectory();
            if (contentFO != null) {
                currentContentDir = FileUtil.toFile(contentFO);
            }

        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }

        J2eeModuleProvider.ConfigSupport configSupport = dtarget.getConfigSupport();
        if (J2eeModule.Type.WAR.equals(dtarget.getModule().getType())) {
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                contextRoot = null;
            }
        }
        if (contextRoot == null) {
            J2eeModuleProvider provider = dtarget.getModuleProvider();
            if (provider instanceof J2eeApplicationProvider) {
                for (J2eeModuleProvider child : ((J2eeApplicationProvider) provider).getChildModuleProviders()) {
                    if (J2eeModule.Type.WAR.equals(child.getJ2eeModule().getType())) {
                        try {
                            contextRoot = child.getConfigSupport().getWebContextRoot();
                            break;
                        } catch (ConfigurationException e) {
                        }
                    }
                }
            }
        }

        if (processLast) {
            processLastTargetModules();
        }
    }

    private boolean canFileDeploy(Target[] targetz, J2eeModule deployable) throws IOException {
        if (targetz == null || targetz.length != 1) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));

            if (targetz != null && LOGGER.isLoggable(Level.FINE)) {
                StringBuilder builder = new StringBuilder("[");
                for (int i = 0; i < targetz.length; i++) {
                    if (i > 0) {
                        builder.append(",");
                    }
                    builder.append("[")
                            .append(targetz[i].getName())
                                .append(",")
                                    .append(targetz[i].getDescription())
                                        .append("]");
                }
                builder.append("]");
                LOGGER.log(Level.FINE, builder.toString());
            }

            return false;
        }

        if (deployable == null || null == deployable.getContentDirectory() || !instance.getIncrementalDeployment().canFileDeploy(targetz[0], deployable))
            return false;

        return true;
    }

    private boolean canFileDeploy(TargetModule[] targetModules, J2eeModule deployable) throws IOException {
        if (targetModules == null || targetModules.length != 1) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));

            if (targetModules != null && LOGGER.isLoggable(Level.FINE)) {
                StringBuilder builder = new StringBuilder("[");
                for (int i = 0; i < targetModules.length; i++) {
                    if (i > 0) {
                        builder.append(",");
                    }
                    builder.append("[")
                            .append(targetModules[i].getId())
                                .append(",")
                                    .append(targetModules[i].getTargetName())
                                        .append("]");
                }
                builder.append("]");
                LOGGER.log(Level.FINE, builder.toString());
            }

            return false;
        }

        if (deployable == null || null == deployable.getContentDirectory() || !instance.getIncrementalDeployment().canFileDeploy(targetModules[0].getTarget(), deployable))
            return false;

        return true;
    }

    private DeploymentChangeDescriptor distributeChanges(TargetModule targetModule, ProgressUI ui) throws IOException {
        ServerFileDistributor sfd = new ServerFileDistributor(instance, dtarget);
        try {
            ui.setProgressObject(sfd);
            ModuleChangeReporter mcr = dtarget.getModuleChangeReporter();
            ResourceChangeReporter rcr = dtarget.getResourceChangeReporter();
            DeploymentChangeDescriptor acd = sfd.distribute(targetModule, mcr, rcr);
            LOGGER.log(Level.FINE, "Change descriptor is {0}", acd);
            return acd;
        } finally {
            ui.setProgressObject(null);
        }
    }

    private DeploymentChangeDescriptor distributeChangesOnSave(TargetModule targetModule, Iterable<Artifact> artifacts) throws IOException {
        ServerFileDistributor sfd = new ServerFileDistributor(instance, dtarget);
        ModuleChangeReporter mcr = dtarget.getModuleChangeReporter();
        ResourceChangeReporter rcr = dtarget.getResourceChangeReporter();
        DeploymentChangeDescriptor acd = sfd.distributeOnSave(targetModule, mcr, rcr, artifacts);
        LOGGER.log(Level.FINE, "Change descriptor is {0}", acd);
        return acd;
    }

    private File initialDistribute(Target target, ProgressUI ui) throws ServerException {
        InitialServerFileDistributor sfd = new InitialServerFileDistributor(dtarget, target);
        try {
            ui.setProgressObject(sfd);
            File ret = sfd.distribute();

            if (sfd.getDeploymentStatus().isFailed()) {
                String statusMessage = sfd.getDeploymentStatus().getMessage();
                String msg = null;
                if (statusMessage != null) {
                    msg = NbBundle.getMessage(TargetServer.class, "MSG_DeployFailed", statusMessage);
                } else {
                    msg = NbBundle.getMessage(TargetServer.class, "MSG_DeployFailedNoMessage");
                }
                throw new ServerException(msg);
            }

            return ret;
        } finally {
            ui.setProgressObject(null);
        }
    }

    private boolean checkServiceImplementations() {
        String missing = null;
        if (instance.getServer().getModuleConfigurationFactory() == null) {
            missing = ModuleConfigurationFactory.class.getName();
        }

        if (missing != null) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_MissingServiceImplementations", missing);
            Logger.getLogger("global").log(Level.INFO, msg);
            return false;
        }

        return true;
    }

    // return list of TargetModule to redeploy
    private TargetModule[] checkUndeployForChangedReferences(Set toRedeploy) {
        // PENDING: what are changed references for ejbmod, j2eeapp???
        if (J2eeModule.Type.WAR.equals(dtarget.getModule().getType())) {
            for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                TargetModule deployed = (TargetModule) j.next();
                File lastContentDir = (deployed.getContentDirectory() == null) ? null : new File(deployed.getContentDirectory());

                // content dir or context root changes since last deploy
                if ((currentContentDir != null && ! currentContentDir.equals(lastContentDir)) ||
                      (contextRoot != null && ! contextRoot.equals(deployed.getContextRoot()))) {

                    distributeTargets.add(deployed.findTarget());
                    undeployTMIDs.add(deployed.delegate());
                    deployed.remove();
                    j.remove();
                }
            }
        }

        return (TargetModule[]) toRedeploy.toArray(new TargetModule[0]);
    }

    // return list of target modules to redeploy
    private TargetModule[] checkUndeployForSharedReferences(Target[] toDistribute) {
        Set distSet = new HashSet(Arrays.asList(toDistribute));
        return checkUndeployForSharedReferences(Collections.EMPTY_SET, distSet);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Set toDistribute) {
        return checkUndeployForSharedReferences(toRedeploy, toDistribute, null);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Set toDistribute, Map queryInfo) {
        // we don't want to undeploy anything when both distribute list and redeploy list are empty
        if (contextRoot == null || (toRedeploy.isEmpty() && toDistribute.isEmpty())) {
            return (TargetModule[]) toRedeploy.toArray(new TargetModule[0]);
        }

        Set allTargets = new HashSet(Arrays.asList(TargetModule.toTarget((TargetModule[]) toRedeploy.toArray(new TargetModule[0]))));
        allTargets.addAll(toDistribute);
        Target[] targs = (Target[]) allTargets.toArray(new Target[0]);

        boolean shared = false;
        List addToDistributeWhenSharedDetected = new ArrayList();
        List removeFromRedeployWhenSharedDetected = new ArrayList();
        List addToUndeployWhenSharedDetected = new ArrayList();
        List sharerTMIDs;

        TargetModuleIDResolver tmidResolver = instance.getTargetModuleIDResolver();
        if (tmidResolver != null) {
            if (queryInfo == null) {
                queryInfo = new HashMap();
                queryInfo.put(TargetModuleIDResolver.KEY_CONTEXT_ROOT, contextRoot);
            }

            TargetModuleID[] haveSameReferences = TargetModule.EMPTY_TMID_ARRAY;
            if (targs.length > 0) {
                haveSameReferences = tmidResolver.lookupTargetModuleID(queryInfo, targs);
            }
            for (int i=0; i<haveSameReferences.length; i++) {
                haveSameReferences[i] = new TargetModule(keyOf(haveSameReferences[i]), haveSameReferences[i]);
            }
            sharerTMIDs = Arrays.asList(haveSameReferences);

            for (Iterator i=sharerTMIDs.iterator(); i.hasNext();) {
                TargetModule sharer = (TargetModule) i.next();
                if ((toRedeploy.size() > 0 && !toRedeploy.contains(sharer))
                        || toDistribute.contains(sharer.getTarget())) {
                    shared = true;
                    addToUndeployWhenSharedDetected.add(sharer.delegate());
                } else {
                    removeFromRedeployWhenSharedDetected.add(sharer);
                    addToDistributeWhenSharedDetected.add(sharer.getTarget());
                }
            }
        }

        // this is in addition to the above check: TMID provided from tomcat
        // plugin does not have module deployment name element
        if (!shared) {
            sharerTMIDs = TargetModule.findByContextRoot(dtarget.getServer(), contextRoot);
            sharerTMIDs = TargetModule.initDelegate(sharerTMIDs, getAvailableTMIDsMap());

            for (Iterator i=sharerTMIDs.iterator(); i.hasNext();) {
                TargetModule sharer = (TargetModule) i.next();
                boolean redeployHasSharer = false;
                for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                    TargetModule redeploying = (TargetModule) j.next();
                    if (redeploying.equals(sharer) && redeploying.getContentDirectory().equals(sharer.getContentDirectory())) {
                        redeployHasSharer = true;
                        break;
                    }
                }
                if (! redeployHasSharer ||
                    toDistribute.contains(sharer.getTarget())) {
                    shared = true;
                    addToUndeployWhenSharedDetected.add(sharer.delegate());
                } else {
                    removeFromRedeployWhenSharedDetected.add(sharer);
                    addToDistributeWhenSharedDetected.add(sharer.getTarget());
                }
            }
        }

        if (shared) {
            undeployTMIDs.addAll(addToUndeployWhenSharedDetected);
            //erase memory of them if any
            TargetModule.removeByContextRoot(dtarget.getServer(), contextRoot);
            // transfer from redeploy to distribute
            toRedeploy.removeAll(removeFromRedeployWhenSharedDetected);
            distributeTargets.addAll(addToDistributeWhenSharedDetected);
        }

        return (TargetModule[]) toRedeploy.toArray(new TargetModule[0]);
    }

    private Map<String, TargetModuleID> getAvailableTMIDsMap() {
        if (availablesMap != null) {
            return availablesMap;
        }

        // existing TMID's
        DeploymentManager dm = instance.getDeploymentManager();
        availablesMap = new HashMap<String, TargetModuleID>();
        try {
            ModuleType type = J2eeModuleAccessor.getDefault().getJsrModuleType(dtarget.getModule().getType());
            TargetModuleID[] ids = dm.getAvailableModules(type, targets);
            if (ids == null) {
                return availablesMap;
            }
            for (int i=0; i<ids.length; i++) {
                availablesMap.put(keyOf(ids[i]), ids[i]);
            }
        } catch (TargetException te) {
            throw new IllegalArgumentException(te);
        }
        return availablesMap;
    }

    /**
     * Process last deployment TargetModuleID's for undeploy, redistribute, redeploy and oldest timestamp
     */
    private void processLastTargetModules() {
        TargetModule[] targetModules = dtarget.getTargetModules();

        // new module
        if (targetModules == null || targetModules.length == 0) {
            distributeTargets.addAll(Arrays.asList(targets));
            checkUndeployForSharedReferences(targets);
            return;
        }

        Set targetNames = new HashSet();
        for (int i=0; i<targets.length; i++) targetNames.add(targets[i].getName());

        Set toRedeploy = new HashSet(); //type TargetModule
        for (int i=0; i<targetModules.length; i++) {
            // not my module
           if (! targetModules[i].getInstanceUrl().equals(instance.getUrl())
                   || !targetNames.contains(targetModules[i].getTargetName())) {
                continue;
            }

            TargetModuleID tmID = (TargetModuleID) getAvailableTMIDsMap().get(targetModules[i].getId());

            // no longer a deployed module on server
            if (tmID == null) {
                Target target = targetModules[i].findTarget();
                if (target != null) {
                    distributeTargets.add(target);
                }
            } else {
                targetModules[i].initDelegate(tmID);
                toRedeploy.add(targetModules[i]);
            }
        }

        DeploymentManager dm = instance.getDeploymentManager();

        // check if redeploy not suppported and not incremental then transfer to distribute list
        if (incremental == null && getApplication() == null) {
            toRedeploy = Collections.EMPTY_SET;
        } else if (incremental == null) {
            long lastModified = getApplication().lastModified();
            for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                TargetModule deployed = (TargetModule) j.next();
                if (lastModified >= deployed.getTimestamp()) {
                    //transfer to distribute
                    if (! dm.isRedeploySupported()) {
                        distributeTargets.add(deployed.findTarget());
                        undeployTMIDs.add(deployed.delegate());
                        j.remove();
                    }
                } else {
                    // no need to redeploy
                    j.remove();
                }
            }
        }

        redeployTargetModules = checkUndeployForChangedReferences(toRedeploy);
        Set targetSet = new HashSet(distributeTargets);
        redeployTargetModules = checkUndeployForSharedReferences(toRedeploy, targetSet);
    }

    private File getApplication() {
        if (application != null) return application;
        try {
            FileObject archiveFO = dtarget.getModule().getArchive();
            if (archiveFO == null) return null;
            application = FileUtil.toFile(archiveFO);
            return application;
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.SEVERE, ioe.getMessage());
            return null;
        }
    }

    public void startTargets(Mode mode, ProgressUI ui) throws ServerException {
        if (instance.getStartServer().isAlsoTargetServer(null)) {
            switch (mode) {
                case DEBUG: {
                    instance.startDebug(ui);
                    break;
                }
                case PROFILE: {
                    final CountDownLatch latch = new CountDownLatch(1);
                    ServerInstance.StateListener sl = new ServerInstance.StateListener() {

                        @Override
                        public void stateChanged(int oldState, int newState) {
                            if (newState == ServerInstance.STATE_STOPPED ||
                                    newState == ServerInstance.STATE_PROFILING) {
                                latch.countDown();
                            }
                        }
                    };

                    instance.addStateListener(sl);
                    try {
                        instance.startProfile(false, ui);
                        try {
                            // need to wait for profiler to load the agent etc.
                            // 60 seconds timeout; instrumentation may slow down the startup significantly
                            latch.await(60, TimeUnit.SECONDS);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            // proceed to exit
                        }
                    } finally {
                        instance.removeStateListener(sl);
                    }
                    break;
                }
                case RUN: {
                    instance.start(ui);
                }
            }

            this.targets = dtarget.getServer().toTargets();
            return;
        }
        instance.start(ui);
        this.targets = dtarget.getServer().toTargets();
        switch (mode) {
            case DEBUG: {
                for (int i = 0; i < targets.length; i++) {
                    instance.startDebugTarget(targets[i], ui);
                }
                break;
            }
            case RUN: {
                for (int i = 0; i < targets.length; i++) {
                    instance.startTarget(targets[i], ui);
                }
                break;
            }
        }
    }

    private static String keyOf(TargetModuleID tmid) {
        /*StringBuffer sb =  new StringBuffer(256);
        sb.append(tmid.getModuleID());
        sb.append("@"); //NOI18N
        sb.append(tmid.getTarget().getName());
        return sb.toString();*/
        return tmid.toString();
    }

    //collect root modules into TargetModule with timestamp
    private TargetModuleID[] saveRootTargetModules(TargetModuleID [] modules) {
        long timestamp = System.currentTimeMillis();

        Set originals = new HashSet();
        for (int i=0; i<modules.length; i++) {
            if (modules[i].getParentTargetModuleID() == null) {
                String id = keyOf(modules[i]);
                String targetName = modules[i].getTarget().getName();
                String fromDir = "";
                if (null != currentContentDir)
                    fromDir = currentContentDir.getAbsolutePath();
                TargetModule tm = new TargetModule(id, instance.getUrl(), timestamp, fromDir, contextRoot, modules[i]);
                deployedRootTMIDs.add(tm);
                originals.add(modules[i]);
            }
        }
        return (TargetModuleID[]) originals.toArray(new TargetModuleID[0]);
    }

    public TargetModule[] deploy(ProgressUI ui, boolean forceRedeploy) throws IOException, ServerException {
        ProgressObject po = null;
        boolean hasActivities = false;

        init(ui, true, true);

        boolean missingModule = false;
        if (J2eeModule.Type.EAR.equals(dtarget.getModule().getType())
                && dtarget.getModule() instanceof J2eeApplication
                && redeployTargetModules != null
                && redeployTargetModules.length == 1) {

            // TODO more precise check
            // this is namely because of glassfish deploying EAR without EJB module
            // see gf issue #5240

            int redeployChildrenCount = redeployTargetModules[0].getChildTargetModuleID() != null
                    ? redeployTargetModules[0].getChildTargetModuleID().length
                    : 0;

            missingModule = redeployChildrenCount < ((J2eeApplication) dtarget.getModule()).getModules().length;
            if (missingModule) {
                LOGGER.log(Level.INFO, "Enterprise application needs to be redeployed due to missing module");
            }
        }

        if (forceRedeploy || missingModule) {
            if (redeployTargetModules != null) {
                for (int i = 0; i < redeployTargetModules.length; i++) {
                    distributeTargets.add(redeployTargetModules [i].findTarget ());
                    undeployTMIDs.add(redeployTargetModules [i].delegate());
                    redeployTargetModules [i].remove();
                }
                redeployTargetModules = null;
            }
        }

        File plan = null;
        J2eeModule deployable = null;
        ModuleConfigurationProvider mcp = dtarget.getModuleConfigurationProvider();
        if (mcp != null)
            deployable = mcp.getJ2eeModule(null);
        boolean hasDirectory = (dtarget.getModule().getContentDirectory() != null);

        // undeploy if necessary
        if (undeployTMIDs.size() > 0) {
            TargetModuleID[] tmIDs = (TargetModuleID[]) undeployTMIDs.toArray(new TargetModuleID[0]);
            ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Undeploying"));
            ProgressObject undeployPO = instance.getDeploymentManager().undeploy(tmIDs);
            try {
                ProgressObjectUtil.trackProgressObject(ui, undeployPO, instance.getDeploymentTimeout()); // lets use the same timeout as for deployment
            } catch (TimeoutException e) {
                // undeployment failed, try to continue anyway
            }
        }

        // handle initial file deployment or distribute
        if (distributeTargets.size() > 0) {
            hasActivities = true;
            Target[] targetz = (Target[]) distributeTargets.toArray(new Target[0]);
            IncrementalDeployment lincremental = IncrementalDeployment.getIncrementalDeploymentForModule(incremental, deployable);
            if (lincremental != null && hasDirectory && canFileDeploy(targetz, deployable)) {
                ModuleConfiguration cfg = dtarget.getModuleConfigurationProvider().getModuleConfiguration();
                File dir = initialDistribute(targetz[0], ui);
                if (lincremental instanceof IncrementalDeployment2) {
                    DeploymentContext deployment = DeploymentContextAccessor.getDefault().createDeploymentContext(
                            deployable, dir, null, dtarget.getModuleProvider().getRequiredLibraries(), null);
                    po = ((IncrementalDeployment2) lincremental).initialDeploy(targetz[0], deployment);
                } else {
                    po = lincremental.initialDeploy(targetz[0], deployable, cfg, dir);
                }
                trackDeployProgressObject(ui, po, false);
            } else {  // standard DM.distribute
                if (getApplication() == null) {
                    throw new NoArchiveException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                }

                ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Distributing", application));
                plan = dtarget.getConfigurationFile();
                DeploymentManager dm = instance.getDeploymentManager();
                if (dm instanceof DeploymentManager2) {
                    DeploymentContext deployment = DeploymentContextAccessor.getDefault().createDeploymentContext(
                            dtarget.getModule(), getApplication(), plan, dtarget.getModuleProvider().getRequiredLibraries(), null);
                    po = ((DeploymentManager2)dm).distribute(targetz, deployment);
                } else {
                    po = dm.distribute(targetz, getApplication(), plan);
                }
                trackDeployProgressObject(ui, po, false);
            }
        }

        // handle increment or standard redeploy
        if (redeployTargetModules != null && redeployTargetModules.length > 0) {
            hasActivities = true;
            // defend against incomplete J2eeModule objects.
            IncrementalDeployment lincremental = IncrementalDeployment.getIncrementalDeploymentForModule(incremental, deployable);
            if (lincremental != null && hasDirectory && canFileDeploy(redeployTargetModules, deployable)) {
                DeploymentChangeDescriptor acd = distributeChanges(redeployTargetModules[0], ui);
                if (anyChanged(acd)) {
                    ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_IncrementalDeploying", redeployTargetModules[0]));
                    if (lincremental instanceof IncrementalDeployment2) {
                        DeploymentContext deployment = DeploymentContextAccessor.getDefault().createDeploymentContext(
                                deployable, null, null, dtarget.getModuleProvider().getRequiredLibraries(), acd);
                        po = ((IncrementalDeployment2) lincremental).incrementalDeploy(redeployTargetModules[0].delegate(), deployment);
                    } else {
                        po = lincremental.incrementalDeploy(redeployTargetModules[0].delegate(), acd);
                    }
                    trackDeployProgressObject(ui, po, true);

                } else { // return original target modules
                    return dtarget.getTargetModules();
                }
            } else { // standard redeploy
                if (getApplication() == null)
                    throw new IllegalArgumentException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));

                ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Redeploying", application));
                TargetModuleID[] tmids = TargetModule.toTargetModuleID(redeployTargetModules);
                if (plan == null) plan = dtarget.getConfigurationFile();
                DeploymentManager dm = instance.getDeploymentManager();
                if (dm instanceof DeploymentManager2) {
                    DeploymentContext deployment = DeploymentContextAccessor.getDefault().createDeploymentContext(
                            dtarget.getModule(), getApplication(), plan, dtarget.getModuleProvider().getRequiredLibraries(), null);
                    po = ((DeploymentManager2)dm).redeploy(tmids, deployment);
                } else {
                    po = dm.redeploy(tmids, getApplication(), plan);
                }
                trackDeployProgressObject(ui, po, false);
            }
        }

        if (hasActivities) {
            return (TargetModule[]) deployedRootTMIDs.toArray(new TargetModule[0]);
        } else {
            return dtarget.getTargetModules();
        }
    }

    public void undeploy(ProgressUI ui, boolean startServer) throws IOException, ServerException {

        // TODO is this valid for multiple targets behind one server (bit theoretical) ?
        if (!instance.isRunning() && !startServer) {
            return;
        }

        init(ui, startServer, false);

        TargetModule[] modules = getDeploymentDirectoryModules();
        if (modules.length <= 0) {
            return;
        }

        List<TargetModuleID> toUndeploy = new ArrayList<TargetModuleID>();
        for (TargetModule module : modules) {
            toUndeploy.add(module.delegate());
            module.remove();
        }

        TargetModuleID[] tmIDs = (TargetModuleID[]) toUndeploy.toArray(new TargetModuleID[0]);
        ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Undeploying"));
        ProgressObject undeployPO = instance.getDeploymentManager().undeploy(tmIDs);
        try {
            ProgressObjectUtil.trackProgressObject(ui, undeployPO, instance.getDeploymentTimeout()); // lets use the same timeout as for deployment
        } catch (TimeoutException e) {
            // undeployment failed, try to continue anyway
            LOGGER.log(Level.INFO, "Undeploy timeouted");
        }
    }

    /**
     * Inform the plugin about the deploy action, even if there was
     * really nothing needed to be deployed.
     *
     * @param modules list of modules which are being deployed
     */
    public void notifyIncrementalDeployment(TargetModuleID[] modules) {
        if (modules !=  null && incremental != null) {
            for (int i = 0; i < modules.length; i++) {
                incremental.notifyDeployment(modules[i]);
            }
        }
    }

    public static boolean anyChanged(DeploymentChangeDescriptor acd) {
        return (acd.manifestChanged() || acd.descriptorChanged() || acd.classesChanged()
        || acd.ejbsChanged() || acd.serverDescriptorChanged() || acd.serverResourcesChanged());
    }

    public boolean supportsDeployOnSave(TargetModule[] modules) throws IOException {
        J2eeModule deployable = null;
        ModuleConfigurationProvider deployment = dtarget.getModuleConfigurationProvider();
        if (deployment != null) {
            deployable = deployment.getJ2eeModule(null);
        }

        boolean hasDirectory = (dtarget.getModule().getContentDirectory() != null);
        IncrementalDeployment lincremental = IncrementalDeployment.getIncrementalDeploymentForModule(incremental, deployable);
        if (lincremental == null || !hasDirectory || !canFileDeploy(modules, deployable)
                || !lincremental.isDeployOnSaveSupported()) {
            return false;
        }
        return true;
    }

    public DeployOnSaveManager.DeploymentState notifyArtifactsUpdated(
            J2eeModuleProvider provider, Iterable<Artifact> artifacts) {

        if (!instance.isRunning()) {
            return DeployOnSaveManager.DeploymentState.MODULE_NOT_DEPLOYED;
        }

        if (!DeployOnSaveManager.isServerStateSupported(instance)) {
            return DeployOnSaveManager.DeploymentState.SERVER_STATE_UNSUPPORTED;
        }

        try {
            init(null, false, false);
        } catch (ServerException ex) {
            // this should never occur
            Exceptions.printStackTrace(ex);
        }

        // This may happen because of events coming in for server resources
        // because of COS enabled all the time :( For web resources these
        // events are not even fired.
        if (dtarget.getTargetModules() == null) {
            return DeployOnSaveManager.DeploymentState.MODULE_NOT_DEPLOYED;
        }

        TargetModule[] modules;
        try {
            modules = getDeploymentDirectoryModules();
        } catch (IllegalStateException ex) {
            // this is strange and might signal we don't have access to server
            // (tomcat) or something more serious such as disconnected DM
            // being use
            LOGGER.log(Level.INFO, null, ex);
            return DeployOnSaveManager.DeploymentState.SERVER_STATE_UNSUPPORTED;
        }

        try {
            if (!supportsDeployOnSave(modules)) {
                return DeployOnSaveManager.DeploymentState.MODULE_NOT_DEPLOYED;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        // FIXME target
        TargetModule targetModule = dtarget.getTargetModules()[0];
        if (!targetModule.hasDelegate()) {
            return DeployOnSaveManager.DeploymentState.MODULE_NOT_DEPLOYED;
        }

        ProgressUI ui = new ProgressUI(NbBundle.getMessage(TargetServer.class,
                provider.isOnlyCompileOnSaveEnabled() ? "MSG_CompileOnSave" : "MSG_DeployOnSave",
                provider.getDeploymentName()), false);
        ui.start(0);
        try {
            boolean serverResources = false;
            for (Artifact artifact : artifacts) {
                if (artifact.isServerResource()) {
                    serverResources = true;
                    break;
                }
            }

            try {
                // FIXME libraries stored in server specific descriptor
                // do not match server resources
                if (serverResources && !provider.isOnlyCompileOnSaveEnabled()) {
                    DeploymentHelper.deployServerLibraries(provider);
                    DeploymentHelper.deployDatasources(provider);
                    DeploymentHelper.deployMessageDestinations(provider);
                }
            } catch (DatasourceAlreadyExistsException ex) {
                LOGGER.log(Level.INFO, null, ex);
                return DeployOnSaveManager.DeploymentState.DEPLOYMENT_FAILED;
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.INFO, null, ex);
                return DeployOnSaveManager.DeploymentState.DEPLOYMENT_FAILED;
            }

            DeploymentChangeDescriptor changes = distributeChangesOnSave(targetModule, artifacts);
            if (serverResources) {
                ChangeDescriptorAccessor.getDefault().withChangedServerResources(changes);
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, changes.toString());
            }
            if (provider.isOnlyCompileOnSaveEnabled()) {
                // XXXX is this right response? it should not result in any error
                return DeployOnSaveManager.DeploymentState.MODULE_NOT_DEPLOYED;
            }

            boolean completed = reloadArtifacts(ui, modules, changes);
            if (!completed) {
                LOGGER.log(Level.INFO, "On save deployment failed");
                return DeployOnSaveManager.DeploymentState.DEPLOYMENT_FAILED;
            }
            return DeployOnSaveManager.DeploymentState.MODULE_UPDATED;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return DeployOnSaveManager.DeploymentState.DEPLOYMENT_FAILED;
        } finally {
            ui.finish();
        }
    }

    private boolean reloadArtifacts(ProgressUI ui, TargetModule[] modules, DeploymentChangeDescriptor desc) {
        boolean completed = true;
        for (TargetModule module : modules) {
            ProgressObject obj = incremental.deployOnSave(module.delegate(), desc);
            try {
                // this also save last deploy timestamp
                completed = completed && trackDeployProgressObject(ui, obj, true);
            } catch (ServerException ex) {
                Exceptions.printStackTrace(ex);
                completed = false;
            }
        }
        notifyIncrementalDeployment(modules);

        return completed;
    }

    private TargetModule[] getDeploymentDirectoryModules() {
        TargetModule[] modules = dtarget.getTargetModules();

        if (modules == null) {
            return new TargetModule[]{};
        }

        ServerInstance serverInstance = dtarget.getServer().getServerInstance();
        Set<String> targetNames = new HashSet<String>();
        for (int i = 0; i < targets.length; i++) {
            targetNames.add(targets[i].getName());
        }

        Set<TargetModule> ret = new HashSet<TargetModule>();
        for (TargetModule module : modules) {
            // not my module
            if (!module.getInstanceUrl().equals(serverInstance.getUrl())
                    || ! targetNames.contains(module.getTargetName())) {
                continue;
            }

            TargetModuleID tmID = (TargetModuleID) getAvailableTMIDsMap().get(module.getId());

            // no longer a deployed module on server
            if (tmID != null) {
                module.initDelegate(tmID);
                ret.add(module);
            }
        }
        return ret.toArray(new TargetModule[0]);
    }

    /**
     * Waits till the deploy progress object is in final state or till the timeout
     * runs out. If the deploy completes successfully the module will be started
     * if needed.
     *
     * @param ui progress ui which will be notified about progress object changes .
     * @param po progress object which will be tracked.
     * @param incremental is it incremental deploy?
     * @return true if the progress object completed successfully, false otherwise
     */
    private boolean trackDeployProgressObject(ProgressUI ui, ProgressObject po, boolean incremental) throws ServerException {
        long deploymentTimeout = instance.getDeploymentTimeout();
        long startTime = System.currentTimeMillis();
        try {
            boolean completed = ProgressObjectUtil.trackProgressObject(ui, po, deploymentTimeout);
            if (completed) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                modules = saveRootTargetModules(modules);
                if (!incremental) {
                    // if incremental, plugin is responsible for starting module, depending on nature of changes
                    ProgressObject startPO = instance.getDeploymentManager().start(modules);
                    long deployTime = System.currentTimeMillis() - startTime;
                    return ProgressObjectUtil.trackProgressObject(ui, startPO, deploymentTimeout - deployTime);
                }
            }
            return completed;
        } catch (TimeoutException e) {
            throw new ServerException(NbBundle.getMessage(TargetServer.class, "MSG_DeploymentTimeoutExceeded"));
        }
    }

    public abstract static class DeploymentContextAccessor {

        private static volatile DeploymentContextAccessor accessor;

        public static void setDefault(DeploymentContextAccessor accessor) {
            if (DeploymentContextAccessor.accessor != null) {
                throw new IllegalStateException("Already initialized accessor"); // NOI18N
            }
            DeploymentContextAccessor.accessor = accessor;
        }

        public static DeploymentContextAccessor getDefault() {
            if (accessor != null) {
                return accessor;
            }

            Class c = DeploymentContext.class;
            try {
                Class.forName(c.getName(), true, DeploymentContextAccessor.class.getClassLoader());
            } catch (ClassNotFoundException cnf) {
                Exceptions.printStackTrace(cnf);
            }

            return accessor;
        }

        public abstract DeploymentContext createDeploymentContext(J2eeModule module, File moduleFile,
                    File deploymentPlan, File[] requiredLibraries, AppChangeDescriptor changes);
    }

    public static class NoArchiveException extends IllegalArgumentException {

        public NoArchiveException(String s) {
            super(s);
        }
    }
}
