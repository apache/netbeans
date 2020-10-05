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

package org.netbeans.modules.java.freeform.jdkselection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manages JDK configuration on disk.
 * @see "issue #64160"
 * @author Jesse Glick
 */
public class JdkConfiguration {

    private static final String NS_GENERAL = "http://www.netbeans.org/ns/project/1"; // NOI18N
    private static final String NBJDK_PROPERTIES = "nbproject/nbjdk.properties"; // NOI18N
    private static final String NBJDK_ACTIVE = "nbjdk.active"; // NOI18N
    public static final String NBJDK_XML = "nbproject/nbjdk.xml"; // NOI18N
    public static final String JDK_XML = "nbproject/jdk.xml"; // NOI18N
    private static final String PLATFORM_ID_DEFAULT = "default_platform"; // NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    public JdkConfiguration(Project project, AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
    }

    /**
     * Initialize a project to use a selected JDK.
     * No-op for projects already initialized in this way.
     * Initially does not select a particular JDK (i.e. uses default).
     * @param p a freeform project to initialize with jdk.xml and so on
     * @param helper its helper
     * @throws IOException if writing anything fails
     */
    private void initialize() throws IOException {
        project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                createJdkXml();
                if (project.getProjectDirectory().getFileObject(NBJDK_XML) != null) {
                    return;
                }
                Element generalDataE = Util.getPrimaryConfigurationData(helper);
                Document nbjdkDoc = createNbjdkXmlSkeleton();
                rebindAllActions(generalDataE, nbjdkDoc);
                writeXML(nbjdkDoc, NBJDK_XML);
                Util.putPrimaryConfigurationData(helper, generalDataE);
                ProjectManager.getDefault().saveProject(project);
            }
        });
    }

    private void createJdkXml() throws IOException {
        ProjectManager.getDefault().saveProject(project); // GFH requires it
        new GeneratedFilesHelper(helper).refreshBuildScript(JDK_XML, JdkConfiguration.class.getResource("jdk.xsl"), true);
    }

    /**
     * Inserts an import to jdk.xml, as well as associated property definitions, into
     * an Ant script. The script should reside in the nbproject/ dir and have basedir="..".
     * Will not insert a duplicate import if one already exists.
     */
    public static void insertJdkXmlImport(Document doc) {
        NodeList nl = doc.getElementsByTagName("import"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            if (((Element) nl.item(i)).getAttribute("file").equals("jdk.xml")) { // NOI18N
                return;
            }
        }
        Element projectE = doc.getDocumentElement();
        Element propE = doc.createElement("property"); // NOI18N
        propE.setAttribute("file", NBJDK_PROPERTIES); // NOI18N
        projectE.appendChild(propE);
        propE = doc.createElement("property"); // NOI18N
        propE.setAttribute("name", "user.properties.file"); // NOI18N
        propE.setAttribute("location", "${netbeans.user}/build.properties"); // NOI18N
        projectE.appendChild(propE);
        propE = doc.createElement("property"); // NOI18N
        propE.setAttribute("file", "${user.properties.file}"); // NOI18N
        projectE.appendChild(propE);
        Element importE = doc.createElement("import"); // NOI18N
        importE.setAttribute("file", "jdk.xml"); // NOI18N
        projectE.appendChild(importE);
    }

    private Document createNbjdkXmlSkeleton() {
        Document nbjdkDoc = XMLUtil.createDocument("project", null, null, null); // NOI18N
        Element projectE = nbjdkDoc.getDocumentElement();
        // XXX for better fidelity would use ${ant.script}#/project[@name]
        projectE.setAttribute("name", ProjectUtils.getInformation(project).getName()); // NOI18N
        projectE.setAttribute("basedir", ".."); // NOI18N
        insertJdkXmlImport(nbjdkDoc);
        return nbjdkDoc;
    }

    private void rebindAllActions(Element generalDataE, Document nbjdkDoc) {
        Element projectE = nbjdkDoc.getDocumentElement();
        Set<String> targetsCreated = new HashSet<String>();
        // XXX remove any definition of ${ant.script}, which will by now be obsolete
        Element ideActionsE = XMLUtil.findElement(generalDataE, "ide-actions", Util.NAMESPACE);
        if (ideActionsE != null) {
            for (Element actionE : XMLUtil.findSubElements(ideActionsE)) {
                rebindAction(actionE, projectE, targetsCreated);
            }
        }
        Element viewE = XMLUtil.findElement(generalDataE, "ide-actions", Util.NAMESPACE);
        if (viewE != null) {
            Element contextMenuE = XMLUtil.findElement(viewE, "context-menu", Util.NAMESPACE);
            if (contextMenuE != null) {
                for (Element actionE : XMLUtil.findSubElements(contextMenuE)) {
                    if (!actionE.getLocalName().equals("action")) {
                        continue; // ignore <ide-action> here
                    }
                    rebindAction(actionE, projectE, targetsCreated);
                }
            }
        }
        // XXX need to change the customizer to also rebind actions added later!
        // Tricky however because TargetMappingPanel is in ant/freeform and knows nothing of jdk.xml.
        // Dangerous to simply rebind every action which is added later via customizer, probably.
    }

    private void rebindAction(Element actionE, Element projectE, Set<String> targetsCreated) {
        Element scriptE = XMLUtil.findElement(actionE, "script", Util.NAMESPACE); // NOI18N
        String script;
        if (scriptE != null) {
            script = XMLUtil.findText(scriptE);
            actionE.removeChild(scriptE);
        } else {
            script = "build.xml"; // NOI18N
        }
        scriptE = actionE.getOwnerDocument().createElementNS(Util.NAMESPACE, "script"); // NOI18N
        scriptE.appendChild(actionE.getOwnerDocument().createTextNode(NBJDK_XML));
        actionE.insertBefore(scriptE, actionE.getFirstChild());
        List<String> targetNames = new ArrayList<String>();
        for (Element targetE : XMLUtil.findSubElements(actionE)) {
            if (!targetE.getLocalName().equals("target")) { // NOI18N
                continue;
            }
            targetNames.add(XMLUtil.findText(targetE));
        }
        if (targetNames.isEmpty()) {
            targetNames.add(null);
        }
        String scriptPath = evaluator.evaluate(script);
        for (String target : targetNames) {
            if (targetsCreated.add(target)) {
                createOverride(projectE, target, scriptPath);
            }
        }
    }

    private static void createOverride(Element projectE, String target, String script) {
        Element targetE = projectE.getOwnerDocument().createElement("target"); // NOI18N
        if (target != null) {
            targetE.setAttribute("name", target); // NOI18N
        }
        String depends;
        if (target != null && /*XXX not very precise*/target.indexOf("debug") != -1) {
            depends = "-jdk-init,-jdk-presetdef-nbjpdastart"; // NOI18N
        } else {
            // XXX what about profiler?
            depends = "-jdk-init"; // NOI18N
        }
        targetE.setAttribute("depends", depends); // NOI18N
        Element antE = projectE.getOwnerDocument().createElement("ant"); // NOI18N
        if (target != null) {
            antE.setAttribute("target", target); // NOI18N
        }
        if (!script.equals("build.xml")) { // NOI18N
            antE.setAttribute("antfile", script); // NOI18N
        }
        antE.setAttribute("inheritall", "false"); // NOI18N
        targetE.appendChild(antE);
        projectE.appendChild(targetE);
    }

    private void writeXML(Document doc, String path) throws IOException {
        FileObject fo = FileUtil.createData(project.getProjectDirectory(), path);
        FileLock lock = fo.lock();
        try {
            OutputStream os = fo.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Tries to find the selected Java platform for a project.
     * May return null.
     */
    public JavaPlatform getSelectedPlatform() {
        EditableProperties ep = helper.getProperties(NBJDK_PROPERTIES);
        String plaf = ep.getProperty(NBJDK_ACTIVE);
        if (plaf != null) {
            for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                if (plaf.equals(getPlatformID(p))) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Tries to set the Java platform for a project.
     */
    public void setSelectedPlatform(JavaPlatform jdk) throws IOException {
        assert jdk != null;
        initialize();
        EditableProperties ep = helper.getProperties(NBJDK_PROPERTIES);
        ep.setProperty(NBJDK_ACTIVE, getPlatformID(jdk));
        helper.putProperties(NBJDK_PROPERTIES, ep);
        ProjectManager.getDefault().saveProject(project);
    }

    private static String getPlatformID(JavaPlatform platform) {
        String s = platform.getProperties().get("platform.ant.name"); // NOI18N
        if (s != null) {
            return s;
        } else {
            return PLATFORM_ID_DEFAULT;
        }
    }

}
