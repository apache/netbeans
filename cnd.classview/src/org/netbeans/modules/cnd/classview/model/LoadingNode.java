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

package org.netbeans.modules.cnd.classview.model;

import java.awt.Image;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;


final class LoadingNode extends BaseNode {

    public LoadingNode() {
        super(Children.LEAF);
        setName("dummy"); // NOI18N
        setDisplayName(I18n.getMessage("Loading")); // NOI18N
    }
    
    /** Implements AbstractCsmNode.getData() */
    @Override
    public CsmObject getCsmObject() {
	return null;
    }

    @Override
    public Image getIcon(int param) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/classview/resources/waitNode.gif"); // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoadingNode) {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
}
