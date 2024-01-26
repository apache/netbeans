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

import java.io.IOException;
import java.util.*;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.*;

/**
 *
 * @author Christian Lenz
 */
public class AltAttributeVisitor implements ElementVisitor {

    private static final String ALT_ATTR = "alt"; // NOI18N

    private final HtmlRuleContext context;
    private final List<Hint> hints;

    public AltAttributeVisitor(Rule rule, HtmlRuleContext context, List<Hint> hints) throws IOException {
        this.context = context;
        this.hints = hints;
    }

    @Override
    public void visit(Element node) {
        // We should only be invoked for opening tags
        if (!(node instanceof OpenTag)) {
            return;
        }

        // We are only interested in img, area, applet elements
        String lowerCaseTag = node.id().toString().toLowerCase();
        if (!(lowerCaseTag.equals("img") || lowerCaseTag.equals("area") || lowerCaseTag.equals("applet"))) { // NOI18N
            return;
        }

        OpenTag ot = (OpenTag) node;
        if (ot.getAttribute(ALT_ATTR) == null) {
            hints.add(new AddMissingAltAttributeHint(context, new OffsetRange(node.from(), node.to())));
        }
    }
}
