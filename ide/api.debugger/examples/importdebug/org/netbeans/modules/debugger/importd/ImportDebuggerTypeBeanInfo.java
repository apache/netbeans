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

package org.netbeans.modules.debugger.importd;

import java.beans.*;
import java.awt.Image;


/** Object that provides beaninfo for {@link ImportDebuggerType}.
*
* @author Jan Jancura
*/
public class ImportDebuggerTypeBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;

    private static BeanDescriptor descr;
    static {
        descr = new BeanDescriptor (ImportDebuggerType.class);
        descr.setName (ImportDebugger.getLocString ("CTL_Import_Debugger_Type"));
    }

    /* gets FileSystemBeanInfo
    * @return FileSystemBeanInfo
    */
/*    public final BeanInfo[] getAdditionalBeanInfo () {
        return new BeanInfo[] {new DebuggerTypeBeanInfo ()};
    }*/

    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon = loadImage("/org/netbeans/modules/debugger/resources/jpdaDebugging.gif"); // NOI18N
            }
            return icon;
        } else { // 32
            if (icon32 == null) {
                icon32 = loadImage("/org/netbeans/modules/debugger/resources/jpdaDebugging32.gif"); // NOI18N
            }
            return icon32;
        }
    }
}
