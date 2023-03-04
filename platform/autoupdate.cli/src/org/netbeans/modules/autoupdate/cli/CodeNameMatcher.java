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

package org.netbeans.modules.autoupdate.cli;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle;

final class CodeNameMatcher {
    private final Pattern[] patterns;
    private CodeNameMatcher(Pattern[] arr) {
        this.patterns = arr;
    }

    @NbBundle.Messages({
        "# {0} - regexp",
        "MSG_CantCompileRegex=Invalid regular expession ''{0}''"
    })
    static CodeNameMatcher create(Env env, String[] pattern) {
        Pattern[] arr = new Pattern[pattern.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                arr[i] = Pattern.compile(pattern[i]);
            } catch (PatternSyntaxException e) {
                env.getErrorStream().println(Bundle.MSG_CantCompileRegex(pattern[i]));
            }
        }
        return new CodeNameMatcher(arr);
    }

    boolean matches(String txt) {
        for (Pattern p : patterns) {
            if (p == null) {
                continue;
            }
            if (p.matcher(txt).matches()) {
                return true;
            }
        }
        return false;
    }

    boolean isEmpty() {
        return patterns.length == 0;
    }

    @Override
    public String toString() {
        return Arrays.toString(patterns);
    }


}
