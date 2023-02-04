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

package org.netbeans.modules.javascript.karma.browsers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Pair;


public abstract class Browser {

    protected abstract List<String> getIdentifiers();

    protected abstract Pattern getOutputFileLinePattern();

    public Pair<String, Integer> getOutputFileLine(String line) {
        if (!isOutputFileLine(line)) {
            return null;
        }
        Matcher matcher = getOutputFileLinePattern().matcher(line);
        if (matcher.find()) {
            return Pair.of(matcher.group("FILE"), Integer.parseInt(matcher.group("LINE"))); // NOI18N
        }
        return null;
    }

    private boolean isOutputFileLine(String line) {
        return line.contains(".js:"); // NOI18N
    }

}
