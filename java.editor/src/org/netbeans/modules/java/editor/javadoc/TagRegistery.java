/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;

/**
 *
 * @author Jan Pokorsky
 */
final class TagRegistery {
    
    private static final Set<ElementKind> ALL_KINDS = EnumSet.<ElementKind>of(
            ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
            ElementKind.CONSTRUCTOR, ElementKind.ENUM,
            ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
            ElementKind.INTERFACE, ElementKind.METHOD,
            // OTHER stands for Overview here
            ElementKind.OTHER, ElementKind.MODULE,
            ElementKind.PACKAGE);
    
    private static final TagRegistery DEFAULT = new TagRegistery();
    
    private final List<TagEntry> tags;
    
    public static TagRegistery getDefault() {
        return DEFAULT;
    }

    public List<TagEntry> getTags(ElementKind kind, boolean inline) {
        List<TagEntry> selection = new ArrayList<TagEntry>();
        for (TagEntry te : tags) {
            if (te.isInline == inline && te.whereUsed.contains(kind)) {
                selection.add(te);
            }
        }
        return selection;
    }
    
    private TagRegistery() {
        this.tags = new ArrayList<TagEntry>(20);
        addTag("@author", false, EnumSet.of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.ENUM, ElementKind.INTERFACE,
                ElementKind.OTHER, ElementKind.MODULE,
                ElementKind.PACKAGE));
        addTag("@exception", false, EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR));
        // deprecated: not in PACKAGE and OVERVIEW!
        addTag("@deprecated", false, EnumSet.<ElementKind>of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.CONSTRUCTOR, ElementKind.ENUM,
                ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
                ElementKind.INTERFACE, ElementKind.METHOD));

        addTag("@param", false, EnumSet.of(
                ElementKind.METHOD, ElementKind.CONSTRUCTOR, ElementKind.CLASS,
                ElementKind.INTERFACE));
        addTag("@return", false, EnumSet.of(ElementKind.METHOD));
        addTag("@see", false, ALL_KINDS);
        addTag("@serial", false, EnumSet.<ElementKind>of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.ENUM,
                ElementKind.ENUM_CONSTANT, ElementKind.FIELD,
                ElementKind.INTERFACE,
                ElementKind.PACKAGE));

        // serialData can be used just for writeObject, readObject, writeExternal, readExternal, writeReplace, and readResolve methods
        addTag("@serialData", false, EnumSet.of(ElementKind.METHOD));
        addTag("@serialField", false, EnumSet.of(ElementKind.FIELD));
        addTag("@since", false, ALL_KINDS);
        addTag("@throws", false, EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR));
        addTag("@version", false, EnumSet.<ElementKind>of(
                ElementKind.ANNOTATION_TYPE, ElementKind.CLASS,
                ElementKind.ENUM,
                ElementKind.INTERFACE,
                ElementKind.OTHER, ElementKind.PACKAGE));
        addTag("@hidden", false, ALL_KINDS);
        addTag("@provides", false, EnumSet.of(ElementKind.MODULE));
        addTag("@uses", false, EnumSet.of(ElementKind.MODULE));
        
        addTag("@code", true, ALL_KINDS);
        addTag("@docRoot", true, ALL_KINDS);
        // just in empty tag description
        addTag("@inheritDoc", true, EnumSet.of(ElementKind.METHOD));
        addTag("@link", true, ALL_KINDS);
        addTag("@linkplain", true, ALL_KINDS);
        addTag("@literal", true, ALL_KINDS);
        addTag("@value", true, EnumSet.of(ElementKind.FIELD));
    }
    
    void addTag(String name, boolean isInline, Set<ElementKind> where) {
        if (name == null || where == null) {
            throw new NullPointerException();
        }
        TagEntry te = new TagEntry();
        te.name = name;
        te.isInline = isInline;
        te.whereUsed = where;
        this.tags.add(te);
    }
    
    static final class TagEntry {
        String name;
        boolean isInline;
        Set<ElementKind> whereUsed;
        String format;
    }
}
