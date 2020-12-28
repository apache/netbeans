/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.api;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;

public final class FileLocation {
    private static final Pattern[] LOCATION_RECOGNIZER_PATTERNS = new Pattern[]{
        PythonLineConvertorFactory.PYTHON_STACKTRACE_PATTERN
        //        PythonLineConvertorFactory.RAILS_RECOGNIZER,
//        PythonLineConvertorFactory.RUBY_COMPILER_WIN_MY,
//        PythonLineConvertorFactory.RUBY_COMPILER,
//        PythonLineConvertorFactory.RUBY_COMPILER_WIN,
    };

    public final String file;
    public final int line;

    public FileLocation(String file, int line) {
        this.file = file;
        this.line = line;
    }

    // TODO: find a better place for this method (doesn't have anything to
    // do with external execution)
    public static FileLocation getLocation(String line) {

        final int fileGroup = 1;
        final int lineGroup = 2;

        if (line.length() > 400) {
            return null;
        }

        for (Pattern pattern : LOCATION_RECOGNIZER_PATTERNS) {
            Matcher match = pattern.matcher(line);

            if (match.matches()) {
                String file = null;
                int lineno = -1;

                if (fileGroup != -1) {
                    file = match.group(fileGroup);
                    // Make some adjustments - easier to do here than in the regular expression
                    // (See 109721 and 109724 for example)
                    if (file.startsWith("\"")) { // NOI18N
                        file = file.substring(1);
                    }
                    if (file.startsWith("./")) { // NOI18N
                        file = file.substring(2);
                    }
                    if (!(PythonLineConvertorFactory.EXT_RE.matcher(file).matches() || new File(file).isFile())) {
                        return null;
                    }
                }

                if (lineGroup != -1) {
                    String linenoStr = match.group(lineGroup);

                    try {
                        lineno = Integer.parseInt(linenoStr);
                    } catch (NumberFormatException nfe) {
                        Exceptions.printStackTrace(nfe);
                        lineno = 0;
                    }
                }

                return new FileLocation(file, lineno);
            }
        }

        return null;
    }

}
