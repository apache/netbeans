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
