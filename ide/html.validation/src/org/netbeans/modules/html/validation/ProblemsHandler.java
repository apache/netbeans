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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;

/**
 *
 * @author marekfukala
 */
public class ProblemsHandler {

    private List<ProblemDescription> problems;
    private boolean finished;

    public void startProblems() {
        problems = new ArrayList<ProblemDescription>();
        finished = false;
    }

    public void endProblems() {
        finished = true;
    }

    public void addProblem(ProblemDescription pd) {
        if(finished) {
            throw new IllegalStateException("Already finished session"); //NOI18N
        }
        problems.add(pd);
    }

    public List<ProblemDescription> getProblems() {
        if(!finished) {
            throw new IllegalStateException("Session not finished yet, probably someone forgot to call endProblems()?!?!"); //NOI18N
        }

        return problems;
    }

}
