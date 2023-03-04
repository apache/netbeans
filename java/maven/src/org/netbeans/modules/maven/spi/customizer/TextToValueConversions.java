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

package org.netbeans.modules.maven.spi.customizer;

import java.util.Map;
import org.netbeans.modules.maven.customizer.ActionMappings;

/**
 *
 * @author mkleint
 * @since 2.94
 */
public final class TextToValueConversions {

    private TextToValueConversions() {
    }
    
    /**
     * take the given string and attempt to convert it to a properties list (key value pairs) as used in actions panel
     * @param text
     * @return 
     */
    public static Map<String, String> convertStringToActionProperties(String text) {
        return ActionMappings.convertStringToActionProperties(text);
    }

}
