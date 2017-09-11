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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.awt.Image;
import java.beans.BeanInfo;
import java.lang.ref.WeakReference;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public class SourceGroupTreeElement implements TreeElement {
    
    private WeakReference<SourceGroup> sg;
    private FileObject dir;
    private Icon icon;
    private String displayName;
    
    private static String PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; // NOI18N

    SourceGroupTreeElement(SourceGroup sg) {
        this.sg = new WeakReference<SourceGroup>(sg);
        dir = sg.getRootFolder();
 
        icon = sg.getIcon(false);
        if ( icon == null ) {
            try {
                Image image = DataObject.find(sg.getRootFolder()).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                image = ImageUtilities.mergeImages( image, ImageUtilities.loadImage(PACKAGE_BADGE), 7, 7 );
                icon = new ImageIcon(image);
            } catch (DataObjectNotFoundException d) {
            }
        }
        displayName = sg.getDisplayName();
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        return TreeElementFactory.getTreeElement(FileOwnerQuery.getOwner(dir));
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return displayName;
    }

    @Override
    public Object getUserObject() {
        SourceGroup s = sg.get();
        if (s==null) {
            s = FolderTreeElement.getSourceGroup(dir);
        }
        return s;
    }
}

