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
package org.netbeans.modules.python.editor.refactoring.ui;

import javax.swing.Icon;
import org.netbeans.modules.python.editor.refactoring.PythonRefUtils;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.*;

public class RefactoringTreeElement implements TreeElement {
    RefactoringElement element;
    ElementGrip thisFeature;
    ElementGrip parent;

    RefactoringTreeElement(RefactoringElement element) {
        this.element = element;
        thisFeature = getFeature(element.getLookup().lookup(ElementGrip.class));
        parent = thisFeature.getParent();
        if (parent == null) {
            parent = thisFeature;
        }
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(parent);
        } else {
            return TreeElementFactory.getTreeElement(element.getParentFile());
        }
    }

    private ElementGrip getFeature(ElementGrip el) {
        return el;
    }

    @Override
    public Icon getIcon() {
        return thisFeature.getIcon();
    }

    @Override
    public String getText(boolean isLogical) {
        if (isLogical) {
            return PythonRefUtils.htmlize(thisFeature.toString()) + " ... " + element.getDisplayText();
        } else {
            return element.getDisplayText();
        }
    }

    @Override
    public Object getUserObject() {
        return element;
    }
}
