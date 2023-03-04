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
