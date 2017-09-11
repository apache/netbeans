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
