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

package org.netbeans.modules.maven;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;

public class TemplateAttrProviderTest extends NbTestCase {

    public TemplateAttrProviderTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testAttributes() throws Exception {
        System.setProperty("test.load.sync", "true");
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml",
"<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd'>\n" +
"    <modelVersion>4.0.0</modelVersion>\n" +
"    <groupId>testgrp</groupId>\n" +
"    <artifactId>testart</artifactId>\n" +
"    <version>1.0</version>\n" +
"    <name>Test</name>\n" +
"    <licenses>\n" +
"        <license>\n" +
"            <name>Apache 2.0</name>\n" +
"            <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>\n" +
"        </license>\n" +
"    </licenses>\n" +
"    <organization>\n" +
"        <name>Yoyodyne Corp.</name>\n" +
"    </organization>\n" +
"</project>\n" +
"");
        String attrs = ProjectManager.getDefault().findProject(d).getLookup().lookup(CreateFromTemplateAttributesProvider.class).attributesFor(null, DataFolder.findFolder(d), null).toString();
        assertTrue("Expected {displayName=Test, license=apache20, name=testart, organization=Yoyodyne Corp.} but was " + attrs,
                attrs.contains("displayName=Test") && attrs.contains("license=apache20") && attrs.contains("name=testart") && attrs.contains("organization=Yoyodyne Corp."));
    }

}
