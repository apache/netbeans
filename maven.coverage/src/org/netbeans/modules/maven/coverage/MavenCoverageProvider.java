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

package org.netbeans.modules.maven.coverage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ProjectServiceProvider(service=CoverageProvider.class, projectType="org-netbeans-modules-maven") // not limited to a packaging
public final class MavenCoverageProvider implements CoverageProvider {

    private static final String GROUP_COBERTURA = "org.codehaus.mojo"; // NOI18N
    private static final String ARTIFACT_COBERTURA = "cobertura-maven-plugin"; // NOI18N
    private static final String GROUP_JOCOCO = "org.jacoco";
    private static final String ARTIFACT_JOCOCO = "jacoco-maven-plugin";

    private static final Logger LOG = Logger.getLogger(MavenCoverageProvider.class.getName());

    private final Project p;
    private Map<String, MavenSummary> summaryCache;

    public MavenCoverageProvider(Project p) {
        this.p = p;
    }

    public @Override boolean supportsHitCounts() {
        return true;
    }

    public @Override boolean supportsAggregation() {
        return false;
    }

    private boolean hasPlugin(String groupId, String artifactId) {
        NbMavenProject prj = p.getLookup().lookup(NbMavenProject.class);
        if (prj == null) {
            return false;
        }
        MavenProject mp = prj.getMavenProject();
        if (PluginPropertyUtils.getReportPluginVersion(mp, groupId, artifactId) != null) {
            return true;
        }
        if (PluginPropertyUtils.getPluginVersion(mp, groupId, artifactId) != null) {
            // For whatever reason, was configured as a direct build plugin... fine.
            return true;
        }
        // In fact you _could_ just run the plugin directly here... but perhaps the user did not want to do so.
        return false;
    }

    public @Override boolean isEnabled() {
        return hasPlugin(GROUP_COBERTURA, ARTIFACT_COBERTURA) || hasPlugin(GROUP_JOCOCO, ARTIFACT_JOCOCO);
    }

    public @Override boolean isAggregating() {
        throw new UnsupportedOperationException();
    }

    public @Override void setAggregating(boolean aggregating) {
        throw new UnsupportedOperationException();
    }

    public @Override Set<String> getMimeTypes() {
        return Collections.singleton("text/x-java"); // NOI18N
    }

    public @Override void setEnabled(boolean enabled) {
        // XXX add plugin configuration here if not already present
    }

    private @CheckForNull File report() {
        if (hasPlugin(GROUP_JOCOCO, ARTIFACT_JOCOCO)) {
            String outputDirectory = PluginPropertyUtils.getReportPluginProperty(p, GROUP_JOCOCO, ARTIFACT_JOCOCO, "outputDirectory", null);
            if (outputDirectory == null) {
                outputDirectory = PluginPropertyUtils.getPluginProperty(p, GROUP_JOCOCO, ARTIFACT_JOCOCO, "outputDirectory", null, null);
            }
            if (outputDirectory == null) {
                try {
                    outputDirectory = (String) PluginPropertyUtils.createEvaluator(p).evaluate("${project.reporting.outputDirectory}/jacoco");
                } catch (ExpressionEvaluationException x) {
                    LOG.log(Level.WARNING, null, x);
                    return null;
                }
            }
            return FileUtil.normalizeFile(new File(outputDirectory, "jacoco.xml"));
        } else {
        String outputDirectory = PluginPropertyUtils.getReportPluginProperty(p, GROUP_COBERTURA, ARTIFACT_COBERTURA, "outputDirectory", null);
        if (outputDirectory == null) {
            outputDirectory = PluginPropertyUtils.getPluginProperty(p, GROUP_COBERTURA, ARTIFACT_COBERTURA, "outputDirectory", null, null);
        }
        if (outputDirectory == null) {
            try {
                outputDirectory = (String) PluginPropertyUtils.createEvaluator(p).evaluate("${project.reporting.outputDirectory}/cobertura");
            } catch (ExpressionEvaluationException x) {
                LOG.log(Level.WARNING, null, x);
                return null;
            }
        }
        return FileUtil.normalizeFile(new File(outputDirectory, "coverage.xml"));
        }
    }

    public @Override synchronized void clear() {
        File r = report();
        if (r != null && r.isFile() && r.delete()) {
            summaryCache = null;
            CoverageManager.INSTANCE.resultsUpdated(p, MavenCoverageProvider.this);
        }
    }

    private FileChangeListener listener;

    private @CheckForNull Pair<File, org.w3c.dom.Document> parse() {
        File r = report();
        if (r == null) {
            LOG.fine("undefined report location");
            return null;
        }
        CoverageManager.INSTANCE.setEnabled(p, true); // XXX otherwise it defaults to disabled?? not clear where to call this
        if (listener == null) {
            listener = new FileChangeAdapter() {
                public @Override void fileChanged(FileEvent fe) {
                    fire();
                }
                public @Override void fileDataCreated(FileEvent fe) {
                    fire();
                }
                public @Override void fileDeleted(FileEvent fe) {
                    fire();
                }
                private void fire() {
                    synchronized (MavenCoverageProvider.this) {
                        summaryCache = null;
                    }
                    CoverageManager.INSTANCE.resultsUpdated(p, MavenCoverageProvider.this);
                }
            };
            FileUtil.addFileChangeListener(listener, r);
        }
        if (!r.isFile()) {
            LOG.log(Level.FINE, "missing {0}", r);
            return null;
        }
        if (r.length() == 0) {
            // When not previously existent, seems to get created first and written later; file event picks it up when empty.
            LOG.log(Level.FINE, "empty {0}", r);
            return null;
        }
        try {
            org.w3c.dom.Document report = XMLUtil.parse(new InputSource(r.toURI().toString()), true, false, XMLUtil.defaultErrorHandler(), new EntityResolver() {
                                     public @Override InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                                         if (systemId.equals("http://cobertura.sourceforge.net/xml/coverage-04.dtd")) {
                                             return new InputSource(MavenCoverageProvider.class.getResourceAsStream("coverage-04.dtd")); // NOI18N
                                         } else if (publicId.equals("-//JACOCO//DTD Report 1.0//EN")) {
                                             return new InputSource(MavenCoverageProvider.class.getResourceAsStream("jacoco-1.0.dtd"));
                                         } else {
                                             return null;
                                         }
                                     }
                                 });
            LOG.log(Level.FINE, "parsed {0}", r);
            return Pair.of(r, report);
        } catch (/*IO,SAX*/Exception x) {
            LOG.log(Level.INFO, "Could not parse " + r, x);
            return null;
        }
    }

    private ClassPath srcPath() {
        ProjectSourcesClassPathProvider pscp = p.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        assert pscp != null;
        ClassPath cp = pscp.getProjectSourcesClassPath(ClassPath.SOURCE);
        assert cp != null;
        return cp;
    }
    
    public @Override FileCoverageDetails getDetails(final FileObject fo, final Document doc) {
        String path = srcPath().getResourceName(fo);
        if (path == null) {
            return null;
        }
        MavenDetails det = null;
        synchronized (this) {
            MavenSummary summ = summaryCache != null ? summaryCache.get(path) : null;
            if (summ != null) {
                det = summ.getDetails();
                //we have to set the linecount here, as the entire line span is not apparent from the parsed xml, giving strange results then.
                det.lineCount = doc.getDefaultRootElement().getElementCount();
            }
        }
        return det;
    }

    public @Override List<FileCoverageSummary> getResults() {
        Pair<File, org.w3c.dom.Document> r = parse();
        if (r == null) {
            return null;
        }
        ClassPath src = srcPath();
        List<FileCoverageSummary> summs = new ArrayList<FileCoverageSummary>();
        Map<String, MavenSummary> summaries = new HashMap<String, MavenSummary>();
        boolean jacoco = hasPlugin(GROUP_JOCOCO, ARTIFACT_JOCOCO);
        NodeList nl = r.second().getElementsByTagName(jacoco ? "sourcefile" : "class"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            Element clazz = (Element) nl.item(i);
            String filename;
            List<Element> lines;
            String name;
            if (jacoco) {
                filename = ((Element) clazz.getParentNode()).getAttribute("name") + '/' + clazz.getAttribute("name");
                lines = new ArrayList<Element>();
                for (Element line : XMLUtil.findSubElements(clazz)) {
                    if (line.getTagName().equals("line")) {
                        lines.add(line);
                    }
                }
                name = filename.replaceFirst("[.]java$", "").replace('/', '.');
            } else {
                filename = clazz.getAttribute("filename");
                Element linesE = XMLUtil.findElement(clazz, "lines", null); // NOI18N
                lines = linesE != null ? XMLUtil.findSubElements(linesE) : Collections.<Element>emptyList();
                // XXX nicer to collect together nested classes in same compilation unit
                name = clazz.getAttribute("name").replace('$', '.');
            }
            FileObject java = src.findResource(filename); // NOI18N
            if (java == null) {
                continue;
            }
            final MavenSummary summar = summaryOf(java, name, lines, jacoco, r.first().lastModified());
            summaries.put(filename, summar);
            summs.add(summar);
        }
        synchronized (this) {
            summaryCache = summaries;
        }
        return summs;
    }
    
    private MavenSummary summaryOf(FileObject java, String name, List<Element> lines, boolean jacoco, long lastUpdated) {
        // Not really the total number of lines in the file at all, but close enough - the ones Cobertura recorded.
        int lineCount = 0;
        int executedLineCount = 0;
        Map<Integer, Integer> detLines = new HashMap<Integer, Integer>();
        for (Element line : lines) {
            lineCount++;
            String attr = line.getAttribute(jacoco ? "ci" : "hits");
            String num = line.getAttribute(jacoco ? "nr" : "number");
            detLines.put(Integer.valueOf(num) - 1,Integer.valueOf(attr));
            if (!attr.equals("0")) {
                executedLineCount++;
            }
        }
        MavenDetails det = new MavenDetails(java, lastUpdated, lineCount, detLines);
        MavenSummary s = new MavenSummary(java, name, det, executedLineCount);
        return s;
    }

    public @Override String getTestAllAction() {
        return hasPlugin(GROUP_JOCOCO, ARTIFACT_JOCOCO) ? "jacoco" : "cobertura";
        // XXX and Test button runs COMMAND_TEST_SINGLE on file, which is not good here; cf. CoverageSideBar.testOne
    }

    private static class MavenSummary extends FileCoverageSummary {
        private final MavenDetails details;

        public MavenSummary(FileObject file, String displayName, MavenDetails details, int executedLineCount) {
            super(file, displayName, details.getLineCount(), executedLineCount, 0, 0);
            this.details = details;
            details.setSummary(this);
        }
        
        MavenDetails getDetails() {
            return details;
        }
        
    }
    
    private static class MavenDetails implements FileCoverageDetails {
        private final FileObject fileObject;
        private final long lastUpdated;
        private FileCoverageSummary summary;
        private final Map<Integer, Integer> lineHitCounts;
        int lineCount;

        public MavenDetails(FileObject fileObject, long lastUpdated, int lineCount, Map<Integer, Integer> lineHitCounts) {
            this.fileObject = fileObject;
            this.lastUpdated = lastUpdated;
            this.lineHitCounts = lineHitCounts;
            this.lineCount = lineCount;
        }
        
        
        @Override
        public FileObject getFile() {
            return fileObject;
        }

        @Override
        public int getLineCount() {
            return lineCount;
        }

        @Override
        public boolean hasHitCounts() {
            return true;
        }

        @Override
        public long lastUpdated() {
            return lastUpdated;
        }

        @Override
        public FileCoverageSummary getSummary() {
            return summary;
        }
        public void setSummary(FileCoverageSummary summary) {
            this.summary = summary;
        }

        @Override
        public CoverageType getType(int lineNo) {
            Integer count = lineHitCounts.get(lineNo);
            return count == null ? CoverageType.INFERRED : count == 0 ? CoverageType.NOT_COVERED : CoverageType.COVERED;
        }

        @Override
        public int getHitCount(int lineNo) {
            Integer ret = lineHitCounts.get(lineNo);
            if (ret == null) {
                return 0;
            }
            return ret;
        }
    
    }
}
