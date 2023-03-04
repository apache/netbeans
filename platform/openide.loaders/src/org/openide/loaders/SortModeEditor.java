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

package org.openide.loaders;

import java.beans.*;

/** Editor for sorting mode
 * @author Jaroslav Tulach, Jesse Glick
 */
class SortModeEditor extends PropertyEditorSupport {
    /** modes */
    private static final DataFolder.SortMode[] values = {
        DataFolder.SortMode.NONE,
        DataFolder.SortMode.NAMES,
        DataFolder.SortMode.CLASS,
        DataFolder.SortMode.FOLDER_NAMES,
        DataFolder.SortMode.LAST_MODIFIED,
        DataFolder.SortMode.SIZE,
        DataFolder.SortMode.EXTENSIONS,
        DataFolder.SortMode.NATURAL
    };

    /** Names for modes. First is for displaying files */
    private static final String[] modes = {
        DataObject.getString ("VALUE_sort_none"),
        DataObject.getString ("VALUE_sort_names"),
        DataObject.getString ("VALUE_sort_class"),
        DataObject.getString ("VALUE_sort_folder_names"),
        DataObject.getString ("VALUE_sort_last_modified"),
        DataObject.getString ("VALUE_sort_size"),
        DataObject.getString ("VALUE_sort_extensions"),
        DataObject.getString ("VALUE_sort_natural")
    };

    /** @return names of the two possible modes */
    @Override
    public String[] getTags () {
        return modes;
    }

    /** @return text for the current value (File or Element mode) */
    @Override
    public String getAsText () {
        Object obj = getValue ();
        for (int i = 0; i < values.length; i++) {
            if (obj == values[i]) {
                return modes[i];
            }
        }
        return null;
    }

    /** Setter.
    * @param str string equal to one value from modes array
    */
    @Override
    public void setAsText (String str) {
        for (int i = 0; i < modes.length; i++) {
            if (str.equals (modes[i])) {
                setValue (values[i]);
                return;
            }
        }
    }
}
