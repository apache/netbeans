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
package org.netbeans.modules.web.jsf.icefaces;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.JsfComponentUtils;
import org.netbeans.modules.web.jsf.icefaces.ui.Icefaces2CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Icefaces2Implementation implements JsfComponentImplementation {

    private static final Logger LOGGER = Logger.getLogger(Icefaces2Implementation.class.getName());

    /**
     * Name of the node in NetBeans preferences.
     */
    public static final String PREFERENCES_NODE = "icefaces";
    /**
     * Framework name used also for statistics.
     */
    public static final String ICEFACES_NAME = "ICEfaces"; //NOI18N
    /**
     * Base class for which is searched by detecting ICEfaces2 on the classpath of the project.
     */
    public static final String ICEFACES_CORE_CLASS = "org.icefaces.impl.facelets.tag.icefaces.core.ConfigHandler"; //NOI18N
    /**
     * Name of preferred library which was used for last time.
     */
    public static final String PREF_LIBRARY_NAME = "preffered-library";

    private Icefaces2Customizer customizer;

    // Constants for web.xml
    private static final String FACES_SAVING_METHOD = "javax.faces.STATE_SAVING_METHOD"; //NOI18N
    private static final String FACES_SKIP_COMMENTS = "javax.faces.FACELETS_SKIP_COMMENTS"; //NOI18N

    // ICEfaces Maven resources
    private static final String MAVEN_DEP_CORE = "org.icefaces:icefaces:3.1.0:jar"; //NOI18N
    private static final String MAVEN_DEP_ACE = "org.icefaces:icefaces-ace:3.1.0:jar"; //NOI18N

    @Override
    public String getName() {
        return ICEFACES_NAME;
    }

    @NbBundle.Messages({
        "Icefaces2Implementation.icefaces.display.name=ICEfaces"
    })
    @Override
    public String getDisplayName() {
        return Bundle.Icefaces2Implementation_icefaces_display_name();
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(Icefaces2Implementation.class, "DESC_IcefacesImplementation"); //NOI18N
    }

    @Override
    public Set<FileObject> extend(WebModule webModule, JsfComponentCustomizer jsfComponentCustomizer) {
        // Extend project classpath
        extendClasspath(webModule, jsfComponentCustomizer);

        // Update web.xml DD if required
        try {
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(webModule.getDeploymentDescriptor());

            // add context-param - javax.faces.STATE_SAVING_METHOD
            InitParam savingMethodParam = (InitParam) ddRoot.createBean("InitParam");    //NOI18N
            savingMethodParam.setParamName(FACES_SAVING_METHOD);
            savingMethodParam.setParamValue("server"); //NOI18N
            ddRoot.addContextParam(savingMethodParam);

            // add context-param - javax.faces.FACELETS_SKIP_COMMENTS
            InitParam skipCommentsParam = (InitParam) ddRoot.createBean("InitParam");    //NOI18N
            skipCommentsParam.setParamName(FACES_SKIP_COMMENTS);
            skipCommentsParam.setParamValue("true"); //NOI18N
            ddRoot.addContextParam(skipCommentsParam);

            ddRoot.write(webModule.getDeploymentDescriptor());
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Exception during updating web.xml DD", ex); //NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during updating web.xml DD", ex); //NOI18N
        }

        // generate ICEfaces welcome page
        try {
            FileObject welcomePage = generateWelcomePage(webModule);
            Collections.singleton(welcomePage);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during welcome page creation", ex); //NOI18N
        }
        return Collections.<FileObject>emptySet();
    }

    private static void extendClasspath(WebModule webModule, JsfComponentCustomizer jsfComponentCustomizer) {
        try {
            List<Library> libraries = new ArrayList<Library>(1);
            Library ifLibrary = null;

            // maven based projects
            if (JsfComponentUtils.isMavenBased(webModule)) {
                ifLibrary = getMavenLibrary();
            } else {
                // get the ICEfaces library from customizer
                if (jsfComponentCustomizer != null) {
                    Icefaces2CustomizerPanelVisual icefacesPanel =
                            ((Icefaces2CustomizerPanelVisual) jsfComponentCustomizer.getComponent());
                    String chosenLibrary = icefacesPanel.getIcefacesLibrary();
                    ifLibrary = LibraryManager.getDefault().getLibrary(chosenLibrary);
                }

                // search for library stored in ICEfaces2 preferences
                if (ifLibrary == null) {
                    Preferences preferences = getIcefacesPreferences();
                    ifLibrary = LibraryManager.getDefault().getLibrary(
                            preferences.get(Icefaces2Implementation.PREF_LIBRARY_NAME, "")); //NOI18N
                }

                // otherwise search for any registered ICEfaces library in IDE
                if (ifLibrary == null) {
                    ifLibrary = Icefaces2Customizer.getIcefacesLibraries().get(0);
                }
            }

            if (ifLibrary != null) {
                libraries.add(ifLibrary);
                FileObject[] javaSources = webModule.getJavaSources();
                ProjectClassPathModifier.addLibraries(
                        libraries.toArray(new Library[1]),
                        javaSources[0],
                        ClassPath.COMPILE);
            } else {
                LOGGER.log(Level.SEVERE, "No ICEfaces library was found."); //NOI18N
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        }
    }

    private static Library getMavenLibrary() {
        return JsfComponentUtils.createMavenDependencyLibrary(
                ICEFACES_NAME + "-maven-lib", //NOI18N
                new String[]{MAVEN_DEP_CORE, MAVEN_DEP_ACE},
                new String[0]);
    }

    private static FileObject generateWelcomePage(WebModule webModule) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Other/welcomeIcefaces.xhtml"); //NOI18N
        DataObject templateDO = DataObject.find(templateFO);
        DataObject generated = templateDO.createFromTemplate(
                DataFolder.findFolder(webModule.getDocumentBase()),
                "welcomeIcefaces"); //NOI18N
        JsfComponentUtils.reformat(generated);

        // update and reformat index page
        updateIndexPage(webModule);

        return generated.getPrimaryFile();
    }

    private static void updateIndexPage(WebModule webModule) throws DataObjectNotFoundException {
        FileObject indexFO = webModule.getDocumentBase().getFileObject("index.xhtml"); //NOI18N
        DataObject indexDO = DataObject.find(indexFO);
        JsfComponentUtils.enhanceFileBody(indexDO, "</h:body>", "<br />\n<h:link outcome=\"welcomeIcefaces\" value=\"ICEfaces welcome page\" />"); //NOI18N
        if (indexFO.isValid() && indexFO.canWrite()) {
            JsfComponentUtils.reformat(indexDO);
        }
    }

    @Override
    public Set<JsfVersion> getJsfVersion() {
        return EnumSet.of(JsfVersion.JSF_2_0, JsfVersion.JSF_2_1, JsfVersion.JSF_2_2);
    }

    @Override
    public boolean isInWebModule(WebModule webModule) {
        ClassPath classpath = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        if (classpath.findResource(ICEFACES_CORE_CLASS.replace('.', '/') + ".class") != null) { //NOI18N
            return true;
        }
        return false;
    }

    @Override
    public JsfComponentCustomizer createJsfComponentCustomizer(WebModule webModule) {
        if (customizer == null) {
            customizer = new Icefaces2Customizer();
        }
        return customizer;
    }

    @Override
    public void remove(WebModule webModule) {
        try {
            List<Library> icefacesLibraries;
            if (JsfComponentUtils.isMavenBased(webModule)) {
                icefacesLibraries = Arrays.asList(getMavenLibrary());
            } else {
                icefacesLibraries = Icefaces2Customizer.getIcefacesLibraries();
            }
             ProjectClassPathModifier.removeLibraries(icefacesLibraries.toArray(new Library[0]), webModule.getJavaSources()[0], ClassPath.COMPILE);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during removing JSF suite from an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during removing JSF suite from an web project", ex); //NOI18N
        }
    }

    /**
     * Gets {@code NbPreferences} for ICEfaces plugin.
     *
     * @return Preferences of the ICEfaces
     */
    public static Preferences getIcefacesPreferences() {
        return NbPreferences.forModule(Icefaces2Customizer.class).node(Icefaces2Implementation.PREFERENCES_NODE);
    }
}
