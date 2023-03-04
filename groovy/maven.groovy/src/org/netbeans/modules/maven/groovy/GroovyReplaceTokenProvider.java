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

package org.netbeans.modules.maven.groovy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * @see org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider
 */
@ProjectServiceProvider(service={ReplaceTokenProvider.class, ActionConvertor.class}, projectType="org-netbeans-modules-maven")
public class GroovyReplaceTokenProvider implements ReplaceTokenProvider, ActionConvertor {

    private static final String CLASSNAME = "className";                    //NOI18N
    private static final String CLASSNAME_EXT = "classNameWithExtension";   //NOI18N
    private static final String PACK_CLASSNAME = "packageClassName";        //NOI18N
    private static final String CLASSPATHSCOPE = "classPathScope";          //NOI18N

    private final Project project;

    public GroovyReplaceTokenProvider(Project project) {
        this.project = project;
    }

    @Override
    public Map<String,String> createReplacements(String action, Lookup lookup) {
        FileObject fo = lookup.lookup(FileObject.class);
        if (fo == null) {
            SingleMethod m = lookup.lookup(SingleMethod.class);
            if (m != null) {
                fo = m.getFile();
            }
        }
        if (isGroovyFile(fo)) {
            for (SourceGroup group : ProjectUtils.getSources(project).getSourceGroups(GroovySourcesImpl.TYPE_GROOVY)) {
                String relPath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                if (relPath != null) {
                    Map<String,String> replaceMap = new HashMap<String,String>();
                    replaceMap.put(CLASSNAME_EXT, fo.getNameExt());
                    replaceMap.put(CLASSNAME, fo.getName());
                    String pack = FileUtil.getRelativePath(group.getRootFolder(), fo.getParent());
                    if (pack != null) { //#141175
                        replaceMap.put(PACK_CLASSNAME, (pack + (pack.length() > 0 ? "." : "") + fo.getName()).replace('/', '.')); //NOI18N
                    } else {
                        replaceMap.put(PACK_CLASSNAME, fo.getName());
                    }
                    replaceMap.put(CLASSPATHSCOPE, group.getName().equals(GroovySourcesImpl.NAME_GROOVYTESTSOURCE) ? "test" : "runtime"); //NOI18N
                    return replaceMap;
                }
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public String convert(String action, Lookup lookup) {
        if (ActionProvider.COMMAND_RUN_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_SINGLE.equals(action)) {

            final FileObject fo = lookup.lookup(FileObject.class);
            if (isGroovyFile(fo)) {
                if (isInTestFolder(fo)) {
                    return null;
                }

                //TODO this only applies to groovy files with main() method.
                // we should have a way to execute any groovy script? how?
                // running groovy tests is another specialized usecase.
                return action + ".main"; //NOI18N
            }
        }
        return null;
    }

    private boolean isInTestFolder(FileObject file) {
        if (file.getPath().indexOf("/test/groovy") != -1) { //NOI18N
            return true;
        }
        return false;
    }

    private boolean isGroovyFile(FileObject file) {
        if (file != null && "text/x-groovy".equals(file.getMIMEType())) { //NOI18N
            return true;
        }
        return false;
    }
}
