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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.eclipse.core.spi.LaunchConfiguration;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses given workspace and fills up it with found data.
 *
 * @author mkrauskopf
 */
final class WorkspaceParser {
    
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(WorkspaceParser.class.getName());
    
    private static final String VM_XML = "org.eclipse.jdt.launching.PREF_VM_XML"; // NOI18N
    private static final String IGNORED_CP_ENTRY = "##<cp entry ignore>##"; // NOI18N
    
    private static final String VARIABLE_PREFIX = "org.eclipse.jdt.core.classpathVariable."; // NOI18N
    private static final int VARIABLE_PREFIX_LENGTH = VARIABLE_PREFIX.length();
    
    private static final String RESOURCES_VARIABLE_PREFIX = "pathvariable."; // NOI18N
    private static final int RESOURCES_VARIABLE_PREFIX_LENGTH = RESOURCES_VARIABLE_PREFIX.length();

    private static final String USER_LIBRARY_PREFIX = "org.eclipse.jdt.core.userLibrary."; // NOI18N
    private static final int USER_LIBRARY_PREFIX_LENGTH = USER_LIBRARY_PREFIX.length();

    private static final String JSF_LIB_NS = "http://www.eclipse.org/webtools/jsf/schema/jsflibraryregistry.xsd"; // NOI18N
    
    private final Workspace workspace;
    
    /** Creates a new instance of WorkspaceParser */
    WorkspaceParser(Workspace workspace) {
        this.workspace = workspace;
    }
    
    /** Returns classpath content from project's .classpath file */
    void parse() throws ProjectImporterException {
        try {
            parseLaunchingPreferences();
            parseCorePreferences();
            parseResourcesPreferences();
            parseWorkspaceProjects();
            parseJSFLibraryRegistryV2();
            parseLaunchConfigurations();
        } catch (IOException e) {
            throw new ProjectImporterException(
                    "Cannot load workspace properties", e); // NOI18N
        }
    }

    private void parseLaunchingPreferences() throws IOException, ProjectImporterException {
        if (!workspace.getLaunchingPrefsFile().exists()) {
            workspace.setJREContainers(new HashMap<String, String>());
            return;
        }
        for (Map.Entry<String,String> entry : EclipseUtils.loadProperties(workspace.getLaunchingPrefsFile()).entrySet()) {
            if (entry.getKey().equals(VM_XML)) {
                Map<String,String> vmMap = PreferredVMParser.parse(entry.getValue());
                workspace.setJREContainers(vmMap);
            }
        }
    }
    
    private void parseCorePreferences() throws IOException, ProjectImporterException {
        for (Map.Entry<String,String> entry : EclipseUtils.loadProperties(workspace.getCorePreferenceFile()).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.startsWith(VARIABLE_PREFIX)) {
                Workspace.Variable var = new Workspace.Variable(key.substring(VARIABLE_PREFIX_LENGTH), value);
                workspace.addVariable(var);
            } else if (key.startsWith(USER_LIBRARY_PREFIX) && !value.startsWith(IGNORED_CP_ENTRY)) { // #73542
                String libName = key.substring(USER_LIBRARY_PREFIX_LENGTH);
                List<String> jars = new ArrayList<String>();
                List<String> javadocs = new ArrayList<String>();
                List<String> sources = new ArrayList<String>();
                UserLibraryParser.getJars(libName, value, jars, javadocs, sources);
                workspace.addUserLibrary(libName, jars, javadocs, sources);
            } // else we don't use other properties in the meantime
        }
    }
    
    /*private*/ void parseJSFLibraryRegistryV2() throws IOException {
        if (!workspace.getUserJSFLibraries().exists()) {
            return;
        }
        Document xml;
        try {
            xml = XMLUtil.parse(new InputSource(Utilities.toURI(workspace.getUserJSFLibraries()).toString()), false, true, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException e) {
            IOException ioe = (IOException) new IOException(workspace.getUserJSFLibraries() + ": " + e.toString()).initCause(e); // NOI18N
            throw ioe;
        }
        
        Element root = xml.getDocumentElement();
        if (!"JSFLibraryRegistry".equals(root.getLocalName()) || // NOI18N
            !JSF_LIB_NS.equals(root.getNamespaceURI())) {
            return;
        }
        for (Element el : XMLUtil.findSubElements(root)) {
            String libraryName = el.getAttribute("Name"); // NOI18N
            List<String> jars = new ArrayList<String>();
            for (Element file : XMLUtil.findSubElements(el)) {
                String path = file.getAttribute("SourceLocation"); // NOI18N
                if (!"false".equals(file.getAttribute("RelativeToWorkspace"))) { // NOI18N
                    path = new File(workspace.getDirectory(), path).getPath();
                }
                jars.add(path);
            }
            // TODO: in Ganymede Javadoc/sources customization does not seem to be persisted. eclipse defect??
            workspace.addUserLibrary(libraryName, jars, null, null);
        }
    }
    
    private void parseResourcesPreferences() throws IOException, ProjectImporterException {
        if (!workspace.getResourcesPreferenceFile().exists()) {
            return;
        }
        for (Map.Entry<String,String> entry : EclipseUtils.loadProperties(workspace.getResourcesPreferenceFile()).entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(RESOURCES_VARIABLE_PREFIX)) {
                Workspace.Variable var = new Workspace.Variable(key.substring(RESOURCES_VARIABLE_PREFIX_LENGTH), entry.getValue());
                workspace.addResourcesVariable(var);
            }
        }
    }
    
    private void parseWorkspaceProjects() throws ProjectImporterException {
        // directory filter
        FileFilter dirFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        
        Set<File> projectsDirs = new HashSet<File>();
        // let's find internal projects
        File[] innerDirs = workspace.getDirectory().listFiles(dirFilter);
        for (int i = 0; i < innerDirs.length; i++) {
            File prjDir = innerDirs[i];
            if (EclipseUtils.isRegularProject(prjDir)) {
                // we cannot load projects recursively until we have loaded
                // information of all projects in the workspace
                logger.finest("Found a regular Eclipse Project in: " // NOI18N
                        + prjDir.getAbsolutePath());
                if (!projectsDirs.contains(prjDir)) {
                    addLightProject(projectsDirs, prjDir, true);
                } else {
                    logger.finest("Trying to add the same project twice: " // NOI18N
                            + prjDir.getAbsolutePath());
                }
            } // else .metadata or something we don't care about yet
        }
        
        // let's try to find external projects
        File[] resourceDirs = workspace.getResourceProjectsDir().listFiles(dirFilter);
        for (int i = 0; i < resourceDirs.length; i++) {
            File resDir = resourceDirs[i];
            File location = getLocation(resDir);
            if (location != null) {
                if (EclipseUtils.isRegularProject(location)) {
                    logger.finest("Found a regular Eclipse Project in: " // NOI18N
                            + location.getAbsolutePath());
                    if (!projectsDirs.contains(location)) {
                        addLightProject(projectsDirs, location, false);
                    } else {
                        logger.finest("Trying to add the same project twice: " // NOI18N
                                + location.getAbsolutePath());
                    }
                } else {
                    logger.finest(location.getAbsolutePath() + " does not contain regular project"); // NOI18N
                }
            }
        }
        
        // Project instances with base infos are loaded, let's load all the
        // information we need (we have to do this here because project's
        // classpath needs at least project's names and abs. paths during
        // parsing
        // Load first all .project files to init any links project may have
        // and then .classpath files.
        for (EclipseProject project : workspace.getProjects()) {
            project.setWorkspace(workspace);
            ProjectFactory.getInstance().loadDotProject(project);
        }
        for (EclipseProject project : workspace.getProjects()) {
            ProjectFactory.getInstance().loadDotClassPath(project);
        }
        
        for (EclipseProject project : workspace.getProjects()) {
            project.replaceContainers();
        }
    }
    
    private void addLightProject(Set<File> projectsDirs, File prjDir, boolean internal) {
        EclipseProject project = EclipseProject.createProject(prjDir);
        if (project != null) {
            project.setName(prjDir.getName());
            project.setInternal(internal);
            workspace.addProject(project);
            projectsDirs.add(prjDir);
        }
    }
    
    /** Loads location of external project. */
    private static File getLocation(final File prjDir) throws ProjectImporterException {
        if (".org.eclipse.jdt.core.external.folders".equals(prjDir.getName())) { //NOI18N
            // ignore this. some internal Eclipse stuff
            return null;
        }
        File locationFile = new File(prjDir, ".location"); // NOI18N
        if (locationFile.isFile()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(locationFile);
                return getLocation(fis);
            } catch (IOException e) {
                throw new ProjectImporterException("Error during reading " + // NOI18N
                        ".location file", e); // NOI18N
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        throw new ProjectImporterException(e);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Loads location of external project. Package-private for unit tests only.
     */
    static File getLocation(final InputStream is) throws IOException {
        // starts with 17 bytes.
        long toSkip = 17;
        while(toSkip != 0) {
            toSkip -= is.skip(toSkip);
        }
        // follows byte describing path length
        int pathLength = is.read();
        // follows path itself
        byte[] path = new byte[pathLength];
        int read = is.read(path);
        assert read == pathLength;
        String pathS = new String(path, "ISO-8859-1"); // NOI18N
        if (pathS.startsWith("URI//")) { // #89577 // NOI18N
            pathS = pathS.substring(pathS.indexOf(':') + 1);
        }
        if (pathS.length() == 0) {
            return null;
        }
        return new File(pathS);
    }
    
    private void parseLaunchConfigurations() throws IOException, ProjectImporterException {
        List<LaunchConfiguration> configs = new ArrayList<LaunchConfiguration>();
        File[] launches = new File(workspace.getDirectory(), ".metadata/.plugins/org.eclipse.debug.core/.launches").listFiles(new FilenameFilter() { // NOI18N
            public boolean accept(File dir, String name) {
                return name.endsWith(".launch"); // NOI18N
            }
        });
        if (launches != null) {
            for (File launch : launches) {
                Document doc;
                try {
                    doc = XMLUtil.parse(new InputSource(Utilities.toURI(launch).toString()), false, false, null, null);
                } catch (SAXException x) {
                    throw new ProjectImporterException("Could not parse " + launch, x);
                }
                Element launchConfiguration = doc.getDocumentElement();
                String type = launchConfiguration.getAttribute("type"); // NOI18N
                Map<String,String> attrs = new HashMap<String,String>();
                NodeList nl = launchConfiguration.getElementsByTagName("stringAttribute"); // NOI18N
                for (int i = 0; i < nl.getLength(); i++) {
                    Element stringAttribute = (Element) nl.item(i);
                    attrs.put(stringAttribute.getAttribute("key"), stringAttribute.getAttribute("value")); // NOI18N
                }
                configs.add(new LaunchConfiguration(launch.getName().replaceFirst("\\.launch$", ""), type, // NOI18N
                        attrs.get("org.eclipse.jdt.launching.PROJECT_ATTR"), // NOI18N
                        attrs.get("org.eclipse.jdt.launching.MAIN_TYPE"), // NOI18N
                        attrs.get("org.eclipse.jdt.launching.PROGRAM_ARGUMENTS"), // NOI18N
                        attrs.get("org.eclipse.jdt.launching.VM_ARGUMENTS"))); // NOI18N
            }
        }
        workspace.setLaunchConfigurations(configs);
    }

}
