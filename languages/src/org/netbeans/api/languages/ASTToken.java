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
