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
package org.netbeans.modules.java.editor.overridden;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import static javax.lang.model.element.ElementKind.METHOD;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.editor.java.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ElementDescription {
    
    private static final String PKG_COLOR = Utilities.getHTMLColor(192, 192, 192);
    private final ElementKind imageKind;

    private ClasspathInfo originalCPInfo;
    
    private ElementHandle<Element> handle;
    private ElementHandle<TypeElement> outtermostElement;
    private Collection<Modifier> modifiers;
    private String displayName;
    private final boolean overriddenFlag;

    public ElementDescription(CompilationInfo info, Element element, boolean overriddenFlag) {
        this.originalCPInfo = info.getClasspathInfo();
        this.handle = ElementHandle.create(element);
        if (METHOD == element.getKind() && null != element.getEnclosingElement()) {
            //when showing the implementors/overriders of a method, show the type icon (not the method icon)
            this.imageKind = element.getEnclosingElement().getKind();
        } else {
            this.imageKind = this.handle.getKind();
        }
        this.outtermostElement = ElementHandle.create(SourceUtils.getOutermostEnclosingTypeElement(element));
        this.modifiers = element.getModifiers();
        this.displayName = overriddenFlag ? computeDisplayNameIsOverridden(element) : computeDisplayNameOverrides(element);
        this.overriddenFlag = overriddenFlag;
    }

    private static String computeDisplayNameIsOverridden(Element element) throws IllegalStateException {
        TypeElement clazz;

        if (element.getKind().isClass() || element.getKind().isInterface()) {
            clazz = (TypeElement) element;
        } else {
            assert element.getKind() == ElementKind.METHOD : element.getKind();
            
            clazz = (TypeElement) element.getEnclosingElement();
        }

        StringBuilder displayName = new StringBuilder();
        Element parent = clazz.getEnclosingElement();

        displayName.append("<html>"); //NOI18N
        displayName.append(computeSimpleName(clazz));

        while (   isAnonymous(parent)
               || parent.getKind() == ElementKind.CONSTRUCTOR
               || parent.getKind() == ElementKind.INSTANCE_INIT
               || parent.getKind() == ElementKind.METHOD
               || parent.getKind() == ElementKind.STATIC_INIT) {
            displayName.append(' ');
            displayName.append(NbBundle.getMessage(ElementDescription.class, "NAME_In"));
            displayName.append(' ');
            displayName.append(computeSimpleName(parent));
            parent = parent.getEnclosingElement();
        }

        displayName.append(' ');
        displayName.append(PKG_COLOR);
        displayName.append('(');
        displayName.append(getQualifiedName(parent));
        displayName.append(')');

        return displayName.toString();
    }

    private static boolean isAnonymous(Element el) {
        if (!el.getKind().isClass()) return false;
        
        TypeElement clazz = (TypeElement) el;

        return    clazz.getQualifiedName() == null
               || clazz.getQualifiedName().length() == 0
               || clazz.getSimpleName() == null
               || clazz.getSimpleName().length() == 0;
    }

    private static String computeSimpleName(Element clazz) {
        String simpleName;

        if (isAnonymous(clazz)) {
            return NbBundle.getMessage(ElementDescription.class, "NAME_AnonynmousInner");
        } else {
            simpleName = clazz.getSimpleName().toString();
        }

        return simpleName;
    }

    private static CharSequence getQualifiedName(Element el) {
        if (el.getKind() == ElementKind.PACKAGE) {
            return ((PackageElement) el).getQualifiedName();
        } else if (el.getKind().isClass() || el.getKind().isInterface()) {
            return ((TypeElement) el).getQualifiedName();
        } else {
            throw new IllegalStateException();
        }
    }

    private static String computeDisplayNameOverrides(Element element) {
        return ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
    }

    public FileObject getSourceFile() {
        FileObject file = SourceUtils.getFile(outtermostElement, originalCPInfo);
        if (file != null)
            return SourceUtils.getFile(outtermostElement, ClasspathInfo.create(file));
        else
            return null;
    }
    
    public void open() {
        if (!ElementOpen.open(originalCPInfo, ElementDescription.this.getHandle())) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public ClasspathInfo getOriginalCPInfo() {
        return originalCPInfo;
    }

    public ElementHandle<Element> getHandle() {
        return handle;
    }

    public Icon getIcon() {
        Image badge;

        if (overriddenFlag) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/java/editor/resources/is-overridden-badge.png");
        } else {
            badge = ImageUtilities.loadImage("org/netbeans/modules/java/editor/resources/overrides-badge.png");
        }

        Image icon = ImageUtilities.icon2Image(ElementIcons.getElementIcon(imageKind, modifiers));

        return ImageUtilities.image2Icon(ImageUtilities.mergeImages(icon, badge, 16, 0));
    }

    public boolean isOverridden() {
        return overriddenFlag;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public Collection<Modifier> getModifiers() {
        return modifiers;
    }

}
