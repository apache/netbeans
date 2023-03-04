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

package org.netbeans.modules.refactoring.java.ui.tree;

import javax.swing.Icon;
import org.netbeans.modules.refactoring.java.ui.UIUtilities;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;

/**
 *
 * @author Jan Becicka
 */
public class ElementGripTreeElement implements TreeElement {
    
    private ElementGrip element;
    /** Creates a new instance of JavaTreeElement */
    public ElementGripTreeElement(ElementGrip element) {
        this.element = element;
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        ElementGrip enclosing = element.getParent();
        if (isLogical) {
            if (enclosing == null) {
                return TreeElementFactory.getTreeElement(element.getFileObject());
            }
            return TreeElementFactory.getTreeElement(enclosing);
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
        return UIUtilities.htmlize(element.toString());
    }

    @Override
    public Object getUserObject() {
        return element;
    }
}
