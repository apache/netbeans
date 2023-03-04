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
package org.netbeans.modules.java.source.tasklist;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public abstract class CompilerSettings {

    protected CompilerSettings() {
    }
        
    public static final String ENABLE_LINT = "enable_lint";
    
    public static String getCommandLine(ClasspathInfo cpInfo) {
        ClassPath sourceCP = cpInfo.getClassPath(PathKind.SOURCE);
        FileObject[] roots = sourceCP != null ? sourceCP.getRoots() : new FileObject[0];
        FileObject file = roots.length > 0 ? roots[0] : null;
        
        for (CompilerSettings cs : Lookup.getDefault().lookupAll(CompilerSettings.class)) {
            String cl = cs.buildCommandLine(file);
            
            if (cl != null) return cl;
        }
        
        return "";
    }
    
    protected abstract String buildCommandLine(@NullAllowed FileObject file);
}
