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

import com.sun.source.tree.Tree;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringTreeElement implements TreeElement { 
    
    private RefactoringElement refactoringElement;
    private ElementGrip thisFeature;
    private ElementGrip parent;
    private Icon icon;
    
    RefactoringTreeElement(RefactoringElement element) {
        final Lookup lookup = element.getLookup();
        this.refactoringElement = element;
        thisFeature = getFeature(lookup.lookup(ElementGrip.class));
        parent = thisFeature;
        icon = lookup.lookup(Icon.class);
    }
    
    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(parent);
        } else {
            return TreeElementFactory.getTreeElement(refactoringElement.getParentFile());
        }
    }
    
    private ElementGrip getFeature(ElementGrip el) {
        if (el.getKind() == Tree.Kind.VARIABLE) {
          return el.getParent();  
        }
        return el;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return refactoringElement.getDisplayText();
    }

    @Override
    public Object getUserObject() {
        return refactoringElement;
    }
}
