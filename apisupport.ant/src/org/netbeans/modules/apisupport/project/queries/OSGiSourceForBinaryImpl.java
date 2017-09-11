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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Maps {@code build/osgi/code.name.base-1.2.3.qual-i-fier.jar} to {@code somemodule/src}.
 */
public class OSGiSourceForBinaryImpl implements SourceForBinaryQueryImplementation {

    private final SuiteProject suite;

    public OSGiSourceForBinaryImpl(SuiteProject suite) {
        this.suite = suite;
    }

    public @Override Result findSourceRoots(URL binaryRoot) {
        Matcher m = Pattern.compile("jar:file:.+/build/osgi/([^/-]+)-[^/]+[.]jar!/").matcher(binaryRoot.toString());
        if (m.matches()) {
            return new R(m.group(1));
        } else {
            return null;
        }
    }

    private class R implements Result {

        private final String cnb;

        public R(String cnb) {
            this.cnb = cnb;
        }

        public @Override FileObject[] getRoots() {
            for (Project module : suite.getLookup().lookup(SubprojectProvider.class).getSubprojects()) {
                NbModuleProject nbm = (NbModuleProject) module;
                if (nbm.getCodeNameBase().equals(cnb)) {
                    FileObject src = nbm.getSourceDirectory();
                    return src != null ? new FileObject[] {src} : new FileObject[0];
                }
            }
            try {
                ModuleEntry entry = ModuleList.findOrCreateModuleListFromSuite(suite.getProjectDirectoryFile(), null).getEntry(cnb);
                if (entry != null) {
                    File src = entry.getSourceLocation();
                    if (src != null) {
                        FileObject srcFO = FileUtil.toFileObject(new File(src, "src")); // NOI18N
                        if (srcFO != null) {
                            return new FileObject[] {srcFO};
                        }
                    }
                }
            } catch (IOException x) {
                Logger.getLogger(OSGiSourceForBinaryImpl.class.getName()).log(Level.INFO, null, x);
            }
            return new FileObject[0];
        }

        public @Override void addChangeListener(ChangeListener l) {}

        public @Override void removeChangeListener(ChangeListener l) {}

    }

}
