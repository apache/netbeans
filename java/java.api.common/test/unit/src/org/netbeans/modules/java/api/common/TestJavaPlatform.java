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
package org.netbeans.modules.java.api.common;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public final class TestJavaPlatform extends J2SEPlatformImpl {
    
    public TestJavaPlatform(
            @NonNull final String name,
            @NonNull final FileObject installFolder,
            @NonNull final Map<String,String> sysProps) {
        super(
                name,
                PropertyUtils.getUsablePropertyName(name),
                Collections.singletonList(installFolder.toURL()),
                new HashMap<String, String>(),
                sysProps,
                Collections.emptyList(),
                Collections.emptyList());
    }
    
    @NonNull
    public static JavaPlatform createModularPlatform(
            @NonNull final File installFolder) {        
        return new TestJavaPlatform(
                installFolder.getName(),
                FileUtil.toFileObject(installFolder),
                createSysProperties(installFolder));
    }
    
    private static Map<String,String> createSysProperties(@NonNull final File installFolder) {
        final Map<String,String> res = new HashMap<>();
        res.put("java.specification.version","9");  //NOI18N
        res.put("java.version","9");    //NOI18N
        res.put("java.home", installFolder.getAbsolutePath());  //NOI18N
        return res;
    }
    
}
