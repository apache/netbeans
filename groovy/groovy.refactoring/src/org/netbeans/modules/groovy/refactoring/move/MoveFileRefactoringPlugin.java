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

package org.netbeans.modules.groovy.refactoring.move;

import org.netbeans.modules.groovy.refactoring.GroovyRefactoringPlugin;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public class MoveFileRefactoringPlugin extends GroovyRefactoringPlugin {

    public MoveFileRefactoringPlugin(FileObject fileObject, RefactoringElement element, AbstractRefactoring refactoring) {
        super(element, fileObject, refactoring);
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        // TODO: Move class
        return null;
    }
}
