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

package org.netbeans.libs.git.jgit;

/**
 *
 * @author ondra
 */
public class IgnoreRule extends org.eclipse.jgit.ignore.FastIgnoreRule {

    private final String pattern;
    private final String noNegationPattern;
    
    public IgnoreRule (String pattern) {
        super(pattern.strip());
        this.pattern = pattern;
        String neg = pattern.strip();
        this.noNegationPattern = neg.startsWith("!") ? neg.substring(1) : null;
    }

    public String getPattern (boolean preprocess) {
        String retval = pattern;
        if (preprocess) {
            if (noNegationPattern != null) {
                retval = noNegationPattern;
            }
            if (!getNameOnly() && !retval.startsWith("/")) {
                retval = "/" + retval;
            }
        }
        return retval;
    }

    @Override
    public boolean isMatch(String target, boolean isDirectory) {
        String trimmed = pattern.strip();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            // this is a comment or an empty line
            return false;
        } else {
            return super.isMatch(target, isDirectory);
        }
    }

}
