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

package org.netbeans.editor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

/**
* Token context defines the environment in which only a limited set
* of tokens can be used. This set can be retrieved by calling
* the <tt>getTokenIDs()</tt> method. The context can contain other
* contexts which means that the context can possibly switch
* into one of its children contexts during lexing.
* The child context can also have other children that can
* work in the same way.
* In this way context-paths can be created. They describe
* the way how was the token lexically recognized.
* The context can be retrieved when the syntax-class is known
* using the <tt>get</tt> method.
* 
*
* @author Miloslav Metelka
* @version 1.00
*/

public class TokenContext {

    private static final TokenContext[] EMPTY_CHILDREN = new TokenContext[0];

    private final String namePrefix;

    private final TokenContext[] children;

    private final HashMap pathCache = new HashMap(37);

    private final ArrayList tokenIDList = new ArrayList();

    private final ArrayList tokenCategoryList = new ArrayList();

    private TokenID[] tokenIDs;

    private TokenCategory[] tokenCategories;

    private TokenContextPath contextPath;

    private TokenContextPath[] allContextPaths;

    private TokenContextPath[] lastContextPathPair;

    public TokenContext(String namePrefix) {
        this(namePrefix, EMPTY_CHILDREN);
    }

    /** Construct new token-context.
    * @param namePrefix name that will prefix all the token-ids names.
    * @param children child token contexts.
    */
    public TokenContext(String namePrefix, TokenContext[] children) {
        if (namePrefix == null) {
            throw new IllegalArgumentException("Name prefix must be non-null."); // NOI18N
        }

        this.namePrefix = namePrefix.intern();
        this.children = (children != null) ? children : EMPTY_CHILDREN;

        contextPath = TokenContextPath.get(new TokenContext[] { this });
    }

    /** Get the prefix that this context adds to the name of its tokens. */
    public String getNamePrefix() {
        return namePrefix;
    }

    /** Get the children contexts of this context. It returns empty-array
    * if there are no children.
    */
    public TokenContext[] getChildren() {
        return children;
    }

    /** Add token-id to the set of token-ids that belong to this context. */
    protected void addTokenID(TokenID tokenID) {
        synchronized (tokenIDList) {
            tokenIDList.add(tokenID);
            tokenIDs = null;

            // Check whether there's a valid and new category for this token-id
            TokenCategory tcat = tokenID.getCategory();
            if (tcat != null && tokenCategoryList.indexOf(tcat) < 0) {
                tokenCategoryList.add(tcat);
                tokenCategories = null;
            }
        }
    }

    /** Add all static-final token-id fields declared
    * in this token-context using <tt>Class.getDeclaredFields()</tt> call.
    */
    protected void addDeclaredTokenIDs() throws IllegalAccessException, SecurityException {
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            int flags = Modifier.STATIC | Modifier.FINAL;
            if ((fields[i].getModifiers() & flags) == flags
                    && TokenID.class.isAssignableFrom(fields[i].getType())
               ) {
                addTokenID((TokenID)fields[i].get(null));
            }
        }
    }

    /** Get the token-ids that belong to this token-context. It doesn't
    * return the children's token-ids.
    */
    public TokenID[] getTokenIDs() {
        if (tokenIDs == null) {
            synchronized (tokenIDList) {
                tokenIDs = (TokenID[])tokenIDList.toArray(new TokenID[tokenIDList.size()]);
            }
        }

        return tokenIDs;
    }

    /** Get the token-categories that belong to this token-context. It doesn't
    * return the children's token-categories.
    */
    public TokenCategory[] getTokenCategories() {
        if (tokenCategories == null) {
            synchronized (tokenCategoryList) {
                tokenCategories = (TokenCategory[])tokenCategoryList.toArray(
                                      new TokenCategory[tokenCategoryList.size()]);
            }
        }

        return tokenCategories;
    }

    /** Get the context path for this token-context. */
    public TokenContextPath getContextPath() {
        return contextPath;
    }

    /** Get the context path for this token-context that is derived
    * from the path of one of the children.
    */
    public TokenContextPath getContextPath(TokenContextPath childPath) {
        if (childPath == null) {
            return contextPath;
        }

        TokenContextPath[] lastPair = lastContextPathPair;
        if (lastPair == null || lastPair[0] != childPath) {
            synchronized (pathCache) {
                lastPair = (TokenContextPath[])pathCache.get(childPath);
                if (lastPair == null) {
                    // Build the array of contexts
                    TokenContext[] origContexts = childPath.getContexts();
                    TokenContext[] contexts = new TokenContext[origContexts.length + 1];
                    System.arraycopy(origContexts, 0, contexts, 0, origContexts.length);
                    contexts[origContexts.length] = this;

                    TokenContextPath path = TokenContextPath.get(contexts);

                    lastPair = new TokenContextPath[] { childPath, path };
                    pathCache.put(childPath, lastPair);
                }
                lastContextPathPair = lastPair;
            }
        }

        return lastPair[1];
    }

    /** Get all the context paths for this token-context including
    * itself as the first one and all its children.
    */
    public TokenContextPath[] getAllContextPaths() {
        synchronized (tokenIDList) {
            if (allContextPaths == null) {
                ArrayList cpList = new ArrayList();
                cpList.add(getContextPath());

                for (int i = 0; i < children.length; i++) {
                    TokenContextPath[] childPaths = children[i].getAllContextPaths();
                    for (int j = 0; j < childPaths.length; j++) {
                        cpList.add(getContextPath(childPaths[j]));
                    }
                }

                allContextPaths = new TokenContextPath[cpList.size()];
                cpList.toArray(allContextPaths);
            }

            return allContextPaths;
        }
    }

}
