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

package org.netbeans.editor.ext.java;

import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldManager;

import org.netbeans.modules.java.editor.fold.JavaElementFoldManager;

/**
 * Java fold maintainer creates and updates folds for java sources.
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated This package introduces a dependency on an obsoleted pre-NetBeans 6.5 features.
 * It is possible to use generic {@link FoldType} categories and use {@link FoldType#isKindOf(org.netbeans.api.editor.fold.FoldType)}.
 */

@Deprecated
public abstract class JavaFoldManager implements FoldManager {

    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = FoldType.INITIAL_COMMENT; // NOI18N

    public static final FoldType IMPORTS_FOLD_TYPE = JavaElementFoldManager.IMPORTS_FOLD_TYPE;
    
    public static final FoldType JAVADOC_FOLD_TYPE = JavaElementFoldManager.JAVADOC_FOLD_TYPE;

    public static final FoldType CODE_BLOCK_FOLD_TYPE = JavaElementFoldManager.METHOD_BLOCK_FOLD_TYPE;

    public static final FoldType METHOD_BLOCK_FOLD_TYPE = JavaElementFoldManager.METHOD_BLOCK_FOLD_TYPE;
    
    public static final FoldType INNERCLASS_TYPE = JavaElementFoldManager.INNERCLASS_TYPE;
    
    private static final String IMPORTS_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String COMMENT_FOLD_DESCRIPTION = "/*...*/"; // NOI18N

    private static final String JAVADOC_FOLD_DESCRIPTION = "/**...*/"; // NOI18N
    
    private static final String CODE_BLOCK_FOLD_DESCRIPTION = "{...}"; // NOI18N

    @Deprecated
    public static final FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(INITIAL_COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);

    @Deprecated
    public static final FoldTemplate IMPORTS_FOLD_TEMPLATE
        = new FoldTemplate(IMPORTS_FOLD_TYPE, IMPORTS_FOLD_DESCRIPTION, 0, 0);

    @Deprecated
    public static final FoldTemplate JAVADOC_FOLD_TEMPLATE
        = new FoldTemplate(JAVADOC_FOLD_TYPE, JAVADOC_FOLD_DESCRIPTION, 3, 2);

    @Deprecated
    public static final FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
            = new FoldTemplate(CODE_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);

    @Deprecated
    public static final FoldTemplate METHOD_BLOCK_FOLD_TEMPLATE
            = new FoldTemplate(METHOD_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);

    @Deprecated
    public static final FoldTemplate INNER_CLASS_FOLD_TEMPLATE
        = new FoldTemplate(INNERCLASS_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);

    
    protected static final class FoldTemplate {
        
        private FoldType type;
        
        private String description;
        
        private int startGuardedLength;
        
        private int endGuardedLength;
        
        protected FoldTemplate(FoldType type, String description,
        int startGuardedLength, int endGuardedLength) {
            this.type = type;
            this.description = description;
            this.startGuardedLength = startGuardedLength;
            this.endGuardedLength = endGuardedLength;
        }
        
        public FoldType getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getStartGuardedLength() {
            return startGuardedLength;
        }
        
        public int getEndGuardedLength() {
            return endGuardedLength;
        }

    }

}
