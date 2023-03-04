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
package org.netbeans.modules.javafx2.editor.completion.model;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;
import org.netbeans.modules.javafx2.editor.completion.beans.FxEvent;

/**
 * Represents an event handler attached to an {@link FxInstance}.
 * The event handler's content may represent a script, or a method reference.
 * 
 * @author sdedic
 */
public final class EventHandler extends FxNode implements HasContent {
    private String event;
    
    /**
     * Content - script code
     */
    private Object content;

    /**
     * If true, the content is actually a method name
     */
    private boolean methodRef;
    
    /**
     * Handle to the actual method handler
     */
    private ElementHandle<ExecutableElement>    handler;
    
    /**
     * Resolved eventInfo
     */
    private FxEvent   eventInfo;

    EventHandler(String eventName) {
        this.event = eventName;
    }
    
    EventHandler asMethodRef() {
        this.methodRef = true;
        return this;
    }
    
    void addContent(CharSequence content) {
        this.content = PropertySetter.addCharContent(this.content, content);
    }
    
    public String getEvent() {
        return event;
    }

    /**
     * Returns content of the event handler. As event handlers may
     * contain whole script contents, the content may be quite large. 
     * @return 
     */
    public CharSequence getContent() {
        if (methodRef) {
            return null;
        }
        return doGetContent();
    }
    
    public boolean hasContent() {
        return content != null;
    }
    
    private CharSequence doGetContent() {
        CharSequence s = PropertySetter.getValContent(this.content);
        if (s != content) {
            content = s;
        }
        return s;
    }

    /**
     * Provides access to the FxEvent definition, if it was found.
     * Returns {@code null}, if the event name does not correspond to
     * any event on the parent {@link FxInstance}
     * 
     * @return resolved FxEvent instance or {@code null}
     */
    @CheckForNull
    public FxEvent getEventInfo() {
        return eventInfo;
    }

    @Override
    public Kind getKind() {
        return Kind.Event;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitEvent(this);
    }

    @Override
    public String getSourceName() {
        return "on" + Character.toUpperCase(event.charAt(0)) + event.substring(1);
    }
    
    public CharSequence getHandlerName() {
        if (!methodRef) {
            return null;
        }
        return doGetContent();
    }
    
    void setEventInfo(FxEvent info) {
        this.eventInfo = info;
    }

    void setHandler(ElementHandle<ExecutableElement> handler) {
        this.handler = handler;
    }
    
    /**
     * Determines whether the event is processed using script, or controller method.
     * If returns true, {@link #getContent()} should be used to extract script's
     * contents. Language of the script must be declared in the document.
     * <p/>
     * On false, the {@link #getHandlerName()} provides name of the handler method,
     * and {@link #getHandler()} handle to the Java element for the handler method.
     * @return true, if the event handler contains script fragment.
     */
    public boolean isScript() {
        return !methodRef;
    }
    
    public ElementHandle<ExecutableElement> getHandler() {
        return handler;
    }

    @Override
    @SuppressWarnings("rawtypes")
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        this.eventInfo = (FxEvent)info;
        this.setHandler((ElementHandle<ExecutableElement>)nameHandle);
    }
    
    
}
