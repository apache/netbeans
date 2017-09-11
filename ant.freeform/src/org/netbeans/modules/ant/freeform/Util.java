/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
