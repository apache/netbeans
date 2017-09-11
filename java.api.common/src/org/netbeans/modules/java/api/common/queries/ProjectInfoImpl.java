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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Default implementation for Java language based projects that use Ant as their
 * build tool.
 */
abstract class ProjectInfoImpl implements ProjectInformation, AntProjectListener {

    public static final String DEFAULT_ELEMENT_NAME = "name"; // NOI18N
    private static final String UNKNOWN = "???";    //NOI18N
    private final Object guard = new Object(); // guard for property changes
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Project project;
    private final String elementName;
    private final Icon icon;
    private String name;
    private String displayName;

    /**
     * Create an instance with all items that will not change during the lifetime
     * of the project.
     * 
     * @param project project that we hold information for
     * @param projectHelper class to read/update the project configuration
     * @param configurationNameSpace the specific name space that this project type uses
     * @param icon project icon
     * @param elementName configuration element name
     */
    public ProjectInfoImpl(Project project, Icon icon, String elementName) {
        this.project = project;
        this.icon = icon;
        this.elementName = elementName;
    }

    @Override
    public String getName() {
        synchronized (guard) {
            if (name == null) {
                name = PropertyUtils.getUsablePropertyName(getDisplayName());
            }
            return name;
        }
    }

    @Override
    public String getDisplayName() {
        synchronized (guard) {
            if (displayName == null) {
                displayName = getElementTextFromConfiguration(elementName);
            }

            return displayName;
        }
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Project getProject() {
        return project;
    }

    /**
     * Get the text from the named element of the primary configuration node.
     *
     * @param elementName name of the element that contains the property value
     * @return the property value, or '???' if not found
     */
    protected String getElementTextFromConfiguration(final String elementName) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            @Override
            public String run() {
                Element data = getPrimaryConfigurationData();
                Element element = XMLUtil.findElement(data, elementName, null);
                String name = element == null ? null : XMLUtil.findText(element);
                if (name == null) {
                    name = UNKNOWN;
                }
                return name;
            }
        });
    }

    /**
     * Get the primary configuration data for this project.
     * 
     * @return
     */
    protected abstract Element getPrimaryConfigurationData();

    /**
     * Notify all listeners of the property change passing the old and new values.
     *
     * @param prop property name
     * @param oldValue old value of the property
     * @param newValue new value of the property
     */
    protected void firePropertyChange(String prop, Object oldValue, Object newValue) {
        pcs.firePropertyChange(prop, oldValue, newValue);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        // only interested in changes to nbproject/project.xml
        if (AntProjectHelper.PROJECT_XML_PATH.equals(ev.getPath())) {
            // Could be various kinds of changes, but name & displayName might have changed.
            String oldName;
            String oldDisplayName;
            String newName;
            String newDisplayName;

            synchronized (guard) {
                oldName = name;
                oldDisplayName = displayName;
                // reset so they are re-read
                name = null;
                displayName = null;
                newName = getName();
                newDisplayName = getDisplayName();
            }

            firePropertyChange(ProjectInformation.PROP_NAME, oldName, newName);
            firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldDisplayName, newDisplayName);
        }
    }

    @Override
    public void propertiesChanged(AntProjectEvent ev) {
        // not interested in any properties
    }
}
