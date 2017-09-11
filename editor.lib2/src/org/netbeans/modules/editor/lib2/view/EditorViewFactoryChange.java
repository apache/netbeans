/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Offset range describing change in particular view factory.
 * <br>
 * Each factory may fire a list of changes of different types at once.
 * 
 * @author Miloslav Metelka
 */
public final class EditorViewFactoryChange {
    
    private final int startOffset;
    
    private final int endOffset;
    
    private final Type type;

    public static EditorViewFactoryChange create(int startOffset, int endOffset, Type type) {
        return new EditorViewFactoryChange(startOffset, endOffset, type);
    }
    
    public static List<EditorViewFactoryChange> createList(int startOffset, int endOffset, Type type) {
        return Collections.singletonList(create(startOffset, endOffset, type));
    }

    public static List<EditorViewFactoryChange> createList(EditorViewFactoryChange... changes) {
        return Arrays.asList(changes);
    }

    private EditorViewFactoryChange(int startOffset, int endOffset, Type type) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.type = type;
    }
    
    int getStartOffset() {
        return startOffset;
    }
    
    int getEndOffset() {
        return endOffset;
    }
    
    Type getType() {
        return type;
    }
    
    public enum Type {
    
        /**
         * Characters may have their coloring changed but paragraph views can be retained.
         * Mark local views as dirty but they can be recomputed later when it's necessary
         * to display them.
         */
        CHARACTER_CHANGE,
        /**
         * Rebuild paragraph views in the given area as soon as possible since they might change
         * their vertical spans (e.g. due to fold collapse/expand).
         */
        PARAGRAPH_CHANGE,
        /**
         * Rebuild all views in the given area from scratch (typically whole document)
         * due to some global change e.g. change in settings (line height changed etc.).
         */
        REBUILD
    
    }

    @Override
    public String toString() {
        return getType() + ":<" + getStartOffset() + "," + getEndOffset() + ">"; // NOI18N
    }

}
