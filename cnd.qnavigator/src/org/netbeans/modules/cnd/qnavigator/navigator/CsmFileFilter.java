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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.util.NbPreferences;

/**
 *
 */
public class CsmFileFilter {
    
    public CsmFileFilter(){
        Preferences ps = NbPreferences.forModule(CsmFileFilter.class);
        sortByName = SortMode.valueOf(ps.get("SortByName", sortByName.name())); // NOI18N
    }

    public boolean isApplicable(CsmOffsetable object){
       if (!isShowForwardFunctionDeclarations() && CsmKindUtilities.isFunctionDeclaration(object)) {
            CsmFunctionDefinition def = ((CsmFunction) object).getDefinition();
            if (def != null && !def.equals(object) && !CsmKindUtilities.isMethod(def)) {
                return !object.getContainingFile().equals(def.getContainingFile());
            }
       } else if (!isShowForwardClassDeclarations() && 
               (CsmKindUtilities.isClassForwardDeclaration(object) ||
                CsmKindUtilities.isEnumForwardDeclaration(object))) {
           return false;
       } else if (!isShowTypedef() && CsmKindUtilities.isTypedef(object)) {
           return false;
       } else if ((!isShowVariable() || !isShowField())&& CsmKindUtilities.isVariable(object)) {
           if ( CsmKindUtilities.isClassMember(object)) {
               return isShowField();
           } else {
               return isShowVariable();
           }
       } else if (!isShowUsing() &&
                 (CsmKindUtilities.isUsing(object) ||
                  CsmKindUtilities.isNamespaceAlias(object))) {
           return false;
       }
        return true;
    }
    public boolean isApplicableInclude(){
        return isShowInclude();
    }
    public boolean isApplicableMacro(){
        return isShowMacro();
    }
    
    public boolean isShowInclude() {
        return showInclude.isSelected();
    }

    public void setShowInclude(boolean showInclude) {
        this.showInclude.setSelected(showInclude);
    }

    public boolean isShowMacro() {
        return showMacro.isSelected();
    }

    public void setShowMacro(boolean showMacro) {
        this.showMacro.setSelected(showMacro);
    }

    public boolean isShowForwardFunctionDeclarations() {
        return showForwardFunctionDeclarations.isSelected();
    }

    public void setShowForwardFunctionDeclarations(boolean showForwardFunctionDeclarations) {
        this.showForwardFunctionDeclarations.setSelected(showForwardFunctionDeclarations);
    }

    public boolean isShowForwardClassDeclarations() {
        return showForwardClassDeclarations.isSelected();
    }

    public void setShowForwardClassDeclarations(boolean showForwardClassDeclarations) {
        this.showForwardClassDeclarations.setSelected(showForwardClassDeclarations);
    }

    public boolean isShowTypedef() {
        return showTypedef.isSelected();
    }

    public void setShowTypedef(boolean showTypedef) {
        this.showTypedef.setSelected(showTypedef);
    }

    public boolean isShowVariable() {
        return showVariable.isSelected();
    }

    public void setShowVariable(boolean showVariable) {
        this.showVariable.setSelected(showVariable);
    }

    public boolean isShowField() {
        return showField.isSelected();
    }

    public void setShowField(boolean showField) {
        this.showField.setSelected(showField);
    }

    public boolean isShowUsing() {
        return showUsing.isSelected();
    }

    public void setShowUsing(boolean showUsing) {
        this.showUsing.setSelected(showUsing);
    }

    public SortMode getSortMode() {
        return sortByName;
    }

    public void setSortMode(SortMode sortByName) {
        this.sortByName = sortByName;
        Preferences ps = NbPreferences.forModule(CsmFileFilter.class);
        ps.put("SortByName", sortByName.name()); // NOI18N
    }

    public boolean isGroupByKind() {
        return groupByKind.isSelected();
    }

    public void setGroupByKind(boolean groupKind) {
        this.groupByKind.setSelected(groupKind);
    }

    public boolean isExpandAll() {
        return expandAll.isSelected();
    }

    public void setExpandAll(boolean expandAll) {
        this.expandAll.setSelected(expandAll);
    }

    private final BooleanFilter showForwardClassDeclarations = new BooleanFilter("ShowForwardClassDeclarations", true); // NOI18N
    private final BooleanFilter showForwardFunctionDeclarations = new BooleanFilter("ShowForwardFunctionDeclarations", false); // NOI18N
    private final BooleanFilter showInclude = new BooleanFilter("ShowInclude", false); // NOI18N
    private final BooleanFilter showMacro = new BooleanFilter("ShowMacro", true); // NOI18N;
    private final BooleanFilter showTypedef = new BooleanFilter("ShowTypedef", true); // NOI18N
    private final BooleanFilter showVariable = new BooleanFilter("ShowVariable", true); // NOI18N
    private final BooleanFilter showField = new BooleanFilter("ShowField", true); // NOI18N
    private final BooleanFilter showUsing = new BooleanFilter("ShowUsing", false); // NOI18N
    private SortMode sortByName = SortMode.Name;
    private final BooleanFilter groupByKind = new BooleanFilter("groupByKind", false); // NOI18N
    private final BooleanFilter expandAll = new BooleanFilter("expandAll", false); // NOI18N

    public enum SortMode { Name, Offset }

    private static final class BooleanFilter {
        private boolean value;
        private final String name;
        private BooleanFilter(String name, boolean defauilt){
            this.name = name;
            this.value = getPreferences().getBoolean(name, defauilt);
        }
        public boolean isSelected(){
            return value;
        }
        public void setSelected(boolean value){
            this.value = value;
            getPreferences().putBoolean(name, value);
        }
        private Preferences getPreferences(){
            return NbPreferences.forModule(CsmFileFilter.class);
        }
    }
}
