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

package org.netbeans.modules.java.freeform;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * #147458: logs information about a freeform project being opened.
 */
class UsageLogger {

    private static final Logger LOG = Logger.getLogger("org.netbeans.ui.metrics.freeform"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(UsageLogger.class.getName(), 1, false, false);

    private UsageLogger() {}

    public static void log(final Project p) {
        if (LOG.isLoggable(Level.INFO)) {
            RP.post(new Runnable() {
                public void run() {
                    Object[] data;
                    try {
                        data = data(p);
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                        return;
                    }
                    LogRecord rec = new LogRecord(Level.INFO, "USG_FREEFORM_PROJECT"); // NOI18N
                    rec.setParameters(data);
                    rec.setLoggerName(LOG.getName());
                    rec.setResourceBundle(NbBundle.getBundle(UsageLogger.class));
                    rec.setResourceBundleName(UsageLogger.class.getPackage().getName() + ".Bundle"); // NOI18N
                    LOG.log(rec);
                }
            });
        }
    }

    private static Object[] data(Project p) throws Exception {
        ProjectAccessor accessor = p.getLookup().lookup(ProjectAccessor.class);
        if (accessor == null) {
            throw new IllegalArgumentException("no ProjectAccessor");
        }
        AntProjectHelper helper = accessor.getHelper();
        PropertyEvaluator eval = accessor.getEvaluator();
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(p);
        int compilationUnits = 0;
        int compilationUnitsMissingBuiltTo = 0;
        int compilationUnitsMultipleRoots = 0;
        Set<String> classpathEntries = new HashSet<String>();
        Element java = JavaProjectGenerator.getJavaCompilationUnits(aux);
        if (java != null) {
            for (Element compilationUnitEl : XMLUtil.findSubElements(java)) {
                compilationUnits++;
                int builtTos = 0;
                int roots = 0;
                for (Element other : XMLUtil.findSubElements(compilationUnitEl)) {
                    String name = other.getLocalName();
                    if (name.equals("package-root")) { // NOI18N
                        roots++;
                    } else if (name.equals("built-to")) { // NOI18N
                        builtTos++;
                    } else if (name.equals("classpath")) { // NOI18N
                        String text = XMLUtil.findText(other);
                        if (text != null) {
                            String textEval = eval.evaluate(text);
                            if (textEval != null) {
                                for (String entry : textEval.split("[:;]")) {
                                    if (entry.length() > 0) {
                                        classpathEntries.add(entry);
                                    }
                                }
                            }
                        }
                    }
                }
                if (builtTos == 0) {
                    compilationUnitsMissingBuiltTo++;
                }
                if (roots > 1) {
                    compilationUnitsMultipleRoots++;
                }
            }
        }
        int targets = 0;
        {
            String antScriptS = eval.getProperty(ProjectConstants.PROP_ANT_SCRIPT);
            if (antScriptS == null) {
                antScriptS = "build.xml"; // NOI18N
            }
            FileObject antScript = FileUtil.toFileObject(helper.resolveFile(antScriptS));
            if (antScript != null) {
                AntProjectCookie apc = DataObject.find(antScript).getLookup().lookup(AntProjectCookie.class);
                if (apc != null) {
                    try {
                        targets = TargetLister.getTargets(apc).size();
                    } catch (IOException ioe) {
                        //pass - Broken build.xml which may happen for freeform, targets = 0 and log usage
                    }
                }
            }
        }
        boolean webData = aux.getConfigurationFragment("web-data", "http://www.netbeans.org/ns/freeform-project-web/2", true) != null || // NOI18N
                aux.getConfigurationFragment("web-data", "http://www.netbeans.org/ns/freeform-project-web/1", true) != null; // NOI18N
        /* XXX takes about 1msec per source file to count them, even with a warm disk cache:
        int sourceFiles = 0;
        for (SourceGroup g : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            for (FileObject kid : NbCollections.iterable(g.getRootFolder().getChildren(true))) {
                if (kid.hasExt("java")) { // NOI18N
                    sourceFiles++;
                }
            }
        }
         */
        // XXX other things which could be reported:
        // number of <properties>s (other than the original project location sometimes inserted by the New Project wizard) or <property-file>s defined
        // number of <view-item>s (other than those inserted by the GUI) defined
        // whether a custom Java platform is configured for the project
        // number of subprojects (i.e. classpath entries corresponding to project-owned sources)
        // number of context-sensitive actions defined
        // number of targets bound to non-context-sensitive actions
        return new Object[] { // Bundle.properties#USG_FREEFORM_PROJECT must match these fields
            someOrMany(compilationUnits),
            someOrMany(compilationUnitsMissingBuiltTo),
            someOrMany(compilationUnitsMultipleRoots),
            someOrMany(classpathEntries.size()),
            someOrMany(targets),
            webData,
        };
    }

    private static String someOrMany(int count) {
        if (count < 10) {
            return Integer.toString(count);
        } else {
            return "~e^" + Math.round(Math.log(count));
        }
    }

}
