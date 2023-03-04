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
package org.netbeans.modules.refactoring.java.api;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementIcons;

/**
 * Wrapper class for ElementHandles, TreePathHandles and TypeMirrorHandles.
 * It contains referemce to appropriste handle + name and icon
 * @author Jan Becicka
 */
public final class MemberInfo<H> {
    private H member;
    private String htmlText;
    private Icon icon;
    private Group group;
    private Set<Modifier> modifiers;
    private boolean makeAbstract;
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum Group {
        /** 
         * member is in implements clause
         */ 
        IMPLEMENTS,
        /**  
         * member represents method
         */
        METHOD,
        /**  
         * member represents field
         */
        FIELD,
        /**
         * member represents type
         */
        TYPE;
    }
    
    /** Creates a new instance of MemberInfo describing a field
     * to be pulled up.
     * @param member 
     * @param name 
     * @param htmlText 
     * @param icon 
     */
    private MemberInfo(H member, String name, String htmlText, Icon icon) {
        this.member = member;
        this.htmlText = htmlText;
        this.icon = icon;
        this.name =name;
    }
    
    public H getElementHandle() {
        return member;
    }
    
    /**
     * 
     * @return 
     */
    public String getHtmlText() {
        return htmlText;
    }

    public static <T extends TypeMirror> MemberInfo<TypeMirrorHandle<T>> create(T el, Tree t, CompilationInfo c) {
        MemberInfo<TypeMirrorHandle<T>> mi = new MemberInfo<TypeMirrorHandle<T>>(TypeMirrorHandle.create(el), t.toString(), "implements " + t.toString(), ElementIcons.getElementIcon(ElementKind.INTERFACE, null)); // NOI18N
        mi.group = Group.IMPLEMENTS;
        return mi;
    }

    public static <T extends Element> MemberInfo<ElementHandle<T>> create(T el, CompilationInfo c) {
        String format = ElementHeaders.NAME;
        Group g = Group.TYPE;
        if (el.getKind() == ElementKind.FIELD) {
            format += " : " + ElementHeaders.TYPE; // NOI18N
            g=Group.FIELD;
        } else if (el.getKind() == ElementKind.METHOD) {
            format += ElementHeaders.PARAMETERS + " : " + ElementHeaders.TYPE; // NOI18N
            g=Group.METHOD;
        } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
            format += ElementHeaders.PARAMETERS;
            g=Group.METHOD;
        } 

        MemberInfo<ElementHandle<T>> mi = new MemberInfo<ElementHandle<T>>(ElementHandle.create(el), el.getSimpleName().toString(), ElementHeaders.getHeader(el, c, format), ElementIcons.getElementIcon(el.getKind(), el.getModifiers()));
        mi.modifiers = el.getModifiers();
        mi.group = g;
        return mi;
    }

    public static <T extends Element> MemberInfo<ElementHandle<T>> create(T el, CompilationInfo c, Group group) {
        MemberInfo<ElementHandle<T>> mi = new MemberInfo<ElementHandle<T>>(ElementHandle.create(el), el.getSimpleName().toString(), ElementHeaders.getHeader(el, c, ElementHeaders.NAME), ElementIcons.getElementIcon(el.getKind(), el.getModifiers()));
        mi.group = group;
        mi.modifiers = el.getModifiers();
        return mi;
    }

    public static MemberInfo<TreePathHandle> create(TreePath tpath, CompilationInfo c) {
        String format = ElementHeaders.NAME;
        Group g = null;
        Element el = c.getTrees().getElement(tpath);
        if (el == null) {
            return null;
        }
        if (el.getKind() == ElementKind.FIELD) {
            format += " : " + ElementHeaders.TYPE; // NOI18N
            g=Group.FIELD;
        } else if (el.getKind() == ElementKind.METHOD) {
            format += ElementHeaders.PARAMETERS + " : " + ElementHeaders.TYPE; // NOI18N
            g=Group.METHOD;
        } else if (el.getKind().isInterface()) {
            g=Group.IMPLEMENTS;
            format = "implements " + format; // NOI18N
        }

        MemberInfo<TreePathHandle> mi = new MemberInfo<TreePathHandle>(TreePathHandle.create(tpath, c), el.getSimpleName().toString(), ElementHeaders.getHeader(el, c, format), ElementIcons.getElementIcon(el.getKind(), el.getModifiers()));
        mi.modifiers = el.getModifiers();
        mi.group = g;
        return mi;
    }

    private MemberInfo(H handle, String htmlText, Icon icon, String name, Group group, Set<Modifier> modifiers, boolean makeAbstract) {
        this.member = handle;
        this.htmlText = htmlText;
        this.icon = icon;
        this.name = name;
        this.group = group;
        this.modifiers = modifiers;
        this.makeAbstract = makeAbstract;
    }

    public static <T extends TypeMirror> MemberInfo<TypeMirrorHandle<T>> createImplements(TypeMirrorHandle handle, String htmlText, Icon icon, String name) {
        return new MemberInfo<TypeMirrorHandle<T>>(handle, htmlText, icon, name, Group.IMPLEMENTS, Collections.<Modifier>emptySet(), false);
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public Group getGroup() {
        return group;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof MemberInfo && ((MemberInfo) o).member instanceof ElementHandle) {
            return ((ElementHandle) ((MemberInfo) o).member).signatureEquals((ElementHandle)this.member);
        }
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return member.hashCode();
    }
    
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    public boolean isMakeAbstract() {
        return makeAbstract;
    }
    
    public void setMakeAbstract(Boolean b) {
        this.makeAbstract = b;
    }
    
    @Override
    public String toString() {
        return htmlText;
    }
    
}
