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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxEvent;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class EventCompleter implements Completer, Completer.Factory {
    
    private CompletionContext ctx;
    
    private FxInstance instance;
    
    private FxBean fxBean;
    
    private boolean moreItems;

    EventCompleter(CompletionContext ctx, FxInstance inst) {
        this.ctx = ctx;
        this.instance = inst;
        this.fxBean = inst.getDefinition();
    }
    
    public EventCompleter() {
    }
    
    private EventCompletionItem createItem(FxEvent e, boolean noInherit) {
        String cn = e.getEventClassName();
        int dot = cn.lastIndexOf('.');
        cn = cn.substring(dot + 1);
        EventCompletionItem item = new EventCompletionItem(
                cn, 
                ctx.isAttribute(), 
                ctx, 
                e.getSymbol());
        item.setInherited(noInherit ||
                fxBean.getDeclareadInfo().getEvent(e.getName()) != null);
        return item;
    }
    
    private void completeShallow(List<CompletionItem> result) {
        String prefix = ctx.getPrefix();
        
        Set<String> eventNames = new HashSet<String>();
        
        if (prefix.contains("on")) {
            eventNames.addAll(fxBean.getEventNames());
            if (fxBean.usesBuilder()) {
                eventNames.addAll(fxBean.getBuilder().getEventNames());
            }
        } else {
            eventNames.addAll(fxBean.getDeclareadInfo().getEventNames());
            if (fxBean.usesBuilder()) {
                eventNames.addAll(fxBean.getBuilder().getDeclareadInfo().getEventNames());
            }
            moreItems = true;
        }
        for (String s : eventNames) {
            FxEvent e = fxBean.getEvent(s);
            if (e == null && fxBean.usesBuilder()) {
                e = fxBean.getBuilder().getEvent(s);
            }
            if (e.isPropertyChange()) {
                moreItems = true;
                continue;
            }
            result.add(createItem(e, true));
        }
    }
    
    private void completeAllEvents(List<CompletionItem> result) {
        for (String s : fxBean.getEventNames()) {
            FxEvent e = fxBean.getEvent(s);
            result.add(createItem(e, true));
        }
        if (fxBean.usesBuilder()) {
            for (String s : fxBean.getBuilder().getEventNames()) {
                if (fxBean.getEventNames().contains(s)) {
                    continue;
                }
                FxEvent e = fxBean.getBuilder().getEvent(s);
                result.add(createItem(e, true));
            }
        }
    }

    @Override
    public List<? extends CompletionItem> complete() {
        List<CompletionItem> result = new ArrayList<CompletionItem>();
        if (ctx.getCompletionType() == CompletionProvider.COMPLETION_QUERY_TYPE) {
            completeShallow(result);
        } else {
            completeAllEvents(result);
        }
        return result;
    }

    @Override
    public boolean hasMoreItems() {
        return moreItems;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        switch (ctx.getType()) {
            case CHILD_ELEMENT:
            case PROPERTY:
            case PROPERTY_ELEMENT:
                break;
            default:
                return null;
        }
        FxNode n = ctx.getElementParent();
        if (n == null || n.getKind() != FxNode.Kind.Instance) {
            return null;
        }
        FxInstance inst = (FxInstance)n;
        if (inst.getDefinition() == null) {
            return null;
        }
        return new EventCompleter(ctx, inst);
    }
   
}
