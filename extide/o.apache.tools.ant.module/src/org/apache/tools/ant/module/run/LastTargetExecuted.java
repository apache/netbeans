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

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 * Records the last Ant target(s) that was executed.
 * @author Jesse Glick
 */
public class LastTargetExecuted implements BuildExecutionSupport.ActionItem {
    
    private LastTargetExecuted() {
    }
    
    private File buildScript;
    //private static int verbosity;
    private String[] targets;
    private Map<String,String> properties;
    private Set<String> concealedProperties;
    private String displayName;
    private Thread thread;
    private Boolean shouldSaveAllDocs;
    private Predicate<String> canReplace;
    private Predicate<String> canBeReplaced;
    private boolean wasRegistered;

    /** Called from {@link TargetExecutor}. */
    static LastTargetExecuted record(
            File buildScript,
            String[] targets,
            Map<String,String> properties,
            @NonNull final Set<String> concealedProperties,
            String displayName,
            @NullAllowed final Boolean shouldSaveAllDocs,
            @NonNull final Predicate<String> canReplace,
            @NonNull final Predicate<String> canBeReplaced,
            Thread thread,
            final boolean shouldRegister) {
        LastTargetExecuted rec = new LastTargetExecuted();
        rec.buildScript = buildScript;
        //LastTargetExecuted.verbosity = verbosity;
        rec.targets = targets;
        rec.properties = properties;
        rec.concealedProperties = concealedProperties;
        rec.displayName = displayName;
        rec.thread = thread;
        rec.shouldSaveAllDocs = shouldSaveAllDocs;
        rec.canReplace = canReplace;
        rec.canBeReplaced = canBeReplaced;
        if (shouldRegister) {
            BuildExecutionSupport.registerRunningItem(rec);
        }
        rec.wasRegistered = shouldRegister;
        return rec;
    }

    static void finish(LastTargetExecuted exc) {
        if (exc.wasRegistered) {
            BuildExecutionSupport.registerFinishedItem(exc);
        }
    }

    /**
     * Get the last build script to be run.
     * @return the last-run build script, or null if nothing has been run yet (or the build script disappeared etc.)
     */
    public static AntProjectCookie getLastBuildScript(LastTargetExecuted ext) {
        if (ext.buildScript != null && ext.buildScript.isFile()) {
            FileObject fo = FileUtil.toFileObject(ext.buildScript);
            assert fo != null;
            return AntScriptUtils.antProjectCookieFor(fo);
        }
        return null;
    }
    

    /**
     * Try to rerun the last task.
     */
    public static ExecutorTask rerun(LastTargetExecuted exec) throws IOException {
        AntProjectCookie apc = getLastBuildScript(exec);
        if (apc == null) {
            // Can happen in case the build script was deleted (similar to #84874).
            // Also make sure to disable RunLastTargetAction.
            cs.fireChange();
            return null;
        }
        TargetExecutor t = new TargetExecutor(apc, exec.targets);
        //t.setVerbosity(verbosity);
        t.setProperties(exec.properties);
        t.setConcealedProperties(exec.concealedProperties);
        t.setDisplayName(exec.displayName); // #140999: do not recalculate
        if (exec.shouldSaveAllDocs != null) {
            t.setSaveAllDocuments(exec.shouldSaveAllDocs);
        }
        t.setTabReplaceStrategy(exec.canReplace, exec.canBeReplaced);
        return t.execute();
    }
    
    private static final ChangeSupport cs = new ChangeSupport(LastTargetExecuted.class);
    
 

    public String getDisplayName() {
        return displayName;
    }

    public void repeatExecution() {
       RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    rerun(LastTargetExecuted.this);
                } catch (IOException ioe) {
                    AntModule.err.notify(ioe);
                }
            }
        });
    }

    public boolean isRunning() {
        return thread != null ? thread.isAlive() : false;
    }

    public void stopRunning() {
        if (thread != null) {
            AntBridge.getInterface().stop(thread);
        }
    }

    @Override
    public String getAction() {
       String p = properties != null ? properties.get("nb.internal.action.name") : null;
       return  p != null ? p : "xxx-custom";
    }

    @Override
    public FileObject getProjectDirectory() {
        String wd = this.properties.get("work.dir");
        if (wd != null) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(new File(wd)));
        }
        return FileUtil.toFileObject(buildScript.getParentFile());
    }

    
    //equals + hashcode handle duplicates in history list
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.buildScript != null ? this.buildScript.hashCode() : 0);
        hash = 71 * hash + Arrays.deepHashCode(this.targets);
        hash = 71 * hash + getAction().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LastTargetExecuted other = (LastTargetExecuted) obj;
        if (!((this.properties.get("work.dir") != null && this.properties.get("work.dir").equals(other.properties.get("work.dir"))) ||
                this.buildScript.getParentFile().getAbsolutePath().equals(other.buildScript.getParentFile().getAbsolutePath()) ||
                this.buildScript.getParentFile().getAbsolutePath().equals(other.properties.get("work.dir")) || 
                other.buildScript.getParentFile().getAbsolutePath().equals(this.properties.get("work.dir")))) {
            return false;
        }
        return getAction().equals(other.getAction());
    }
    
    
}
