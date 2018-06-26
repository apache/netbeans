/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.richfaces;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.JsfComponentUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.richfaces.ui.Richfaces4CustomizerPanelVisual;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
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
public class Richfaces4Implementation implements JsfComponentImplementation {

    private static final Logger LOGGER = Logger.getLogger(Richfaces4Implementation.class.getName());

    private Richfaces4Customizer customizer;

    private static final String RICHFACES_NAME = "RichFaces"; //NOI18N

    // ICEfaces Maven resources
    private static final String MAVEN_REPO = "default:https://repository.jboss.org/nexus/content/groups/public-jboss/"; //NOI18N
    private static final String MAVEN_DEP_CORE = "org.richfaces.core:richfaces-core-impl:4.3.3.Final:jar";      //NOI18N
    private static final String MAVEN_DEP_UI = "org.richfaces.ui:richfaces-components-ui:4.3.3.Final:jar";      //NOI18N

    public static final Set<String> RF_LIBRARIES = new HashSet<String>();
    public static final Map<String, String> RF_DEPENDENCIES = new HashMap<String, String>();

    public static final String PREF_RICHFACES_NODE = "richfaces"; //NOI18N
    public static final String PREF_RICHFACES_LIBRARY = "base-library"; //NOI18N

    static {
        RF_LIBRARIES.add("org.richfaces.application.Module"); //NOI18N
        RF_LIBRARIES.add("org.richfaces.application.ServiceLoader"); //NOI18N
        RF_LIBRARIES.add("org.richfaces.el.ValueDescriptor"); //NOI18N
        RF_LIBRARIES.add("org.richfaces.el.ValueReference"); //NOI18N
        RF_DEPENDENCIES.put("com.google.common.base.Functions", "guava.jar"); //NOI18N
        RF_DEPENDENCIES.put("org.w3c.css.sac.Parser", "sac.jar"); //NOI18N
        RF_DEPENDENCIES.put("com.steadystate.css.parser.ParseException", "cssparser.jar"); //NOI18N
    }

    public Richfaces4Implementation() {
    }

    @Override
    public String getName() {
        return RICHFACES_NAME;
    }

    @NbBundle.Messages({
        "Richfaces4Implementation.richfaces.display.name=RichFaces"
    })
    @Override
    public String getDisplayName() {
        return Bundle.Richfaces4Implementation_richfaces_display_name();
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(Richfaces4Implementation.class, "LBL_RichFaces_Description");  //NOI18N
    }

    @Override
    public void remove(WebModule webModule) {
        try {
            List<Library> richfacesLibraries;
            if (JsfComponentUtils.isMavenBased(webModule)) {
                richfacesLibraries = Arrays.asList(getMavenLibrary());
            } else {
                richfacesLibraries = Richfaces4Customizer.getRichfacesLibraries();
            }
            ProjectClassPathModifier.removeLibraries(richfacesLibraries.toArray(
                    new Library[richfacesLibraries.size()]), webModule.getJavaSources()[0], ClassPath.COMPILE);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during removing JSF suite from an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during removing JSF suite from an web project", ex); //NOI18N
        }
    }

    @Override
    public Set<org.openide.filesystems.FileObject> extend(WebModule webModule, JsfComponentCustomizer jsfComponentCustomizer) {
        // Add library to webmodule classpath
        extendClasspath(webModule, jsfComponentCustomizer);

        // generate Richfaces welcome page
        try {
            FileObject welcomePage = generateWelcomePage(webModule);
            return Collections.singleton(welcomePage);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during welcome page creation", ex); //NOI18N
        }
        return Collections.<FileObject>emptySet();
    }

    private static void extendClasspath(WebModule webModule, JsfComponentCustomizer jsfComponentCustomizer) {
        try {
            List<Library> libraries = new ArrayList<Library>(1);
            Library rfLibrary = null;

            if (JsfComponentUtils.isMavenBased(webModule)) {
                rfLibrary = getMavenLibrary();
            } else {
                // get the RF library from customizer
                if (jsfComponentCustomizer != null) {
                    Richfaces4CustomizerPanelVisual panel = (Richfaces4CustomizerPanelVisual) jsfComponentCustomizer.getComponent();
                    rfLibrary = LibraryManager.getDefault().getLibrary(panel.getRichFacesLibrary());
                }

                // or search for library stored in Richfaces preferences
                if (rfLibrary == null) {
                    Preferences preferences = getRichfacesPreferences();
                    rfLibrary = LibraryManager.getDefault().getLibrary(
                            preferences.get(Richfaces4Implementation.PREF_RICHFACES_LIBRARY, "")); //NOI18N
                }

                // otherwise search for any registered RF library in IDE
                if (rfLibrary == null) {
                    rfLibrary = Richfaces4Customizer.getRichfacesLibraries().get(0);
                }
            }

            if (rfLibrary != null) {
                FileObject[] javaSources = webModule.getJavaSources();
                libraries.add(rfLibrary);
                ProjectClassPathModifier.addLibraries(
                        libraries.toArray(new Library[1]),
                        javaSources[0],
                        ClassPath.COMPILE);
            } else {
                LOGGER.log(Level.SEVERE, "No RichFaces library was found.");
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.WARNING, "Exception during extending an web project", ex); //NOI18N
        }
    }

    private static Library getMavenLibrary() {
        return JsfComponentUtils.createMavenDependencyLibrary(
                RICHFACES_NAME + "-maven-lib", //NOI18N
                new String[]{MAVEN_DEP_CORE, MAVEN_DEP_UI},
                new String[]{MAVEN_REPO});
    }

    private static FileObject generateWelcomePage(WebModule webModule) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Other/welcomeRichfaces.xhtml"); //NOI18N
        DataObject templateDO = DataObject.find(templateFO);
        DataObject generated = templateDO.createFromTemplate(
                DataFolder.findFolder(webModule.getDocumentBase()),
                "welcomeRichfaces"); //NOI18N
        JsfComponentUtils.reformat(generated);

        // update and reformat index page
        updateIndexPage(webModule);

        return generated.getPrimaryFile();
    }

    private static void updateIndexPage(WebModule webModule) throws DataObjectNotFoundException {
        FileObject documentBase = webModule.getDocumentBase();
        if (documentBase != null) {
            FileObject indexFO = documentBase.getFileObject("index.xhtml"); //NOI18N
            if (indexFO != null) {
                DataObject indexDO = DataObject.find(indexFO);
                JsfComponentUtils.enhanceFileBody(indexDO, "</h:body>", "<br />\n<h:link outcome=\"welcomeRichfaces\" value=\"Richfaces welcome page\" />"); //NOI18N
                if (indexFO.isValid() && indexFO.canWrite()) {
                    JsfComponentUtils.reformat(indexDO);
                }
            }
        }
    }

    @Override
    public Set<JSFVersion> getJsfVersion() {
        return EnumSet.of(JSFVersion.JSF_2_0, JSFVersion.JSF_2_1, JSFVersion.JSF_2_2);
    }

    @Override
    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule webModule) {
        ClassPath classpath = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        Iterator<String> iterator = RF_LIBRARIES.iterator();
        while (iterator.hasNext()) {
            String libraryName = iterator.next();
            if (classpath.findResource(libraryName.replace('.', '/') + ".class") == null) { //NOI18N
                return false;
            }
        }
        return true;
    }

    @Override
    public JsfComponentCustomizer createJsfComponentCustomizer(WebModule webModule) {
        if (customizer == null) {
            customizer = new Richfaces4Customizer();
        }
        return customizer;
    }

    /**
     * Gets {@link Preferences} for RichFaces4.
     *
     * @return {@code Preferences} of the RichFaces4
     */
    public static Preferences getRichfacesPreferences() {
        return NbPreferences.forModule(Richfaces4Implementation.class).
                node(Richfaces4Implementation.PREF_RICHFACES_NODE);
    }

}
