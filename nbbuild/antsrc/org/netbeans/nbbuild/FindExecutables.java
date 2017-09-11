/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Locates files which should have the execute bit set.
 */
public class FindExecutables extends Task {

    private String allmodules;
    public void setAllmodules(String allmodules) {
        this.allmodules = allmodules;
    }

    private String exeIncludeProperty;
    public void setExeIncludeProperty(String exeIncludeProperty) {
        this.exeIncludeProperty = exeIncludeProperty;
    }

    public @Override void execute() throws BuildException {
        StringBuilder b = new StringBuilder();
        for (String module : allmodules.split(",")) {
            // Have to replace substitutions in e.g. o.jruby.distro/nbproject/project.properties:
            final AtomicReference<String> execFiles = new AtomicReference<String>();
            final String prefix = "Load.";
            class Load extends Property {
                protected @Override void addProperty(String n, String v) {
                    addProperty(n, (Object) v);
                }
                protected void addProperty(String n, Object v) {
                    if (n.equals(prefix + "nbm.executable.files")) {
                        execFiles.set((String) v);
                    }
                }
            }
            Load load = new Load();
            load.setProject(getProject());
            load.setPrefix(prefix);
            load.setFile(new File(getProject().getProperty("nb_all"), module + "/nbproject/project.properties"));
            load.execute();
            if (execFiles.get() != null) {
                String cluster = getProject().getProperty(module + ".dir").replaceFirst(".+[/\\\\]", "");
                for (String pattern : execFiles.get().split(",")) {
                    if (b.length() > 0) {
                        b.append(',');
                    }
                    b.append(cluster).append('/').append(pattern);
                }
            }
        }
        getProject().setNewProperty(exeIncludeProperty, b.toString());
    }

}
