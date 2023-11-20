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
package org.netbeans.modules.html.editor.hints.other;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.utils.HtmlTagContextUtils;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Christian Lenz
 */
public class AddMissingAltAttributeHint extends Hint {

    public AddMissingAltAttributeHint(HtmlRuleContext context, OffsetRange range) {
        super(AddMissingAltAttributeRule.getInstance(),
            AddMissingAltAttributeRule.getInstance().getDescription(),
            context.getFile(),
            range,
            Collections.<HintFix>singletonList(new AddMissingAltAttributeHintFix(context, range)),
            10);
    }

    private static class AddMissingAltAttributeHintFix implements HintFix {

        private static final Logger LOGGER = Logger.getLogger(AddMissingAltAttributeHintFix.class.getSimpleName());

        HtmlRuleContext context;
        OffsetRange range;

        public AddMissingAltAttributeHintFix(HtmlRuleContext context, OffsetRange range) {
            this.context = context;
            this.range = range;
        }

        @Override
        public String getDescription() {
            return AddMissingAltAttributeRule.getInstance().getDisplayName();
        }

        @Override
        public void implement() throws Exception {
            EventQueue.invokeLater(() -> {
                try {
                    Source source = Source.create(context.getFile());
                    OffsetRange adjustContextRange = HtmlTagContextUtils.adjustContextRange(source.getDocument(false), range.getStart(), range.getEnd(), true);

                    source.getDocument(false).insertString(adjustContextRange.getEnd(), " alt=\"\"", null); // NOI18N
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested());
                }
            });

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

}
