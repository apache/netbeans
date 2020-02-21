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

package org.netbeans.modules.cnd.remote.mapper;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 *
 */
/*package*/final class HostMappingProviderSamba implements HostMappingProvider {

    @Override
    public Map<String, String> findMappings(ExecutionEnvironment execEnv, ExecutionEnvironment otherExecEnv) {
        Map<String, String> mappings = new HashMap<>();
        ProcessUtils.ExitStatus exit = ProcessUtils.execute(execEnv, "cat", "/etc/sfw/smb.conf"); //NOI18N
        if (exit.isOK()) {
            mappings.putAll(parseOutput(new StringReader(exit.getOutputString())));
        }
        return mappings;
    }

    @Override
    public boolean isApplicable(PlatformInfo hostPlatform, PlatformInfo otherPlatform) {
        return otherPlatform.isWindows() && hostPlatform.isUnix();
    }

    private static final String GLOBAL = "global"; //NOI18N
    private static final String PATH = "path"; //NOI18N

    static Map<String, String> parseOutput(Reader outputReader) {
        Map<String, String> mappings = new HashMap<>();
        SimpleConfigParser parser = new SimpleConfigParser();
        parser.parse(outputReader);
        for (String name : parser.getSections()) {
            if (!GLOBAL.equals(name)) {
                String path = parser.getAttributes(name).get(PATH); //TODO: investigate case-sensitivity
                if (path != null) {
                    mappings.put(name, path);
                }
            }
        }
        return mappings;
    }
}
