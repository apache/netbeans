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

package org.netbeans.modules.lexer.gen;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.spi.lexer.util.LexerUtilities;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;

/**
 * Information about the language necessary for generating
 * language class source. It's obtained by parsing a XML description
 * and it can optionally be modified.
 * <BR>There is also possibility to generate xml description
 * describing the token ids. The generated description
 * can then be edited and extended by e.g. adding sample text(s)
 * or assigning token ids into token categories.
 * The language xml description then serves
 * as a source of extended information for the generation of the language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LanguageData {
    
    public static final Comparator IDS_FIELD_NAME_COMPARATOR = new IdsFieldNameComparator();
    
    public static final Comparator IDS_INT_ID_COMPARATOR = new IdsIntIdComparator();
    
    private TokenTypes tokenTypes;
    
    private String languageClassName;
    
    private String lexerClassName;
    
    private List ids;
    
    private List unmodifiableIds;
    
    private int uniqueIntId;
    
    public LanguageData() {
        /* Start with 1 to ensure the 0 is free
         * for yacc-like tools that use 0 for EOF.
         */
        uniqueIntId = 1;
        ids = new ArrayList();
        unmodifiableIds = Collections.unmodifiableList(ids);
    }
    
    /**
     * Get the token-types registered with this language data.
     */
    public final TokenTypes getTokenTypes() {
        return tokenTypes;
    }
    
    /**
     * Register the token-types into this language data
     * and apply them to this language data.
     */
    public void registerTokenTypes(TokenTypes tokenTypes) {
        this.tokenTypes = tokenTypes;
        
        // Let the token types update this data
        tokenTypes.updateData(this);
    }

    /**
     * @return class name of the generated language class without package.
     */
    public String getLanguageClassName() {
        return languageClassName;
    }
    
    public void setLanguageClassName(String languageClassName) {
        this.languageClassName = languageClassName;
    }

    /**
     * @return full classname of the generated lexer class (including package).
     */
    public String getLexerClassName() {
        return lexerClassName;
    }
    
    public void setLexerClassName(String lexerClassName) {
        this.lexerClassName = lexerClassName;
    }
    
    /**
     * @return unmodifiable list of tokenIds planned to be generated.
     *  To create a new mutable tokenId the {@link #newId(String)}
     *  can be used.
     */
    public List getIds() {
        return unmodifiableIds;
    }
    
    /**
     * Get copy of ids list sorted by a specified comparator.
     * @param c comparator to be used. {@link #IDS_FIELD_NAME_COMPARATOR}
     *  or {@link #IDS_INT_ID_COMPARATOR} can be used among others.
     * @return copy of ids list sorted by a specified comparator.
     */
    public List getSortedIds(Comparator c) {
        List idsCopy = new ArrayList(ids);
        Collections.sort(idsCopy, c);
        return idsCopy;
    }

    /**
     * Create new mutable token id and add it to the list of the current ids.
     * @return created and added mutable token id.
     */
    public MutableTokenId newId(String name) {
        if (findId(name) != null) {
            throw new IllegalArgumentException("Id named " + name + " already exists.");
        }

        MutableTokenId id = createId(name);
        ids.add(id);
        
        return id;
    }
    
    /**
     * Remove the given id from the list of the token ids.
     */
    public void remove(MutableTokenId id) {
        for (Iterator it = ids.iterator(); it.hasNext();) {
            if (id == it.next()) {
                it.remove();
                return;
            }
        }
        throw new IllegalArgumentException("id=" + id + " not found");
    }

    /**
     * Find the mutable tokenId with the given name.
     * @return mutable token id with the given name.
     */
    public MutableTokenId findId(String name) {
        for (Iterator it = getIds().iterator(); it.hasNext();) {
            MutableTokenId id = (MutableTokenId)it.next();
            if (name.equals(id.getName())) {
                return id;
            }
        }
        return null;
    }
    
    /**
     * Find the mutable tokenId by its integer identification.
     * @param intId integer identification of the tokenId.
     * @return mutable tokenId with given integer identification.
     */
    public MutableTokenId findId(int intId) {
        List ids = getIds();
        int cnt = ids.size();
        for (int i = 0; i < cnt; i++) {
            MutableTokenId id = (MutableTokenId)ids.get(i);
            if (id.getIntId() == intId) {
                return id;
            }
        }
        return null;
    }

    /**
     * Find the mutable tokenId by its token type name returned
     * by {@link MutableTokenId#getTokenTypeName()}.
     * @param tokenTypeName name of the field in the token-types class.
     * @return mutable tokenId with the given tokenTypeName.
     */
    public MutableTokenId findIdByTokenTypeName(String tokenTypeName) {
        List ids = getIds();
        int cnt = ids.size();
        for (int i = 0; i < cnt; i++) {
            MutableTokenId id = (MutableTokenId)ids.get(i);
            if (tokenTypeName.equals(id.getTokenTypeName())) {
                return id;
            }
        }
        return null;
    }

    /**
     * @return maximum intId among all the tokenIds.
     *   <BR><B>Note:</B>It's necessary that all the tokenIds
     *   have the non-numeric tokenIds resolved to numbers
     *   prior invocation of this method.<B>
     *   The {@link #findNonNumericIntIds()} can be used to find those.
     *   The non-numeric intIds are silently ignored by the method.
     */
    public int findMaxIntId() {
        List ids = getIds();
        int cnt = ids.size();
        int maxIntId = 0;
        for (int i = 0; i < cnt; i++) {
            MutableTokenId id = (MutableTokenId)ids.get(i);
            maxIntId = Math.max(maxIntId, id.getIntId());
        }
            
        return maxIntId;
    }        

    /**
     * @return unique intId not yet used for other intIds.
     */
    public int uniqueIntId() {
        return uniqueIntId++;
    }
    
    /**
     * Skip the given intId (and all intIds that are lower than it)
     * so they will not be returned by {@link #uniqueIntId()}.
     * @param intId id to skip.
     */
    public void skipIntId(int intId) {
        uniqueIntId = Math.max(uniqueIntId, ++intId);
    }

    /**
     * @return newly created mutable tokenId instance.
     *  <BR>Subclasses may override this method to return
     *  subclass of {@link MutableTokenId).
     */
    protected MutableTokenId createId(String name) {
        return new MutableTokenId(this, name);
    }

    /**
     * Generate unique intIds for the tokenIds that have
     * intId set to implicit value -1.
     * <BR>The {@link #uniqueIntId()} is used to obtain
     * the intIds.
     */
    public void updateUnassignedIntIds() {
        for (Iterator it = getIds().iterator(); it.hasNext();) {
            MutableTokenId id = (MutableTokenId)it.next();
            if (id.getIntId() == -1) {
                id.assignUniqueIntId();
            }
        }
    }
    
    /**
     * Check whether this language data object is in a consistent state.
     * The test includes checking for duplicate intIds.
     */
    public void check() {
        Map info = new HashMap();
        
        for (Iterator it = getIds().iterator(); it.hasNext();) {
            MutableTokenId id = (MutableTokenId)it.next();
            int intId = id.getIntId();
            if (intId < 0) {
                throw new IllegalStateException(
                    "Id " + id.getName()
                    + " has invalid intId=" + intId
                    + "\nLanguage data dump follows:\n"
                    + toString()

                );
            }
            
            MutableTokenId dupIntId = (MutableTokenId)info.put(new Integer(intId), id);
            if (dupIntId != null) {
                throw new IllegalStateException("Ids " + id.getName()
                    + " and " + dupIntId.getName()
                    + " have the same intId=" + intId
                    + "\nLanguage data dump follows:\n"
                    + toString());
            }
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        List mtids = getIds();
        int mtidsCount = mtids.size();
        for (int i = 0; i < mtidsCount; i++) {
            sb.append(mtids.get(i));
            sb.append('\n');
        }
        
        return sb.toString();
    }
        
    
    /**
     * Produce textual xml description for the integer token constants fields
     * found in the token constants class file.
     * <BR>This description can be used as a skeleton for the future
     * xml description file.
     */
    public String createDescription() {

        StringBuffer sb = new StringBuffer();
        appendDescriptionStart(sb);
        
        for (Iterator idsIterator = getIds().iterator();
             idsIterator.hasNext();) {

            MutableTokenId id = (MutableTokenId)idsIterator.next();
            String idName = id.getName();
            if (idName != null) {
                LexerGenUtilities.appendSpaces(sb, 4);
                sb.append("<TokenId name=\"");
                sb.append(LexerGenUtilities.idToLowerCase(idName));
                sb.append("\">\n");
                LexerGenUtilities.appendSpaces(sb, 4);
                sb.append("</TokenId>\n");
            }
        }

        appendDescriptionEnd(sb);

        return sb.toString();
    }

    protected void appendDescriptionStart(StringBuffer sb) {
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<!DOCTYPE Language SYSTEM ");
        sb.append("\"???/src/org/netbeans/lexer/language.dtd\">\n");
        sb.append("<Language>\n");
    }

    protected void appendDescriptionEnd(StringBuffer sb) {
        sb.append("</Language>\n");
    }

    private static final class IdsFieldNameComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            MutableTokenId id1 = (MutableTokenId)o1;
            MutableTokenId id2 = (MutableTokenId)o2;
            return id1.getFieldName().compareTo(id2.getFieldName());
        }

    }
    
    private static final class IdsIntIdComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            MutableTokenId id1 = (MutableTokenId)o1;
            MutableTokenId id2 = (MutableTokenId)o2;
            return id1.getIntId() - id2.getIntId();
        }

    }
    
}

