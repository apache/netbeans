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
package org.netbeans.modules.javafx2.editor.parser.processors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxEvent;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;

/**
 *
 * @author sdedic
 */
public class EventResolver extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    private BuildEnvironment    env;
    private FxBean bean;
    private TypeElement controllerElement;
    private TypeMirror eventType;
    
    public EventResolver() {
    }

    EventResolver(BuildEnvironment env) {
        this.env = env;
    }

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new EventResolver(env);
    }

    @Override
    public void visitSource(FxModel source) {
        String controller = source.getController();
        if (controller != null) {
            ElementHandle<TypeElement> cTypeH = source.getControllerType();
            if (cTypeH != null) {
                controllerElement = cTypeH.resolve(env.getCompilationInfo());
            }
        }
        TypeElement el = env.getCompilationInfo().getElements().getTypeElement(FX_EVENT_TYPE);
        if (el == null) {
            return;
        }
        eventType = el.asType();
        super.visitSource(source);
    }
    
    
    private static final String FX_EVENT_TYPE = "javafx.event.Event"; // NOI18N
    
    
    @Override
    public void visitBaseInstance(FxInstance decl) {
        FxBean saveBean = this.bean;
        try {
            this.bean = decl.getDefinition();
            super.visitBaseInstance(decl);
        } finally {
            this.bean = saveBean;
        }
    }

    @NbBundle.Messages({
        "# {0} - bean class name",
        "# {1} - event name",
        "ERR_eventNotExists=The class {0} does not support event ''{1}''.",
        "ERR_controllerUndefined=Controller is not defined on root component",
        "ERR_handlerMethodNotAccessible=Handler method is not accessible. Make public, or annotate with @FXML",
        "ERR_handlerBadSignagure=Invalid handler method signature",
        "ERR_handlerNotFound=Handler method not found"
    })
    @Override
    public void visitEvent(EventHandler eh) {
        if (bean == null) {
            // already reported
            return;
        }
        String eventName = eh.getEvent();
        FxEvent ev = bean.getEvent(eventName);
        
        if (ev == null && bean.getBuilder() != null) {
            ev = bean.getBuilder().getEvent(eventName);
        }

        int offs = env.getTreeUtilities().positions(eh).getStart();
        if (ev == null) {
            String sourceClassName = bean.getClassName();
            env.addError(new ErrorMark(
                    offs,
                    eh.getEvent().length() + 2, /* 'on' prefix */
                    "event-undefined",
                    ERR_eventNotExists(sourceClassName, eh.getEvent()),
                    eh
            ));
            return;
        }
        env.getAccessor().resolve(eh, null, null, null, ev);
        if (eh.isScript()) {
            return;
        }
        int st = env.getTreeUtilities().positions(eh).getContentStart();
        int end = env.getTreeUtilities().positions(eh).getContentEnd();
        String c = eh.getRoot().getController();
        if (c == null) {
            env.addError(new ErrorMark(
                    st,
                    end - st,
                    "event-controller-undefined",
                    ERR_controllerUndefined(),
                    eh
            ));
            return;
        }
        if (controllerElement == null) {
            // reported elsewhere
            return;
        }
        CharSequence cname = eh.getHandlerName();
        ExecutableElement found = null;
        ExecutableElement partialFound = null;
        boolean notAssignable = false;
        for (ExecutableElement ce : ElementFilter.methodsIn(env.getCompilationInfo().getElements().getAllMembers(controllerElement))) {
            if (!ce.getSimpleName().contentEquals(cname)) {
                continue;
            }
            partialFound = ce;
            if (ce.getParameters().size() > 1) {
                continue;
            }
            if (ce.getParameters().isEmpty()) {
                if (found == null) {
                    found = ce;
                }
                continue;
            }
            TypeMirror paramType = ce.getParameters().get(0).asType();
            if (env.getCompilationInfo().getTypes().isAssignable(paramType, eventType)) {
                found = ce;
                if (!FxClassUtils.isFxmlAccessible(ce)) {
                    // can report, since it is THE handler, but inaccessible
                    env.addError(new ErrorMark(
                        st,
                        end - st,
                        "event-handler-inaccessible",
                        ERR_handlerMethodNotAccessible(),
                        eh
                    ));
                }
                break;
            } else {
                notAssignable = true;
            }
        }
        if (found != null) {
            env.getAccessor().resolve(eh, ElementHandle.create(found), null, null, ev);
        } else {
            if (notAssignable) {
                env.addError(new ErrorMark(
                    st,
                    end - st,
                    "event-handler-bad-signature",
                    ERR_handlerMethodNotAccessible(),
                    eh
                ));
            } else {
                env.addError(new ErrorMark(
                    st,
                    end - st,
                    "event-handler-not-found",
                    ERR_handlerNotFound(),
                    eh
                ));
            }
        }
        super.visitEvent(eh);
    }
    
}
