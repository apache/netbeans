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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.folding;

import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldManager;

/**
 *  Fold maintainer/manager base class for C and C++ (not yet supporting Fortran).
 *  This code is derived from the NetBeans 4.1 versions of the JavaFoldManager
 *  in the java/editor module.
 */

abstract class CppFoldManagerBase implements FoldManager {

    public static final FoldType INITIAL_COMMENT_FOLD_TYPE =
		    new FoldType("initial-comment"); // NOI18N

    public static final FoldType INCLUDES_FOLD_TYPE = new FoldType("includes"); // NOI18N
    
    public static final FoldType COMMENT_FOLD_TYPE = new FoldType("block-comments"); // NOI18N
    
    public static final FoldType LINE_COMMENT_FOLD_TYPE = new FoldType("line-comments"); // NOI18N

    public static final FoldType CODE_BLOCK_FOLD_TYPE = new FoldType("code-block"); // NOI18N
    
    public static final FoldType IFDEF_FOLD_TYPE = new FoldType("#ifdef"); // NOI18N

    private static final String INCLUDES_FOLD_DESCRIPTION = "..."; // NOI18N

    private static final String COMMENT_FOLD_DESCRIPTION = "/*...*/"; // NOI18N
    
    private static final String LINE_COMMENT_FOLD_DESCRIPTION = "//..."; // NOI18N

    private static final String CODE_BLOCK_FOLD_DESCRIPTION = "{...}"; // NOI18N
    
    private static final String IFDEF_FOLD_DESCRIPTION = "..."; // NOI18N
    
    public static final FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(INITIAL_COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);

    public static final FoldTemplate INCLUDES_FOLD_TEMPLATE
        = new FoldTemplate(INCLUDES_FOLD_TYPE, INCLUDES_FOLD_DESCRIPTION, 1, 0);

    public static final FoldTemplate COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(COMMENT_FOLD_TYPE, COMMENT_FOLD_DESCRIPTION, 2, 2);
    
    public static final FoldTemplate LINE_COMMENT_FOLD_TEMPLATE
        = new FoldTemplate(LINE_COMMENT_FOLD_TYPE, LINE_COMMENT_FOLD_DESCRIPTION, 2, 0);    

    public static final FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
        = new FoldTemplate(CODE_BLOCK_FOLD_TYPE, CODE_BLOCK_FOLD_DESCRIPTION, 1, 1);

    public static final FoldTemplate IFDEF_FOLD_TEMPLATE
        = new FoldTemplate(IFDEF_FOLD_TYPE, IFDEF_FOLD_DESCRIPTION, 0, 0);

    public static final String CODE_FOLDING_ENABLE = "code-folding-enable"; //NOI18N
    /** Collapse methods by default */
    public static final String CODE_FOLDING_COLLAPSE_METHOD = "code-folding-collapse-method"; //NOI18N
    
    /** Collapse inner classes by default */
    public static final String CODE_FOLDING_COLLAPSE_INNERCLASS = "code-folding-collapse-innerclass"; //NOI18N
    
    /** Collapse import section default */
    public static final String CODE_FOLDING_COLLAPSE_IMPORT = "code-folding-collapse-import"; //NOI18N
    
    /** Collapse javadoc comment by default */
    public static final String CODE_FOLDING_COLLAPSE_JAVADOC = "code-folding-collapse-javadoc"; //NOI18N

    /** Collapse initial comment by default */
    public static final String CODE_FOLDING_COLLAPSE_INITIAL_COMMENT = "code-folding-collapse-initial-comment"; //NOI18N
    
    /**
     * Collapse code blocks by default
     */
    public static final String CODE_FOLDING_COLLAPSE_CODE_BLOCK = "code-folding-collapse-code-block"; // NOI18N
    /**
     * Collapse code comments by default
     */
    public static final String CODE_FOLDING_COLLAPSE_COMMENT = "code-folding-collapse-comment"; // NOI18N
    
    /* Copied from JavaFoldManger in java/editor/lib */
    protected static final class FoldTemplate {
        
        private final FoldType type;
        
        private final String description;
        
        private final int startGuardedLength;
        
        private final int endGuardedLength;
        
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
