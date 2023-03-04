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
