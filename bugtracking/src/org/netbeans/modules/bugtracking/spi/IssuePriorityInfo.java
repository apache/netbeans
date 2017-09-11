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

package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;

/**
 * Represents information related to one particular issue priority. 
 * The Priority attributes are used in various Task Dashboard features 
 * - e.g. Icon is shown next to an Issue, etc.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class IssuePriorityInfo {
    private final String id;
    private final String displayName;
    private final Image icon;

    /**
     * Creates a IssuePriorityInfo. 
     * Note that when no icon is provided the Tasks Dashboard will 
     * use default icons given by the order of Priority infos returned
     * via {@link IssuePriorityProvider#getPriorityInfos()}
     * 
     * @param id - priority id as given by the particular implementation
     * @param displayName - priority name as given by the particular implementation
     * @see IssuePriorityProvider#getPriorityInfos() 
     * @since 1.85
     */
    public IssuePriorityInfo(String id, String displayName) {
        this(id, displayName, null);
    }
    
    /**
     * Creates a IssuePriorityInfo. 
     * 
     * @param id - priority id as given by the particular implementation
     * @param displayName - priority name as given by the particular implementation
     * @param icon - priority icon as given by the particular implementation
     * @see IssuePriorityProvider#getPriorityInfos() 
     * @since 1.85
     */
    public IssuePriorityInfo(String id, String displayName, Image icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    /**
     * Returns the display name for this Priority.
     * 
     * @return display name associated with this Priority
     * @since 1.85
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the icon to be shown next to an Issue in the Tasks Dashboard. 
     * 
     * @return icon associated with this Priority
     * @see IssuePriorityProvider#getPriorityInfos()
     * @since 1.85
     */
    public Image getIcon() {
        return icon;
    }

    /**
     * Returns a unique id for this Priority.
     * 
     * @return a unique id
     * @since 1.85
     */
    public String getID() {
        return id;
    }
}
