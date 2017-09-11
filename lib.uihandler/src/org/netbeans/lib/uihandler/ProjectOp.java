/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
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
