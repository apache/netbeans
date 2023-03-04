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

package  org.netbeans.modules.web.taglib;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import org.openide.util.ImageUtilities;

/** TLD Loader bean info.
*
* @author Simran Gleason
*/
public class TLDLoaderBeanInfo extends SimpleBeanInfo {

    /** @param type Desired type of the icon
     * @return returns the Image loader's icon
     */
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/web/taglib/resources/tags.gif"); // NOI18N
    }
}

