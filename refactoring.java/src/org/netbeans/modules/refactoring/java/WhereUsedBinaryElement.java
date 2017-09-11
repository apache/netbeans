/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
