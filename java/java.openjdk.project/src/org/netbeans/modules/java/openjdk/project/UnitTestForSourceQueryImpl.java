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
package org.netbeans.modules.java.openjdk.project;

import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.openjdk.project.JDKProject;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private final JDKProject prj;

    public UnitTestForSourceQueryImpl(JDKProject prj) {
        this.prj = prj;
    }

    @Override
    public URL[] findUnitTests(FileObject source) {
        SourceGroup[] groups = ProjectUtils.getSources(prj)
                                           .getSourceGroups(SourcesImpl.SOURCES_TYPE_JDK_PROJECT_TESTS);
        return notInReturn(source, groups);
    }

    @Override
    public URL[] findSources(FileObject unitTest) {
        SourceGroup[] groups = ProjectUtils.getSources(prj)
                                           .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return notInReturn(unitTest, groups);
    }

    private URL[] notInReturn(FileObject file, SourceGroup[] groups) {
        return Arrays.stream(groups)
                     .map(sg -> sg.getRootFolder())
                     .filter(root -> FileUtil.isParentOf(root, file) || root == file)
                     .map(f -> f.toURL())
                     .toArray(s -> new URL[s]);
    }
}
