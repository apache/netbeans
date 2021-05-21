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

package org.netbeans.modules.editor.settings.storage.api;

import java.util.Collection;
import javax.swing.text.AttributeSet;

/**
 * Getters and setters for font & color editor profiles. Instances of this 
 * class should be registered in <code>MimeLookup</code> for particular mime types.
 *
 * @author Jan Jancura
 */
public abstract class FontColorSettingsFactory {

    /**
     * Gets all token font and colors for given scheme or null, if 
     * scheme does not exists. 
     * 
     * @param profile the name of profile
     *
     * @return token font and colors
     */
    public abstract Collection<AttributeSet> getAllFontColors (String profile);
    
    /**
     * Gets default values for all font & colors for given profile, or null
     * if profile does not exist or if it does not have any defaults. 
     * 
     * @param profile the name of profile
     *
     * @return default values for all font & colors
     */
    public abstract Collection<AttributeSet> getAllFontColorDefaults 
        (String profile);
    
    /**
     * Sets all token font and colors for given scheme. 
     * 
     * @param profile the name of profile
     * @param fontColors new colorings
     */
    public abstract void setAllFontColors (
        String profile,
        Collection<AttributeSet> fontColors
    );
    
    /**
     * Sets default values for all token font and colors for given scheme. 
     * 
     * @param profile the name of profile
     * @param fontColors new colorings
     */
    public abstract void setAllFontColorsDefaults (
        String profile,
        Collection<AttributeSet> fontColors
    );
}
