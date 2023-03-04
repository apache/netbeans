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
package org.netbeans.modules.refactoring.java;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.FiltersManager;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.refactoring.java.Bundle.*;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.openide.text.Line;
import org.openide.text.NbDocument;

public class WhereUsedBinaryElement extends SimpleRefactoringElementImplementation implements FiltersManager.Filterable {
    private final String htmlText;
    private final String elementText;
    private final FileObject fo;
    private final boolean fromTest;
    private final boolean fromPlatform;

    private WhereUsedBinaryElement(FileObject fo, boolean inTest, boolean inPlatform) {
        final String name = fo.getNameExt(); //NOI18N
        this.htmlText = "<b>" + name + "</b>"; //NOI18N
        this.elementText = name;
        this.fromTest = inTest;
        this.fromPlatform = inPlatform;
        this.fo = fo;
    }
    
    @Override
    public String getDisplayText() {
        return htmlText;
    }

    @Override
    public Lookup getLookup() {
        Icon icon = null;
        try {
            ImageIcon imageIcon = new ImageIcon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            Boolean inTestFile = ElementGripFactory.getDefault().inTestFile(fo);
            if (Boolean.TRUE == inTestFile) {
                Image mergeImages = ImageUtilities.mergeImages(imageIcon.getImage(),
                        ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_test.png", false).getImage(), 4, 4);
                imageIcon = new ImageIcon(mergeImages);
            }
            icon = imageIcon;
        } catch (DataObjectNotFoundException ex) {
            // ignore
        }
        return icon != null ? Lookups.fixed(icon, fo.getParent()) : Lookups.fixed(fo.getParent());
    }

    @Override
    public PositionBounds getPosition() {
        return null;
    }

    @Override
    public String getText() {
        return elementText;
    }

    @Override
    public void performChange() {
    }

    @Override
    public void openInEditor() {
        if(fo == null || !fo.isValid()) {
             StatusDisplayer.getDefault().setStatusText(WARN_ElementNotFound());
        } else {
            try {
                DataObject od = DataObject.find(fo);
                NbDocument.openDocument(od, 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            } catch (DataObjectNotFoundException ex) {
                // ignore
            }
        }
    }

    @Override
    public FileObject getParentFile() {
        return fo.getParent();
    }

    public static WhereUsedBinaryElement create(FileObject fo, boolean fromTest, boolean fromPlatform) {
        return new WhereUsedBinaryElement(fo, fromTest, fromPlatform);
    }

    @Override
    public boolean filter(FiltersManager manager) {
        boolean show = true;
        
        if(JavaWhereUsedQueryPlugin.DEPENDENCIES) {
            if(fromPlatform) {
                show = show && manager.isSelected(JavaWhereUsedFilters.PLATFORM.getKey());
            } else { // inDependency
                show = show && manager.isSelected(JavaWhereUsedFilters.DEPENDENCY.getKey());
            }
        }
        
        if(fromTest) {
            show = show && manager.isSelected(JavaWhereUsedFilters.TESTFILE.getKey());
        }
        
        if(JavaWhereUsedQueryPlugin.DEPENDENCIES) {
            show = show && manager.isSelected(JavaWhereUsedFilters.BINARYFILE.getKey());
        }
        
        return show;
    }
}
