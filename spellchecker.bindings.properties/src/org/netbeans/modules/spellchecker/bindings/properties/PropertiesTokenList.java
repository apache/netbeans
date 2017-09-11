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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
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
