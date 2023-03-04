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
package org.netbeans.modules.css.prep.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;

public class VersionOutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory2 {

    private final Pattern versionPattern;

    volatile String version;


    public VersionOutputProcessorFactory(String versionPattern) {
        assert versionPattern != null;
        this.versionPattern = Pattern.compile(versionPattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
        return InputProcessors.bridge(new LineProcessor() {

            @Override
            public void processLine(String line) {
                assert version == null : version + " :: " + line;
                version = parseVersion(line);
            }

            @Override
            public void reset() {
            }

            @Override
            public void close() {
            }

        });
    }

    public String getVersion() {
        return version;
    }

    public String parseVersion(String line) {
        Matcher matcher = versionPattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
