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

package org.netbeans.modules.j2ee.ejbjarproject;
import java.util.ArrayList;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;


public class Utils {

    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.j2ee.ejbjarproject"); // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.j2ee.ejbjarproject"); // NOI18N
    
    private static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;

    private Utils() {
    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty (WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }

    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty (WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, i);
            component.getAccessibleContext().setAccessibleDescription (step);
            resultSteps[i + offset] = step;
        }
    }

    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }

    public static boolean areInSameJ2EEApp(Project p1, Project p2) {
        Set globalPath = GlobalPathRegistry.getDefault().getSourceRoots();
        Iterator iter = globalPath.iterator();
        while (iter.hasNext()) {
            FileObject sourceRoot = (FileObject)iter.next();
            Project project = FileOwnerQuery.getOwner(sourceRoot);
            if (project != null) {
                Object j2eeAppProvider = project.getLookup().lookup(J2eeApplicationProvider.class);
                if (j2eeAppProvider != null) { // == it is j2ee app
                    J2eeApplicationProvider j2eeApp = (J2eeApplicationProvider)j2eeAppProvider;
                    J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();
                    if ((j2eeModules != null) && (j2eeModules.length > 0)) { // == there are some modules in the j2ee app
                        J2eeModuleProvider affectedPrjProvider1 = p1.getLookup().lookup(J2eeModuleProvider.class);
                        J2eeModuleProvider affectedPrjProvider2 = p2.getLookup().lookup(J2eeModuleProvider.class);
                        if (affectedPrjProvider1 != null && affectedPrjProvider2 != null) {
                            List childModules = Arrays.asList(j2eeModules);
                            if (childModules.contains(affectedPrjProvider1) &&
                                childModules.contains(affectedPrjProvider2)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // =========================================================================

    // utils for ejb code synchronization
    
    
    
    /** Returns list of all EJB projects that can be called from the caller project.
     *
     * @param enterpriseProject the caller enterprise project
     */
    public static Project [] getCallableEjbProjects (Project enterpriseProject) {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        
        boolean isCallerEJBModule = false;
        J2eeModuleProvider callerJ2eeModuleProvider = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        if (callerJ2eeModuleProvider != null && callerJ2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.EJB)) {
            // TODO: HACK - this should be set by calling AntArtifactQuery.findArtifactsByType(p, EjbProjectConstants.ARTIFACT_TYPE_EJBJAR)
            // but now freeform doesn't implement this correctly
            isCallerEJBModule = true;
        }
        // TODO: HACK - this must be solved by freeform's own implementation of EnterpriseReferenceContainer, see issue 57003
        // call ejb should not make this check, all should be handled in EnterpriseReferenceContainer
        boolean isCallerFreeform = enterpriseProject.getClass().getName().equals("org.netbeans.modules.ant.freeform.FreeformProject");
        
        List<Project> filteredResults = new ArrayList<Project>(allProjects.length);
        for (int i = 0; i < allProjects.length; i++) {
            boolean isEJBModule = false;
            J2eeModuleProvider j2eeModuleProvider = allProjects[i].getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null && j2eeModuleProvider.getJ2eeModule().getType().equals(J2eeModule.Type.EJB)) {
                isEJBModule = true;
            }
            if ((isEJBModule && !isCallerFreeform) ||
                (isCallerFreeform && enterpriseProject.equals(allProjects[i]))) {
                filteredResults.add(allProjects[i]);
            }
        }
        return filteredResults.toArray(new Project[filteredResults.size()]);
    }

    /**
     * Logs the UI gesture.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle,String message, Object[] params) {
        Parameters.notNull("message", message);
        Parameters.notNull("bundle", bundle);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(UI_LOGGER.getName());
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        UI_LOGGER.log(logRecord);
    }

    /**
     * Logs a usage event.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }
    
    /**
     * Creates an URL of a classpath or sourcepath root
     * For the existing directory it returns the URL obtained from {@link File#toUri()}
     * For archive file it returns an URL of the root of the archive file
     * For non existing directory it fixes the ending '/'
     * @param root the file of a root
     * @param offset a path relative to the root file or null (eg. src/ for jar:file:///lib.jar!/src/)" 
     * @return an URL of the root
     * @throws MalformedURLException if the URL cannot be created
     */
    public static URL getRootURL (File root, String offset) throws MalformedURLException {
        URL url = root.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        } else if (!root.exists()) {
            url = new URL(url.toExternalForm() + "/"); // NOI18N
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }

}
