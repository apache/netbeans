/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

package org.netbeans.modules.gradle.api;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleTask implements Serializable {

    private static final Pattern CAMLE_CASE_SPLITTER = Pattern.compile("[A-Z][0-9a-z]*");

    final String path;
    final String group;
    final String name;
    final String description;

    public GradleTask(String path, String group, String name, String description) {
        this.path = path;
        this.group = group;
        this.name = name;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group != null ? group : GradleBaseProject.PRIVATE_TASK_GROUP;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPrivate() {
        return group == null;
    }

    public boolean matches(String abbrev) {
        return abbrevMatch(abbrev, name);
    }

    static boolean abbrevMatch(String abbrev, String name) {
        if (abbrev.isEmpty()) {
            return true;
        }
        if (abbrev.length() > name.length()) {
            return false;
        }
        Matcher abbrevMatcher = CAMLE_CASE_SPLITTER.matcher(abbrev);
        Matcher nameMatcher = CAMLE_CASE_SPLITTER.matcher(name);
        while (abbrevMatcher.find()) {
            if (!nameMatcher.find() || !nameMatcher.group().startsWith(abbrevMatcher.group())) {
                return false;
            }
        }
        return true;
    }
}
