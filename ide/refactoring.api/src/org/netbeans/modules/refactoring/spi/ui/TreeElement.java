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

import javax.swing.Icon;

/**
 * Tree Elements are shown in Refactoring Preview.
 * If you want to implement your own TreeElements, you
 * must register your TreeElementFactoryImplementation 
 * @see TreeElementFactoryImplementation
 * @see TreeElementFactory
 * @author Jan Becicka
 */
public interface TreeElement {
    /**
     * @param isLogical true if parent in lagical view is requested.
     * @return parent of this TreeElement
     */
    public TreeElement getParent(boolean isLogical);
    
    /**
     * @return icon for this TreeElement
     */
    public Icon getIcon();
    /**
     * @param isLogical true if logical description is requested
     * @return text of this TreeElement
     */
    public String getText(boolean isLogical);
    /**
     * @return corresponding object, usually RefactoringElement, FileObject
     */
    public Object getUserObject();
}

