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

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;

/**
 * A simple completor for general attribute value items
 * 
 * Takes an array of strings, the even elements being the display text of the items
 * and the odd ones being the corresponding documentation of the items
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class AttributeValueCompletor extends Completor {

    private final String[] itemTextAndDocs;

    public AttributeValueCompletor(String[] itemTextAndDocs, int invocationOffset) {
        super(invocationOffset);
        this.itemTextAndDocs = itemTextAndDocs;
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        return context.getCurrentTokenOffset() + 1;
    }
    
    @Override
    protected void compute(CompletionContext context) throws IOException {
        int caretOffset = context.getCaretOffset();
        String typedChars = context.getTypedPrefix();
        
        for (int i = 0; i < itemTextAndDocs.length; i += 2) {
            if(isCancelled()) {
                return;
            }
            
            if (itemTextAndDocs[i].startsWith(typedChars)) {
                SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                        itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                addCacheItem(item);
            }
        }
    }
        
    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.CHARACTER_STRING_ACCEPTOR);
    }

    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
}
            
}
