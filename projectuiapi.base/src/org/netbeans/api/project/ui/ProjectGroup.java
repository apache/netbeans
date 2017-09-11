/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.project.ui;

import java.util.prefs.Preferences;
import org.netbeans.modules.project.uiapi.BaseUtilities;

/**
 * Object describing a project group, in most cases the currently active project group.
 * @author mkleint
 * @since 1.61
 */
public final class ProjectGroup {
    private final Preferences prefs;
    private final String name;

    ProjectGroup(String name, Preferences prefs) {
        this.name = name;
        this.prefs = prefs;
    }
    
    /**
     * name of the project group as given by user
     * @return 
     */
    public String getName() {
        return name;
    }
    
    /**
     * use this method to store and retrieve preferences related to project groups.
     * @param clazz
     * @return 
     */
    public Preferences preferencesForPackage(Class clazz) {
        return prefs.node(clazz.getPackage().getName().replace(".", "/"));
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectGroup other = (ProjectGroup) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }
 
    
    static class AccessorImpl extends BaseUtilities.ProjectGroupAccessor {
        
        
         public void assign() {
             if (BaseUtilities.ACCESSOR == null) {
                 BaseUtilities.ACCESSOR = this;
             }
         }
    
        @Override
        public ProjectGroup createGroup(String name, Preferences prefs) {
            return new ProjectGroup(name, prefs);
        }
    }    
    
    
}
