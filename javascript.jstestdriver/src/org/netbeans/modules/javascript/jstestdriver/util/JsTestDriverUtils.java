/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.jstestdriver.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.jstestdriver.preferences.JsTestDriverPreferences;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Mutex;

public final class JsTestDriverUtils {

    static final Logger LOGGER = Logger.getLogger(JsTestDriverUtils.class.getName());

    private JsTestDriverUtils() {
    }

    public static File getJsTestDriverConfigDir(Project project) {
        // prefer directory for current karma config file
        String config = JsTestDriverPreferences.getConfig(project);
        if (config != null) {
            File configFile = new File(config);
            if (configFile.isFile()) {
                return configFile.getParentFile();
            }
        }
        // simply return project directory
        return FileUtil.toFile(project.getProjectDirectory());
    }

    @CheckForNull
    public static File findJsTestDriverConfig(File configDir) {
        assert configDir != null;
        File config = new File(configDir, "jsTestDriver.conf"); // NOI18N
        if (config.isFile()) {
            return config;
        }
        return null;
    }

    /**
     * Opens the file and optionally set cursor to the line. This action is always run in AWT thread.
     * @param file path of a file to open
     * @param line line of a file to set cursor to, {@code -1} if no specific line is needed
     * @param column column within a line of a file to set cursor to, {@code -1} if no specific column is needed
     */
    public static void openFile(File file, int line, int column) {
        assert file != null;

        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            LOGGER.log(Level.INFO, "FileObject not found for {0}", file);
            return;
        }

        DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.INFO, "DataObject not found for {0}", file);
            return;
        }

        if (line == -1) {
            // simply open file
            EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
            ec.open();
            return;
        }

        // open at specific line
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) {
            LOGGER.log(Level.INFO, "LineCookie not found for {0}", file);
            return;
        }
        Line.Set lineSet = lineCookie.getLineSet();
        try {
            final Line currentLine = lineSet.getCurrent(line - 1);
            final int col = column == -1 ? -1 : column - 1;
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    currentLine.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, col);
                }
            });
        } catch (IndexOutOfBoundsException exc) {
            LOGGER.log(Level.FINE, null, exc);
        }
    }

}
