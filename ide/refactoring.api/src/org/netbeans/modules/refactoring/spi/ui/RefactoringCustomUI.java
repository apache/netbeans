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
package org.netbeans.modules.refactoring.spi.ui;

import java.awt.Component;
import java.util.Collection;
import javax.swing.Icon;
import org.netbeans.modules.refactoring.api.RefactoringElement;

/**
 * Backward compatible extension to RefactoringUI providing custom preview panel.
 * If implementation of RefactoringUI also implement RefactoringCustomUI, default 
 * Refactoring Preview appears with new "Custom View" toggle button, which
 * shows custom Component
 * 
 * this interface is just prototype and might be subject of change.
 * 
 * @author Jan Becicka
 */
public interface RefactoringCustomUI {
    /**
     * @return component to show
     */ 
    Component getCustomComponent(Collection<RefactoringElement> elements);
    /**
     * @return icon for toggle button
     */
    Icon getCustomIcon();
    /**
     * tooltip for toggle button
     */ 
    String getCustomToolTip();
}
