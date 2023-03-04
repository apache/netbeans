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
