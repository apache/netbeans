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

package org.netbeans.modules.javascript.karma.browsers;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

final class Firefox extends Browser {

    // e.g.: @/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js:16:32
    static final Pattern OUTPUT_FILE_LINE_PATTERN = Pattern.compile("@(?<FILE>.+?):(?<LINE>\\d+)(:\\d+)?$"); // NOI18N

    @Override
    protected List<String> getIdentifiers() {
        return Collections.singletonList("Firefox"); // NOI18N
    }

    @Override
    protected Pattern getOutputFileLinePattern() {
        return OUTPUT_FILE_LINE_PATTERN;
    }

}
