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

package org.netbeans.modules.gradle.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Laszlo Kishalmi
 */
@SuppressWarnings("rawtypes")
public interface ProjectInfoExtractor {

    Result fallback(GradleFiles files);

    Result extract(Map<String, Object> props, final Map<Class, Object> otherInfo);

    interface Result {

        static Result NONE = new Result() {

            @Override
            public Set<?> getExtract() {
                return Set.of();
            }

            @Override
            public Set<String> getProblems() {
                return Set.of();
            }
        };

        Set<?> getExtract();

        Set<String> getProblems();
    }

    public static class DefaultResult implements Result {

        final Object extract;
        final Set<String> problems;

        public DefaultResult(Object extract, Set<String> problems) {
            this.extract = extract;
            this.problems = Collections.unmodifiableSet(new LinkedHashSet<>(problems));
        }

        public DefaultResult(Object extract, String... problems) {
            this.extract = extract;
            this.problems = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(problems)));
        }

        @Override
        public Set<?> getExtract() {
            return Set.of(extract);
        }

        @Override
        public Set<String> getProblems() {
            return problems;
        }

    }
}
