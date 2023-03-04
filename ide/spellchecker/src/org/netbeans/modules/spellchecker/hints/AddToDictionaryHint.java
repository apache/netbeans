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
package org.netbeans.modules.spellchecker.hints;

import org.netbeans.modules.spellchecker.ComponentPeer;
import org.netbeans.modules.spellchecker.DictionaryImpl;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;

/**
 *
 * @author Jan Lahoda
 */
public final class AddToDictionaryHint implements EnhancedFix {

    private DictionaryImpl d;
    private String         word;
    private String         text;
    private ComponentPeer  peer;
    private String         sortText;

    public AddToDictionaryHint(ComponentPeer peer, DictionaryImpl d, String word, String text, String sortText) {
        this.peer = peer;
        this.d = d;
        this.word = word;
        this.text = text;
        this.sortText = sortText;
    }
    
    public String getText() {
        return String.format(text, word);
    }

    public ChangeInfo implement() {
        d.addEntry(word);
	peer.reschedule();
        
	return null;
    }

    public CharSequence getSortText() {
        return sortText;
    }

}
