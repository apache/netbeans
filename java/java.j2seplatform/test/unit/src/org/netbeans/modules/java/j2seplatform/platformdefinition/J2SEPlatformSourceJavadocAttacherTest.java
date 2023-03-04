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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.util.Arrays;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.AbstractJ2SEAttacherTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author sdedic
 */
public class J2SEPlatformSourceJavadocAttacherTest extends AbstractJ2SEAttacherTestBase {

    public J2SEPlatformSourceJavadocAttacherTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        JavaPlatform p = JavaPlatform.getDefault();
        classesRootURL = Arrays.stream(p.getBootstrapLibraries().getRoots()).map(
                f -> URLMapper.findURL(f, URLMapper.INTERNAL)).
                filter(
                        u -> !u.toString().contains("nb-javac")
                ).findAny().get();
    }
}
