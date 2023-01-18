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
package org.netbeans.installer.utils.system.resolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;

/**
 *
 * @author Dmitry Lipin
 */
public class EnvironmentVariableResolver implements StringResolver {

    private static final Pattern p = Pattern.compile("(?<!\\\\)\\$E\\{(.*?)\\}");

    public String resolve(String string, ClassLoader loader) {
        String parsed = string;
        Matcher matcher = p.matcher(parsed);
        while (matcher.find()) {
            try {
                String name = matcher.group(1);
                String value = SystemUtils.getEnvironmentVariable(name);
                if(value!=null) {
                    parsed = parsed.replace(matcher.group(), value);
                }
            } catch (NativeException e) {
                ErrorManager.notifyDebug(StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            }
        }
        return parsed;
    }
}
