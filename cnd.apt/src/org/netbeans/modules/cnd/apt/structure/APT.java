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

package org.netbeans.modules.cnd.apt.structure;

import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * Abstract Preprocessing Tree
 * LISP-like trees:
 * ROOT
 * |
 * Child1-Child2
 * 
 * the original idea is antlr.AST approach, but no setters => immutable =>
 * could be used at the same time by multiple APT Visitors.
 *
 */
public interface APT {
    public interface Type {
        public static final int INVALID             = 0;
        public static final int FILE                = INVALID + 1;
        public static final int TOKEN_STREAM        = FILE + 1;
        public static final int INCLUDE             = TOKEN_STREAM + 1;
        public static final int INCLUDE_NEXT        = INCLUDE + 1;
        public static final int DEFINE              = INCLUDE_NEXT + 1;
        public static final int UNDEF               = DEFINE + 1;
        public static final int IFDEF               = UNDEF + 1;
        public static final int IFNDEF              = IFDEF + 1;
        public static final int IF                  = IFNDEF + 1;
        public static final int ELIF                = IF + 1;
        public static final int ELSE                = ELIF + 1;
        public static final int ENDIF               = ELSE + 1;
        public static final int PRAGMA              = ENDIF + 1;
        public static final int LINE                = PRAGMA + 1;
        public static final int ERROR               = LINE + 1;
        public static final int PREPROC_UNKNOWN     = ERROR + 1; // unrecognized #-directive
    }
    
    /** method called consequently token by token to let APT node to init itself 
     * when initializing is finished APT node returns false (not accepted token)
     */
    public boolean accept(APTFile curFile, APTToken token);
    
    /** Get the associated token */
    public APTToken getToken();
    
    /** Get the first child of this node; null if no children */
    public APT getFirstChild();

    /** Get	the next sibling in line after this one */
    public APT getNextSibling();

    /** Get the token text for this node */
    public String getText();

    /** Get the type for this node */
    public int/*Type*/ getType();

    /** Get start offset of the node's first wrapped token */
    public int getOffset();
    
    /** Get end offset of the node's last wrapped token */
    public int getEndOffset();
    
//    /** Need for error handling */
//    public int getLine();
//
//    /** Need for error handling */
//    public int getColumn();

//    /** Get number of children of this node; if leaf, returns 0 */
//    public int getNumberOfChildren();
    
    /** Set the first child of a node. */
    public void setFirstChild(APT child);

    /** Set the next sibling after this one. */
    public void setNextSibling(APT next);
//
//    /** Set the token text for this node */
//    public void setText(String text);
//
//    /** Set the type for this node */
//    public void setType(int/*Type*/ type);

//    public String toString();

//    public String toStringList();
//
//    public String toStringTree();
    
}
