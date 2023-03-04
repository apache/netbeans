/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.languages;

import java.util.List;

/**
 * Represents one token in AST.
 */
public final class ASTToken extends ASTItem {

    private String      identifier;
    private int         type;
    
    
    /**
     * Creates new token with given parameters.
     * 
     * @param mimeType a mime type of token 
     * @param type a type of token 
     * @param identifier token identifier
     * @param offset token offset
     * @param length token length
     * @param children a list of token children
     * @return new ASTToken
     */
    public static ASTToken create (
        Language        language,
        int             type,
        String          identifier,
        int             offset,
        int             length,
        List<? extends ASTItem> children
    ) {
        return new ASTToken (
            language,
            type, 
            identifier, 
            offset, 
            length,
            children
        );
    }
    
    /**
     * Creates new token with given parameters.
     * 
     * @param mimeType a mime type of token 
     * @param type a type of token 
     * @param identifier token identifier
     * @param offset token offset
     * @param length token length
     * @param children a list of token children
     * @return new ASTToken
     */
    public static ASTToken create (
        Language        language,
        String          typeName,
        String          identifier,
        int             offset,
        int             length,
        List<? extends ASTItem> children
    ) {
        int typeID = ((org.netbeans.modules.languages.Language) language).getTokenID (typeName);
        return new ASTToken (
            language,
            typeID, 
            identifier, 
            offset, 
            length,
            children
        );
    }
    
    
    /**
     * Creates new token with given parameters, no children and length 
     * derived from identifier.
     * 
     * @param mimeType a mime type of token 
     * @param type a type of token 
     * @param identifier token identifier
     * @param offset token offset
     * @return new ASTToken
     */
    public static ASTToken create (
        Language        language,
        int             type,
        String          identifier,
        int             offset
    ) {
        return new ASTToken (
            language,
            type, 
            identifier, 
            offset, 
            identifier == null ? 0 : identifier.length (),
            null
        );
    }

    
    private ASTToken (
        Language                language,
        int                     type, 
        String                  identifier, 
        int                     offset,
        int                     length,
        List<? extends ASTItem> children
    ) {
        super (language, offset, length, children);
        this.identifier = identifier;
        this.type = type;
    }

    /**
     * Retruns type of token.
     * 
     * @return type of token
     */
    public int getTypeID () {
        return type;
    }
    
    public String getTypeName () {
        org.netbeans.modules.languages.Language l = (org.netbeans.modules.languages.Language) getLanguage ();
        if (l == null) return null;
        return l.getTokenType (type);
    }

    /**
     * Retruns token identifier.
     * 
     * @return token identifier
     */
    public String getIdentifier () {
        return identifier;
    }
    
    private String toString;

    /**
     * Retruns string representation of this token.
     * 
     * @return string representation of this token
     */
    public String toString () {
        if (toString == null) {
            StringBuffer sb = new StringBuffer ();
            sb.append ('<').append (getTypeName ());
            if (identifier != null)
                sb.append (",'").
                   append (e (identifier)).
                   append ("'");
            sb.append ('>');
            toString = sb.toString ();
        }
        return toString;
    }
        
    private static String e (CharSequence t) {
        StringBuilder sb = new StringBuilder ();
        int i, k = t.length ();
        for (i = 0; i < k; i++) {
            if (t.charAt (i) == '\t')
                sb.append ("\\t");
            else
            if (t.charAt (i) == '\r')
                sb.append ("\\r");
            else
            if (t.charAt (i) == '\n')
                sb.append ("\\n");
            else
                sb.append (t.charAt (i));
        }
        return sb.toString ();
    }
}
