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

package org.netbeans.modules.html.editor.refactoring.api;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class ExtractInlinedStyleRefactoring extends AbstractRefactoring {

    public enum Mode {
        refactorToExistingEmbeddedSection,
        refactorToNewEmbeddedSection,
        refactorToReferedExternalSheet,
        refactorToExistingExternalSheet,
        refactorToNewExternalSheet,
    }

    private Mode mode;
    private SelectorType selectorType;
    private OffsetRange existingEmbeddedCssSection;
    private FileObject externalSheet;

    public ExtractInlinedStyleRefactoring(Lookup refactoringSource) {
        super(refactoringSource);
    }

    public OffsetRange getExistingEmbeddedCssSection() {
        return existingEmbeddedCssSection;
    }

    public void setExistingEmbeddedCssSection(OffsetRange existingEmbeddedCssSection) {
        this.existingEmbeddedCssSection = existingEmbeddedCssSection;
    }

    public FileObject getExternalSheet() {
        return externalSheet;
    }

    public void setExternalSheet(FileObject externalSheet) {
        this.externalSheet = externalSheet;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public void setSelectorType(SelectorType selectorType) {
        this.selectorType = selectorType;
    }

}
