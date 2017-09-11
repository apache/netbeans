/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.spring.beans.completion.completors;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.openide.util.Exceptions;

/**
 * Handles the bean tag's depends-on attribute value completion
 * 
 * @author Rohan Ranade
 */
public class BeanDependsOnCompletor extends BeansRefCompletor {

    public BeanDependsOnCompletor(boolean includeGlobal, int invocationOffset) {
        super(includeGlobal, invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        int index = context.getCurrentTokenOffset() + 1;
        String prefix = context.getTypedPrefix();
        if (StringUtils.hasText(prefix)) {
            int sepOffset = Math.max(Math.max(prefix.lastIndexOf(','), prefix.lastIndexOf(';')), prefix.lastIndexOf(' ')); // NOI18N
            if (sepOffset != -1) {
                index += sepOffset + 1;
            }
        }

        return index;
    }

    @Override
    protected String getContextPrefix(CompletionContext context) {
        String contextPrefix = "";
        try {
            contextPrefix = context.getDocument().getText(getAnchorOffset(), context.getCaretOffset() - getAnchorOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return contextPrefix;
    }

    @Override
    protected Set<String> getForbiddenNames(CompletionContext context) {
        // filter out existing entries in the value string
        String typedPrefix = context.getTypedPrefix();
        if(!StringUtils.hasText(typedPrefix)) {
            return Collections.emptySet();
        }
        
        int startIdx = context.getCurrentTokenOffset() + 1;
        int length = getAnchorOffset() - startIdx;
        
        if(length <= 0) {
            return Collections.emptySet();
        }
        
        String existingStr = typedPrefix.substring(0, length);
        List<String> names = StringUtils.tokenize(existingStr, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS);
        return new HashSet<String>(names);
    }
}
