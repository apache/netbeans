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

/**
 *
 * @author Dmitry Lipin
 */
public class FieldResolver implements StringResolver {

    private static final Pattern p = Pattern.compile("(?<!\\\\)\\$F\\{((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+[a-zA-Z_][a-zA-Z_0-9]*)\\.([a-zA-Z_][a-zA-Z_0-9]*)\\}");

    public String resolve(String string, ClassLoader loader) {
        Matcher matcher;
        String parsed = string;
        matcher = p.matcher(parsed);
        while (matcher.find()) {
            String classname = matcher.group(1);
            String fieldname = matcher.group(2);

            try {
                Object object = loader.loadClass(classname).getField(fieldname).get(null);
                if (object != null) {
                    String value = object.toString();
                    
                    parsed = parsed.replace(matcher.group(), value);
                }
            } catch (IllegalArgumentException e) {
                ErrorManager.notifyDebug(
                        StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            } catch (SecurityException e) {
                ErrorManager.notifyDebug(
                        StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            } catch (ClassNotFoundException e) {
                ErrorManager.notifyDebug(
                        StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            } catch (IllegalAccessException e) {
                ErrorManager.notifyDebug(
                        StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            } catch (NoSuchFieldException e) {
                ErrorManager.notifyDebug(
                        StringUtils.format(
                        ERROR_CANNOT_PARSE_PATTERN, matcher.group()), e);
            }
        }
        return parsed;
    }
}
