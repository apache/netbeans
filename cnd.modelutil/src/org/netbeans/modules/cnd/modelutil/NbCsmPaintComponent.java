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
