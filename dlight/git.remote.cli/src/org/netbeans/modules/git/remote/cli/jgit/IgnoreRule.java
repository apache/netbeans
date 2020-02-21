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
