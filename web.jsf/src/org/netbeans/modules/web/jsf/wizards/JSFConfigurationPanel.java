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

package org.netbeans.modules.web.jsf.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JsfPreferences;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author petr, alexeybutenko
 */
public class JSFConfigurationPanel extends WebModuleExtender implements WebModuleExtender.Savable {

    private final JSFFrameworkProvider framework;
    private final ExtenderController controller;
    private JSFConfigurationPanelVisual component;
    private boolean isFrameworkAddition;

    private static final String PREFERRED_LANGUAGE="jsf.language"; //NOI18N

    private JsfPreferences preferences;
    private WebModule webModule;

    private LibraryType libraryType;
    private Library jsfCoreLibrary;
    private ServerLibrary serverLibrary;
    private String newLibraryName;
    private File installedResource;

    // facelets configuratin
    private boolean enableFacelets;
    private boolean debugFacelets;
    private boolean skipComments;
    private boolean createExamples;
    
    //jsf configuration
    private String facesSuffix;
    private String facesMapping;
    private boolean validateXml;
    private boolean verifyObjects;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // Enum declarations
    public enum LibraryType {USED, NEW, SERVER};
    public enum PreferredLanguage {
        
        JSP("JSP"), Facelets("Facelets"); //NOI18N
        
        private String name;

        private PreferredLanguage(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public JSFConfigurationPanel(JSFFrameworkProvider framework, ExtenderController controller, boolean isFrameworkAddition) {
        this(framework, controller, isFrameworkAddition, null, null);
    }

    public JSFConfigurationPanel(JSFFrameworkProvider framework, ExtenderController controller, boolean isFrameworkAddition, JsfPreferences preferences, WebModule webModule) {
        this.framework = framework;
        this.controller = controller;
        this.isFrameworkAddition = isFrameworkAddition;
        this.preferences = preferences;
        this.webModule = webModule;

        debugFacelets = true;
        skipComments = true;
        createExamples = true;
        facesSuffix = ".xhtml"; //NOI18N
        validateXml = true;
        verifyObjects = false;
        facesMapping = "/faces/*"; //NOI18N
        enableFacelets = preferences == null ? false : preferences.getPreferredLanguage() == PreferredLanguage.Facelets;

        getComponent();
    }

    @Override
    public synchronized JSFConfigurationPanelVisual getComponent() {
        if (component == null) {
            component = new JSFConfigurationPanelVisual(this, isFrameworkAddition, webModule != null);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(JSFConfigurationPanel.class);
    }

    public String getFacesSuffix(){
        return facesSuffix;
    }

    public String getFacesMapping(){
        return facesMapping;
    }

    private void setFacesMapping(String facesMapping) {
        this.facesMapping = facesMapping;
    }

    @Override
    public void update() {
        component.update();
    }

    @Override
    public boolean isValid() {
        getComponent();
        if (component.valid()) {
            setFacesMapping(component.getURLPattern());
            return true;
        }
        return false;
    }

    @Override
    public Set extend(WebModule webModule) {
        Project project = FileOwnerQuery.getOwner(webModule.getDocumentBase());
        preferences = JsfPreferences.forProject(project);

        PreferredLanguage preferredLang = component.getPreferredLanguage();
        if ((preferredLang == PreferredLanguage.JSP) || (preferredLang == PreferredLanguage.Facelets)) {
            preferences.setPreferredLanguage(preferredLang);
        }
        return framework.extendImpl(webModule, component.getJsfComponentCustomizers());
    }

    public ExtenderController getController() {
        return controller;
    }

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public String getServletName(){
        return component.getServletName();
    }

    public void setServletName(String name){
        component.setServletName(name);
    }

    protected boolean isMaven() {
        ExtenderController.Properties properties = getController().getProperties();
        Boolean isMaven = (Boolean) properties.getProperty("maven"); //NOI18N
        return isMaven != null && isMaven;
    }

    @Deprecated
    /*
     * Use getFacesMapping() instead
     */
    public String getURLPattern(){
        return component.getURLPattern();
    }

    public void setURLPattern(String pattern){
        if (component !=null)
            component.setURLPattern(pattern);
    }

    public boolean validateXML(){
        return validateXml;
    }

    public void setValidateXML(boolean ver){
        validateXml = ver;
    }

    public boolean verifyObjects(){
        return verifyObjects;
    }

    public void setVerifyObjects(boolean val){
        verifyObjects = val;
    }

    public boolean packageJars(){
        return component.packageJars();
    }

    public String getNewLibraryName(){
        return newLibraryName;
    }

    public void setNewLibraryName(String version){
        this.newLibraryName = version;
        fireChangeEvent();
    }

    public File getInstallResource(){
        return installedResource;
    }

    public void setInstallResource(File folder){
        installedResource = folder;
        fireChangeEvent();
    }

    public boolean isDebugFacelets(){
        return debugFacelets;
    }

    public void setDebugFacelets(boolean value){
        debugFacelets = value;
    }

    public boolean isSkipComments(){
        return skipComments;
    }

    public void setSkipComments(boolean value){
        skipComments = value;
    }

    public boolean isCreateExamples(){
        return createExamples;
    }

    public void setCreateExamples(boolean value){
        createExamples = value;
    }

    public boolean isEnableFacelets() {
        return enableFacelets;
    }

    /**
     * Almost setter for enableFacelets variable. If force set to {@code true} then
     * the change has to be done, otherwise it's switched just in cases the enableFacelets
     * param equals {@code true}.
     */
    protected void updateEnableFacelets(boolean enableFacelets, boolean force) {
        // If there is a new change in the Preferred language check box
        if (this.enableFacelets != enableFacelets) {
            if (enableFacelets != false || force) {
                this.enableFacelets = enableFacelets;
            }
            PreferredLanguage preferredLanguage = component.getPreferredLanguage();
            if (preferences != null && preferredLanguage != null) {
                preferences.setPreferredLanguage(preferredLanguage);
            }
        }
    }

    protected void setEnableFacelets(boolean enableFacelets) {
        updateEnableFacelets(enableFacelets, false);
    }

    public LibraryType getLibraryType(){
        return libraryType;
    }

    public void setLibraryType(LibraryType value){
        libraryType = value;
    }

    public Library getLibrary(){
        return jsfCoreLibrary;
    }

    protected void setLibrary(Library library){
        this.jsfCoreLibrary = library;
        fireChangeEvent();
    }

    public ServerLibrary getServerLibrary() {
        return serverLibrary;
    }

    public WebModule getWebModule() {
        return webModule;
    }

    protected void setServerLibrary(ServerLibrary library){
        this.serverLibrary = library;
        fireChangeEvent();
    }

    public List<? extends JsfComponentImplementation> getEnabledJsfDescriptors() {
        return component.getActivedJsfDescriptors();
    }

    @Override
    public void save(WebModule webModule) {
        JSFConfigurationPanelVisual panel = getComponent();

        // handle preferred page language
        PreferredLanguage selectedLanguage = panel.getPreferredLanguage();
        if (PreferredLanguage.Facelets == selectedLanguage) {
            setEnableFacelets(true);
        } else {
            setEnableFacelets(false);
        }

        // handle JSF suites
        List<? extends JsfComponentImplementation> activedImplementations = panel.getActivedJsfDescriptors();
        List<JsfComponentImplementation> usedImplementations = new ArrayList<JsfComponentImplementation>();
        for (JsfComponentImplementation jsfImplementation : panel.getAllJsfDescriptors()) {
            if (jsfImplementation.isInWebModule(webModule)) {
                usedImplementations.add(jsfImplementation);
            }
        }

        // get list of newly added JSF suite implementations
        List<? extends JsfComponentImplementation> addedImplementations =
                new ArrayList<JsfComponentImplementation>(activedImplementations);
        addedImplementations.removeAll(usedImplementations);
        for (JsfComponentImplementation jsfComponentImplementation : addedImplementations) {
            jsfComponentImplementation.extend(webModule, jsfComponentImplementation.createJsfComponentCustomizer(null));
        }

        // get list of removed JSF suite implementations
        List<? extends JsfComponentImplementation> removedImplementations =
                new ArrayList<JsfComponentImplementation>(usedImplementations);
        removedImplementations.removeAll(activedImplementations);
        for (JsfComponentImplementation jsfComponentImplementation : removedImplementations) {
            jsfComponentImplementation.remove(webModule);
        }
    }

}
