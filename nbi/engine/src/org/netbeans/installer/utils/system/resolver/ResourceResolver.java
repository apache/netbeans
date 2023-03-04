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
package org.netbeans.installer.utils.system.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class ResourceResolver implements StringResolver {

    public String resolve(String string, ClassLoader loader) {
        String parsed = string;
        Matcher matcher = Pattern.compile("(?<!\\\\)\\$R\\{(.*?)(;(.*)?)?}").matcher(parsed);
        while (matcher.find()) {
            String path = matcher.group(1);
            String charset = matcher.group(3);
            if (charset != null) {
                charset = charset.trim();
                if (charset.equals(StringUtils.EMPTY_STRING)) {
                    charset = null;
                }
            }
            InputStream inputStream = null;
            try {
                inputStream = ResourceUtils.getResource(path, loader);
                if(inputStream !=null) {
                    parsed = parsed.replace(matcher.group(), StringUtils.readStream(inputStream, charset));
                } else {
                    LogManager.log("Cannot find resource " + path + " using classloader " + loader);
                    parsed = null;
                    break;
                }
            } catch (IOException e) {
                ErrorManager.notifyDebug(StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ErrorManager.notifyDebug("Cannot close input stream after reading resource: " + matcher.group(), e);
                    }
                }
            }
        }
        return parsed;
    }
}
