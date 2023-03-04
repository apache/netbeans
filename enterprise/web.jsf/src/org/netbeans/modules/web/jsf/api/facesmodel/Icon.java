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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The icon type contains small-icon and large-icon elements
 * that specify the file names for small and large GIF, JPEG,
 * or PNG icon images used to represent the parent element in a
 * GUI tool.
 *
 * The xml:lang attribute defines the language that the
 * icon file names are provided in. Its value is "en" (English)
 * by default
 *
 * @author Petr Pisl
 */

public interface Icon extends LangAttribute {
    
    public static final String SMALL_ICON = JSFConfigQNames.SMALL_ICON.getLocalName();
    public static final String LARGE_ICON = JSFConfigQNames.LARGE_ICON.getLocalName();
    /**
     * The small-icon element contains the name of a file
     * containing a small (16 x 16) icon image. The file
     * name is a relative path within the Deployment
     * Component's Deployment File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @return the path to the small icon
     */
    public String getSmallIcon();
    
    /**
     * The small-icon element contains the name of a file
     * containing a small (16 x 16) icon image. The file
     * name is a relative path within the Deployment
     * Component's Deployment File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @param smallIcon the file name
     */
    public void setSmallIcon(String smallIcon);
    
    /**
     * The large-icon element contains the name of a file
     * containing a large
     * (32 x 32) icon image. The file name is a relative
     * path within the Deployment Component's Deployment
     * File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @return the path to the large icon
     */
    public String getLargeIcon();
    
    /**
     * The large-icon element contains the name of a file
     * containing a large
     * (32 x 32) icon image. The file name is a relative
     * path within the Deployment Component's Deployment
     * File.
     *
     * The image may be in the GIF, JPEG, or PNG format.
     * The icon can be used by tools.
     * @param largeIcon the path to the large icon
     */
    public void setLargeIcon(String largeIcon);
    
}
