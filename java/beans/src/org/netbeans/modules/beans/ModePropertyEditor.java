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

package org.netbeans.modules.beans;

import java.beans.*;
import static org.netbeans.modules.beans.BeanUtils.*;

/** property editor for mode property of Prperty patterns
*
* @author Petr Hrebejk
*/
public class ModePropertyEditor extends PropertyEditorSupport {

    /** Array of tags
    */
    private static String[] tags;
    private static final int [] values = {
        PropertyPattern.READ_WRITE,
        PropertyPattern.READ_ONLY,
        PropertyPattern.WRITE_ONLY };

    /** @return names of the supported member Acces types */
    public String[] getTags() {
        if (tags == null) {
            tags = new String[] {
                getString( "LAB_ReadWriteMODE" ),
                getString( "LAB_ReadOnlyMODE" ),
                getString( "LAB_WriteOnlyMODE" )
            };
        }
        return tags;
    }

    /** @return text for the current value */
    public String getAsText () {
        int value = ((Integer)getValue()).intValue();

        for (int i = 0; i < values.length ; i++)
            if (values[i] == value)
                return getTags()[i];

        return getString( "LAB_Unsupported" );
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        for (int i = 0; i < getTags().length ; i++)
            if (getTags()[i] == text) {
                setValue(values[i]);
                return;
            }

        setValue(0);
    }
}
