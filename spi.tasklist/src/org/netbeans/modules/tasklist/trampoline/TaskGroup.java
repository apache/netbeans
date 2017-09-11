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

package org.netbeans.modules.tasklist.trampoline;

import java.awt.Image;
import java.util.List;

/**
 * A class that describes Task group, e.g. Error, Warning, TODO etc. Task groups are
 * visible to the user in Task List's window.
 * 
 * @author S. Aubrecht
 */
public final class TaskGroup implements Comparable<TaskGroup> {
    
    private String name;
    private String displayName;
    private String description;
    private Image icon;
    private int index;
    
    /** 
     * Creates a new instance of TaskGroup
     *  
     * @param name Group's id
     * @param displayName Group's display name
     * @param description Group's description (for tooltips)
     * @param icon Group's icon
     */
    public TaskGroup( String name, String displayName, String description, Image icon ) {
        assert null != name;
        assert null != displayName;
        assert null != icon;
        
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    /**
     * @return List of all available TaskGroups.
     */
    public static List<? extends TaskGroup> getGroups() {
        return TaskGroupFactory.getDefault().getGroups();
    }
    
    /**
     * @return Identification of the group.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Group's display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return Group's description (for tooltips etc)
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return Group's icon.
     */
    public Image getIcon() {
        return icon;
    }

    public int compareTo( TaskGroup otherGroup ) {
        return index - otherGroup.index;
    }
    
    void setIndex( int index ) {
        this.index = index;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        final TaskGroup test = (TaskGroup) o;

        if (this.name != test.name && this.name != null &&
            !this.name.equals(test.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
