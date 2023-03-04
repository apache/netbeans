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

package org.netbeans.modules.spring.beans.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.LazyCompletionItem;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 * Based on LazyTypeCompletionItem from Java Editor module
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class LazyTypeCompletionItem extends SpringXMLConfigCompletionItem implements LazyCompletionItem {

    public static LazyTypeCompletionItem create(int substitutionOffset, ElementHandle<TypeElement> eh, JavaSource javaSource) {
        return new LazyTypeCompletionItem(substitutionOffset, eh, javaSource);
    }
    
    private ElementHandle<TypeElement> eh;
    private JavaSource javaSource;
    private String name;
    private SpringXMLConfigCompletionItem delegate;
    private String simpleName;
    private String pkgName;
    private int prefWidth = -1;
    private String sortText;
    
    private LazyTypeCompletionItem(int substitutionOffset, ElementHandle<TypeElement> eh, JavaSource javaSource) {
        super(substitutionOffset);
        this.eh = eh;
        this.javaSource = javaSource;
        this.name = eh.getQualifiedName();
        int idx = name.lastIndexOf('.'); //NOI18N
        this.simpleName = idx > -1 ? name.substring(idx + 1) : name;
        this.pkgName = idx > -1 ? name.substring(0, idx) : ""; //NOI18N
        this.sortText = this.simpleName + SpringXMLConfigCompletionItem.getImportanceLevel(this.pkgName) + "#" + this.pkgName; // NOI18N
    }

    public boolean accept() {
        if(eh != null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController cc) throws Exception {
                        cc.toPhase(Phase.ELEMENTS_RESOLVED);
                        if (eh != null) {
                            TypeElement e = eh.resolve(cc);
                            if (e != null && isAccessibleClass(e)) {
                                delegate = SpringXMLConfigCompletionItem.createTypeItem(substitutionOffset,
                                            e, eh, cc.getElements().isDeprecated(e), true);
                            }
                            eh = null;
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        return delegate != null;
    }
    
    private static boolean isAccessibleClass(TypeElement te) {
        NestingKind nestingKind = te.getNestingKind();
        return (nestingKind == NestingKind.TOP_LEVEL) || (nestingKind == NestingKind.MEMBER && te.getModifiers().contains(Modifier.STATIC));
    }
    
    public int getSortPriority() {
        return 200;
    }

    public CharSequence getSortText() {
        return sortText;
    }

    public CharSequence getInsertPrefix() {
        return simpleName;
    }

    @Override
    public CompletionTask createDocumentationTask() {
        if(delegate != null) {
            return delegate.createDocumentationTask();
        }
        
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        if(delegate != null) {
            return delegate.createToolTipTask();
        }
        
        return null;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (delegate != null)
            delegate.defaultAction(component);
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (delegate != null)
            delegate.processKeyEvent(evt);
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        if (prefWidth < 0)
            prefWidth = CompletionUtilities.getPreferredWidth(simpleName + " (" + pkgName + ")", null, g, defaultFont); //NOI18N
        return prefWidth;
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        if (delegate != null)
            delegate.render(g, defaultFont, defaultColor, backgroundColor, width, height, selected);
    }
}
