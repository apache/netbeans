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

package org.netbeans.modules.gradle.javaee.nodes;

import java.awt.Image;
import java.util.Collections;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.*;

import static org.netbeans.modules.gradle.javaee.nodes.Bundle.*;

/**
 *
 * @author Laszlo Kishalmi
 */
@NbBundle.Messages("LBL_Web_Pages=Web Pages")
class WebPagesNode extends FilterNode {

    @StaticResource
    private static final String WEB_BADGE = "org/netbeans/modules/gradle/javaee/resources/WebPagesBadge.png"; //NOI18N

    final FileObject root;

    public WebPagesNode(Node original, FileObject root) {
        super(original);
        this.root = root;
    }

    @Override
    public String getDisplayName() {
        return LBL_Web_Pages();

    }

    @Override
    public String getHtmlDisplayName() {
        try {
            String s = LBL_Web_Pages();
            String result = root.getFileSystem().getDecorator().annotateNameHtml(
                    s, Collections.singleton(root));

            //Make sure the super string was really modified
            if (result != null && !s.equals(result)) {
                return result;
            }
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return super.getHtmlDisplayName();
    }

    @Override
    public Image getIcon(int param) {
        Image retValue = super.getIcon(param);
        return ImageUtilities.mergeImages(retValue, ImageUtilities.loadImage(WEB_BADGE), 8, 8);
    }

    @Override
    public Image getOpenedIcon(int param) {
        Image retValue = super.getOpenedIcon(param);
        return ImageUtilities.mergeImages(retValue, ImageUtilities.loadImage(WEB_BADGE), 8, 8);
    }

}
