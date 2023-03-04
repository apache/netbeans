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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;

/**
 * Akin to (a simple subset of) &lt;subant> but failures in subtasks are collected
 * and optionally sent to a JUnit-format report rather than halting the build.
 */
public class SubAntJUnitReport extends Task {

    private Path buildPath;
    public void setBuildPath(Path buildPath) {
        this.buildPath = buildPath;
    }
    public void addConfiguredBuildPath(Path buildPath) {
        this.buildPath = buildPath;
    }

    private Ant ant;
    public @Override void init() {
        if (ant == null) {
            ant = new Ant(this);
            ant.init();
        }
    }

    private String targetToRun;
    public void setTarget(String target) {
        init();
        ant.setTarget(target);
        this.targetToRun = target;
    }

    /** @see Ant#createProperty */
    public Property createProperty() {
        init();
        return ant.createProperty();
    }

    /** @see Ant#setInheritAll */
    public void setInheritAll(boolean inheritAll) {
        init();
        ant.setInheritAll(inheritAll);
    }

    /** @see Ant#setInheritRefs */
    public void setInheritRefs(boolean inheritRefs) {
        init();
        ant.setInheritRefs(inheritRefs);
    }

    private boolean failOnError;
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    private File report;
    public void setReport(File report) {
        this.report = report;
    }

    public @Override void execute() throws BuildException {
        Map<String,String> pseudoTests = new HashMap<>();
        for (String path : buildPath.list()) {
            log("Entering: " + path);
            File dir = new File(path);
            ant.setDir(dir); // XXX Ant 1.8.0 (Ant #30569): ant.setUseNativeBasedir(true);
            final StringBuilder errors = new StringBuilder();
            BuildListener listener = new BuildListener() {
                String task = null;
                public void messageLogged(BuildEvent ev) {
                    if (task != null && ev.getPriority() <= Project.MSG_WARN) {
                        errors.append('\n').append(ev.getMessage());
                    }
                }
                public void taskStarted(BuildEvent ev) {
                    task = ev.getTask().getTaskName();
                }
                public void taskFinished(BuildEvent ev) {
                    task = null;
                }
                public void buildStarted(BuildEvent ev) {}
                public void buildFinished(BuildEvent ev) {}
                public void targetStarted(BuildEvent ev) {}
                public void targetFinished(BuildEvent ev) {}
            };
            String msg = null;
            getProject().addBuildListener(listener);
            try {
                ant.execute();
            } catch (BuildException x) {
                if (failOnError) {
                    throw x;
                } else {
                    msg = x.getMessage().replaceFirst("(?s).*The following error occurred while executing this line:\r?\n", "") + errors;
                }
            } catch (Throwable x) {
                if (failOnError) {
                    throw new BuildException(x, getLocation());
                } else {
                    StringWriter sw = new StringWriter();
                    x.printStackTrace(new PrintWriter(sw));
                    msg = sw.toString();
                }
            } finally {
                getProject().removeBuildListener(listener);
            }
            pseudoTests.put(path, msg);
            if (msg != null) {
                log("Failed to build " + path + ": " + msg, Project.MSG_WARN);
            } else {
                log("Exiting: " + path);
            }
        }
        JUnitReportWriter.writeReport(this, SubAntJUnitReport.class.getName() + "." + targetToRun, report, deleteCommonKeyPrefixes(pseudoTests));
    }

    private static <T> Map<String,T> deleteCommonKeyPrefixes(Map<String,T> m) {
        Iterator<String> keys = m.keySet().iterator();
        if (!keys.hasNext()) {
            return m;
        }
        String prefix = keys.next();
        while (keys.hasNext()) {
            String k = keys.next();
            while (!k.startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        Map<String,T> m2 = new HashMap<>();
        for (Map.Entry<String,T> entry : m.entrySet()) {
            m2.put(entry.getKey().substring(prefix.length()), entry.getValue());
        }
        return m2;
    }

}
