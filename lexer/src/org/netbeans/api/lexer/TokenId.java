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

package org.netbeans.api.lexer;

/**
 * Identifier of a token (could also be called a token-type).
 * <br>
 * It is not a token, because it does not contain
 * the text (also called image) of the token.
 *
 * <p>
 * Token ids are typically defined as enums by the following pattern:
 * <pre>
 * public enum JavaTokenId implements TokenId {
 *
 *     ERROR(null, "error"),
 *     IDENTIFIER(null, "identifier"),
 *     ABSTRACT("abstract", "keyword"),
 *     ...
 *     SEMICOLON(";", "separator"),
 *     ...
 *
 *
 *     private final String fixedText; // Used by lexer for production of flyweight tokens
 *
 *     private final String primaryCategory;
 *
 *     JavaTokenId(String fixedText, String primaryCategory) {
 *         this.fixedText = fixedText;
 *         this.primaryCategory = primaryCategory;
 *     }
 *
 *     public String fixedText() {
 *         return fixedText;
 *     }
 *
 *     public String primaryCategory() {
 *         return primaryCategory;
 *     }
 *
 * }
 * </pre>
 *
 * <p>
 * Token ids can also be generated (e.g. by lexer generation tools)
 * by using {@link org.netbeans.spi.lexer.LanguageHierarchy#newId(String,int,String)} method.
 * <br>
 * All token ids of a language must have both
 * unique ordinal and name.
 * <br/>
 * Token name should be all uppercase while token categories should be named
 * in lowercase.
 *
 * <p>
 * Detailed information and rules for naming can be found
 * in <A href="http://lexer.netbeans.org/doc/token-id-naming.html">TokenId Naming</A>.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenId {
    
    /**
     * Get name of this tokenId.
     * <p>
     * It can serve for several purposes such as finding
     * a possible style information for the given token.
     * </p>
     *
     * @return non-null unique name of the TokenId. The name must be unique
     *  among other TokenId instances of the particular language
     *  where it is defined. The name should consist of
     *  uppercase alphanumeric letters and underscores only.
     */
    String name();

    /**
     * Get integer identification of this tokenId.
     *
     * @return numeric identification of this TokenId.
     *  <BR>
     *  Ordinal must be a non-negative
     *  integer unique among all the tokenIDs inside the language
     *  where it is declared.
     *  <BR>
     *  The ordinals are usually defined and adopted from lexer
     *  generator tool that generates the lexer for the given language.
     *  <BR>
     *  They do not have to be consecutive.
     *  <br>
     *  On they other hand there should
     *  not be big gaps (e.g. 100 or more) because
     *  indexing arrays are constructed based on the ordinal values
     *  so the length of the indexing array corresponds
     *  to the highest ordinal of all the token ids declared
     *  for the particular language.
     *  <BR>
     *  The intIds allow more efficient use
     *  of the tokenIds in switch-case statements.
     */
    int ordinal();

    /**
     * Get name of primary token category into which this token belongs.
     * <br/>
     * Other token categories for this id can be defined in the language hierarchy.
     *
     * @return name of the primary token category into which this token belongs
     *  or null if there is no primary category for this token.
     */
    String primaryCategory();
    
}
