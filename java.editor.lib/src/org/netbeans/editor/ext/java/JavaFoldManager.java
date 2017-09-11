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

package org.netbeans.editor.ext.java;

import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldManager;

import static org.netbeans.editor.ext.java.Bundle.*;
import org.openide.util.NbBundle;

/**
 * Java fold maintainer creates and updates folds for java sources.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class JavaFoldManager implements FoldManager {

    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = FoldType.INITIAL_COMMENT; // NOI18N

    @NbBundle.Messages("FoldType_Imports=Imports")
    public static final FoldType IMPORTS_FOLD_TYPE = FoldType.create("import", FoldType_Imports(), 
	     new org.netbeans.api.editor.fold.FoldTemplate(0, 0, "...")); // NOI18N
    
    @NbBundle.Messages("FoldType_Javadoc=Javadoc Comments")
    public static final FoldType JAVADOC_FOLD_TYPE = FoldType.DOCUMENTATION.derive("javadoc", FoldType_Javadoc(), 
	     new org.netbeans.api.editor.fold.FoldTemplate(3, 2, "/**...*/")); // NOI18N

    @NbBundle.Messages("FoldType_Methods=Methods")
    public static final FoldType CODE_BLOCK_FOLD_TYPE = FoldType.MEMBER.derive("method", FoldType_Methods(), 
	     new org.netbeans.api.editor.fold.FoldTemplate(1, 1, "{...}")); // NOI18N
    
    @NbBundle.Messages("FoldType_InnerClasses=Inner Classes")
    public static final FoldType INNERCLASS_TYPE = FoldType.NESTED.derive("innerclass", "Inner Classes", 
	     new org.netbeans.api.editor.fold.FoldTemplate(1, 1, "{...}")); // NOI18N
    
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
