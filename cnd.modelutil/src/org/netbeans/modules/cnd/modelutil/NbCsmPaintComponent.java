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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelutil;

import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import javax.swing.Icon;

/**
 * container of paint components
 * manager of icons
 */
public abstract class NbCsmPaintComponent extends CsmPaintComponent {

    public final static class NbNamespaceAliasPaintComponent extends CsmPaintComponent.NamespaceAliasPaintComponent{

        public NbNamespaceAliasPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.NAMESPACE_ALIAS, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }    

    public final static class NbNamespacePaintComponent extends CsmPaintComponent.NamespacePaintComponent{

        public NbNamespacePaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.NAMESPACE_DEFINITION, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbEnumPaintComponent extends CsmPaintComponent.EnumPaintComponent{
        
        public NbEnumPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.ENUM, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbEnumeratorPaintComponent extends CsmPaintComponent.EnumeratorPaintComponent {
        
        public NbEnumeratorPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.ENUMERATOR, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbMacroPaintComponent extends CsmPaintComponent.MacroPaintComponent{
        
        public NbMacroPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.MACRO, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }

    public final static class NbTemplateParameterPaintComponent extends CsmPaintComponent.TemplateParameterPaintComponent {
        
        public NbTemplateParameterPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getTempleteParameterIcon();
            setIcon(newIcon);
            return newIcon;            
        }
    }

    public final static class NbClassPaintComponent extends CsmPaintComponent.ClassPaintComponent{
        
        public NbClassPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.CLASS, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }

    public final static class NbTypedefPaintComponent extends CsmPaintComponent.TypedefPaintComponent{
        
        public NbTypedefPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.TYPEDEF, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbStructPaintComponent extends CsmPaintComponent.StructPaintComponent{
        
        public NbStructPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.STRUCT, 0);
            setIcon(newIcon);
            return newIcon;            
        }
        
    }

    public final static class NbUnionPaintComponent extends CsmPaintComponent.UnionPaintComponent{
        
        public NbUnionPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon superIcon = super.getIcon();
            if (superIcon != null) 
                return superIcon;
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.UNION, 0);
            setIcon(newIcon);
            return newIcon;            
        }
    }
    
    public final static class NbGlobalVariablePaintComponent extends CsmPaintComponent.GlobalVariablePaintComponent {
        
        public NbGlobalVariablePaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.GLOBAL);
            return newIcon;             
        }
    }
    
    public final static class NbLocalVariablePaintComponent extends CsmPaintComponent.LocalVariablePaintComponent {
        
        public NbLocalVariablePaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.LOCAL);
            return newIcon;             
        }
    }
    
    public final static class NbFileLocalVariablePaintComponent extends CsmPaintComponent.FileLocalVariablePaintComponent {
        
        public NbFileLocalVariablePaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon() {
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.FILE_LOCAL);
            return newIcon;             
        }
    }    
    
    public final static class NbFieldPaintComponent extends CsmPaintComponent.FieldPaintComponent{

        public NbFieldPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.VARIABLE, getModifiers()|CsmUtilities.MEMBER);
            return newIcon;            
        }
    
    }    
    
    public final static class NbFileLocalFunctionPaintComponent extends CsmPaintComponent.FileLocalFunctionPaintComponent{
        
        public NbFileLocalFunctionPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION_DEFINITION, getModifiers()|CsmUtilities.GLOBAL|CsmUtilities.STATIC);
            return newIcon;             
        }
    }
    
    public final static class NbGlobalFunctionPaintComponent extends CsmPaintComponent.GlobalFunctionPaintComponent{
        
        public NbGlobalFunctionPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION_DEFINITION, getModifiers()|CsmUtilities.GLOBAL);
            return newIcon;             
        }
    }
    
    public final static class NbMethodPaintComponent extends CsmPaintComponent.MethodPaintComponent{
        
        public NbMethodPaintComponent(){
            super();
        }
        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION_DEFINITION, getModifiers()|CsmUtilities.MEMBER);
            return newIcon;            
        }
    }
    
    public final static class NbConstructorPaintComponent extends CsmPaintComponent.ConstructorPaintComponent{
        
        public NbConstructorPaintComponent(){
            super();
        }

        
        @Override
        protected Icon getIcon(){
            Icon newIcon = CsmImageLoader.getIcon(CsmDeclaration.Kind.FUNCTION_DEFINITION, getModifiers()|CsmUtilities.CONSTRUCTOR);
            return newIcon;            
        }
        
    }    
    
    public final static class NbStringPaintComponent extends CsmPaintComponent.StringPaintComponent {
        
        public NbStringPaintComponent(){
            super();
        }
    }    
}
