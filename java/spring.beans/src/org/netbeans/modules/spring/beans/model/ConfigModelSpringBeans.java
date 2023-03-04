/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.spring.beans.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.spring.api.beans.model.FileSpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * The {@link SpringBeans} implementation for multiple config files.
 *
 * @author Andrei Badea
 */
public class ConfigModelSpringBeans implements SpringBeans {

    private final Map<File, SpringBeanSource> file2BeanSource;

    public ConfigModelSpringBeans(Map<File, SpringBeanSource> file2BeanSource) {
        this.file2BeanSource = file2BeanSource;
    }

    public SpringBean findBean(String name) {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess() : "The SpringBeans instance has escaped the Action.run() method";
        return findBean(name, new HashSet<String>());
    }
    
    private SpringBean findBean(String name, Set<String> visitedNames) {
        if(visitedNames.contains(name)) {
            return null; // loop, break!
        }
        
        for (SpringBeanSource beanSource : file2BeanSource.values()) {
            SpringBean bean = beanSource.findBean(name);
            if (bean != null) {
                return bean;
            }
        }

        visitedNames.add(name);
        // handle aliases
        for(SpringBeanSource beanSource : file2BeanSource.values()) {
            String aliasName = beanSource.findAliasName(name);
            if (aliasName != null) {
                return findBean(aliasName, visitedNames);
            }
        }
        
        return null;
    }

    public FileSpringBeans getFileBeans(FileObject fo) {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess() : "The SpringBeans instance has escaped the Action.run() method";
        File file = FileUtil.toFile(fo);
        if (file != null) {
            return file2BeanSource.get(file);
        }
        return null;
    }

    public List<SpringBean> getBeans() {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess() : "The SpringBeans instance has escaped the Action.run() method";
        List<SpringBean> result = new ArrayList<SpringBean>(file2BeanSource.size() * 20);
        for (SpringBeanSource beanSource : file2BeanSource.values()) {
            result.addAll(beanSource.getBeans());
        }
        return Collections.unmodifiableList(result);
    }

    public Set<String> getAliases() {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess() : "The SpringBeans instance has escaped the Action.run() method";
        Set<String> aliases = new HashSet<String>(file2BeanSource.size() * 5);
        for (SpringBeanSource beanSource : file2BeanSource.values()) {
            aliases.addAll(beanSource.getAliases());
        }
        return Collections.unmodifiableSet(aliases);
    }
}
