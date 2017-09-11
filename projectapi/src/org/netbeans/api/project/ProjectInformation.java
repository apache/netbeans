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

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.spi.project.ProjectIconAnnotator;

/**
 * General information about a project.
 * <strong>Use {@link ProjectUtils#getInformation} as a client.</strong>
 * Use {@link Project#getLookup} as a provider.
 * @author Jesse Glick
 */
public interface ProjectInformation {

    /** Property name fired when the code name changes. */
    String PROP_NAME = "name"; // NOI18N

    /** Property name fired when the display name changes. */
    String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    
    /** Property name fired when the icon changes. */
    String PROP_ICON = "icon"; // NOI18N
    
    /**
     * Get a programmatic code name suitable for use in build scripts or other
     * references.
     * <p>
     * Project names should typically be distinctive enough to distinguish
     * between different projects with some kind of relationships, <em>but</em>
     * any usage of this name must take into account that they are not forced
     * to be unique.
     * <p>
     * Should not contain odd characters; should be usable as a directory name
     * on disk, as (part of) an Ant property name, etc.
     * XXX precise format - at least conforms to XML NMTOKEN or ID
     * @return a code name
     * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/PropertyUtils.html#getUsablePropertyName(java.lang.String)"><code>PropertyUtils.getUsablePropertyName</code></a>
     */
    String getName();
    
    /**
     * Get a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * XXX precise format - probably XML PCDATA
     * @return a display name for the project
     */
    String getDisplayName();
    
    /** 
     * Gets icon for given project.
     * Usually determined by the project type.
     * @return icon of the project.
     * @see ProjectIconAnnotator
     */
    Icon getIcon();
    
    /**
     * Get the associated project.
     * @return the project for which information is being provided
     */
    Project getProject();
    
    /**
     * Add a listener to property changes.
     * Only {@link #PROP_NAME}, {@link #PROP_DISPLAY_NAME}, and {@link #PROP_ICON} may be fired.
     * Since the event source is the info object, you may use {@link #getProject}.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a listener to property changes.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
