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
package org.netbeans.modules.bugtracking.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;

/**
 *
 * @author jpeska
 */
public class Category {

    private String name;
    private List<IssueImpl> tasks;
    private boolean loaded;

    public Category(String name, List<IssueImpl> tasks) {
        this(name, tasks, true);
    }

    public Category(String name) {
        this(name, new ArrayList<IssueImpl>(), false);
    }

    public Category(String name, List<IssueImpl> tasks, boolean loaded) {
        this.name = name;
        this.tasks = tasks;
        this.loaded = loaded;
    }

    public void removeTask(IssueImpl task) {
        tasks.remove(task);
    }

    public void addTask(IssueImpl task) {
        tasks.add(task);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IssueImpl> getTasks() {
        return tasks;
    }

    public void setTasks(List<IssueImpl> tasks) {
        if (!loaded && tasks != null) {
            loaded = true;
        }
        this.tasks = tasks;
    }

    public boolean persist() {
        return true;
    }

    public void reload() {
        this.loaded = false;
        refresh();
    }

   /**
     * Return int value user for sorting category nodes. Default value is 100. Sorting is ascending.
     *
     */
    public int sortIndex() {
        return 100;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Category other = (Category) obj;
        return name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    public void refresh() {
        if (loaded) {
            refreshTasks();
        } else {
            DashboardUtils.loadCategory(this);
        }
    }

    protected void refreshTasks() {
        Map<RepositoryImpl, List<String>> map = getTasksToRepository(this.getTasks());
        Set<RepositoryImpl> repositoryKeys = map.keySet();
        for (RepositoryImpl repository : repositoryKeys) {
            List<String> ids = map.get(repository);
            repository.getIssueImpls(ids.toArray(new String[ids.size()]));
        }
    }

    private Map<RepositoryImpl, List<String>> getTasksToRepository(List<IssueImpl> tasks) {
        Map<RepositoryImpl, List<String>> map = new HashMap<RepositoryImpl, List<String>>();
        for (IssueImpl issue : tasks) {
            RepositoryImpl repositoryKey = issue.getRepositoryImpl();
            if (map.containsKey(repositoryKey)) {
                map.get(repositoryKey).add(issue.getID());
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(issue.getID());
                map.put(repositoryKey, list);
            }
        }
        return map;
    }
}
