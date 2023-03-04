/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.beans.impl.model;

import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;

/**
 * since cdi 1.1 behavior is based on archoe type, beans may not be discoverable
 *
 * @author sp153251
 */
public class ArchiveTypeBindingTypeFilter<T extends Element> extends Filter<T> {

    static <T extends Element> ArchiveTypeBindingTypeFilter<T> get(Class<T> clazz) {
        assertElement(clazz);
        // could be changed to cached ThreadLocal access
        if (clazz.equals(Element.class) || clazz.equals(TypeElement.class)) {
            return (ArchiveTypeBindingTypeFilter<T>) new ArchiveTypeBindingTypeFilter<>();
        }
        return null;
    }
    private WebBeansModelImplementation myImpl;

    void init(WebBeansModelImplementation impl) {
        myImpl = impl;
    }

    @Override
    void filter(Set<T> set) {
        super.filter(set);
        if (myImpl.getBeansModel() != null) {
            switch (myImpl.getBeansModel().getBeanArchiveType()) {
                case NONE:
                    set.clear();
                    break;
                case IMPLICIT:
                    CompilationController compInfo = myImpl.getModel().getCompilationController();
                    for (Iterator<T> iterator = set.iterator(); iterator
                            .hasNext();) {
                        Element element = iterator.next();
                        //TODO: reqwrite with ScopeChecker, avoid duplicates
                        boolean isNormalScopeOrScopeOrSingleton = AnnotationUtil.getAnnotationMirror(element, compInfo,
                                AnnotationUtil.NORMAL_SCOPE_FQN, 
                                AnnotationUtil.SCOPE_FQN, 
                                AnnotationUtil.REQUEST_SCOPE_FQN, 
                                AnnotationUtil.SESSION_SCOPE_FQN,
                                AnnotationUtil.APPLICATION_SCOPE_FQN, 
                                AnnotationUtil.CONVERSATION_SCOPE_FQN, 
                                AnnotationUtil.DEPENDENT_SCOPE_FQN,
                                AnnotationUtil.CDISINGLETON) != null;
                        if (isNormalScopeOrScopeOrSingleton) {
                            continue;
                        } 
                        if (AnnotationUtil.isSessionBean(element, compInfo)) {
                            continue;
                        }
                        iterator.remove();
                    }
            }
        }
    }
}
