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
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public final class AnnotationUtil {

    public static final String ANY = "javax.enterprise.inject.Any";                 // NOI18N
    public static final String ANY_JAKARTA = "jakarta.enterprise.inject.Any";                 // NOI18N

    public static final String VALUE = "value";                                     // NOI18N

    public static final String INJECT = "Inject";                                   // NOI18N

    public static final String INJECT_FQN = "javax.inject."+INJECT;                 // NOI18N
    public static final String INJECT_FQN_JAKARTA = "jakarta.inject."+INJECT;                 // NOI18N

    public static final String DECORATOR = "javax.decorator.Decorator";             // NOI18N
    public static final String DECORATOR_JAKARTA = "jakarta.decorator.Decorator";             // NOI18N

    public static final String PRODUCES = "Produces";

    public static final String PRODUCES_FQN = "javax.enterprise.inject."+ PRODUCES; // NOI18N
    public static final String PRODUCES_FQN_JAKARTA = "jakarta.enterprise.inject."+ PRODUCES; // NOI18N

    public static final String INTERCEPTOR_BINDING = "InterceptorBinding";          // NOI18N

    public static final String INTERCEPTOR_BINDING_FQN = "javax.interceptor." + INTERCEPTOR_BINDING; // NOI18N
    public static final String INTERCEPTOR_BINDING_FQN_JAKARTA = "jakarta.interceptor." + INTERCEPTOR_BINDING; // NOI18N

    public static final String INTERCEPTOR = "javax.interceptor.Interceptor";       // NOI18N
    public static final String INTERCEPTOR_JAKARTA = "jakarta.interceptor.Interceptor";       // NOI18N

    public static final String NORMAL_SCOPE = "NormalScope";                        // NOI18N

    public static final String NORMAL_SCOPE_FQN = "javax.enterprise.context."+NORMAL_SCOPE;// NOI18N
    public static final String NORMAL_SCOPE_FQN_JAKARTA = "jakarta.enterprise.context."+NORMAL_SCOPE;// NOI18N

    public static final String SCOPE =    "Scope";                                  // NOI18N

    public static final String SCOPE_FQN = "javax.inject."+SCOPE;                   // NOI18N
    public static final String SCOPE_FQN_JAKARTA = "jakarta.inject."+SCOPE;                   // NOI18N

    public static final String REQUEST_SCOPE_FQN = "javax.enterprise.context.RequestScoped";// NOI18N
    public static final String REQUEST_SCOPE_FQN_JAKARTA = "jakarta.enterprise.context.RequestScoped";// NOI18N
    public static final String SESSION_SCOPE_FQN = "javax.enterprise.context.SessionScoped";// NOI18N
    public static final String SESSION_SCOPE_FQN_JAKARTA = "jakarta.enterprise.context.SessionScoped";// NOI18N
    public static final String APPLICATION_SCOPE_FQN = "javax.enterprise.context.ApplicationScoped";// NOI18N
    public static final String APPLICATION_SCOPE_FQN_JAKARTA = "jakarta.enterprise.context.ApplicationScoped";// NOI18N
    public static final String CONVERSATION_SCOPE_FQN = "javax.enterprise.context.ConversationScoped";// NOI18N
    public static final String CONVERSATION_SCOPE_FQN_JAKARTA = "jakarta.enterprise.context.ConversationScoped";// NOI18N
    public static final String DEPENDENT_SCOPE_FQN = "javax.enterprise.context.Dependent";// NOI18N
    public static final String DEPENDENT_SCOPE_FQN_JAKARTA = "jakarta.enterprise.context.Dependent";// NOI18N


    public static final String DISPOSES = "Disposes";                               // NOI18N

    public static final String DISPOSES_FQN = "javax.enterprise.inject." + DISPOSES; // NOI18N
    public static final String DISPOSES_FQN_JAKARTA = "jakarta.enterprise.inject." + DISPOSES; // NOI18N

    public static final String OBSERVES = "Observes";                               // NOI18N

    public static final String OBSERVES_FQN = "javax.enterprise.event." + OBSERVES;           // NOI18N
    public static final String OBSERVES_FQN_JAKARTA = "jakarta.enterprise.event." + OBSERVES;           // NOI18N

    public static final String STATELESS = "javax.ejb.Stateless";                   // NOI18N
    public static final String STATELESS_JAKARTA = "jakarta.ejb.Stateless";                   // NOI18N

    public static final String STATEFUL = "javax.ejb.Stateful";                     // NOI18N
    public static final String STATEFUL_JAKARTA = "jakarta.ejb.Stateful";                     // NOI18N

    public static final String  SINGLETON   = "javax.ejb.Singleton";                // NOI18N
    public static final String  SINGLETON_JAKARTA   = "jakarta.ejb.Singleton";                // NOI18N
    public static final String  CDISINGLETON   = "javax.inject.Singleton";                // NOI18N
    public static final String  CDISINGLETON_JAKARTA   = "jakarta.inject.Singleton";                // NOI18N

    public static final String APPLICATION_SCOPED = "javax.enterprise.context.ApplicationScoped";                 // NOI18N
    public static final String APPLICATION_SCOPED_JAKARTA = "jakarta.enterprise.context.ApplicationScoped";                 // NOI18N

    public static final String DEPENDENT = "javax.enterprise.context.Dependent";                 // NOI18N
    public static final String DEPENDENT_JAKARTA = "jakarta.enterprise.context.Dependent";                 // NOI18N

    public static final String STEREOTYPE = "Stereotype";                           // NOI18N

    public static final String STEREOTYPE_FQN = "javax.enterprise.inject." + STEREOTYPE;  // NOI18N
    public static final String STEREOTYPE_FQN_JAKARTA = "jakarta.enterprise.inject." + STEREOTYPE;   // NOI18N

    public static final String NAMED = "javax.inject.Named";                        // NOI18N
    public static final String NAMED_JAKARTA = "jakarta.inject.Named";                        // NOI18N

    public static final String QUALIFIER = "Qualifier";                             // NOI18N

    public static final String QUALIFIER_FQN = "javax.inject."+QUALIFIER;                              // NOI18N
    public static final String QUALIFIER_FQN_JAKARTA = "jakarta.inject."+QUALIFIER;                              // NOI18N

    public static final String DELEGATE_FQN = "javax.decorator.Delegate";           // NOI18N
    public static final String DELEGATE_FQN_JAKARTA = "jakarta.decorator.Delegate"; // NOI18N

    public static final String SPECIALIZES = "javax.enterprise.inject.Specializes"; // NOI18N
    public static final String SPECIALIZES_JAKARTA = "jakarta.enterprise.inject.Specializes"; // NOI18N

    public static final String INJECTION_POINT = "javax.enterprise.inject.spi.InjectionPoint";           // NOI18N
    public static final String INJECTION_POINT_JAKARTA = "jakarta.enterprise.inject.spi.InjectionPoint";           // NOI18N

    public static final String DEFAULT_FQN = "javax.enterprise.inject.Default";     // NOI18N
    public static final String DEFAULT_FQN_JAKARTA = "jakarta.enterprise.inject.Default";     // NOI18N

    public static final String POST_CONSTRUCT = "javax.annotation.PostConstruct";   // NOI18N
    public static final String POST_CONSTRUCT_JAKARTA = "jakarta.annotation.PostConstruct";   // NOI18N

    public static final String PRE_DESTROY = "javax.annotation.PreDestroy";         // NOI18N
    public static final String PRE_DESTROY_JAKARTA = "jakarta.annotation.PreDestroy";         // NOI18N

    public static final String POST_ACTIVATE = "javax.ejb.PostActivate";            // NOI18N
    public static final String POST_ACTIVATE_JAKARTA = "jakarta.ejb.PostActivate";            // NOI18N

    public static final String PRE_PASSIVATE = "javax.ejb.PrePassivate";            // NOI18N
    public static final String PRE_PASSIVATE_JAKARTA = "jakarta.ejb.PrePassivate";            // NOI18N

    public static final String CONTEXT = "javax.enterprise.context.spi.Context";    // NOI18N
    public static final String CONTEXT_JAKARTA = "jakarta.enterprise.context.spi.Context";    // NOI18N

    public static final String CONVERSATION = "javax.enterprise.context.Conversation";// NOI18N
    public static final String CONVERSATION_JAKARTA = "jakarta.enterprise.context.Conversation";// NOI18N

    public static final String ALTERNATVE = "javax.enterprise.inject.Alternative";   // NOI18N
    public static final String ALTERNATVE_JAKARTA = "jakarta.enterprise.inject.Alternative";   // NOI18N

    public static final String TYPED = "javax.enterprise.inject.Typed";              // NOI18N
    public static final String TYPED_JAKARTA = "jakarta.enterprise.inject.Typed";              // NOI18N

    public static final String NON_BINDING = "javax.enterprise.util.Nonbinding";    // NOI18N
    public static final String NON_BINDING_JAKARTA = "jakarta.enterprise.util.Nonbinding";    // NOI18N

    public static final String PASSIVATING = "passivating";                         // NOI18N

    public static final String PROVIDER = "javax.inject.Provider";// NOI18N
    public static final String PROVIDER_JAKARTA = "jakarta.inject.Provider";// NOI18N

    private AnnotationUtil(){
    }

    public static boolean hasAnnotation(Element element, String annotation,
            CompilationInfo info )
    {
        return getAnnotationMirror(element, annotation, info)!=null;
    }

    public static AnnotationMirror getAnnotationMirror(Element element,
            String annotation,CompilationInfo info )
    {
        return getAnnotationMirror(element, info, annotation);
    }

    /**
     * return AnnotationMirror for first found annotation from annotationFqns
     * @param element
     * @param info
     * @param annotationFqns
     * @return
     */
    public static AnnotationMirror getAnnotationMirror(Element element,
            CompilationInfo info , String... annotationFqns)
    {
        Set<TypeElement> set = new HashSet<TypeElement>();
        Elements els = info.getElements();
        for( String annotation : annotationFqns){
            TypeElement annotationElement = els.getTypeElement(
                    annotation);
            if ( annotationElement != null ){
                set.add( annotationElement );
            }
        }

        List<? extends AnnotationMirror> annotations =
            els.getAllAnnotationMirrors( element );
        for (AnnotationMirror annotationMirror : annotations) {
            Element declaredAnnotation = info.getTypes().asElement(
                    annotationMirror.getAnnotationType());
            if ( set.contains( declaredAnnotation ) ){
                return annotationMirror;
            }
        }
        return null;
    }

    public static boolean isSessionBean(Element element ,
            CompilationInfo compInfo )
    {
        return getAnnotationMirror(element, compInfo, STATEFUL, STATELESS, SINGLETON, STATEFUL_JAKARTA, STATELESS_JAKARTA, SINGLETON_JAKARTA) != null;
    }

    public static boolean isDelegate(Element element, TypeElement parent,
            WebBeansModel model )
    {
        return (AnnotationUtil.hasAnnotation(element, AnnotationUtil.DELEGATE_FQN, model.getCompilationController())
                && AnnotationUtil.hasAnnotation(parent, AnnotationUtil.DECORATOR, model.getCompilationController()))
                || (AnnotationUtil.hasAnnotation(element, AnnotationUtil.DELEGATE_FQN_JAKARTA, model.getCompilationController())
                && AnnotationUtil.hasAnnotation(parent, AnnotationUtil.DECORATOR_JAKARTA, model.getCompilationController()));
    }

    public static boolean isLifecycleCallback( ExecutableElement element ,
            CompilationInfo info )
    {
        AnnotationMirror annotationMirror = getAnnotationMirror(element, info,
                POST_ACTIVATE, POST_CONSTRUCT, PRE_DESTROY, PRE_PASSIVATE,
                POST_ACTIVATE_JAKARTA, POST_CONSTRUCT_JAKARTA, PRE_DESTROY_JAKARTA, PRE_PASSIVATE_JAKARTA
        );
        return annotationMirror != null;
    }

}
