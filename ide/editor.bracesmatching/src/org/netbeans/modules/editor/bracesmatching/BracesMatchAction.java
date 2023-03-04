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
package org.netbeans.modules.editor.bracesmatching;

import java.awt.event.ActionEvent;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Vita Stejskal
 */
public final class BracesMatchAction extends BaseAction {

    private final boolean select;
    
    public static BracesMatchAction createNavigateAction() {
        return new BracesMatchAction(false);
    }
    
    public static BracesMatchAction createSelectAction() {
        return new BracesMatchAction(true);
    }
    
    public BracesMatchAction() {
        this(false);
    }

    public BracesMatchAction(boolean select) {
        super(select ? "selection-match-brace" : "match-brace"); //NOI18N
        this.select = select;
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(BracesMatchAction.class, (String) getValue(NAME)));
    }
    
    public void actionPerformed(ActionEvent e, JTextComponent component) {
        Document document = component.getDocument();
        Caret caret = component.getCaret();
        
        MasterMatcher.get(component).navigate(
            document,
            caret.getDot(), 
            caret,
            select
        );
    }

}
