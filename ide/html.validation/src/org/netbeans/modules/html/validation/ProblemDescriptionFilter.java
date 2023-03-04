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

package org.netbeans.modules.html.validation;

import org.netbeans.modules.html.editor.lib.api.ProblemDescription;

/**
 *
 * @author marekfukala
 */
public interface ProblemDescriptionFilter {

    public boolean accepts(ProblemDescription pd);

    public static class SeverityFilter implements ProblemDescriptionFilter {
        private int severity;

        public SeverityFilter(int severity) {
            this.severity = severity;
        }

        public boolean accepts(ProblemDescription pd) {
            return pd.getType() >= severity;
        }

    }

    public static class CombinedFilter implements ProblemDescriptionFilter {
        private ProblemDescriptionFilter f1, f2;

        public CombinedFilter(ProblemDescriptionFilter f1, ProblemDescriptionFilter f2) {
            this.f1 = f1;
            this.f2 = f2;
        }

        public boolean accepts(ProblemDescription pd) {
            return f1.accepts(pd) && f2.accepts(pd);
        }


    }
}
