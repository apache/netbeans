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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.wizard.api.NodeConfiguration;

/**
 *
 */
public abstract class NodeConfigurationImpl implements NodeConfiguration {
    private boolean isOverrideIncludes;
    private boolean isOverrideFiles;
    private boolean isOverrideMacros;
    private boolean isOverrideUndefinedMacros;
    private NodeConfigurationImpl parent;
    private final Set<String> userIncludes;
    private final Set<String> userFiles;
    private final Map<String, String> userMacros;
    private final Set<String> undefinedMacros;

    public NodeConfigurationImpl() {
        userIncludes = new LinkedHashSet<>();
        userFiles = new HashSet<>();
        userMacros = new HashMap<>();
        undefinedMacros = new LinkedHashSet<>();
    }

    @Override
    public boolean overrideIncludes() {
        return isOverrideIncludes;
    }

    public void setOverrideIncludes(boolean overrideIncludes) {
        isOverrideIncludes = overrideIncludes;
    }

    @Override
    public boolean overrideFiles() {
        return isOverrideFiles;
    }

    public void setOverrideFiles(boolean overrideFiles) {
        isOverrideFiles = overrideFiles;
    }

    @Override
    public boolean overrideMacros() {
        return isOverrideMacros;
    }

    public void setOverrideMacros(boolean overrideMacros) {
        isOverrideMacros = overrideMacros;
    }

    @Override
    public boolean overrideUndefinedMacros() {
        return isOverrideUndefinedMacros;
    }

    public void setOverrideUndefinedMacros(boolean isOverrideUndefinedMacros) {
        this.isOverrideUndefinedMacros = isOverrideUndefinedMacros;
    }

    public void setParent(NodeConfigurationImpl parent) {
        this.parent = parent;
    }

    public NodeConfigurationImpl getParent() {
        return parent;
    }

    @Override
    public Set<String> getUserInludePaths(boolean resulting) {
        if (resulting) {
            return countUserInludePaths();
        } else {
            return userIncludes;
        }
    }

    public void setUserInludePaths(Collection<String> set) {
         userIncludes.clear();
         if (set != null) {
            userIncludes.addAll(set);
         }
    }

    @Override
    public Set<String> getUserInludeFiles(boolean resulting) {
        if (resulting) {
            return countUserInludeFiles();
        } else {
            return userFiles;
        }
    }

    public void setUserInludeFiles(Collection<String> set) {
         userFiles.clear();
         if (set != null) {
            userFiles.addAll(set);
         }
    }

    @Override
    public Map<String, String> getUserMacros(boolean resulting) {
        if (resulting) {
            return countUserMacros();
        } else {
            return userMacros;
        }
    }

    public void setUserMacros(Map<String, String> map) {
        userMacros.clear();
        if (map != null) {
            userMacros.putAll(map);
        }
    }

    @Override
    public Set<String> getUndefinedMacros(boolean resulting) {
        if (resulting) {
            return countUndefinedMacros();
        } else {
            return undefinedMacros;
        }
    }

    public void setUndefinedMacros(Collection<String> set) {
         undefinedMacros.clear();
         if (set != null) {
            undefinedMacros.addAll(set);
         }
    }
    
    public Set<String> countUserInludePaths() {
        if (overrideIncludes()) {
            return userIncludes;
        }
        Set<String> result = new LinkedHashSet<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUserInludePaths(false));
             if (current.overrideIncludes()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
    
    public Set<String> countUserInludeFiles() {
        if (overrideFiles()) {
            return userFiles;
        }
        Set<String> result = new LinkedHashSet<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUserInludeFiles(false));
             if (current.overrideFiles()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
    
    public Map<String, String> countUserMacros() {
        if (overrideMacros()) {
            return userMacros;
        }
        Map<String, String> result =  new HashMap<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.putAll(current.getUserMacros(false));
            if (current.overrideMacros()){
                break;
            }
            current = current.getParent();
        }
        return result;
    }

    public Set<String> countUndefinedMacros() {
        if (overrideUndefinedMacros()) {
            return undefinedMacros;
        }
        Set<String> result = new LinkedHashSet<>();
        NodeConfigurationImpl current = this;
        while(current != null){
            result.addAll(current.getUndefinedMacros(false));
             if (current.overrideUndefinedMacros()) {
                break;
             }
            current = current.getParent();
        }
        return result;
    }
}
