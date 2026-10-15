/*
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
            repository.getIssueImpls(ids.toArray(new String[0]));
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
