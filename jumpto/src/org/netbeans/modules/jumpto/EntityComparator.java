/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
