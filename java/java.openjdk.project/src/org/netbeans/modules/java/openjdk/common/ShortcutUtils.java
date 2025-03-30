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
package org.netbeans.modules.java.openjdk.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.project.Project;

/**
 *
 * @author lahvac
 */
public class ShortcutUtils {

    private static final Logger LOG = Logger.getLogger(ShortcutUtils.class.getName());
    private static ShortcutUtils INSTANCE;

    public static synchronized ShortcutUtils getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new ShortcutUtils();
        }

        return INSTANCE;
    }

    private final ResourceBundle data;

    private ShortcutUtils() {
        ResourceBundle data;
        try (InputStream in = ShortcutUtils.class.getResourceAsStream("shortcut.properties")) {
            data = new PropertyResourceBundle(in);
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
            data = new ResourceBundle() {
                @Override protected Object handleGetObject(String key) {
                    return null;
                }
                @Override public Enumeration<String> getKeys() {
                    return Collections.emptyEnumeration();
                }
            };
        }
        this.data = data;
    }

    private static final Set<String> LANGTOOLS_MODULES =
            new HashSet<>(Arrays.asList("java.compiler", "jdk.compiler",
                                        "jdk.javadoc", "jdk.jdeps", "jdk.jshell"));
    public String inferLegacyRepository(Project prj) {
        if (LANGTOOLS_MODULES.contains(prj.getProjectDirectory().getNameExt()))
            return "langtools";
        return "unknown";
    }

    public boolean shouldUseCustomBuild(String repoName, String pathInRepo) {
        return matches(repoName, pathInRepo, "project");
    }

    public boolean shouldUseCustomTest(String repoName, String pathInRepo) {
        return matches(repoName, pathInRepo, "test");
    }

    private boolean matches(String repoName, String pathInRepo, String key) {
        if (pathInRepo == null) {
            return false;
        }

        String include = null;
        String exclude = null;
        try {
            include = data.getString(repoName + "_include_" + key);
            exclude = data.getString(repoName + "_exclude_" + key);
        } catch (MissingResourceException ex) {
            LOG.log(Level.FINE, null, ex);
        }

        if (include == null || exclude == null)
            return false;

        try {
            return  Pattern.matches(include, pathInRepo) &&
                   !Pattern.matches(exclude, pathInRepo);
        } catch (PatternSyntaxException ex) {
            LOG.log(Level.FINE, null, ex);
            return false;
        }
    }
}
