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
package org.netbeans.modules.gradle.execute;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ServiceProvider(service = JavaRuntimeManager.class)
public class DefaultJavaRuntimeManager implements JavaRuntimeManager {

    private final Map<String, JavaRuntime> defaultRuntimes;
    
    @Messages({
        "# {0} - The version of the Java Runtime",
        "DEFAULT_JAVA_RUNTIME_NAME=Java {0} (Default)"
    })
    public DefaultJavaRuntimeManager() {
        File javaHome = new File(System.getProperty("java.home")); //NOI18N
        int javaVersion = Runtime.version().feature();
        JavaRuntime defaultRuntime = JavaRuntimeManager.createJavaRuntime(DEFAULT_RUNTIME_ID, Bundle.DEFAULT_JAVA_RUNTIME_NAME(javaVersion), javaHome);
        
        defaultRuntimes = Collections.singletonMap(DEFAULT_RUNTIME_ID, defaultRuntime);
    }

    @Override
    public Map<String, JavaRuntime> getAvailableRuntimes() {
        return defaultRuntimes;
    }

}
