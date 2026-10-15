/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.schema.completion;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;

/**
 *
 * @author Alex Petrov (Alexey.Petrov@Sun.com)
 */
public class TagLastCharResultItem extends CompletionResultItem {
    private static final Logger _logger = Logger.getLogger(TagLastCharResultItem.class.getName());

    private int endTagSortPriority = -1;

    public TagLastCharResultItem(String tagName, TokenSequence tokenSequence) {
        super(null, null);
        this.itemText = tagName;
        setTokenSequence(tokenSequence);
    }

    @Override
    public String getDisplayText() {
        return CompletionUtil.TAG_LAST_CHAR;
    }

    @Override
    public String getReplacementText(){
        return getDisplayText();
    }

    @Override
    public int getCaretPosition() {
        return 0;
    }

    @Override
    public CompletionPaintComponent getPaintComponent() {
        if (component == null) {
            component = new CompletionPaintComponent.DefaultCompletionPaintComponent(this);
        }
        return component;
    }

    public void setSortPriority(int sortPriority) {
        this.endTagSortPriority = sortPriority;
    }

    @Override
    public int getSortPriority() {
        return endTagSortPriority;
    }

    @Override
    protected void replaceText(final JTextComponent component, final String text,
        final int offset, final int len) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    if (len > 0) doc.remove(offset, len);

                    String insertingText = getDisplayText();
                    doc.insertString(offset, insertingText, null);
                } catch (Exception e) {
                    _logger.log(Level.SEVERE,
                        e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
                }
            }
        });
    }
}