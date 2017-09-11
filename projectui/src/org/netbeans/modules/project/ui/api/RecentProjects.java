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

package org.netbeans.modules.project.ui.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.List;

import org.netbeans.modules.project.ui.OpenProjectList;

/**
 * Provides simple information about recent projects and fires PropertyChangeEvent
 * in case of change in the list of recent projects
 * @author Milan Kubec
 * @since 1.9.0
 */
public final class RecentProjects {

    /**
     * Property representing recent project information
     */
    public static final String PROP_RECENT_PROJECT_INFO = "RecentProjectInformation"; // NOI18N
    
    private static final RecentProjects INSTANCE = new RecentProjects();
    
    private PropertyChangeSupport pch;
    
    public static RecentProjects getDefault() {
        return INSTANCE;
    }
    
    /**
     * Creates a new instance of RecentProjects
     */
    private RecentProjects() {
        pch = new PropertyChangeSupport(this);
        OpenProjectList.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OpenProjectList.PROPERTY_RECENT_PROJECTS)) {
                    pch.firePropertyChange(new PropertyChangeEvent(RecentProjects.class,
                            PROP_RECENT_PROJECT_INFO, null, null));
                }
            }
        });
    }
    
    /**
     * Gets simple info {@link org.netbeans.modules.project.ui.api.UnloadedProjectInformation} about recent projects in IDE.
     * Project in the list might not exist or might not be valid e.g. in case when
     * project was deleted or changed. It's responsibility of the user of the API
     * to make sure the project exists and is valid.
     * @return list of project information about recently opened projects
     */
    public List<UnloadedProjectInformation> getRecentProjectInformation() {
        return OpenProjectList.getDefault().getRecentProjectsInformation();
    }
    
    /**
     * Adds a listener, use WeakListener or properly remove listeners
     * @param listener listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pch.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a listener
     * @param listener listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pch.removePropertyChangeListener(listener);
    }
    
}
