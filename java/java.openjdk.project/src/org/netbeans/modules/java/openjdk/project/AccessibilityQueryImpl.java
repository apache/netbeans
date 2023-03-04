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
package org.netbeans.modules.java.openjdk.project;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class AccessibilityQueryImpl implements AccessibilityQueryImplementation {

    private final ModuleDescription module;

    public AccessibilityQueryImpl(ModuleDescription module) {
        this.module = module;
    }

    @Override
    public Boolean isPubliclyAccessible(FileObject pkg) {
        ClassPath sourceCP = ClassPath.getClassPath(pkg, ClassPath.SOURCE);

        if (sourceCP == null)
            return null;

        String pack = sourceCP.getResourceName(pkg, '.', false);

        return module.exports.containsKey(pack) && module.exports.get(pack) == null;
    }

}
