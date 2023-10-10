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
package org.netbeans.modules.java.editor.fold;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldTypeProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType = "text/x-java", service = FoldTypeProvider.class, position = 1000)
public class JavaFoldTypeProvider implements FoldTypeProvider {
    private Collection<FoldType>   types = new ArrayList<FoldType>(5);

    public JavaFoldTypeProvider() {
        types.add(JavaElementFoldManager.CODE_BLOCK_FOLD_TYPE);
        types.add(JavaElementFoldManager.INNERCLASS_TYPE);
        types.add(JavaElementFoldManager.METHOD_BLOCK_FOLD_TYPE);
        types.add(JavaElementFoldManager.IMPORTS_FOLD_TYPE);
        types.add(JavaElementFoldManager.JAVADOC_FOLD_TYPE);
        types.add(JavaElementFoldManager.INITIAL_COMMENT_FOLD_TYPE);
        types.add(JavaFoldTypeProvider.BUNDLE_STRING);
    }
    
    
    @Override
    public Collection getValues(Class type) {
        return types;
    }

    @Override
    public boolean inheritable() {
        return false;
    }
    
    /**
     * Represents a reference to a String in a resource bundle.
     * Java module registers a default {@link ContentReader}, which assumes the fold data
     * contains information [TBD] necessary to extract the actual string.
     */
    public static final FoldType BUNDLE_STRING = FoldType.create("bundle-string", 
            NbBundle.getMessage(JavaFoldTypeProvider.class, "Fold_BundleString"), 
            FoldTemplate.DEFAULT_BLOCK);
}
