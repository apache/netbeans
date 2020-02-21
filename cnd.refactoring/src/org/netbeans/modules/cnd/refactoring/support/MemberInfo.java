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
package org.netbeans.modules.cnd.refactoring.support;

import java.util.EnumSet;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;

/**
 * Wrapper class for ElementHandles, TreePathHandles and TypeMirrorHandles.
 * It contains referemce to appropriste handle + name and icon
 */
public final class MemberInfo<H> {

    private final H member;
    private final String htmlText;
    private final Icon icon;
    private final Group group;
    private final Set<CsmVisibility> visibility;
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
         * No group info
         */
        NONE,
//        /**
//         * member is in implements clause
//         */
//        IMPLEMENTS,
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
    
//    /** Creates a new instance of MemberInfo describing a field
//     * to be pulled up.
//     * @param member
//     * @param name
//     * @param htmlText
//     * @param icon
//     */
//    private MemberInfo(H member, String name, String htmlText, Icon icon) {
//        this.member = member;
//        this.htmlText = htmlText;
//        this.icon = icon;
//        this.name =name;
//        this.group = Group.NONE;
//        this.visibility = EnumSet.of(CsmVisibility.NONE);
//    }
    
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

//    public static <T extends TypeMirror> MemberInfo<TypeMirrorHandle<T>> create(T el, Tree t, CompilationInfo c) {
//        MemberInfo<TypeMirrorHandle<T>> mi = new MemberInfo<TypeMirrorHandle<T>>(TypeMirrorHandle.create(el), t.toString(), "implements " + t.toString(), ElementIcons.getElementIcon(ElementKind.INTERFACE, null)); // NOI18N
//        mi.group = Group.IMPLEMENTS;
//        return mi;
//    }
//
//    public static <T extends Element> MemberInfo<ElementHandle<T>> create(T el, CompilationInfo c) {
//        String format = ElementHeaders.NAME;
//        Group g = Group.TYPE;
//        if (el.getKind() == ElementKind.FIELD) {
//            format += " : " + ElementHeaders.TYPE; // NOI18N
//            g=Group.FIELD;
//        } else if (el.getKind() == ElementKind.METHOD) {
//            format += ElementHeaders.PARAMETERS + " : " + ElementHeaders.TYPE; // NOI18N
//            g=Group.METHOD;
//        } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
//            format += ElementHeaders.PARAMETERS;
//            g=Group.METHOD;
//        }
//
//        MemberInfo<ElementHandle<T>> mi = new MemberInfo<ElementHandle<T>>(ElementHandle.create(el), el.getSimpleName().toString(), ElementHeaders.getHeader(el, c, format), ElementIcons.getElementIcon(el.getKind(), el.getModifiers()));
//        mi.visibility = el.getModifiers();
//        mi.group = g;
//        return mi;
//    }
//
//    public static <T extends Element> MemberInfo<ElementHandle<T>> create(T el, CompilationInfo c, Group group) {
//        MemberInfo<ElementHandle<T>> mi = new MemberInfo<ElementHandle<T>>(ElementHandle.create(el), el.getSimpleName().toString(), ElementHeaders.getHeader(el, c, ElementHeaders.NAME), ElementIcons.getElementIcon(el.getKind(), el.getModifiers()));
//        mi.group = group;
//        mi.visibility = el.getModifiers();
//        return mi;
//    }
//
    public static <T extends CsmMember> MemberInfo<T> create(T elem) {
//        String format = "%name%"; // ElementHeaders.NAME; // NOI18N
        CharSequence htmlText = elem.getName();
        Group g = null;
        if (CsmKindUtilities.isField(elem)) {
            CsmField field = (CsmField) elem;
//            format += " : " + "%type%"; //NOI18N ElementHeaders.TYPE; // NOI18N
            htmlText = field.getName().toString() + ": " + field.getType().getText(); //NOI18N
            g=Group.FIELD;
        } else if (CsmKindUtilities.isMethod(elem)) {
            CsmMethod method = (CsmMethod) elem;
//            format += "%parameters%" + " : " + "%type%"; //NOI18N ElementHeaders.TYPE; // NOI18N
            htmlText = method.getSignature() + ": " + method.getReturnType().getText(); //NOI18N
            g=Group.METHOD;
        }

        MemberInfo<T> mi = new MemberInfo<>(elem, htmlText.toString(), CsmImageLoader.getIcon(elem), elem.getName().toString(), g, EnumSet.of(elem.getVisibility()), false);
        return mi;
    }

    private MemberInfo(H handle, String htmlText, Icon icon, String name, Group group, Set<CsmVisibility> modifiers, boolean makeAbstract) {
        this.member = handle;
        this.htmlText = htmlText;
        this.icon = icon;
        this.name = name;
        this.group = group;
        this.visibility = modifiers;
        this.makeAbstract = makeAbstract;
    }

//    public static <T extends TypeMirror> MemberInfo<TypeMirrorHandle<T>> createImplements(TypeMirrorHandle handle, String htmlText, Icon icon, String name) {
//        return new MemberInfo<TypeMirrorHandle<T>>(handle, htmlText, icon, name, Group.IMPLEMENTS, Collections.<Modifier>emptySet(), false);
//    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public Group getGroup() {
        return group;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof MemberInfo && ((MemberInfo) o).member instanceof CsmObject) {
            return ((CsmObject) ((MemberInfo) o).member).equals((CsmObject)this.member);
        }
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return member.hashCode();
    }
    
    public Set<CsmVisibility> getModifiers() {
        return visibility;
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
