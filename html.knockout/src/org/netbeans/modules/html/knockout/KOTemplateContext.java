/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public class KOTemplateContext {

    private final LinkedList<StackItem> scripts = new LinkedList<>();

    private boolean isScriptStart;

    private boolean isId;

    public Pair<Boolean, String> process(@NonNull Token<HTMLTokenId> token) {
        switch (token.id()) {
            case TAG_OPEN:
                isScriptStart = false;
                if ("script".equals(token.text().toString())) { // NOI18N
                    isScriptStart = true;
                    scripts.push(new StackItem());
                }
                break;
            case TAG_CLOSE_SYMBOL:
                if (isScriptStart && !scripts.isEmpty() && scripts.peek().getId() != null) {
                    return Pair.of(true, scripts.peek().getId());
                }
                isScriptStart = false;
            case TAG_CLOSE:
                if ("script".equals(token.text().toString()) && !scripts.isEmpty()) { // NOI18N
                    StackItem item = scripts.pop();
                    if (item.getId() != null) {
                        return Pair.of(false, item.getId());
                    }
                }
                break;
            case ARGUMENT:
                if (isScriptStart) {
                    isId = false;
                    if ("id".equals(token.text().toString())) { // NOI18N
                        isId = true;
                    }
                }
                break;
            case VALUE:
            case VALUE_CSS:
                if (isScriptStart && isId) {
                    CharSequence text = token.text();
                    // XXX
                    if (text.length() > 2 && !scripts.isEmpty()) {
                        scripts.peek().setId(token.text().subSequence(1, text.length() - 1).toString());
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    @CheckForNull
    public String getCurrentScriptId() {
        if (scripts.isEmpty()) {
            return null;
        }
        for (StackItem item : scripts) {
            if (item.getId() != null) {
                return item.getId();
            }
        }
        return null;
    }

    public void clear() {
        scripts.clear();
        isScriptStart = false;
        isId = false;
    }

    public static class TemplateUsage {

        private final KODataBindContext context;

        private final Set<String> parentTemplatesNames = new HashSet<>();

        public TemplateUsage(KODataBindContext context) {
            this.context = context;
        }

        public KODataBindContext getContext() {
            return context;
        }

        public void addParentTemplateName(String name) {
            parentTemplatesNames.add(name);
        }

        public Set<String> getParentTemplatesNames() {
            return Collections.unmodifiableSet(parentTemplatesNames);
        }

        public String getParentTemplateName() {
            if (parentTemplatesNames.isEmpty()) {
                return null;
            }
            return parentTemplatesNames.iterator().next();
        }
    }

    private static class StackItem {

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
