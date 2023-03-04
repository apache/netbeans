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

package org.netbeans.modules.form.beaninfo.swing;

import java.beans.*;
import org.openide.util.ImageUtilities;

/** BeanInfo for JLayeredPane - defines only the icons for now.
*
* @author  Ian Formanek
*/

public class JLayeredPaneBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(javax.swing.JLayeredPane.class);
    }

    @Override
    public java.awt.Image getIcon(int type) {
        if (type == ICON_COLOR_32x32 || type == ICON_MONO_32x32)
            return ImageUtilities.loadImage(
                "javax/swing/beaninfo/images/JLayeredPaneColor32.gif"); // NOI18N
        else
            return ImageUtilities.loadImage(
                "javax/swing/beaninfo/images/JLayeredPaneColor16.gif"); // NOI18N
    }
}
