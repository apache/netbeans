/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.api.jumpto.type;

import org.netbeans.modules.jumpto.type.GoToTypeAction;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;

/**
 * Support for browsing of the types. Opens search dialog for the type name 
 * with possibility to filter the results.
 * 
 * @author Martin Adamek
 * 
 * @since 1.3
 */
public final class TypeBrowser {

    /**
     * Blocking call for opening modal search dialog
     *
     * @param title title of the dialog
     * @param filter optional filter of the results; can be null
     * @param typeProviders type providers defining the scope of the search;
     * if none specified, all type providers from default lookup will be used
     * @return selected type or null if dialog was canceled
     */
    public static TypeDescriptor browse(String title, Filter filter, TypeProvider... typeProviders) {
        return browse(title, null, filter, typeProviders);
    }

    /**
     * Blocking call for opening modal search dialog
     * 
     * @param title title of the dialog
     * @param initialText text that should be prefilled in the type name text field
     * @param filter optional filter of the results; can be null
     * @param typeProviders type providers defining the scope of the search; 
     * if none specified, all type providers from default lookup will be used
     * @return selected type or null if dialog was canceled
     * @since 1.25
     */
    public static TypeDescriptor browse(String title, String initialText, Filter filter, TypeProvider... typeProviders) {
        GoToTypeAction goToTypeAction = new GoToTypeAction(title, filter, false, typeProviders);
        final Iterable<? extends TypeDescriptor> tds = goToTypeAction.getSelectedTypes(true, initialText);
        return tds.iterator().hasNext() ? tds.iterator().next() : null;
    }

    /**
     * Filtering support
     */
    public static interface Filter {
        
        boolean accept(TypeDescriptor typeDescriptor);
        
    }
    
}
