/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
