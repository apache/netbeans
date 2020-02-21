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
