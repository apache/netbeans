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
package org.netbeans.modules.web.refactoring;

import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;

/**
 * A refactoring plugin for web refactorings.
 * 
 * @author Erno Mononen
 */
public class WebRefactoringPlugin implements RefactoringPlugin{

    private final List<WebRefactoring> refactorings;

    public WebRefactoringPlugin(List<WebRefactoring> refactorings) {
        this.refactorings = refactorings;
    }

    public Problem preCheck() {
        Problem result = null;
        for (WebRefactoring each : refactorings){
            result = RefactoringUtil.addToEnd(each.preCheck(), result);
        }
        return result;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
        return;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Problem result = null;
        for (WebRefactoring each : refactorings){
            result = RefactoringUtil.addToEnd(each.prepare(refactoringElements), result);
        }
        return result;
    }
}
