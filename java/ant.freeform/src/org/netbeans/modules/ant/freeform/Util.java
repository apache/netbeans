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

package org.netbeans.modules.ant.freeform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ant.freeform.spi.HelpIDFragmentProvider;
import org.openide.ErrorManager;

/**
 * Miscellaneous utilities.
 * @author Jesse Glick
 */
public class Util {
    
    private Util() {}
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.ant.freeform"); // NOI18N
    
    private static final Pattern VALIDATION = Pattern.compile("([A-Za-z0-9])+"); // NOI18N
    
    public static String getMergedHelpIDFragments(Project p) {
        List<String> fragments = new ArrayList<String>();
        
        for (HelpIDFragmentProvider provider : p.getLookup().lookupAll(HelpIDFragmentProvider.class)) {
            String fragment = provider.getHelpIDFragment();
            
            if (fragment == null || !VALIDATION.matcher(fragment).matches()) {
                throw new IllegalStateException("HelpIDFragmentProvider \"" + provider + "\" provided invalid help ID fragment \"" + fragment + "\"."); // NOI18N
            }
            
            fragments.add(fragment);
        }
        
        Collections.sort(fragments);
        
        StringBuffer result = new StringBuffer();

        for (Iterator<String> i = fragments.iterator(); i.hasNext(); ) {
            result.append(i.next());
            
            if (i.hasNext()) {
                result.append('.');
            }
        }
        
        return result.toString();
    }
    
}
