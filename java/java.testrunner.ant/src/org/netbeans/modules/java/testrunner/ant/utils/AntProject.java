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

package org.netbeans.modules.java.testrunner.ant.utils;

import java.io.File;
import org.apache.tools.ant.module.spi.AntEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Marian Petras
 */
public final class AntProject {

    /** {@code AntEvent} which serves for evaluation of Ant properties */
    private final AntEvent event;
    /** project's base directory. */
    private final File baseDir;
    
    /**
     * Constructor used only in tests.
     */
    AntProject() {
        event = null;
        baseDir = null;
    }

    /**
     */
    public AntProject(AntEvent event) {
        this.event = event;
        String baseDirName = getProperty("basedir");                    //NOI18N
        if (baseDirName == null) {
            baseDirName = ".";                                          //NOI18N
        }
        baseDir = FileUtil.normalizeFile(new File(baseDirName));
    }

    /**
     */
    public String getProperty(String propertyName) {
        return event.getProperty(propertyName);
    }

    /**
     */
    public String replaceProperties(String value) {
        return event.evaluate(value);
    }

    /**
     */
    public File resolveFile(String fileName) {
        return FileUtils.resolveFile(baseDir, fileName);
    }

    /**
     * Return the boolean equivalent of a string, which is considered
     * {@code true} if either {@code "on"}, {@code "true"},
     * or {@code "yes"} is found, ignoring case.
     *
     * @param  s  string to convert to a boolean value
     *
     * @return  {@code true} if the given string is {@code "on"}, {@code "true"}
     *          or {@code "yes"}; or {@ code false} otherwise.
     */
    public static boolean toBoolean(String s) {
        return ("on".equalsIgnoreCase(s)                                //NOI18N
                || "true".equalsIgnoreCase(s)                           //NOI18N
                || "yes".equalsIgnoreCase(s));                          //NOI18N
    }

}
