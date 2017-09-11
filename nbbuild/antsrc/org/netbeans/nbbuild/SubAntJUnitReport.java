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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        Map<String,String> pseudoTests = new HashMap<String,String>();
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
        Map<String,T> m2 = new HashMap<String,T>();
        for (Map.Entry<String,T> entry : m.entrySet()) {
            m2.put(entry.getKey().substring(prefix.length()), entry.getValue());
        }
        return m2;
    }

}
