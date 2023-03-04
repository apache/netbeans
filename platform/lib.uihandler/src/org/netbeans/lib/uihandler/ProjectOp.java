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

package org.netbeans.lib.uihandler;

import java.util.logging.LogRecord;

/** Represents an operation on the list of opened projects.
 *
 * @author Jaroslav Tulach
 * @since 1.7
 */
final class ProjectOp {
    private final String name;
    private final String type;
    private final boolean startup;
    private final int number;

    private ProjectOp(String name, String type, int number, boolean startup) {
        this.name = fixName(name, true);
        this.type = fixName(type, false);
        this.number = number;
        this.startup = startup;
    }

    private static String fixName(String name, boolean isDisplayName) {
        if (isDisplayName) {
            if (name.indexOf("Maven") >= 0) {
                return "Maven";
            }
            if (name.endsWith("Project")) {
                return name.substring(0, name.length() - 7);
            }
        }
        return name;
    }

    /** Human readable name of the project the operation happened on
     */
    public String getProjectDisplayName() {
        return name;
    }

    /** Fully qualified class name of the project.
     */
    public String getProjectType() {
        return type;
    }

    /** Number of projects of this type that has been added.
     * @return positive value if some projects were open, negative if some were closed
     */
    public int getDelta() {
        return number;
    }

    /** Is this report of projects being opened on startup?
     * @return true, if this is the list of projects reported on startup
     * @since 1.16
     */
    public boolean isStartup() {
        return startup;
    }

    /** Finds whether the record was an operation on projects.
     * @param rec the record to test
     * @return null if the record is of unknown format or data about the project operation
     */
    public static ProjectOp valueOf(LogRecord rec) {
        if ("UI_CLOSED_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt;
            try {
                cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            } catch (NumberFormatException numberFormatException) {
                return null;
            }
            return new ProjectOp(name, type, -cnt, false);
        }
        if ("UI_OPEN_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt;
            try {
                cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            } catch (NumberFormatException numberFormatException) {
                return null;
            }
            return new ProjectOp(name, type, cnt, false);
        }
        if ("UI_INIT_PROJECTS".equals(rec.getMessage())) {
            String type = getStringParam(rec, 0, "unknown"); // NOI18N
            String name = getStringParam(rec, 1, "unknown"); // NOI18N
            int cnt;
            try {
                cnt = Integer.parseInt(getStringParam(rec, 2, "0"));
            } catch (NumberFormatException numberFormatException) {
                return null;
            }
            return new ProjectOp(name, type, cnt, true);
        }
        return null;
    }
    
    private static String getStringParam(LogRecord rec, int index, String def) {
        if (rec == null) {
            return def;
        }
        Object[] params = rec.getParameters();
        if (params == null || params.length <= index) {
            return def;
        }
        if (params[index] instanceof String) {
            return (String)params[index];
        }
        return def;
    }
}
