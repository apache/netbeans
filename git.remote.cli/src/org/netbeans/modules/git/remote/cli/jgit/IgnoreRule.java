/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli.jgit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
public class IgnoreRule {

    private final String pattern;
    private Pattern compiled;
    private final String noNegationPattern;
    private final boolean isNameOnly;
    private final boolean isResult;
    private final boolean isDirOnly;

    
    public IgnoreRule (String originalPattern) {
        //super(pattern.trim());
        this.pattern = originalPattern;
        String trimmedPattern = originalPattern.trim();
        isNameOnly = !trimmedPattern.contains("/"); //NOI18N
        isDirOnly = trimmedPattern.endsWith("/"); //NOI18N
        if (trimmedPattern.startsWith("!")) { //NOI18N
            isResult = false;
            noNegationPattern = trimmedPattern.substring(1);
        } else {
            isResult = true;
            noNegationPattern = null;
        }
    }

    public String getPattern (boolean preprocess) {
        String retval = pattern;
        if (preprocess) {
            if (noNegationPattern != null) {
                retval = noNegationPattern;
            }
            if (!getNameOnly() && !retval.startsWith("/")) { //NOI18N
                retval = "/" + retval;
            }
        }
        return retval;
    }

    public boolean isMatch(String target, boolean isDirectory) {
        String trimmed = pattern.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) { //NOI18N
            // this is a comment or an empty line
            return false;
        } else {
            if (!isResult) {
                trimmed = noNegationPattern;
            }
            //TODO use isDirectory
            if (compiled == null) {
                trimmed = trimmed.replace(".", "\\.").replace("+", "\\+").replace("*", ".*").replace("?", "\\?");
                try {
                    compiled = Pattern.compile(trimmed);
                } catch (PatternSyntaxException ex) {
                    ex.printStackTrace(System.err);
                }
            }
            if (compiled != null) {
                Matcher matcher = compiled.matcher(target);
                if (matcher.find()) {
                    if (isResult) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (isResult) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean getNameOnly() {
        return isNameOnly;
    }

    public boolean getResult() {
        return isResult;
    }

}
