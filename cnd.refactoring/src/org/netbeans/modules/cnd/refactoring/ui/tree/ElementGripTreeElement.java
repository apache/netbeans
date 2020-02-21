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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import javax.swing.Icon;
import org.netbeans.modules.cnd.refactoring.support.ElementGrip;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

/**
 * based on ElementGripTreeElement from java refactoring
 */
public class ElementGripTreeElement implements TreeElement {
    
    private ElementGrip element;
    private ElementGrip parent;

    public ElementGripTreeElement(ElementGrip element) {
        this.element = element;
        this.parent = element.getParent();
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            if (parent == null) {
                return TreeElementFactory.getTreeElement(element.getFileObject());
            }
            return TreeElementFactory.getTreeElement(parent);
        } else {
            return TreeElementFactory.getTreeElement(element.getFileObject());
        }
    }

    @Override
    public Icon getIcon() {
        return element.getIcon();
    }

    @Override
    public String getText(boolean isLogical) {
//        return CsmRefactoringUtils.htmlize(element.toString());
        return element.toString();
    }

    @Override
    public Object getUserObject() {
        return element;
    }
}
