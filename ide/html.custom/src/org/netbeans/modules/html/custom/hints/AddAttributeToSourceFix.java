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
package org.netbeans.modules.html.custom.hints;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.conf.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
@NbBundle.Messages(value = {
    "# {0} - a comma separated list of attribute names",
    "# {1} - name of the element where the attribute will be added",
    "addAttributeToSource=Add attribute(s) \"{0}\" to the element \"{1}\""
})
public final class AddAttributeToSourceFix implements HintFix {
    private final Collection<Attribute> attrs;
    private final OpenTag openTag;
    private final Snapshot snapshot;

    public AddAttributeToSourceFix(Collection<Attribute> attrs, OpenTag openTag, Snapshot snapshot) {
        this.attrs = attrs;
        this.openTag = openTag;
        this.snapshot = snapshot;
    }
    
    public AddAttributeToSourceFix(Attribute attributeName, OpenTag openTag, Snapshot snapshot) {
        this(Collections.singleton(attributeName), openTag, snapshot);
    }

    @Override
    public String getDescription() {
        return Bundle.addAttributeToSource(Utils.attributes2String(attrs), openTag.name().toString());
    }
   
    //todo use template
    @Override
    public void implement() throws Exception {
        final StringBuilder insertText = new StringBuilder();
        insertText.append(' '); //NOI18N
        Iterator<Attribute> i = attrs.iterator();
        while(i.hasNext()) {
            Attribute a = i.next();
            insertText.append(a.getName());
            insertText.append("=\"\""); //NOI18N
            if(i.hasNext()) {
                insertText.append(' '); //NOI18N
            }
        }
        final BaseDocument document = (BaseDocument)snapshot.getSource().getDocument(true);
        document.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {
                try {
                    int insertOffset = openTag.from() + "<".length() + openTag.name().length();
                    int documentInsertOffset = snapshot.getOriginalOffset(insertOffset);
                    document.insertString(documentInsertOffset, insertText.toString(), null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        });
        
        LexerUtils.rebuildTokenHierarchy(document);
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
    
}
