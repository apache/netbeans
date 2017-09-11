/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
