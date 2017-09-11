/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Tomas Zezula
 */
class J2SEProjectPlatformImpl implements J2SEProjectPlatform, PropertyChangeListener {

    private static final String SE_PLATFORM = "j2se";   //NOI18N

    private final J2SEProject project;
    private final PropertyChangeSupport support;
    private final PropertyEvaluator eval;

    J2SEProjectPlatformImpl(@NonNull final J2SEProject project) {
        assert project != null;
        this.support = new PropertyChangeSupport(this);
        this.project = project;
        this.eval = project.evaluator();
        this.eval.addPropertyChangeListener(this);
    }

    @Override
    @CheckForNull
    public JavaPlatform getProjectPlatform() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<JavaPlatform>(){
            @Override
            public JavaPlatform run() {
                JavaPlatform jp =  CommonProjectUtils.getActivePlatform(
                    project.evaluator().getProperty(ProjectProperties.PLATFORM_ACTIVE));
                if (jp == null) {
                    jp = ProjectPlatform.forProject(project, eval, SE_PLATFORM);
                }
                return jp;
            }
        });
    }

    @Override
    public void setProjectPlatform(@NonNull final JavaPlatform platform) throws IOException {
        Parameters.notNull("platform", platform);
        if (!SE_PLATFORM.equals(platform.getSpecification().getName())) {
            throw new IllegalArgumentException(
                String.format(
                    "Not J2SE Platform: %s (%s)",       //NOI18N
                    platform.getDisplayName(),
                    platform.getSpecification().getName()));
        }
        if (platform.getInstallFolders().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(
                    "Broken Platform %s",       //NOI18N
                    platform.getDisplayName()));
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    final String platformId = platform.getProperties().get(J2SEProjectProperties.PROP_PLATFORM_ANT_NAME);
                    final UpdateHelper uh = project.getUpdateHelper();
                    final EditableProperties props = uh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.setProperty(ProjectProperties.PLATFORM_ACTIVE, platformId);
                    uh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    updateProjectXml(platform, uh);
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getCause();
        }
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent event) {
        final String propName = event.getPropertyName();
        if (propName == null || ProjectProperties.PLATFORM_ACTIVE.equals(propName)) {
            support.firePropertyChange(PROP_PROJECT_PLATFORM, null, null);
        }
    }

    static boolean updateProjectXml(
            @NonNull final JavaPlatform platform,
            @NonNull final UpdateHelper helper) {
        assert ProjectManager.mutex().isWriteAccess();
        final boolean remove = platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform());
        final Element root = helper.getPrimaryConfigurationData(true);
        boolean changed = false;
        if (remove) {
            final Element platformElement = XMLUtil.findElement(
                root,
                "explicit-platform",    //NOI18N
                J2SEProject.PROJECT_CONFIGURATION_NAMESPACE);
            if (platformElement != null) {
                root.removeChild(platformElement);
                changed = true;
            }
        } else {
            Element insertBefore = null;
            for (Element e : XMLUtil.findSubElements(root)) {
                final String name = e.getNodeName();
                if (! "name".equals(name) &&                  //NOI18N
                    ! "minimum-ant-version".equals(name)) {   //NOI18N
                    insertBefore = e;
                    break;
                }
            }
            final Element platformNode = insertBefore.getOwnerDocument().createElementNS(
                    J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,
                    "explicit-platform"); //NOI18N
            platformNode.setAttribute(
                    "explicit-source-supported",    //NOI18N
                    platform.getSpecification().getVersion().compareTo(new SpecificationVersion("1.3"))>0?   //NOI18N
                        "true":                                                                              //NOI18N
                        "false");                                                                            //NOI18N
            root.insertBefore(platformNode, insertBefore);
            if ("explicit-platform".equals(insertBefore.getNodeName())) { //NOI18N
                root.removeChild(insertBefore);
            }
            changed = true;
        }
        if (changed) {
            helper.putPrimaryConfigurationData(root, true);
        }
        return changed;
    }

}
