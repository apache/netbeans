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

package org.netbeans.modules.javaee.project.api.ant.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public final class J2EEProjectProperties {

    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String J2EE_SERVER_HOME = "j2ee.server.home"; //NOI18N
    public static final String J2EE_DOMAIN_HOME = "j2ee.server.domain"; //NOI18N
    public static final String J2EE_MIDDLEWARE_HOME = "j2ee.server.middleware"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type"; //NOI18N
    public static final String J2EE_PLATFORM_EMBEDDABLE_EJB_CLASSPATH = "j2ee.platform.embeddableejb.classpath"; //NOI18N
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N
    
    public static final String J2EE_PLATFORM_WSCOMPILE_CLASSPATH = "j2ee.platform.wscompile.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_JWSDP_CLASSPATH = "j2ee.platform.jwsdp.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_WSIT_CLASSPATH = "j2ee.platform.wsit.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_WSGEN_CLASSPATH = "j2ee.platform.wsgen.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_WSIMPORT_CLASSPATH = "j2ee.platform.wsimport.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_JSR109_SUPPORT = "j2ee.platform.is.jsr109"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(J2EEProjectProperties.class.getName());
    
    /**
     * Remove obsolete properties from private properties.
     * @param privateProps private properties
     */
    public static void removeObsoleteLibraryLocations(EditableProperties privateProps) {
        // remove special properties from private.properties:
        Iterator<String> propKeys = privateProps.keySet().iterator();
        while (propKeys.hasNext()) {
            String key = propKeys.next();
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || //NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { //NOI18N
                propKeys.remove();
            }
        }
    }
            
    
    public static void setServerProperties(EditableProperties ep, EditableProperties epPriv,
            ClassPathSupport cs, Iterable<ClassPathSupport.Item> items,
            String serverInstanceID, Profile j2eeProfile, J2eeModule.Type moduleType) {
        setServerProperties(null, ep, epPriv, cs, items, serverInstanceID, j2eeProfile, moduleType);
    }

    /**
     * Finds server instance matching parameters.
     * TODO Integrate to j2eeserver together with fix for 198372.
     * 
     * @param serverType type of the server
     * @param moduleType type of the module
     * @param profile java ee profile
     * @return matching instance url or <code>null</code>
     * @since 1.64
     */
    @CheckForNull
    public static String getMatchingInstance(String serverType, J2eeModule.Type moduleType, Profile profile) {
        String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
        for (String instanceID : servInstIDs) {
            try {
                J2eePlatform platformLocal = Deployment.getDefault().getServerInstance(instanceID).getJ2eePlatform();
                if (platformLocal.getSupportedProfiles(moduleType).contains(profile)) {
                    return instanceID;
                }
            } catch (InstanceRemovedException ex) {
                continue;
            }
        }
        return null;
    }

    /**
     * Performs necessary check of server instance. Those include server presence
     * and optionally support for debugging and/or profiling. Unless suppressed
     * it will display warning dialog on a check failure.
     * <p>
     * The check may try to use and set another server of the same type if
     * the current server instance does not exist and the {@code callback}
     * is not null.
     *
     * @param project the project we are checking
     * @param helper the ant helper associated with the project
     * @param profile the Java EE profile project is using
     * @param moduleType the module type of the project
     * @param callback the callback to use to set another server of the same type
     *             when the current instance is not available
     * @param checkDebug {@code true} if the ability to run in debugging mode should be checked
     * @param checkProfile {@code true} if the ability to run in profiling mode should be checked
     * @param noMessages {@code true} to suppress UI dialogs
     * @return {@code true} if the server instance is usable {@code false} otherwise
     * @since 1.8
     */
    public static boolean checkSelectedServer(@NonNull Project project, @NonNull AntProjectHelper helper,
            @NonNull Profile profile, @NonNull J2eeModule.Type moduleType,
            @NullAllowed SetServerInstanceCallback callback,
            boolean checkDebug, boolean checkProfile, boolean noMessages) {

        final PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
        String instanceId = null;
        String instance = eval.getProperty(J2EE_SERVER_INSTANCE);
        if (instance != null) {
            J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
            String sdi = jmp.getServerInstanceID();
            if (sdi != null) {
                String id = Deployment.getDefault().getServerID(sdi);
                if (id != null) {
                    instanceId = sdi;
                }
            }
        }

// if there is some server instance of the type which was used
// previously do not ask and use it
        if (instanceId == null) {
            String serverType = eval.getProperty(J2EE_SERVER_TYPE);
            if (serverType != null) {
                String instanceID = getMatchingInstance(serverType, moduleType, profile);
                if (instanceID != null && callback != null) {
                    callback.setServerInstance(instanceID);
                    instanceId = instanceID;
                }
            }
        }

        if (instanceId != null) {
            try {
                ServerInstance instanceObject = Deployment.getDefault().getServerInstance(instanceId);
                if (checkDebug && !instanceObject.isDebuggingSupported()) {
                    if (!noMessages) {
                        String msg = NbBundle.getMessage(J2EEProjectProperties.class, "MSG_Server_No_Debugging"); //  NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                    }
                    return false;
                }
                if (checkProfile && !instanceObject.isProfilingSupported()) {
                    if (!noMessages) {
                        String msg = NbBundle.getMessage(J2EEProjectProperties.class, "MSG_Server_No_Profiling"); //  NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                    }
                    return false;
                }
            } catch (InstanceRemovedException ex) {
                instanceId = null;
            }
        }
        if (instanceId == null) {
            // no selected server => warning
            if (!noMessages) {
                String msg = NbBundle.getMessage(J2EEProjectProperties.class, "MSG_No_Server_Selected"); //  NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
            }

            return false;
        }
        return true;
    }

    /**
     * Sets all server related properties.
     */
    private static void setServerProperties(Project project, EditableProperties ep, EditableProperties epPriv,
            ClassPathSupport cs, Iterable<ClassPathSupport.Item> items,
            String serverInstanceID, Profile j2eeProfile, J2eeModule.Type moduleType) {
        Deployment deployment = Deployment.getDefault();
        String serverType = deployment.getServerID(serverInstanceID);

        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (!j2eePlatform.getSupportedProfiles(moduleType).contains(j2eeProfile)) {
            Logger.getLogger("global").log(Level.WARNING, "J2EE level: {0} not supported by server {1} for module type WAR",
                    new Object[] {j2eeProfile != null ? j2eeProfile.getDisplayName() : "Unknown J2EE profile version - ", Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID)}); // NOI18N
        }

        // set *always* sharable server properties:
        ep.setProperty(J2EE_SERVER_TYPE, serverType);

        // set *always* private server properties:
        epPriv.setProperty(J2EE_SERVER_INSTANCE, serverInstanceID);

            Map<String, String> roots = extractPlatformLibrariesRoot(j2eePlatform);
            if (roots != null) {
                // path will be relative and therefore stored in project.properties:
                setLocalServerProperties(project, epPriv, ep, j2eePlatform, roots);
            } else {
                // store absolute paths in private.properties:
                setLocalServerProperties(project, ep, epPriv, j2eePlatform, null);
            }

        // set j2ee.platform.jsr109 support
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109)) {
            epPriv.setProperty(J2EE_PLATFORM_JSR109_SUPPORT, "true"); //NOI18N
        }
    }

    /**
     * Update deployment script.
     */
    public static void createDeploymentScript(FileObject dirFO, EditableProperties ep, 
            EditableProperties epPriv, String serverInstanceID, J2eeModule.Type moduleType) {
        // ant deployment support
        File projectFolder = FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, ANT_DEPLOY_BUILD_SCRIPT),
                    moduleType, serverInstanceID);
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            epPriv.setProperty(DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
    }

    /**
     * Callback to a project type for project specific functionality.
     */
    public static interface Callback {
        void registerJ2eePlatformListener(J2eePlatform platform);
        void unregisterJ2eePlatformListener(J2eePlatform platform);
    }

    /**
     * Update server properties. Apart from calling setServerProperties() it update listeners etc.
     * Called when server is changed in project properties or when broken server reference is being updated.
     */
    public static void updateServerProperties(EditableProperties projectProps, EditableProperties privateProps, String newServInstID,
            ClassPathSupport cs, Iterable<ClassPathSupport.Item> items,
            Callback callback, Project proj, Profile profile, J2eeModule.Type moduleType) {

        assert newServInstID != null : "Server isntance id to set can't be null"; // NOI18N

        // update j2ee.platform.classpath
        String oldServInstID = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        if (oldServInstID != null) {
            J2eePlatform oldJ2eePlatform = Deployment.getDefault().getJ2eePlatform(oldServInstID);
            if (oldJ2eePlatform != null) {
                callback.unregisterJ2eePlatformListener(oldJ2eePlatform);
            }
        }
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        if (j2eePlatform == null) {
            // probably missing server error
            Logger.getLogger("global").log(Level.INFO, "J2EE platform is null."); // NOI18N

            // update j2ee.server.type (throws NPE)
            //projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));

            // update j2ee.server.instance
            privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
            removeServerClasspathProperties(privateProps);
            privateProps.remove(J2EE_PLATFORM_JSR109_SUPPORT);
            privateProps.remove(DEPLOY_ANT_PROPS_FILE);
            return;
        }
        callback.registerJ2eePlatformListener(j2eePlatform);

        setServerProperties(proj, projectProps, privateProps, cs, items, newServInstID, profile, moduleType);

        // ant deployment support
        createDeploymentScript(proj.getProjectDirectory(), projectProps, privateProps, newServInstID, moduleType);
    }

    private static void removeServerClasspathProperties(EditableProperties ep) {
        ep.remove(J2EE_PLATFORM_CLASSPATH);
        ep.remove(J2EE_PLATFORM_EMBEDDABLE_EJB_CLASSPATH);
        ep.remove(J2EE_PLATFORM_WSCOMPILE_CLASSPATH);
        ep.remove(J2EE_PLATFORM_WSIMPORT_CLASSPATH);
        ep.remove(J2EE_PLATFORM_WSGEN_CLASSPATH);
        ep.remove(J2EE_PLATFORM_WSIT_CLASSPATH);
        ep.remove(J2EE_PLATFORM_JWSDP_CLASSPATH);
        ep.remove(J2EE_SERVER_HOME);
    }    

    private static void setLocalServerProperties(Project project, EditableProperties epToClean,
            EditableProperties epTarget, J2eePlatform j2eePlatform, Map<String, String> roots) {
        // remove all props first:
        removeServerClasspathProperties(epTarget);

        String classpath = toClasspathString(
                ClasspathUtil.getJ2eePlatformClasspathEntries(project, j2eePlatform), roots);
        epTarget.setProperty(J2EE_PLATFORM_CLASSPATH, classpath);

        // set j2ee.platform.embeddableejb.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_EMBEDDABLE_EJB)) {
            File[] ejbClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_EMBEDDABLE_EJB);
            epTarget.setProperty(J2EE_PLATFORM_EMBEDDABLE_EJB_CLASSPATH,
                    toClasspathString(ejbClasspath, roots));
        }

        // set j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
            epTarget.setProperty(J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                    toClasspathString(wsClasspath, roots));
        }

        // set j2ee.platform.wsimport.classpath, j2ee.platform.wsgen.classpath
        WSStack<JaxWs> wsStack = WSStack.findWSStack(j2eePlatform.getLookup(), JaxWs.class);
        if (wsStack != null) {
            WSTool wsTool = wsStack.getWSTool(JaxWs.Tool.WSIMPORT); // the same as for WSGEN
            if (wsTool!= null && wsTool.getLibraries().length > 0) {
                String librariesList = toClasspathString(wsTool.getLibraries(), roots);
                epTarget.setProperty(J2EE_PLATFORM_WSGEN_CLASSPATH, librariesList);
                epTarget.setProperty(J2EE_PLATFORM_WSIMPORT_CLASSPATH, librariesList);
            }
        }

        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIT);
            epTarget.setProperty(J2EE_PLATFORM_WSIT_CLASSPATH,
                    toClasspathString(wsClasspath, roots));
        }

        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_JWSDP);
            epTarget.setProperty(J2EE_PLATFORM_JWSDP_CLASSPATH,
                    toClasspathString(wsClasspath, roots));
        }

        // remove everything from shared project properties
        removeServerClasspathProperties(epToClean);

        if (roots != null) {
            for (Map.Entry<String, String> entry : roots.entrySet()) {
                    epToClean.setProperty(entry.getValue(), entry.getKey());
            }
        }
    }

    public static Map<String, String> extractPlatformLibrariesRoot(J2eePlatform j2eePlatform) {
        Set<FileObject> toCheck = new HashSet<FileObject>();
        Map<String, String> roots = new HashMap<String, String>();
        File serverFile = j2eePlatform.getServerHome();
        if (serverFile != null) {
            serverFile = FileUtil.normalizeFile(serverFile);
            FileObject server = FileUtil.toFileObject(serverFile);
            if (server != null) {
                roots.put(serverFile.getAbsolutePath().replace('\\', '/'), J2EE_SERVER_HOME); // NOI18N
                toCheck.add(server);
            }
        }
        File domainFile = j2eePlatform.getDomainHome();
        if (domainFile != null) {
            domainFile = FileUtil.normalizeFile(domainFile);
            FileObject domain = FileUtil.toFileObject(domainFile);
            if (domain != null) {
                roots.put(domainFile.getAbsolutePath().replace('\\', '/'), J2EE_DOMAIN_HOME); // NOI18N
                toCheck.add(domain);
            }
        }
        File middlewareFile = j2eePlatform.getMiddlewareHome();
        if (middlewareFile != null) {
            middlewareFile = FileUtil.normalizeFile(middlewareFile);
            FileObject middleware = FileUtil.toFileObject(middlewareFile);
            if (middleware != null) {
                roots.put(middlewareFile.getAbsolutePath().replace('\\', '/'), J2EE_MIDDLEWARE_HOME); // NOI18N
                toCheck.add(middleware);
            }
        }
        
        if (roots.isEmpty()) {
            return extractPlatformLibrariesRootHeuristic(j2eePlatform);
        }
        
        boolean ok = true;
        for (File file : j2eePlatform.getClasspathEntries()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo == null) {
                // if some file from server classpath does not exist then let's
                // ignore it
                continue;
            }
            boolean hit = false;
            for (FileObject root : toCheck) {
                if (FileUtil.isParentOf(root, fo)) {
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                ok = false;
                break;
            }
        }
        if (!ok) {
            return null;
        }
        return roots;
    }
    
    @SuppressWarnings("deprecated")
    private static Map<String, String> extractPlatformLibrariesRootHeuristic(J2eePlatform j2eePlatform) {
        if (j2eePlatform.getPlatformRoots() == null || j2eePlatform.getPlatformRoots().length == 0) {
            return null;
        }
        File rootFile = FileUtil.normalizeFile(j2eePlatform.getPlatformRoots()[0]);
        FileObject root = FileUtil.toFileObject(rootFile);
        if (root == null) {
            return null;
        }
        boolean ok = true;
        for (File file : j2eePlatform.getClasspathEntries()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null && !FileUtil.isParentOf(root, fo)) {
                ok = false;
                break;
            }
        }
        if (!ok) {
            return null;
        }
        return Collections.singletonMap(rootFile.getAbsolutePath().replace('\\', '/'), J2EE_SERVER_HOME); // NOI18N
    }

    public static String toClasspathString(File[] classpathEntries, Map<String, String> roots) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuilder classpath = new StringBuilder();
        for (int i = 0; i < classpathEntries.length; i++) {
            String path = classpathEntries[i].getAbsolutePath().replace('\\', '/'); // NOI18N
            
            if (roots != null) {
                Map.Entry<String,String> replacement = null;
                for (Map.Entry<String, String> entry : roots.entrySet()) {
                    if (path.startsWith(entry.getKey())
                            && (replacement == null || replacement.getKey().length() < entry.getKey().length())) {
                        replacement = entry;
                    }                
                }
                if (replacement != null) {
                    path = "${" + replacement.getValue() + "}"  // NOI18N
                            + path.substring(replacement.getKey().length());
                }
            }
           
            if (classpath.length() > 0) {
                classpath.append(':'); // NOI18N
            }            
            classpath.append(path);
        }
        return classpath.toString();
    }

    private static String toClasspathString(URL[] classpathEntries, Map<String, String> roots) {
        if (classpathEntries == null) {
            return "";
        }
        List<File> files = new ArrayList<File>();
        for (URL url : classpathEntries) {
            try {
                File file = new File(url.toURI()).getAbsoluteFile();
                files.add(file);
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        
        return toClasspathString(files.toArray(new File[0]), roots);
    }

    /**
     * Callback to set the desired server instance.
     * since 1.8
     */
    public static interface SetServerInstanceCallback {

        /**
         * Sets the server instance.
         * 
         * @param serverInstanceId the id of the server instance
         */
        void setServerInstance(String serverInstanceId);
    }
}
