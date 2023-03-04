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
package org.netbeans.modules.profiler.j2ee.impl.icons;

import java.awt.Image;
import java.util.Map;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.spi.IconsProvider;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.IconsProvider.class)
public final class JavaEEIconsProviderImpl extends IconsProvider.Basic {
    
    private static final String JSP_FOLDER_BADGE = "JavaEEIconsProviderImpl.JspFolderBadge"; // NOI18N

    @Override
    protected final void initStaticImages(Map<String, String> cache) {
        cache.put(JavaEEIcons.JAVAEE_PROJECTS, "j2eeProjects.png"); // NOI18N
        cache.put(JavaEEIcons.JSP, "jsp16.png"); // NOI18N
//        cache.put(JavaEEIcons.JSP_FOLDER, null); // Generated dynamically
        cache.put(JSP_FOLDER_BADGE, "webPagesBadge.png"); // NOI18N
        cache.put(JavaEEIcons.TAG, "tag16.png"); // NOI18N
        cache.put(JavaEEIcons.SERVLET, "servletObject.png"); // NOI18N
        cache.put(JavaEEIcons.FILTER, "servletObject.png"); // NOI18N
        cache.put(JavaEEIcons.LISTENER, "servletObject.png"); // NOI18N
    }
    
    @Override
    protected Image getDynamicImage(String key) {
        if (JavaEEIcons.JSP_FOLDER.equals(key)) {
            Image jspFolderBadge = Icons.getImage(JSP_FOLDER_BADGE);
            Image packageIcon = Icons.getImage(JavaEEIcons.PACKAGE);
            return ImageUtilities.mergeImages(packageIcon, jspFolderBadge, 0, 7);
        }
        return null;
    }
    
}
