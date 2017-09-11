/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public class CompletionUtils {
    private static String cachedPrefix = null;
    private static Pattern cachedPattern = null;
    
    public static boolean startsWith(CharSequence theString, String prefix) {
        if (isCamelCasePrefix(prefix)) {
            return startsWithCamelCase(theString, prefix);
        } else {
            return theString.toString().toLowerCase().startsWith(prefix.toLowerCase());
        }
    }
    
    public static boolean isCamelCasePrefix(String prefix) {
        if (prefix == null || prefix.length() < 2 || prefix.charAt(0) == '"')
            return false;
        for (int i = 1; i < prefix.length(); i++) {
            if (Character.isUpperCase(prefix.charAt(i)))
                    return true;                
        }
        return false;
    }

    public static boolean startsWithCamelCase(CharSequence theString, String prefix) {
        if (theString == null || theString.length() == 0 || prefix == null || prefix.length() == 0)
            return false;
        if (!prefix.equals(cachedPrefix) || cachedPattern == null) {
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            int index;
            do {
                index = findNextUpper(prefix, lastIndex + 1);
                String token = prefix.substring(lastIndex, index == -1 ? prefix.length(): index);
                sb.append(Pattern.quote(token)); 
                sb.append(index != -1 ? "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                lastIndex = index;
            } while (index != -1);
            cachedPrefix = prefix;
            cachedPattern = Pattern.compile(sb.toString());
        }
        return cachedPattern.matcher(theString).matches();
    }
    
    private static int findNextUpper(String text, int offset) {        
        for (int i = offset; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i)))
                return i;
        }
        return -1;
    }
    
    public static String getSimpleName(String fqn) {
        if (fqn == null) {
            return null;
        }
        int lastDot = fqn.lastIndexOf('.');
        return lastDot == -1 ? fqn : fqn.substring(lastDot + 1);
    }
    
    /**
     * Adds fx: namespace declaration to the root of the document. Fails with
     * IllegalStateException, if a fx with a conflicting namespace URI is already
     * used in the root element.
     * 
     * @param doc document to modify
     * @param h token hierarchy - to find the root element
     */
    public static Callable<String> makeFxNamespaceCreator(CompletionContext ctx) {
        final String existingPrefix = ctx.findFxmlNsPrefix();
        if (existingPrefix != null) {
            return new Callable<String>() {

                @Override
                public String call() throws Exception {
                    return existingPrefix;
                }
                
            };
        }
        
        final String prefix = ctx.findPrefixString(JavaFXEditorUtils.FXML_FX_NAMESPACE_CURRENT, 
                JavaFXEditorUtils.FXML_FX_PREFIX);
        final Document doc = ctx.getDoc();
        Position pos;
        
        try {
            pos = NbDocument.createPosition(doc, ctx.getRootAttrInsertOffset(), Position.Bias.Forward);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            pos = null;
        }
        
        final Position finalPos = pos;
        return new Callable<String>() {
            public String call() throws Exception {
                if (finalPos == null) {
                    return prefix;
                }
                doc.insertString(finalPos.getOffset(), "xmlns:" + prefix + "=\"" +
                        JavaFXEditorUtils.FXML_FX_NAMESPACE_CURRENT + "\" ", null);
                return prefix;
            }
        };
    }

}
