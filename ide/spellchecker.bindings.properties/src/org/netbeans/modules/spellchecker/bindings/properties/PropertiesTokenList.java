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
package org.netbeans.modules.spellchecker.bindings.properties;

import javax.swing.text.BadLocationException;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.properties.syntax.PropertiesTokenContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Filters properties value tokens.
 *
 * @author Jan Jancura
 */
public class PropertiesTokenList extends AbstractTokenList {


    private boolean hidden = false;

    public PropertiesTokenList (BaseDocument doc) {
        super (doc);
    }

    @Override
    public void setStartOffset(int offset) {
        super.setStartOffset (offset);
        FileObject fileObject = FileUtil.getConfigFile ("Spellcheckers/Properties");
        Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
        hidden = Boolean.TRUE.equals (b);
    }


    @Override
    protected int[] findNextSpellSpan(SyntaxSupport ts, int offset) throws BadLocationException {
        if (ts == null || hidden)
            return new int[] {-1, -1};
        TokenItem item = null;
        int documentLength = ts.getDocument ().getLength ();
        if (offset < documentLength)
            item = ((ExtSyntaxSupport) ts).getTokenChain (offset, documentLength);
        while (item != null && item.getTokenID () != PropertiesTokenContext.VALUE)
            item = item.getNext ();
        if (item == null) return new int[] {-1, -1};
        return new int[] {item.getOffset (), item.getOffset () + item.getImage ().length ()};
    }
}
