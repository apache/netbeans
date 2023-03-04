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

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * This group keeps the usage of the contained description related
 * elements consistent across Java EE deployment descriptors.
 *
 * All elements may occur multiple times with different languages,
 * to support localization of the content.
 *
 * @author Petr Pisl
 */
public interface DescriptionGroup {
    
    public static final String DESCRIPTION = JSFConfigQNames.DESCRIPTION.getLocalName();
    public static final String DISPLAY_NAME = JSFConfigQNames.DISPLAY_NAME.getLocalName();
    public static final String ICON = JSFConfigQNames.ICON.getLocalName();
    
    /**
     *
     * @return
     */
    List<Description> getDescriptions();
    
    /**
     *
     * @param description
     */
    void addDescription(Description description);
    /**
     *
     * @param index
     * @param description
     */
    void addDescription(int index, Description description);
    
    /**
     *
     * @param description
     */
    void removeDescription(Description description);
    
    /**
     *
     * @return
     */
    List<DisplayName> getDisplayNames();
    
    /**
     *
     * @param displayName
     */
    void addDisplayName(DisplayName displayName);
    
    /**
     *
     * @param index
     * @param displayName
     */
    void addDisplayName(int index, DisplayName displayName);
    /**
     *
     * @param displayName
     */
    void removeDisplayName(DisplayName displayName);
    
    /**
     *
     * @return
     */
    List<Icon> getIcons();
    
    /**
     *
     * @param icon
     */
    void addIcon(Icon icon);
    void addIcon(int index, Icon icon);
    void removeIcon(Icon icon);
}
