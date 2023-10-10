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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public final class AnnotationUtil {

    public static final String ANY = "jakarta.enterprise.inject.Any";                 // NOI18N

    public static final String VALUE = "value";                                     // NOI18N

    public static final String INJECT = "Inject";                                   // NOI18N

    public static final String INJECT_FQN = "jakarta.inject."+INJECT;                 // NOI18N

    public static final String DECORATOR = "jakarta.decorator.Decorator";             // NOI18N

    public static final String PRODUCES = "Produces";

    public static final String PRODUCES_FQN = "jakarta.enterprise.inject."+           // NOI18N
        PRODUCES;

    public static final String INTERCEPTOR_BINDING = "InterceptorBinding";          // NOI18N

    public static final String INTERCEPTOR_BINDING_FQN
                = "jakarta.interceptor."+INTERCEPTOR_BINDING;                         // NOI18N

    public static final String INTERCEPTOR = "jakarta.interceptor.Interceptor";       // NOI18N

    public static final String NORMAL_SCOPE = "NormalScope";                        // NOI18N

    public static final String NORMAL_SCOPE_FQN
                                          = "jakarta.enterprise.context."+NORMAL_SCOPE;// NOI18N

    public static final String SCOPE =    "Scope";                                  // NOI18N

    public static final String SCOPE_FQN = "jakarta.inject."+SCOPE;                   // NOI18N

    public static final String REQUEST_SCOPE_FQN = "jakarta.enterprise.context.RequestScoped";// NOI18N
    public static final String SESSION_SCOPE_FQN = "jakarta.enterprise.context.SessionScoped";// NOI18N
    public static final String APPLICATION_SCOPE_FQN = "jakarta.enterprise.context.ApplicationScoped";// NOI18N
    public static final String CONVERSATION_SCOPE_FQN = "jakarta.enterprise.context.ConversationScoped";// NOI18N
    public static final String DEPENDENT_SCOPE_FQN = "jakarta.enterprise.context.Dependent";// NOI18N


    public static final String DISPOSES = "Disposes";                               // NOI18N

    public static final String DISPOSES_FQN  = "jakarta.enterprise.inject."+          // NOI18N
                                        DISPOSES;

    public static final String OBSERVES = "Observes";                               // NOI18N

    public static final String OBSERVES_FQN = "jakarta.enterprise.event."+            // NOI18N
                                            OBSERVES;

    public static final String STATELESS = "jakarta.ejb.Stateless";                   // NOI18N

    public static final String STATEFUL = "jakarta.ejb.Stateful";                     // NOI18N

    public static final String  SINGLETON   = "jakarta.ejb.Singleton";                // NOI18N
    public static final String  CDISINGLETON   = "jakarta.inject.Singleton";                // NOI18N

    public static final String APPLICATION_SCOPED
                    = "jakarta.enterprise.context.ApplicationScoped";                 // NOI18N

    public static final String DEPENDENT
                            = "jakarta.enterprise.context.Dependent";                 // NOI18N

    public static final String STEREOTYPE = "Stereotype";                           // NOI18N

    public static final String STEREOTYPE_FQN =
                    "jakarta.enterprise.inject."+STEREOTYPE;                          // NOI18N

    public static final String NAMED = "jakarta.inject.Named";                        // NOI18N

    public static final String QUALIFIER = "Qualifier";                             // NOI18N

    public static final String QUALIFIER_FQN=
                            "jakarta.inject."+QUALIFIER;                              // NOI18N

    public static final String DELEGATE_FQN =
                                        "jakarta.decorator.Delegate";                 // NOI18N

    public static final String SPECIALIZES = "jakarta.enterprise.inject.Specializes"; // NOI18N

    public static final String INJECTION_POINT =
                            "jakarta.enterprise.inject.spi.InjectionPoint";           // NOI18N

    public static final String DEFAULT_FQN = "jakarta.enterprise.inject.Default";     // NOI18N

    public static final String POST_CONSTRUCT = "jakarta.annotation.PostConstruct";   // NOI18N

    public static final String PRE_DESTROY = "jakarta.annotation.PreDestroy";         // NOI18N

    public static final String POST_ACTIVATE = "jakarta.ejb.PostActivate";            // NOI18N

    public static final String PRE_PASSIVATE = "jakarta.ejb.PrePassivate";            // NOI18N

    public static final String CONTEXT = "jakarta.enterprise.context.spi.Context";    // NOI18N

    public static final String CONVERSATION = "jakarta.enterprise.context.Conversation";// NOI18N

    public static final String ALTERNATVE = "jakarta.enterprise.inject.Alternative";   // NOI18N

    public static final String TYPED = "jakarta.enterprise.inject.Typed";              // NOI18N

    public static final String NON_BINDING = "jakarta.enterprise.util.Nonbinding";    // NOI18N

    public static final String PASSIVATING = "passivating";                         // NOI18N

    public static final String PROVIDER = "jakarta.inject.Provider";// NOI18N

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
        return getAnnotationMirror(element, compInfo, STATEFUL, STATELESS,
                SINGLETON)!= null;
    }

    public static boolean isDelegate(Element element, TypeElement parent,
            WebBeansModel model )
    {
        return AnnotationUtil.hasAnnotation(element,
                AnnotationUtil.DELEGATE_FQN, model.getCompilationController())
                && AnnotationUtil.hasAnnotation( parent,
                        AnnotationUtil.DECORATOR, model.getCompilationController());
    }

    public static boolean isLifecycleCallback( ExecutableElement element ,
            CompilationInfo info )
    {
        AnnotationMirror annotationMirror = getAnnotationMirror(element, info,
                POST_ACTIVATE, POST_CONSTRUCT , PRE_DESTROY, PRE_PASSIVATE);
        return annotationMirror != null;
    }

}
