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

package org.netbeans.modules.jumpto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;

/**
 * An abstraction that helps to define comparator used for ordering list of the
 * entities (i.e. files, types, symbols etc.) that belong to the projects.
 *
 * The successors must implement the method 
 * {@code Comparator.compare(E e1, E e2)}.
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public abstract class EntityComparator<E> implements Comparator<E> {

    protected final String mainProjectName = getMainProjectName();
    protected final
        Collection<String> namesOfOpenProjects = getNamesOfOpenProjects();

    /**
     * Comparies its two strings for order.
     * @param s1 the first {@code String} to be compared (may be {@code null}).
     * @param s2 the second {@code String} to be compared (may be {@code null}).
     * @return  the value <code>0</code> if the argument string is equal to
     *          this string; a value less than <code>0</code> if this string
     *          is lexicographically less than the string argument; and a
     *          value greater than <code>0</code> if this string is
     *          lexicographically greater than the string argument.
     */
    protected int compare(String s1, String s2) {
        if(s1 == null) s1 = ""; // NOI18N
        if(s2 == null) s2 = ""; // NOI18N
        return s1.compareTo(s2);
    }

    protected int compare(String s1, String s2, boolean caseSensitive) {
        if(s1 == null) s1 = ""; // NOI18N
        if(s2 == null) s2 = ""; // NOI18N
        return caseSensitive ? s1.compareTo(s2) : s1.compareToIgnoreCase(s2);
    }

    /**
     * Comparies its two project names for order.
     * It establishes the folowing relation between projects:
     * <pre>
     * Main Project &lt; Open Project &lt; Not Open Project
     * </pre>
     * Note, a lower will be displayed before the rest in the list.
     */
    protected int compareProjects(String p1Name, String p2Name) {
        if(p1Name == null) p1Name = "";  // NOI18N
        if(p2Name == null) p2Name = "";  // NOI18N

        if(p1Name.isEmpty()) {
            if(p2Name.isEmpty()) {
                return 0;
            }
            return 1;
        }
        if(p2Name.isEmpty()) {
            return -1;
        }
        // here both p1 and p2 are not empty
        boolean isP1Closed = !namesOfOpenProjects.contains(p1Name);
        boolean isP2Closed = !namesOfOpenProjects.contains(p2Name);
        if(isP1Closed) {
            if(isP2Closed) {
                return 0;
            }
            return 1;
        }
        if(isP2Closed) {
            return -1;
        }
        // here both p1 and p2 are opened
        if(p1Name.equals(mainProjectName)) {
            if(p2Name.equals(mainProjectName)) {
                return 0; // both p1 and p2 are main projects
            }
            return -1;
        }
        if(p2Name.equals(mainProjectName)) {
            return 1;
        }
        return 0;
    }

    /**
     * Returns a name of the main project (if any).
     * @return a name if main project exists, otherwise {@code null}.
     */
    public static String getMainProjectName() {
        final Project mainProject = OpenProjects.getDefault().getMainProject();
        return mainProject == null ? null :
            ProjectUtils.getInformation(mainProject).getDisplayName();
    }

    /**
     * Returns names of all projects opened in the IDE's GUI.
     * @return a collection of the names.
     */
    private static Collection<String> getNamesOfOpenProjects() {
        ArrayList<String> names = new ArrayList<String>(10);
        for(Project p: OpenProjects.getDefault().getOpenProjects()) {
            String pName = ProjectUtils.getInformation(p).getDisplayName();
            names.add(pName);
        }
        return names;
    }

}
