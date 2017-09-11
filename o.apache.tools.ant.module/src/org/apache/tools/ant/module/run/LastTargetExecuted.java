/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.apache.tools.ant.module.run;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.apache.tools.ant.module.bridge.AntBridge;
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
    
    private LastTargetExecuted() {}
    
    private File buildScript;
    //private static int verbosity;
    private String[] targets;
    private Map<String,String> properties;
    private String displayName;
    private Thread thread;
    
    /** Called from {@link TargetExecutor}. */
    static LastTargetExecuted record(File buildScript, String[] targets, Map<String,String> properties, String displayName, Thread thread) {
        LastTargetExecuted rec = new LastTargetExecuted();
        rec.buildScript = buildScript;
        //LastTargetExecuted.verbosity = verbosity;
        rec.targets = targets;
        rec.properties = properties;
        rec.displayName = displayName;
        rec.thread = thread;
        BuildExecutionSupport.registerRunningItem(rec);
        return rec;
    }

    static void finish(LastTargetExecuted exc) {
        BuildExecutionSupport.registerFinishedItem(exc);
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
        t.setDisplayName(exec.displayName); // #140999: do not recalculate
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
