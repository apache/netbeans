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

package org.netbeans.modules.db.sql.loader;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.UniFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Jesse Beaumont
 */
public class SQLDataLoaderBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo(UniFileLoader.class) };
        } catch (IntrospectionException ie) {
	    Exceptions.printStackTrace(ie);
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        if (type == java.beans.BeanInfo.ICON_COLOR_16x16 ||
                type == java.beans.BeanInfo.ICON_MONO_16x16) {
	    return ImageUtilities.loadImage("org/netbeans/modules/db/sql/loader/resources/sql16.png"); // NOI18N
        } else {
	    return ImageUtilities.loadImage ("org/netbeans/modules/db/sql/loader/resources/sql32.png"); // NOI18N
        }
    }
}
