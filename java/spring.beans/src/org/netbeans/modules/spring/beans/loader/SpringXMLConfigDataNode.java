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
/*
 * Contributor(s): Craig MacKay.
 */

package org.netbeans.modules.spring.beans.loader;

import java.awt.Image;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

public class SpringXMLConfigDataNode extends DataNode {

    public SpringXMLConfigDataNode(SpringXMLConfigDataObject obj) {
        super(obj, Children.LEAF);
    }

    @Override
    public Image getIcon(int i) {
        return ImageUtilities.loadImage("org/netbeans/modules/spring/beans/resources/spring.png");
    }
}
