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
package org.netbeans.modules.testng.spi;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.filesystems.FileUtil;
import org.testng.xml.LaunchSuite;
import org.testng.xml.SuiteGenerator;

/**
 *
 * @author lukas
 */
public final class XMLSuiteSupport {

    private XMLSuiteSupport() {
    }
    
    public static File createSuiteforMethod(File targetFolder, String projectName, String pkgName, String className, String methodName) {
        if (!targetFolder.isDirectory()) {
            throw new IllegalArgumentException(targetFolder.getAbsolutePath() + " is not a directory"); //NOI18N
        }
        Map<String, Collection<String>> classes = new HashMap<String, Collection<String>>();
        Set<String> methods = null;
        if (methodName != null) {
            methods = new HashSet<String>();
            methods.add(methodName);
        }
        pkgName = pkgName.trim();
        classes.put("".equals(pkgName) ? className : pkgName + "." + className, methods); //NOI18N
        LaunchSuite suite = SuiteGenerator.createSuite(projectName, null, classes, null, null, null, 1);
        File f = suite.save(targetFolder);
        FileUtil.refreshFor(targetFolder);
        return f;
    }
}
