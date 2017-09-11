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

package org.netbeans.modules.editor.java;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.Scope;
import com.sun.source.util.Trees;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.java.completion.Utilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.CompositeCompletionItem;
import org.netbeans.spi.editor.completion.LazyCompletionItem;

/**
 *
 * @author Dusan Balek
 */
public abstract class LazyJavaCompletionItem<T extends Element> extends JavaCompletionItem.WhiteListJavaCompletionItem<T> implements LazyCompletionItem {

    public static JavaCompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends, WhiteListQuery.WhiteList whiteList) {
        return new TypeItem(handle, kinds, substitutionOffset, referencesCount, source, insideNew, addTypeVars, afterExtends, whiteList);
    }

    public static JavaCompletionItem createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source, WhiteListQuery.WhiteList whiteList) {
        return new StaticMemberItem(handle, name, substitutionOffset, addSemicolon, referencesCount, source, whiteList);
    }

    private LazyJavaCompletionItem(int substitutionOffset, ElementHandle<? extends Element> handle, Source source, WhiteListQuery.WhiteList whiteList) {
        super(substitutionOffset, handle, whiteList);
        this.source = source;
    }

    private Source source;
    private JavaCompletionItem delegate = null;

    @Override
    public boolean accept() {
        if (delegate == null && getElementHandle() != null) {
            try {
                JavaCompletionProvider.JavaCompletionQuery.javadocBreak.set(true);
                ParserManager.parse(Collections.singletonList(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController controller = CompilationController.get(resultIterator.getParserResult(substitutionOffset));
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        T t = getElementHandle().resolve(controller);
                        if (t != null) {
                            ScopeHolder scopeHolder = (ScopeHolder) controller.getCachedValue(ScopeHolder.class);
                            if (scopeHolder == null || scopeHolder.pos != substitutionOffset) {
                                scopeHolder = new ScopeHolder(substitutionOffset, controller.getTrees().getScope(controller.getTreeUtilities().pathFor(substitutionOffset)));
                                controller.putCachedValue(ScopeHolder.class, scopeHolder, CompilationInfo.CacheClearPolicy.ON_CHANGE);
                            }
                            delegate = getDelegate(controller, scopeHolder.scope, t);
                        }
                    }
                });
            } catch (ParseException t) {
            }
        }
        return delegate != null;
    }

    protected abstract JavaCompletionItem getDelegate(CompilationInfo info, Scope scope, T t);
    
    protected JavaCompletionItem getDelegate() {
        return delegate;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        if (delegate != null) {
            delegate.defaultAction(component);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
        if (delegate != null) {
            delegate.processKeyEvent(evt);
        }
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        if (delegate != null) {
            return delegate.getPreferredWidth(g, defaultFont);
        }
        return 0;
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        if (delegate != null) {
            delegate.render(g, defaultFont, defaultColor, backgroundColor, width, height, selected);
        }
    }

    @Override
    public CompletionTask createDocumentationTask() {
        if (delegate != null) {
            return delegate.createDocumentationTask();
        }
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        if (delegate != null) {
            return delegate.createToolTipTask();
        }
        return null;
    }

    static class TypeItem extends LazyJavaCompletionItem<TypeElement> implements CompositeCompletionItem {

        private EnumSet<ElementKind> kinds;
        private boolean insideNew;
        private boolean addTypeVars;
        private boolean afterExtends;
        private String name;
        private String simpleName;
        private String pkgName;
        private CharSequence sortText;
        private ReferencesCount referencesCount;

        private TypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, handle, source, whiteList);
            this.kinds = kinds;
            this.insideNew = insideNew;
            this.addTypeVars = addTypeVars;
            this.afterExtends = afterExtends;
            this.name = handle.getQualifiedName();
            int idx = name.lastIndexOf('.');
            this.simpleName = idx > -1 ? name.substring(idx + 1) : name;
            this.pkgName = idx > -1 ? name.substring(0, idx) : ""; //NOI18N
            this.sortText = new LazySortText(this.simpleName, this.pkgName, handle, referencesCount);
            this.referencesCount = referencesCount;
        }

        @Override
        protected JavaCompletionItem getDelegate(CompilationInfo info, Scope scope, TypeElement te) {
            Elements elements = info.getElements();
            if (te != null && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(te)) && info.getTrees().isAccessible(scope, te)) {
                if (isOfKind(te, kinds) && (!afterExtends || !te.getModifiers().contains(Modifier.FINAL)) && (!isInDefaultPackage(te) || isInDefaultPackage(scope.getEnclosingClass())) && !Utilities.isExcluded(te.getQualifiedName())) {
                    return createTypeItem(info, te, (DeclaredType) te.asType(), substitutionOffset, referencesCount, elements.isDeprecated(te), insideNew, addTypeVars, false, false, false, getWhiteList());
                }
            }
            return null;
        }

        @Override
        public int getSortPriority() {
            return 700;
        }

        @Override
        public CharSequence getSortText() {
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        @Override
        public List<? extends CompletionItem> getSubItems() {
            return getDelegate() instanceof CompositeCompletionItem ? ((CompositeCompletionItem)getDelegate()).getSubItems() : Collections.<CompletionItem>emptyList();
        }

        @Override
        public String toString() {
            return name;
        }

        private boolean isOfKind(Element e, EnumSet<ElementKind> kinds) {
            if (kinds.contains(e.getKind())) {
                return true;
            }
            for (Element ee : e.getEnclosedElements()) {
                if (isOfKind(ee, kinds)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isInDefaultPackage(Element e) {
            while (e != null && e.getKind() != ElementKind.PACKAGE) {
                e = e.getEnclosingElement();
            }
            return e != null && e.getSimpleName().length() == 0;
        }
    }

    private static class StaticMemberItem extends LazyJavaCompletionItem<TypeElement> {

        private boolean addSemicolon;
        private String name;
        private CharSequence sortText;

        private StaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source, WhiteListQuery.WhiteList whiteList) {
            super(substitutionOffset, handle, source, whiteList);
            this.name = name;
            this.sortText = new LazySortText(this.name, handle.getQualifiedName(), handle, referencesCount);
            this.addSemicolon = addSemicolon;
        }

        @Override
        protected JavaCompletionItem getDelegate(CompilationInfo info, Scope scope, TypeElement te) {
            Elements elements = info.getElements();
            Trees trees = info.getTrees();
            if (te != null) {
                Element element = null;
                boolean multiVersion = false;
                for (Element e : te.getEnclosedElements()) {
                    if ((e.getKind().isField() || e.getKind() == ElementKind.METHOD)
                            && name.contentEquals(Utilities.isCaseSensitive() ? e.getSimpleName() : e.getSimpleName().toString().toLowerCase())
                            && e.getModifiers().contains(Modifier.STATIC)
                            && (Utilities.isShowDeprecatedMembers() || !elements.isDeprecated(e))
                            && trees.isAccessible(scope, e, (DeclaredType) te.asType())) {
                        if (element != null) {
                            multiVersion = true;
                            break;
                        }
                        element = e;
                    }
                }
                if (element != null) {
                    name = element.getSimpleName().toString();
                    return createStaticMemberItem(info, (DeclaredType) te.asType(), element, element.asType(), multiVersion, substitutionOffset, elements.isDeprecated(element), addSemicolon, getWhiteList());
                }
            }
            return null;
        }

        @Override
        public int getSortPriority() {
            return 710;
        }

        @Override
        public CharSequence getSortText() {
            return sortText;
        }

        @Override
        public CharSequence getInsertPrefix() {
            return name;
        }
    }
    
    private static class ScopeHolder {

        private int pos;
        private Scope scope;

        private ScopeHolder(int pos, Scope scope) {
            this.pos = pos;
            this.scope = scope;
        }
    }
}
