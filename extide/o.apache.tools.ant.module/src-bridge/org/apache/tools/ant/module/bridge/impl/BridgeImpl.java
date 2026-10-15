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

package org.apache.tools.ant.module.bridge.impl;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.bridge.BridgeInterface;
import org.apache.tools.ant.module.bridge.IntrospectionHelperProxy;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Implements the BridgeInterface using the current version of Ant.
 * @author Jesse Glick
 */
public class BridgeImpl implements BridgeInterface {

    private static final Logger LOG = Logger.getLogger(BridgeImpl.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(BridgeImpl.class);
    
    /** Number of milliseconds to wait before forcibly halting a runaway process. */
    private static final int STOP_TIMEOUT = 10000;
    
    private final AtomicBoolean classpathInitialized = new AtomicBoolean();
    
    /**
     * Index of loggers by active thread.
     * @see #stop
     */
    private static final Map<Thread,NbBuildLogger> loggersByThread = new WeakHashMap<Thread,NbBuildLogger>();
    
    public BridgeImpl() {
    }
    
    public String getAntVersion() {
        try {
            return Main.getAntVersion();
        } catch (BuildException be) {
            AntModule.err.notify(ErrorManager.INFORMATIONAL, be);
            return NbBundle.getMessage(BridgeImpl.class, "LBL_ant_version_unknown");
        }
    }
    
    public boolean isAnt16() {
        try {
            Class.forName("org.apache.tools.ant.taskdefs.Antlib"); // NOI18N
            return true;
        } catch (ClassNotFoundException e) {
            // Fine, 1.5
            return false;
        }
    }
    
    public IntrospectionHelperProxy getIntrospectionHelper(Class<?> clazz) {
        return new IntrospectionHelperImpl(clazz);
    }
    
    public boolean toBoolean(String val) {
        return Project.toBoolean(val);
    }
    
    public String[] getEnumeratedValues(Class<?> c) {
        if (EnumeratedAttribute.class.isAssignableFrom(c)) {
            try {
                return ((EnumeratedAttribute)c.getDeclaredConstructor().newInstance()).getValues();
            } catch (Exception e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        } else if (Enum.class.isAssignableFrom(c)) { // Ant 1.7.0 (#41058)
            try {
                Enum<?>[] vals = (Enum<?>[]) c.getMethod("values").invoke(null);
                String[] names = new String[vals.length];
                for (int i = 0; i < vals.length; i++) {
                    names[i] = vals[i].name();
                }
                return names;
            } catch (Exception x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }
        
    @Override
    public boolean run(File buildFile, List<String> targets, InputStream in, OutputWriter out, OutputWriter err, Map<String,String> properties,
            Set<? extends String> concealedProperties, int verbosity, String displayName, Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io) {
        if (classpathInitialized.compareAndSet(false, true)) {
            // #46171: Ant expects this path to have itself and whatever else you loaded with it,
            // or AntClassLoader.getResources will not be able to find anything in the Ant loader.
            //Proabably not needed anymore: https://bz.apache.org/bugzilla/show_bug.cgi?id=30161
            Path.systemClasspath = new Path(null, AntBridge.getMainClassPath());
        }
        
        boolean ok = false;
        
        // Important for various other stuff.
        final boolean ant16 = isAnt16();
        
        // Make sure "main Ant loader" is used as context loader for duration of the
        // run. Otherwise some code, e.g. JAXP, will accidentally pick up NB classes,
        // which can cause various undesirable effects.
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCCL = Project.class.getClassLoader();
        LOG.log(Level.FINER, "Fixing CCL: {0} -> {1}", new Object[] {oldCCL, newCCL});
        Thread.currentThread().setContextClassLoader(newCCL);
        try {
        
        final Project project;
        
        // first use the ProjectHelper to create the project object
        // from the given build file.
        final NbBuildLogger logger = new NbBuildLogger(buildFile, out, err, verbosity, displayName, properties, concealedProperties, interestingOutputCallback, handle, io);
        Vector<String> targs;
        try {
            project = new Project();
            project.addBuildListener(logger);
            project.init();
            project.addTaskDefinition("java", ForkedJavaOverride.class); // #56341
            project.addTaskDefinition("input", InputOverride.class); // #155056
            try {
                addCustomDefs(project);
            } catch (IOException e) {
                throw new BuildException(e);
            }
            project.setUserProperty("ant.file", buildFile.getAbsolutePath()); // NOI18N
            // #14993:
            project.setUserProperty("ant.version", Main.getAntVersion()); // NOI18N
            File antHome = AntSettings.getAntHome();
            if (antHome != null) {
                project.setUserProperty("ant.home", antHome.getAbsolutePath()); // NOI18N
            }
            String ENABLE_TESTLISTENER_EVENTS = "ant.junit.enabletestlistenerevents"; // NOI18N; since 1.8.2 in JUnitTask
            project.setProperty(ENABLE_TESTLISTENER_EVENTS, "true"); // NOI18N
            for (Map.Entry<String,String> entry : properties.entrySet()) {
                project.setUserProperty(entry.getKey(), entry.getValue());
            }
            if (in != null && ant16) {
                try {
                    Method m = Project.class.getMethod("setDefaultInputStream", InputStream.class); // NOI18N
                    m.invoke(project, in);
                } catch (Exception e) {
                    AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            LOG.log(Level.FINER, "CCL when configureProject is called: {0}", Thread.currentThread().getContextClassLoader());
            ProjectHelper projhelper = ProjectHelper.getProjectHelper();
            // Cf. Ant #32668 & #32216; ProjectHelper.configureProject undeprecated in 1.7
            project.addReference("ant.projectHelper", projhelper); // NOI18N
            projhelper.parse(project, buildFile);
            
            project.setInputHandler(new NbInputHandler(interestingOutputCallback));
            
            if (targets != null) {
                targs = new Vector<String>(targets);
            } else {
                targs = new Vector<String>(1);
                targs.add(project.getDefaultTarget());
            }
            logger.setActualTargets(targets != null ? targets.toArray(new String[0]) : null);
        }
        catch (BuildException be) {
            logger.buildInitializationFailed(be);
            logger.shutdown();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    AntModule.err.notify(e);
                }
            }
            return false;
        }
        
        project.fireBuildStarted();
        
        // Save & restore system output streams.
        InputStream is = System.in;
        if (in != null && ant16) {
            try {
                Class<? extends InputStream> dis = Class.forName("org.apache.tools.ant.DemuxInputStream").asSubclass(InputStream.class); // NOI18N
                Constructor<? extends InputStream> c = dis.getConstructor(Project.class);
                is = c.newInstance(project);
            } catch (Exception e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        AntBridge.pushSystemInOutErr(is,
                                     new PrintStream(new DemuxOutputStream(project, false)),
                                     new PrintStream(new DemuxOutputStream(project, true)));

        Thread currentThread = Thread.currentThread();
        synchronized (loggersByThread) {
            assert !loggersByThread.containsKey(currentThread);
            loggersByThread.put(currentThread, logger);
        }
        try {
            if (Thread.interrupted()) {
                logger.shutdown();
                return false;
            }
            // Execute the configured project
            //writer.println("#4"); // NOI18N
            project.executeTargets(targs);
            //writer.println("#5"); // NOI18N
            project.fireBuildFinished(null);
            ok = true;
        } catch (Throwable t) {
            // Really need to catch everything, else AntClassLoader.cleanup may
            // not be called, resulting in a memory leak and/or locked JARs (#42431).
            project.fireBuildFinished(t);
        } finally {
            AntBridge.restoreSystemInOutErr();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    AntModule.err.notify(e);
                }
            }
            synchronized (loggersByThread) {
                loggersByThread.remove(currentThread);
            }
        }
        
        // Now check to see if the Project defined any cool new custom tasks.
        RP.post(new PostRun(project, logger));
        
        } finally {
            LOG.log(Level.FINER, "Restoring CCL: {0}", oldCCL);
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
        
        return ok;
    }

    private static final RequestProcessor.Task refreshFilesystemsTask = RP.create(new Runnable() {
        public @Override void run() {
            LOG.log(Level.FINE, "Refreshing filesystems");
            FileUtil.refreshAll(); 
        }
    });

    public void stop(final Thread process) {
        NbBuildLogger logger;
        synchronized (loggersByThread) {
            logger = loggersByThread.get(process);
        }
        if (logger != null) {
            // Try stopping at a safe point.
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BridgeImpl.class, "MSG_stopping", logger.getDisplayNameNoLock()));
            logger.stop();
        }
        process.interrupt();
        // But if that doesn't do it, double-check later...
        // Yes Thread.stop() is deprecated; that is why we try to avoid using it.
        RP.create(new StopProcess(process)).schedule(STOP_TIMEOUT);
    }
    private static class StopProcess implements Runnable {
        private final Thread process;
        StopProcess(Thread process) {
            this.process = process;
        }
        public @Override void run() {
            if (process.isAlive()) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(BridgeImpl.class, "MSG_halting"));
                stopThread();
            }
        }
        @SuppressWarnings("deprecation")
        private void stopThread() {
            process.stop();
        }
    }

    private static void addCustomDefs(Project project) throws BuildException, IOException {
        long start = System.currentTimeMillis();
        if (AntBridge.getInterface().isAnt16()) {
            Map<String,ClassLoader> antlibLoaders = AntBridge.getCustomDefClassLoaders();
            for (Map.Entry<String,ClassLoader> entry : antlibLoaders.entrySet()) {
                String cnb = entry.getKey();
                ClassLoader l = entry.getValue();
                String resource = cnb.replace('.', '/') + "/antlib.xml"; // NOI18N
                URL antlib = l.getResource(resource);
                if (antlib == null) {
                    throw new IOException("Could not find " + resource + " in ant/nblib/" + cnb.replace('.', '-') + ".jar"); // NOI18N
                }
                // Once with no namespaces.
                NbAntlib.process(project, antlib, null, l);
                // Once with.
                String antlibUri = "antlib:" + cnb; // NOI18N
                NbAntlib.process(project, antlib, antlibUri, l);
            }
        } else {
            // For Ant 1.5, just dump in old-style defs in the simplest manner.
            Map<String,Map<String,Class>> customDefs = AntBridge.getCustomDefsNoNamespace();
            for (Map.Entry<String,Class> entry : customDefs.get("task").entrySet()) { // NOI18N
                project.addTaskDefinition(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String,Class> entry : customDefs.get("type").entrySet()) { // NOI18N
                project.addDataTypeDefinition(entry.getKey(), entry.getValue());
            }
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("addCustomDefs took " + (System.currentTimeMillis() - start) + "msec");
        }
    }
    
    private static boolean doGutProject = !Boolean.getBoolean("org.apache.tools.ant.module.bridge.impl.BridgeImpl.doNotGutProject");
    /**
     * Try to break up as many references in a project as possible.
     * Helpful to mitigate the effects of unsolved memory leaks: at
     * least one project will not hold onto all subprojects, and a
     * taskdef will not hold onto its siblings, etc.
     */
    private static void gutProject(Project p) {
        if (!doGutProject) {
            return;
        }
        // XXX should ideally try to wait for all other threads in this thread group
        // to finish - see e.g. #51962 for example of what can happen otherwise.
        try {
            String s = p.getName();
            AntModule.err.log("Gutting extra references in project \"" + s + "\"");
            for (Field f : Project.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if (f.getType().isPrimitive()) {
                    continue;
                }
                f.setAccessible(true);
                Object o = f.get(p);
                if (o == null) {
                    continue;
                }
                try {
                    if (o instanceof Collection<?>) {
                        ((Collection<?>) o).clear();
                        // #69727: do not null out the field (e.g. Project.listeners) in this case.
                        continue;
                    } else if (o instanceof Map<?,?>) {
                        ((Map<?,?>) o).clear();
                        continue;
                    }
                } catch (UnsupportedOperationException e) {
                    // ignore
                }
                if (Modifier.isFinal(f.getModifiers())) {
                    continue;
                }
                if (o.getClass().isArray()) {
                    f.set(p, Array.newInstance(o.getClass().getComponentType(), 0));
                    continue;
                }
                f.set(p, null);
            }
            // #43113: IntrospectionHelper can hold strong refs to dynamically loaded classes
            Field helpersF;
            try {
                helpersF = IntrospectionHelper.class.getDeclaredField("helpers");
            } catch (NoSuchFieldException x) { // Ant 1.7.0
                helpersF = IntrospectionHelper.class.getDeclaredField("HELPERS");
            }
            helpersF.setAccessible(true);
            Object helpersO = helpersF.get(null);
            Map<?,?> helpersM = (Map<?,?>) helpersO;
            helpersM.clear();
            // #46532: java.beans.Introspector caches not cleared well in all cases.
            Introspector.flushCaches();
        } catch (Exception e) {
            // Oh well.
            AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            doGutProject = false;
        }
    }

    private static class PostRun implements Runnable {
        private Project project;
        private NbBuildLogger logger;
        public PostRun(Project project, NbBuildLogger logger) {
            this.project = project;
            this.logger = logger;
        }
        public @Override void run() {
            IntrospectedInfo custom = AntSettings.getCustomDefs();
            @SuppressWarnings("rawtypes")
            Map<String,Map<String,Class>> defs = new HashMap<String, Map<String, Class>>();
            try {
                defs.put("task", NbCollections.checkedMapByCopy(project.getTaskDefinitions(), String.class, Class.class, true));
                defs.put("type", NbCollections.checkedMapByCopy(project.getDataTypeDefinitions(), String.class, Class.class, true));
            } catch (ThreadDeath t) {
                // #137883: late clicks on Stop which can be ignored.
            }
            custom.scanProject(defs);
            AntSettings.setCustomDefs(custom);
            logger.shutdown();
            // #85698: do not invoke multiple refreshes at once
            refreshFilesystemsTask.schedule(0);
            gutProject(project);
            // #185853: make sure these fields do not stick around
            project = null;
            logger = null;
        }
    }
    
}
