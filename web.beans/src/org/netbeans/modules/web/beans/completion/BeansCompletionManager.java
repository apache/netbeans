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
package org.netbeans.modules.web.beans.completion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.w3c.dom.Node;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class BeansCompletionManager {
    
    private static Map<String, BeansCompletor> completors = new HashMap<>();

    private BeansCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {


        // Items for property names 
        BeansCompletor.JavaClassesCompletor javaClassCompletor = new BeansCompletor.JavaClassesCompletor(BeansCompletor.TAG.CLASS);
        registerCompletor(BeansXmlConstants.CLASS, null, javaClassCompletor);
        
        BeansCompletor.JavaClassesCompletor stereotypeClassCompletor = new BeansCompletor.JavaClassesCompletor(BeansCompletor.TAG.STEREOTYPE);
        registerCompletor(BeansXmlConstants.STEREOTYPE, null, stereotypeClassCompletor);
    }
    
    private static final BeansCompletionManager INSTANCE = new BeansCompletionManager();

    public static BeansCompletionManager getDefault() {
        return INSTANCE;
    }

    public int completeAttributeValues(CompletionContext context, List<BeansCompletionItem> valueItems) {
        int anchorOffset = -1;
        
        if(context.getTag() == null) {
            return anchorOffset;
        }
        
        String tagName = context.getTag().getNodeName();
        Token<XMLTokenId> attrib = ContextUtilities.getAttributeToken(context.getDocumentContext());
        String attribName = attrib != null ? attrib.text().toString(): null;

        BeansCompletor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        }
        
        return anchorOffset;
    }

    public int completeValues(CompletionContext context, List<BeansCompletionItem> valueItems) {
        int anchorOffset = -1;         
        DocumentContext docContext = context.getDocumentContext();
        SyntaxElement curElem = docContext.getCurrentElement();
        SyntaxElement prevElem = docContext.getCurrentElement().getPrevious();

        String tagName = curElem.getType() == Node.ELEMENT_NODE ? curElem.getNode().getNodeName() : null;
        BeansCompletor completor = locateCompletor(tagName, null);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
             if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        } 
        return anchorOffset;
    }

    public int completeAttributes(CompletionContext context, List<BeansCompletionItem> attributeItems) {
        return -1;
    }

    public int completeElements(CompletionContext context, List<BeansCompletionItem> elementItems) {
        return -1;
    }

    

    private void registerCompletor(String tagName, String attribName,
            BeansCompletor completor) {
        completors.put(createRegisteredName(tagName, attribName), completor);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (nodeName != null && nodeName.trim().length() > 0) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (attributeName != null && attributeName.trim().length() > 0) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private BeansCompletor locateCompletor(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        return null;
    }
}
