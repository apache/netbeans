/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class ConstantCompleter  implements Completer, Completer.Factory {
    private CompletionContext ctx;
    private FxNewInstance instance;

    private static final String ICON_CONSTANT_VALUE = "org/netbeans/modules/javafx2/editor/resources/property.png"; // NOI18N

    ConstantCompleter(CompletionContext ctx, FxNewInstance instance) {
        this.ctx = ctx;
        this.instance = instance;
    }

    public ConstantCompleter() {
    }
    
    @Override
    public List<? extends CompletionItem> complete() {
        List<String> allValues = new ArrayList<String>(instance.getDefinition().getConstants());
        Collections.sort(allValues);
        String prefix = ctx.getPrefix();
        
        if (prefix != null && prefix.length() > 0) {
            for (Iterator<String> it = allValues.iterator(); it.hasNext(); ) {
                String s = it.next();
                if (!s.startsWith(prefix)) {
                    it.remove();
                }
            }
        }
        if (allValues.isEmpty()) {
            return null;
        }
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        for (String v : allValues) {
            ValueItem vi = new ValueItem(ctx, v, ICON_CONSTANT_VALUE);
            items.add(vi);
        }
        return items;
    }

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        FxInstance i = ctx.getInstanceElement();
        if (!(i instanceof FxNewInstance)) {
            return null;
        }
        FxNewInstance newInst = (FxNewInstance)i;
        String s = ctx.getPropertyName();
        if (s == null || !s.endsWith(":constant")) {
            return null;
        }
        return new ConstantCompleter(ctx, newInst);
    }
    
}
