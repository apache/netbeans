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
package org.netbeans.modules.maven.debug;

import com.sun.jdi.VMOutOfMemoryException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.AbstractMavenExecutor;
import org.netbeans.modules.maven.execute.OutputTabMaintainer;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbCollections;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service={LateBoundPrerequisitesChecker.class, ExecutionResultChecker.class, PrerequisitesChecker.class}, projectType="org-netbeans-modules-maven")
public class DebuggerChecker implements LateBoundPrerequisitesChecker, ExecutionResultChecker, PrerequisitesChecker {
    private static final String ARGLINE = "argLine"; //NOI18N
    private static final String MAVENSUREFIREDEBUG = "maven.surefire.debug"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(DebuggerChecker.class.getName());
    private DebuggerTabMaintainer tabMaintainer;

    public @Override boolean checkRunConfig(final RunConfig config) {
        if (config.getProject() == null) {
            //cannot act on execution without a project instance..
            return true;
        }
        
        if ("debug.fix".equals(config.getActionName()) && RunUtils.isCompileOnSaveEnabled(config)) { //NOI18N
            final String cname = config.getProperties().get("jpda.stopclass"); //NOI18N

            if (cname != null) {

                // tstupka: workarounding problems mentioned in issue #242559  
                //   for some reason files do not get saved before "debug.fix" => reload doesn't always catch the changes
                
                // also that:
                // mkleint: this is (was) commented out because one day the apply changed icon was always enabled (even for the case of a non saved modified file). The code is handling that case.
                // however today I cannot reproduce and the apply changes button is only enabled when the changed file is saved.
                // 
                
                Set<DataObject> mods = DataObject.getRegistry().getModifiedSet();
                if (!mods.isEmpty()) {
                    //TODO compute is any of the files belong to the source roots of this project.
                    //if so, attach the listener and wait for the notification that the files were compiled
                    ClassPath[] cps = config.getProject().getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectClassPaths(ClassPath.SOURCE);
                    final Set<URL> urls = new HashSet<URL>();
                    for (DataObject mod : mods) {
                        for (ClassPath cp : cps) {
                            if (cp.contains(mod.getPrimaryFile())) {
                                FileObject root = cp.findOwnerRoot(mod.getPrimaryFile());
                                if (root != null) {
                                    urls.add(root.toURL());
                                }
                            }
                        }
                    }
                    LifecycleManager.getDefault().saveAll();
                    
                    if (!urls.isEmpty()) {
                        final int count = urls.size();
                        BuildArtifactMapper.ArtifactsUpdated listener = new BuildArtifactMapper.ArtifactsUpdated() {
                            private int countdown = count;
                            @Override
                            public void artifactsUpdated(Iterable<File> artifacts) {
                                //if there are multiple urls we are listening on, wait for the last one.
                                if (countdown > 0) {
                                    countdown = countdown - 1;
                                } else {
                                    //remove the listeners and then reload
                                    for (URL url : urls) {
                                        BuildArtifactMapper.removeArtifactsUpdatedListener(url, this);
                                    }
                                    doReload(config, cname);
                                }
                            }
                         };
                        for (URL url : urls) {
                            BuildArtifactMapper.addArtifactsUpdatedListener(url, listener);
                        }
                    } else {
                        doReload(config, cname);
                    }
                   
                    //TODO spawn a thread that will interrupt the listening after a timeout just to be sure?
                    //RequestProcessor.getDefault().post(null).cancel();
                    
                } else {

                    doReload(config, cname);
                    
                }
                return false;
            }
        }
        boolean debug = "true".equalsIgnoreCase(config.getProperties().get(Constants.ACTION_PROPERTY_JPDALISTEN));//NOI18N
        if (debug && ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equalsIgnoreCase(config.getActionName()) && config.getGoals().contains("surefire:test")) { //NOI18N - just a safeguard
            String newArgs = config.getProperties().get(MAVENSUREFIREDEBUG); //NOI18N
            String oldArgs = config.getProperties().get(ARGLINE); //NOI18N

            String ver = PluginPropertyUtils.getPluginVersion(config.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE); //NOI18N
            //make sure we have both old surefire-plugin and new surefire-plugin covered
            // in terms of property definitions.

            if (ver == null) {
                ver = "2.4"; //assume 2.4+, will be true for 2.0.9+ where defined explicitly, in older versions it will be the latest version.
            }
            ArtifactVersion twopointfour = new DefaultArtifactVersion("2.4"); //NOI18N
            ArtifactVersion current = new DefaultArtifactVersion(ver); //NOI18N
            int compare = current.compareTo(twopointfour);
            if (oldArgs != null && newArgs == null && compare >= 0) {
                //this case is for custom user mapping in nbactions.xml
                // we can move it to new property safely IMHO.
                config.setProperty(MAVENSUREFIREDEBUG, oldArgs);
                config.setProperty(ARGLINE, null);
            }
            if (newArgs != null && compare < 0) {
                oldArgs = (oldArgs == null ? "" : oldArgs) + " " + newArgs;
                config.setProperty(ARGLINE, oldArgs);
                // in older versions this property id dangerous
                config.setProperty(MAVENSUREFIREDEBUG, null);
            }
        }
        if ("true".equals(config.getProperties().get(Constants.ACTION_PROPERTY_JPDAATTACH))) { // NOI18N
            try (ServerSocket ss = new ServerSocket()) {
                ss.bind(null);
                int port = ss.getLocalPort();
                final InetAddress addr = ss.getInetAddress();
                String address = addr.isAnyLocalAddress() ? "localhost" : addr.getHostAddress();
                config.setProperty(Constants.ACTION_PROPERTY_JPDAATTACH, address + ":" + port);
                config.setProperty(Constants.ACTION_PROPERTY_JPDAATTACH_ADDRESS, address);
                config.setProperty(Constants.ACTION_PROPERTY_JPDAATTACH_PORT, "" + port);
            } catch (IOException ex) {
                return false;
            }
        }
        return true;
    }

    protected void doReload(final RunConfig config, final String cname) {
        DebuggerTabMaintainer otm = getOutputTabMaintainer(config.getExecutionName());
        
        InputOutput io = otm.getInputOutput();
        io.select();
        OutputWriter ow = io.getOut();
        try {
            ow.reset();
        } catch (IOException ex) { }
        
        try {
            reload(config.getProject(), ow, cname);
        } finally {
            io.getOut().close();
            otm.markTab();
        }
    }

    public @Override boolean checkRunConfig(RunConfig config, ExecutionContext context) {
        if (config.getProject() == null) {
            //cannot act on execution without a project instance..
            return true;
        }
        
        boolean debug = "true".equalsIgnoreCase(config.getProperties().get(Constants.ACTION_PROPERTY_JPDALISTEN));//NOI18N
        boolean mavenDebug = "maven".equalsIgnoreCase(config.getProperties().get(Constants.ACTION_PROPERTY_JPDALISTEN)); //NOI18N
        if (debug || mavenDebug) {
            String key = "Env.MAVEN_OPTS"; //NOI18N
            if (mavenDebug) {
                String vmargs = "-agentlib:jdwp=transport=dt_socket,server=n,address=${jpda.address}"; //NOI18N
                String orig = config.getProperties().get(key);
                if (orig == null) {
                    orig = System.getenv("MAVEN_OPTS"); // NOI18N
                }
                config.setProperty(key, orig != null ? orig + ' ' + vmargs : vmargs);
            }
            try {
                final Project p = config.getProject();
                NbMavenProject prj = p.getLookup().lookup(NbMavenProject.class);
                MavenJPDAStart start = p.getLookup().lookup(MavenJPDAStart.class);
                start.setName(prj.getMavenProject().getArtifactId());
                String stopClass = config.getProperties().get("jpda.stopclass");
                if (stopClass == null) {
                    stopClass = (String) config.getInternalProperties().get("jpda.stopclass");
                }
                start.setStopClassName(stopClass); //NOI18N
                String sm = (String) config.getInternalProperties().get("jpda.stopmethod");
                if (sm != null) {
                    start.setStopMethod(sm);
                }
                ClassPath addCP = (ClassPath) config.getInternalProperties().get("jpda.additionalClasspath");
                if (addCP != null) {
                    start.setAdditionalSourcePath(addCP);
                }
                String val = start.execute(context.getInputOutput());
                for (Map.Entry<String,String> entry : NbCollections.checkedMapByFilter(config.getProperties(), String.class, String.class, true).entrySet()) {
                    String value = entry.getValue();
                    StringBuilder buf = null;
                    String replaceItem = "${jpda.address}"; //NOI18N
                    int index = value.indexOf(replaceItem);
                    while (index > -1) {
                        String newItem = val;
                        newItem = newItem == null ? "" : newItem; //NOI18N
                        if (buf == null) {
                            buf = new StringBuilder(value);
                        }
                        buf.replace(index, index + replaceItem.length(), newItem);
                        index = buf.indexOf(replaceItem);
                    }
                    // debug must properly update debug port in runconfig...
                    if(entry.getKey().equals(MAVENSUREFIREDEBUG)) {
                        String address = "address="; //NOI18N
                        index = value.indexOf(address);
                        if(index > -1) {
                            if (buf == null) {
                                buf = new StringBuilder(value);
                            }
                            buf.replace(index + 8, buf.length(), val);
                        }
                    }
                    if (buf != null) { // Change the value when necessary only, to keep it's identity otherwise.
                        value = buf.toString();
                    }
                    //                System.out.println("setting property=" + key + "=" + buf.toString());
                    config.setProperty(entry.getKey(), value);
                }
                config.setProperty("jpda.address", val); //NOI18N
            } catch (Throwable th) {
                LOGGER.log(Level.INFO, th.getMessage(), th);
            }
        }
        if (ActionProvider.COMMAND_DEBUG_STEP_INTO.equals(config.getActionName())) {
            //TODO - change the goal from compile to test-compile in case of file coming from
            //the test source roots..
        }
        return true;
    }

    public @Override void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        if (config.getProject() != null && resultCode == 0 && "debug.fix".equals(config.getActionName())) { //NOI18N
            String cname = config.getProperties().get("jpda.stopclass"); //NOI18N
            if (cname != null) {
                reload(config.getProject(), res.getInputOutput().getOut(), cname);
            } else {
                res.getInputOutput().getErr().println("Missing jpda.stopclass property in action mapping definition. Cannot reload class.");
            }
        }
        if (resultCode == 0 && config.getProperties().get(Constants.ACTION_PROPERTY_JPDAATTACH_TRIGGER) == null) {
            try {
                connect(config);
            } catch (DebuggerStartException ex) {
                ex.printStackTrace(res.getInputOutput().getErr());
            }
        }
    }

    static void connect(RunConfig config) throws DebuggerStartException {
        String attachToAddress = config.getProperties().get(Constants.ACTION_PROPERTY_JPDAATTACH);
        if (attachToAddress != null) {
            String transport = config.getProperties().get(Constants.ACTION_PROPERTY_JPDAATTACH_TRANSPORT);
            if (transport == null || "dt_socket".equals(transport)) {
                int colon = attachToAddress.indexOf(':');
                int port;
                try {
                    port = Integer.parseInt(attachToAddress.substring(colon + 1));
                } catch (NumberFormatException ex) {
                    final DebuggerStartException debugEx = new DebuggerStartException("Cannot parse " + attachToAddress.substring(colon + 1) + " as number");
                    debugEx.initCause(ex);
                    throw debugEx;
                }
                String host;
                if (colon > 0) {
                    host = attachToAddress.substring(0, colon);
                } else {
                    host = "localhost";
                }
                JPDADebugger.attach(host, port, new Object[0]);
            } else if ("dt_shmem".equals(transport)) {
                JPDADebugger.attach(attachToAddress, new Object[0]);
            } else {
                LOGGER.log(Level.INFO, "Ignoring unknown transport '"+transport+"'");
            }
        }
    }
    
    
    public void reload(Project project, OutputWriter logger, String classname) {
        // check debugger state
        DebuggerEngine debuggerEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (debuggerEngine == null) {
            logger.println("No debugging sessions was found.");
            return;
        }
        JPDADebugger debugger = debuggerEngine.lookupFirst 
            (null, JPDADebugger.class);
        if (debugger == null) {
            logger.println("Current debugger is not JPDA one.");
            return;
        }
        if (!debugger.canFixClasses ()) {
            logger.println("The debugger does not support Fix action.");
            return;
        }
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
            logger.println("The debugger is not running");
            return;
        }
        
        Map<String, byte[]> map = new HashMap<String, byte[]>();
        EditorContext editorContext = DebuggerManager.
            getDebuggerManager ().lookupFirst (null, EditorContext.class);

        String clazz = classname.replace('.', '/') + ".class"; //NOI18N
        ProjectSourcesClassPathProvider prv = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath[] ccp = prv.getProjectClassPaths(ClassPath.COMPILE);
        FileObject fo2 = null;
        for (ClassPath cp : ccp) {
            fo2 = cp.findResource(clazz);
            if (fo2 != null) {
                break;
            }
        }
        if (fo2 != null) {
            try {
                String basename = fo2.getName();
                for (FileObject classfile : fo2.getParent().getChildren()) {
                    String basename2 = classfile.getName();
                    if (/*#220338*/!"class".equals(classfile.getExt()) || (!basename2.equals(basename) && !basename2.startsWith(basename + '$'))) {
                        continue;
                    }
                    String url = classToSourceURL(classfile, logger);
                    if (url != null) {
                        editorContext.updateTimeStamp(debugger, url);
                    }
                    map.put(classname + basename2.substring(basename.length()), classfile.asBytes());
                }
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
                
        if (map.isEmpty()) {
            logger.println("No class to reload");
            return;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Classes to reload:\n");
            for (String c : map.keySet()) {
                sb.append(" ");
                sb.append(c);
                sb.append("\n");
            }
            logger.println(sb);
        }
        String error = null;
        try {
            debugger.fixClasses (map);
        } catch (UnsupportedOperationException uoex) {
            error = "The virtual machine does not support this operation: "+uoex.getLocalizedMessage();
        } catch (NoClassDefFoundError ncdfex) {
            error = "The bytes don't correspond to the class type (the names don't match): "+ncdfex.getLocalizedMessage();
        } catch (VerifyError ver) {
            error = "A \"verifier\" detects that a class, though well formed, contains an internal inconsistency or security problem: "+ver.getLocalizedMessage();
        } catch (UnsupportedClassVersionError ucver) {
            error = "The major and minor version numbers in bytes are not supported by the VM. "+ucver.getLocalizedMessage();
        } catch (ClassFormatError cfer) {
            error = "The bytes do not represent a valid class. "+cfer.getLocalizedMessage();
            LOGGER.log(Level.INFO, error, cfer); //#216376
        } catch (ClassCircularityError ccer) {
            error = "A circularity has been detected while initializing a class: "+ccer.getLocalizedMessage();
        } catch (VMOutOfMemoryException oomex) {
            error = "Out of memory in the target VM has occurred during class reload.";
        }
        if (error != null) {
            logger.println(error);
        } else {
            logger.println("Code updated");
        }
    }
    
    private String classToSourceURL (FileObject fo, OutputWriter logger) {
            ClassPath cp = ClassPath.getClassPath (fo, ClassPath.EXECUTE);
            if (cp == null) {
                return null;
            }
            FileObject root = cp.findOwnerRoot (fo);
            String resourceName = cp.getResourceName (fo, '/', false);
            if (resourceName == null) {
                logger.println("Can not find classpath resource for "+fo+", skipping...");
                return null;
            }
            int i = resourceName.indexOf ('$');
            if (i > 0) {
                resourceName = resourceName.substring (0, i);
            }
            FileObject[] sRoots = SourceForBinaryQuery.findSourceRoots 
                (root.toURL ()).getRoots ();
            ClassPath sourcePath = ClassPathSupport.createClassPath (sRoots);
            FileObject rfo = sourcePath.findResource (resourceName + ".java");
            if (rfo == null) {
                return null;
            }
            return rfo.toURL ().toExternalForm ();
    }

    private synchronized DebuggerTabMaintainer getOutputTabMaintainer(String name) {
        if(tabMaintainer == null) {
            tabMaintainer = new DebuggerTabMaintainer(name);
        }
        return tabMaintainer;
    }

    private static class TabCtx {
        AbstractMavenExecutor.OptionsAction options;
        @Override
        protected TabCtx clone() {
            TabCtx c = new TabCtx();
            c.options = options;
            return c;
        }
    }
    
    private static class DebuggerTabMaintainer extends OutputTabMaintainer<TabCtx> {
        private TabCtx tabContext = new TabCtx();
        public DebuggerTabMaintainer(String name) {
            super(name);
        }
        @Override
        protected Class<TabCtx> tabContextType() {
            return TabCtx.class;
        }
        @Override
        protected TabCtx createContext() {
            return tabContext.clone();
        }
        @Override protected void reassignAdditionalContext(TabCtx tabContext) { 
            this.tabContext = tabContext;
        }
        @Override
        protected Action[] createNewTabActions() {
            tabContext.options = new AbstractMavenExecutor.OptionsAction();
            return new Action[] { tabContext.options };
        }
        void markTab() {
            markFreeTab();
        }
    }
    
}
