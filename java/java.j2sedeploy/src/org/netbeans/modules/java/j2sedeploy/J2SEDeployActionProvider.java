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
package org.netbeans.modules.java.j2sedeploy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author Tomas Zezula
 * @author Petr Somol
 */
@ProjectServiceProvider(
    service = ActionProvider.class,
    projectTypes={@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-java-j2seproject",position=500)})
public class J2SEDeployActionProvider implements ActionProvider {

    private static final String TARGET_BUILD_NATIVE = "build-native";               //NOI18N
    private static final String NOSCRIPT_SUFFIX = "-noscript";                      //NOI18N
    private static final String PROP_PACKAGE_TYPE = "native.bundling.type";              //NOI18N
    private static final String PROP_DISABLE_JAR_CREATION = "jar.archive.disabled";//NOI18N
    private static final String PROP_BUILD_FILE = "buildfile";                      //NOI18N

    private static final RequestProcessor RP = new RequestProcessor(J2SEDeployActionProvider.class);


    private final Listener listener;
    private boolean isJSAvailable = true;
    private boolean isJSAvailableChecked = false;


    public J2SEDeployActionProvider(@NonNull final Project prj) {
        this.listener = new Listener(prj);
    }

    @Override
    public String[] getSupportedActions() {
        final Set<NativeBundleType> nbts = NativeBundleType.getSupported();
        final Queue<String> res = new ArrayDeque<>(nbts.size());
        for (NativeBundleType nbt : nbts) {
            res.add(nbt.getCommand());
        }
        return res.toArray(new String[0]);
    }

    @NbBundle.Messages("LBL_No_Build_XML_Found=The project does not have a valid build script {0}.")
    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final Project prj = context.lookup(Project.class);
        if (prj == null) {
            throw new IllegalArgumentException(String.format(
                "The context %s has no Project.",   //NOI18N
                context));
        }
        final NativeBundleType nbt = NativeBundleType.forCommand(command);
        if (nbt == null) {
            throw new IllegalArgumentException(String.format(
                "Unsupported command %s.",  //NOI18N
                command));
        }
        final FileObject buildScript = findBuildScript(listener.getProject());
        if (buildScript == null || !buildScript.isValid()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                Bundle.LBL_No_Build_XML_Found(getBuildXmlName(listener.getProject())),
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        final ActionProgress listener = ActionProgress.start(context);
        boolean success = false;
        try {
            final Properties p = new Properties();
            p.setProperty(PROP_PACKAGE_TYPE, nbt.getAntProperyValue());
            p.setProperty(PROP_DISABLE_JAR_CREATION, Boolean.TRUE.toString());
            String noScript = isJavaScriptAvailable() ? "" : NOSCRIPT_SUFFIX; // NOI18N
            final ExecutorTask task = ActionUtils.runTarget(
                buildScript,
                new String[] {TARGET_BUILD_NATIVE.concat(noScript)},
                p);
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task _tmp) {
                    listener.finished(task.result() == 0);
                }
            });
            success = true;
        } catch (IOException ex) {            
            Exceptions.printStackTrace(ex);
        } finally {
            if (!success) {
                listener.finished(false);
            }
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return supportsCommand(command) &&
               listener.getProject().equals(context.lookup(Project.class)) &&
               listener.isEnabled();
    }

    private boolean isJavaScriptAvailable() {
        if(isJSAvailableChecked) {
            return isJSAvailable;
        }
        ScriptEngineManager mgr = Scripting.createManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory: factories) {
            List<String> engNames = factory.getNames();
            for(String name: engNames) {
                if(name.equalsIgnoreCase("js") || name.equalsIgnoreCase("javascript")) { //NOI18N
                    isJSAvailableChecked = true;
                    isJSAvailable = true;
                    return isJSAvailable;
                }
            }
        }
        isJSAvailableChecked = true;
        isJSAvailable = false;
        return isJSAvailable;
    }

    private boolean supportsCommand (@NonNull final String command) {
        for (String supportedCommand : getSupportedActions()) {
            if (supportedCommand.equals(command)) {
                return true;
            }
        }
        return false;
    }
    
    @CheckForNull
    private static FileObject findBuildScript (@NonNull final Project prj) {                
        return prj.getProjectDirectory().getFileObject(getBuildXmlName(prj));
    }

    @NonNull
    private static String getBuildXmlName (@NonNull final Project prj) {
        final J2SEPropertyEvaluator evalProvider = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        String buildScriptPath = evalProvider == null ?
            null :
            evalProvider.evaluator().getProperty(PROP_BUILD_FILE);
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }


    

    private static final class Listener implements Runnable, PropertyChangeListener {

        private final Project prj;
        private final RequestProcessor.Task refresh;
        private final AtomicBoolean initialized;
        private volatile Boolean cachedEnabled;

        Listener(@NonNull final Project prj) {
            Parameters.notNull("prj", prj); //NOI18N
            this.prj = prj;
            this.initialized = new AtomicBoolean();
            refresh = RP.create(this);                    
        }

        @Override
        public void run() {
            ProjectManager.mutex().readAccess(new Runnable() {
                @Override
                public void run() {
                    isEnabled();
                }
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || J2SEDeployProperties.NATIVE_BUNDLING_ENABLED.equals(propName)) {
                cachedEnabled = null;
                refresh.schedule(0);
            }
        }

        boolean isEnabled() {
            Boolean res = cachedEnabled;
            if (res != null) {
                return res;
            }
            final J2SEPropertyEvaluator j2seEval = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
            if (j2seEval == null) {
               cachedEnabled = res = Boolean.FALSE;
            } else {
                final PropertyEvaluator eval = j2seEval.evaluator();
                if (initialized.compareAndSet(false, true)) {
                    eval.addPropertyChangeListener(this);
                }
                cachedEnabled = res = isTrue(eval.getProperty(J2SEDeployProperties.NATIVE_BUNDLING_ENABLED));
            }
            return res;
        }

        @NonNull
        Project getProject() {
            return prj;
        }

        private static boolean isTrue(@NullAllowed String value) {
            return "true".equals(value) ||  //NOI18N
                   "yes".equals(value)  ||  //NOI18N
                   "on".equals(value);      //NOI18N
        }
    }

}
