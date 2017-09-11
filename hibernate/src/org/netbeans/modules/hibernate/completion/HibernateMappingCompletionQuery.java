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
package org.netbeans.modules.hibernate.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.hibernate.completion.CompletionContext.CompletionType;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingCompletionQuery extends AsyncCompletionQuery {

    private int queryType;
    private int caretOffset;
    private JTextComponent component;

    public HibernateMappingCompletionQuery(int queryType, int caretOffset) {
        this.queryType = queryType;
        this.caretOffset = caretOffset;
    }

    @Override
    protected void preQueryUpdate(JTextComponent component) {
        //XXX: look for invalidation conditions
        this.component = component;
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }

    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        List<HibernateCompletionItem> completionItems = new ArrayList<HibernateCompletionItem>();
        int anchorOffset = getCompletionItems(doc, caretOffset, completionItems);
        resultSet.addAllItems(completionItems);
        if(anchorOffset != -1) {
            resultSet.setAnchorOffset(anchorOffset);
        }
        
        resultSet.finish();
    }
    
    // This method is here for unit testing purpose
    int getCompletionItems(Document doc, int caretOffset, List<HibernateCompletionItem> completionItems) {
        int anchorOffset = -1;
        
        CompletionContext context = new CompletionContext(doc, caretOffset);
        
        if (context.getCompletionType() == CompletionType.NONE) {
            return anchorOffset;
        }

        switch (context.getCompletionType()) {
            case ATTRIBUTE_VALUE:
                anchorOffset = HibernateMappingCompletionManager.getDefault().completeAttributeValues(context, completionItems);
                break;
            case ATTRIBUTE:
                anchorOffset = HibernateMappingCompletionManager.getDefault().completeAttributes(context, completionItems);
                break;
            case TAG:
                anchorOffset = HibernateMappingCompletionManager.getDefault().completeElements(context, completionItems);
                break;
            }
        
        return anchorOffset;
    }
}
