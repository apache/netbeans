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

package org.netbeans.modules.refactoring.java.ui;

import java.util.List;
import java.util.Set;
import javax.swing.KeyStroke;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.java.ui.instant.InstantOption;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public interface InstantRefactoringUI {

    public KeyStroke getKeyStroke();

    public void updateInput(CharSequence text);

    public RefactoringUI getRefactoringUI();

    public String getName();

    public AbstractRefactoring getRefactoring();

    public List<InstantOption> getOptions();
    
    public Set<MutablePositionRegion> getRegions();

    public Set<MutablePositionRegion> optionChanged(InstantOption instantOption);
    
}
