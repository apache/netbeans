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

package org.netbeans.modules.options.colors;

import java.util.Comparator;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
public final class CategoryComparator implements Comparator<AttributeSet> {
    String default_string = NbBundle.getMessage(org.netbeans.modules.editor.NbEditorKit.class, "default");
                
    public int compare (AttributeSet o1, AttributeSet o2) {
        String name_1 = name(o1);
        String name_2 = name(o2);
	if (name_1.startsWith (default_string))
	    return name_2.startsWith (default_string) ? 0 : -1;
        if (name_2.startsWith (default_string))
            return 1;
	return name_1.compareTo (name_2);
    }
    
    private static String name (AttributeSet o) {
        return ((String) o.getAttribute(EditorStyleConstants.DisplayName));
    }
    
}
