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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wizard.impl;

import java.awt.Image;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * An abstract node that uses a file folder icon. Ideally the icon is
 * taken from the Node delegate of DataFolder, if it is available.
 * Otherwise a default icon is used.
 *
 * @author  Nathan Fiedler
 */
public class FolderNode extends AbstractNode {
    /** The source for our folder icons. */
    private static Node iconSource;

    static {
        FileObject fobj = FileUtil.getConfigRoot();
        try {
            DataObject dobj = DataObject.find(fobj);
            iconSource = dobj.getNodeDelegate();
        } catch (DataObjectNotFoundException donfe) {
            // In this case, we have our default icons, which are not
            // platform-conformant, but they are better than nothing.
        }
    }

    public FolderNode(Children children) {
        super(children);
    }

    public Image getIcon(int type) {
        if (iconSource != null) {
            return iconSource.getIcon(type);
        } else {
            String url = NbBundle.getMessage(FolderNode.class,
                    "IMG_FolderNode_Closed");
            return org.openide.util.Utilities.loadImage(url);
        }
    }

    public Image getOpenedIcon(int type) {
        if (iconSource != null) {
            return iconSource.getOpenedIcon(type);
        } else {
            String url = NbBundle.getMessage(FolderNode.class,
                    "IMG_FolderNode_Opened");
            return org.openide.util.Utilities.loadImage(url);
        }
    }
}
