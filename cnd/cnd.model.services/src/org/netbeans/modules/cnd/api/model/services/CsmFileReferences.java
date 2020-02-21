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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmTemplateBasedReferencedObject;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.Lookup;

/**
* Provides a list of CsmReferences of the identifiers in the CsmFile
*
*/
public abstract class CsmFileReferences {
   /**
    * Provides visiting of the identifiers of the CsmFile
    */
   public abstract void accept(CsmScope csmScope, Document doc, Visitor visitor);

   /**
    * Provides visiting of the identifiers of the CsmFile and point prefered
    * kinds of references
    */
   public abstract void accept(CsmScope csmScope, Document doc, Visitor visitor, Set<CsmReferenceKind> preferedKinds);

   public abstract void visit(Collection<CsmReference> refs, ReferenceVisitor visitor);

   /**
    * A dummy resolver that do nothing.
    */
   private static final CsmFileReferences EMPTY = new Empty();
      /** default instance */
   private static CsmFileReferences DEFAULT;
      protected CsmFileReferences() {
   }
      /** Static method to obtain the CsmFileReferences implementation.
    * @return the resolver
    */
   public static CsmFileReferences getDefault() {
       /*no need for sync synchronized access*/
       if (DEFAULT != null) {
           return DEFAULT;
       }
       DEFAULT = Lookup.getDefault().lookup(CsmFileReferences.class);
       return DEFAULT == null ? EMPTY : DEFAULT;
   }
      //
   // Implementation of the default query
   //
   private static final class Empty extends CsmFileReferences {
       Empty() {
       }

       @Override
       public void accept(CsmScope csmScope, Document doc, Visitor visitor) {
           // do nothing
       }
              @Override
       public void accept(CsmScope csmScope, Document doc, Visitor visitor, Set<CsmReferenceKind> preferedKinds) {
           // do nothing
       }

       @Override
       public void visit(Collection<CsmReference> refs, ReferenceVisitor visitor) {
           // do nothing
       }
   }
   
   /**
    * visitor interface
    */
   public interface Visitor extends Interrupter {
       /**
        * This method is invoked for every matching reference in the file.
        *
        * @param context  reference with its lexical context
        */
       void visit(CsmReferenceContext context);
       
   }

   public interface ReferenceVisitor extends Interrupter {

       /**
        * This method is invoked for every matching reference in the file.
        *
        * @param context  reference with its lexical context
        */
       void visit(CsmReference ref);
   }

   /**
    * Determines whether reference is dereferenced template parameter
    */
   public static boolean isTemplateBased(CsmReferenceContext context) {
       if (2 <= context.size() && isDereference(context.getToken())) {
           CsmReference ref = context.getReference(context.size() - 2);
           if (ref != null) {
               if (getDefault().isThis(ref)) {
                   return hasTemplateBasedAncestors(findContextClass(context), new ClassifiersAntiLoop());
               }
               CsmObject refObj = ref.getReferencedObject();
               if (isTemplateParameterInvolved(refObj)) {
                   return true;
               } else {
                   CsmType type = getType(refObj);
                   if (CsmUtilities.isAutoType(type) && CsmKindUtilities.isOffsetable(refObj)) {
                       GetTypeClassHandler handler = new GetTypeClassHandler();
                       CsmExpressionResolver.resolveType((CsmOffsetable) refObj, null, handler);
                       return hasTemplateBasedAncestors(handler.clazz, new ClassifiersAntiLoop());
                   }
                   return hasTemplateBasedAncestors(type, new ClassifiersAntiLoop());
               }
           }
       } else {
           // it isn't a dereference - check current context
           return hasTemplateBasedAncestors(findContextClass(context), new ClassifiersAntiLoop());
       }
       return false;
   }
   
   private static class GetTypeClassHandler implements CsmExpressionResolver.ResolvedTypeHandler {

        CsmClass clazz = null;
       
        @Override
        public void process(CsmType resolvedType) {
            if (resolvedType != null) {
                CsmClassifier cls = resolvedType.getClassifier();
                if (CsmKindUtilities.isClass(cls)) {
                    clazz = (CsmClass) cls;
                }
            }
        }
   }

   private static CsmType getType(CsmObject obj) {
       if (CsmKindUtilities.isFunction(obj)) {
           return ((CsmFunction)obj).getReturnType();
       } else if (CsmKindUtilities.isVariable(obj)) {
           CsmType varType = ((CsmVariable)obj).getType();
           // Do not check for auto type here because all the work 
           // with resolved type should be done inside handler. 
           // But the contract of this method requires to return it outside.
           return varType;
       } else if(CsmKindUtilities.isTypedef(obj) || CsmKindUtilities.isTypeAlias(obj)) {
           return ((CsmTypedef) obj).getType();
       }
       return null;
   }

   private static CsmClass findContextClass(CsmReferenceContext context) {
       CsmObject owner = context.getReference().getOwner();
       while (CsmKindUtilities.isScopeElement(owner)) {
           if (CsmKindUtilities.isClass(owner)) {
               return (CsmClass) owner;
           } else if (CsmKindUtilities.isClassMember(owner)) {
               return ((CsmMember) owner).getContainingClass();
           } else if (CsmKindUtilities.isFunctionDefinition(owner)) {
               CsmFunction decl = ((CsmFunctionDefinition) owner).getDeclaration();
               if (CsmKindUtilities.isClassMember(decl)) {
                   return ((CsmMember) decl).getContainingClass();
               }
           } else if (CsmKindUtilities.isVariableDefinition(owner)) {
               CsmVariable decl = ((CsmVariableDefinition) owner).getDeclaration();
               if (CsmKindUtilities.isClassMember(decl)) {
                   return ((CsmMember) decl).getContainingClass();
               }
           }
           owner = ((CsmScopeElement) owner).getScope();
       }
       return null;
   }

   public static boolean hasTemplateBasedAncestors(CsmType type) {
       return hasTemplateBasedAncestors(type, new ClassifiersAntiLoop());
   }

   private static boolean hasTemplateBasedAncestors(CsmType type, ClassifiersAntiLoop handledClasses) {
       if( type != null) {
           CsmClassifier cls = type.getClassifier();
           if (CsmKindUtilities.isClass(cls)) {
               return hasTemplateBasedAncestors((CsmClass) cls, handledClasses);
           }
       }
       return false;
   }
      
   private static boolean hasTemplateBasedAncestors(CsmClass cls, ClassifiersAntiLoop handledClasses) {
       if (cls != null) {
           if (handledClasses.contains(cls)) {
                return false;
           }
           handledClasses.add(cls);
           if (isActualInstantiation(cls)) {
               return false; // like my_class<int, char>
           }
           for (CsmInheritance inh : cls.getBaseClasses()) {
               if (inh.getAncestorType().isTemplateBased()) {
                   return true;
               }
               CsmClassifier classifier = inh.getClassifier();
               if (classifier instanceof CsmClass) { // paranoia                   
                   if (hasTemplateBasedAncestors((CsmClass) classifier, handledClasses)) {
                       return true;
                   }
               }
           }
       }
       return false;
   }

   /**
    * Determines whether it is indeed instantiation -
    * not a specialization, not a part of the template itself, etc.
    * @return true
    */
   private static boolean isActualInstantiation(CsmClass cls) {
       if (CsmKindUtilities.isInstantiation(cls)) {
           CsmInstantiation instantiation = (CsmInstantiation) cls;
           Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = instantiation.getMapping();
           for (CsmSpecializationParameter param : mapping.values()) {
               if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                   if (((CsmTypeBasedSpecializationParameter) param).isTemplateBased()) {
                       return false;
                   }
               }
           }
           return true;
       }
       return false;
   }
      /**
    * Determines whether reference is dereferenced macro or
    * if it's in macro arguments
    */
   public static boolean isMacroBased(CsmReferenceContext context) {
       if (2 <= context.size() && isDereference(context.getToken())) {
           CsmReference ref = context.getReference(context.size() - 2);
           if (ref != null) {
               if (CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                   return true;
               }
           }
       }
       for (int i = context.size() - 1; 0 < i; --i) {
           if (context.getToken(i) == CppTokenId.LPAREN) {
               CsmReference ref = context.getReference(i - 1);
               if (ref != null && CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                   return true;
               }
           }
       }
       return false;
   }

   public static boolean isBuiltInBased(CsmReference ref) {
       CharSequence txt = null;
       if (ref != null) {
           txt = ref.getText();
       }
       if (txt != null && txt.length() > 0) {
           return CharSequenceUtils.startsWith(txt, "__"); //NOI18N
       }
       return false;
   }

   public static boolean isAfterUnresolved(CsmReferenceContext context) {
       if (2 <= context.size() && isDereference(context.getToken())) {
           CsmReference ref = context.getReference(context.size() - 2);
           if (ref != null && !getDefault().isThis(ref)) {
                final CsmObject referencedObject = ref.getReferencedObject();
                if (referencedObject == null ||
                       referencedObject instanceof CsmTemplateBasedReferencedObject) {
                   return true;
               }
           }
       }
       return false;
   }

   public static boolean isTemplateParameterInvolved(CsmObject obj) {
       if (CsmKindUtilities.isTemplateParameter(obj)) {
           return true;
       }
       CsmType type = getType(obj);
       if (CsmUtilities.isAutoType(type) && CsmKindUtilities.isOffsetable(obj)) {
           IsTypeTemplateBasedHandler handler = new IsTypeTemplateBasedHandler();
           CsmExpressionResolver.resolveType((CsmOffsetable) obj, null, handler);
           return handler.isTemplateBased;
       }
       return (type == null) ? false : type.isTemplateBased();
   }
   
   private static final class IsTypeTemplateBasedHandler implements CsmExpressionResolver.ResolvedTypeHandler {
       
        boolean isTemplateBased = false;

        @Override
        public void process(CsmType resolvedType) {
            if (resolvedType != null) {
                isTemplateBased = resolvedType.isTemplateBased();
            }
        }
   }

   public static boolean isDereference(CppTokenId token) {
       if (token == null) {
           return false;
       }
       switch (token) {
           case DOT:
           case DOTMBR:
           case ARROW:
           case ARROWMBR:
           case SCOPE:
               return true;
           default:
               return false;
       }
   }

   public static boolean isBracket(CppTokenId token) {
       if (token == null) {
           return false;
       }
       switch (token) {
           case LBRACE:
           case LBRACKET:
           case LPAREN:
           case LT:
               return true;
           default:
               return false;
       }
   }

   protected boolean isThis(CsmReference ref) {
       return ref != null && "this".equals(ref.getText()); //NOI18N
   }
}
