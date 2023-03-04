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

package org.netbeans.modules.web.beans.navigation;

import java.util.Set;

import org.netbeans.api.java.source.ElementHandle;
import org.openide.filesystems.FileObject;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import org.netbeans.api.java.source.ui.ElementJavadoc;

/**
 * The interface representing Java elements in hierarchy and members pop up windows.
 * 
 * Copy of JavaElement at java.navigation
 * 
 * @author ads
 */
public interface JavaElement {
    String getName();    
    Set<Modifier> getModifiers();
    ElementKind getElementKind();
    String getLabel();
    String getFQNLabel();
    String getTooltip();
    Icon getIcon();
    boolean isDisabled();
    ElementJavadoc getJavaDoc();
    void gotoElement();
    FileObject getFileObject();
    ElementHandle<?> getElementHandle();
}
