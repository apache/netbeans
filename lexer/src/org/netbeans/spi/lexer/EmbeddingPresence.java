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

package org.netbeans.spi.lexer;

/**
 * Defines whether a default embedding can be present for the given token id or not.
 * <br/>
 * It allows to speed up <code>TokenSequence.embedded()</code> calls considerably in most cases.
 * <br/>
 * This only affects the default embedding creation. Custom embedding creation
 * can always be performed by <code>TokenSequene.createEmbedding()</code>.
 *
 * @author Miloslav Metelka
 */

public enum EmbeddingPresence {

    /**
     * Creation of the default embedding for the particular {@link org.netbeans.api.lexer.TokenId}
     * will be attempted for the first time but if there will be no embedding 
     * created then there will be no other attempts for embedding creation
     * for any tokens with the same token id.
     * <br/>
     * This corresponds to the most usual case that the embedding presence
     * only depends on a token id.
     * <br/>
     * This is the default for {@link LanguageHierarchy#embeddingPresence(org.netbeans.api.lexer.TokenId)}.
     */
    CACHED_FIRST_QUERY,
    
    /**
     * Default embedding creation will always be attempted for each token since
     * the embedding presence varies (it may depend on token's text or other token properties).
     * <br/>
     * For example if a string literal token would only qualify for an embedding
     * if it would contain a '\' character but not otherwise then this method
     * should return true for string literal token id.
     * <br/>
     * This option presents no performance improvement.
     */
    ALWAYS_QUERY,

    /**
     * There is no default embedding for the given {@link org.netbeans.api.lexer.TokenId}
     * and its creation will not be attempted.
     * <br/>
     * This is useful e.g. for keywords and operators.
     */
    NONE,

}
