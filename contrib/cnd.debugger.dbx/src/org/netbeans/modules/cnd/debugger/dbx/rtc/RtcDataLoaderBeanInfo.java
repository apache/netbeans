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

package org.netbeans.modules.cnd.debugger.dbx.rtc;


import java.beans.*;

import org.openide.ErrorManager;

public class RtcDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo() {
	try {
	    System.out.printf("RtcDataLoaderBeanInfo.getAdditionalBeanInfo()\n"); // NOI18N
	    return new BeanInfo[] {
		Introspector.getBeanInfo(RtcDataLoader.class.getSuperclass())
	    };
	} catch (IntrospectionException ie) {
	    ErrorManager.getDefault().notify(ie);
	    return null;
	}
    }

    /* LATER
    This seems to be done using Nodes
    Actually it probably has to do with the loader icon in the loader-pool UI

    private final static String ICON_RESOURCE =
	"org/netbeans/modules/cnd/debugger/common2/icons/Refresh.gif"; //NOI18N

    public Image getIcon(int type) {
	System.out.printf("RtcDataLoaderBeanInfo.getIcon()\n");
	return Utilities.loadImage(ICON_RESOURCE);
    }
    */
}
