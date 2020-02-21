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

package org.netbeans.modules.cnd.discovery.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of FolderProperties.
 * Enough in most cases.
 * 
 */
public final class FolderImpl implements FolderProperties {
    private final String path;
    private final ItemProperties.LanguageKind language;
    private final Set<String> userIncludes = new LinkedHashSet<>();
    private final Set<String> userFiles = new LinkedHashSet<>();
    private final Set<String> systemIncludes = new LinkedHashSet<>();
    private final Map<String, String> userMacros = new HashMap<>();
    private final Set<String> undefinedMacros = new LinkedHashSet<>();
    private final List<SourceFileProperties> files = new ArrayList<>();
    
    public FolderImpl(String path, SourceFileProperties source) {
        this.path = path;
        this.language = source.getLanguageKind();
        update(source);
    }

    void update(SourceFileProperties source){
        files.add(source);
        userIncludes.addAll(source.getUserInludePaths());
        for (String currentPath : source.getUserInludePaths()) {
            userIncludes.add(DiscoveryUtils.convertRelativePathToAbsolute(source,currentPath));
        }
        userFiles.addAll(source.getUserInludeFiles());
        systemIncludes.addAll(source.getSystemInludePaths());
        userMacros.putAll(source.getUserMacros());
        undefinedMacros.addAll(source.getUndefinedMacros());
    }
    
    @Override
    public String getItemPath() {
        return path;
    }
    
    @Override
    public List<SourceFileProperties> getFiles() {
        return files;
    }
    
    @Override
    public List<String> getUserInludePaths() {
        return new ArrayList<>(userIncludes);
    }
    
    @Override
    public List<String> getUserInludeFiles() {
        return new ArrayList<>(userFiles);
    }
    
    @Override
    public List<String> getSystemInludePaths() {
        return new ArrayList<>(systemIncludes);
    }
    
    @Override
    public Map<String, String> getUserMacros() {
        return userMacros;
    }

    @Override
    public List<String> getUndefinedMacros() {
        return new ArrayList<>(undefinedMacros);
    }
    
    @Override
    public Map<String, String> getSystemMacros() {
        return null;
    }
    
    @Override
    public ItemProperties.LanguageKind getLanguageKind() {
        return language;
    }

    @Override
    public String getCompilerName() {
        return "";
    }

    @Override
    public LanguageStandard getLanguageStandard() {
        // now folder do not divided by language standards
        return LanguageStandard.Unknown;
    }
}
