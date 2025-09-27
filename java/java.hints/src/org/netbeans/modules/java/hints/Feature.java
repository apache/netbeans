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

package org.netbeans.modules.java.hints;

import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.CompilationInfo;

/**
 * Language feature or incubating API lookup utility.
 */
public enum Feature {

    // somewhat similar to com.sun.tools.javac.code.Source.Feature

    /// https://openjdk.org/jeps/361
    SWITCH_EXPRESSIONS(12, 14),

    /// https://openjdk.org/jeps/378
    TEXT_BLOCK(13, 15),

    /// https://openjdk.org/jeps/394
    INSTANCEOF_PATTERN(14, 16),

    /// https://openjdk.org/jeps/441
    SWITCH_PATTERN(17, 21),

    /// https://openjdk.org/jeps/444
    VIRTUAL_THREADS(19, 21),

    /// https://openjdk.org/jeps/440
    RECORD_PATTERN(19, 21);

    /// preview begin
    public final int PREVIEW;

    /// feature release
    public final int RELEASE;

    private Feature(int preview, int release) {
        this.PREVIEW = preview;
        this.RELEASE = release;
    }

    public boolean isEnabled(CompilationInfo info) {
        int langLevel = info.getSourceVersion().ordinal();
        return isReleased(langLevel)
            || (langLevel >= PREVIEW && CompilerOptionsQuery.getOptions(info.getFileObject()).getArguments().contains("--enable-preview"));
    }

    public boolean isReleased(int langLevel) {
        return langLevel >= RELEASE;
    }

}
