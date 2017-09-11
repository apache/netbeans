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
package org.netbeans.modules.javafx2.editor.completion.beans;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.ElementHandle;

/**
 * Describes event source on an FX object. 
 *
 * @author sdedic
 */
public final class FxEvent extends FxDefinition {
    /**
     * FQN of the event fired
     */
    private String eventClassName;
    
    /**
     * Java type of the event
     */
    private ElementHandle<TypeElement>  eventType;

    /**
     * True, if the event is a property change
     */
    private boolean propertyChange;

    /**
     * Name of the event object class
     * 
     * @return event object class name
     */
    public String getEventClassName() {
        return eventClassName;
    }

    /**
     * Type of the event object. May return {@code null},
     * if the event type wasn't resolved (class was missing}
     * 
     * @return handle to the event type
     */
    @CheckForNull
    public ElementHandle<TypeElement> getEventType() {
        return eventType;
    }

    FxEvent(String name) {
        super(name);
    }
    
    void setPropertyChange(boolean change) {
        this.propertyChange = change;
    }

    void setEventClassName(String eventClassName) {
        this.eventClassName = eventClassName;
    }

    void setEventType(ElementHandle<TypeElement> eventType) {
        this.eventType = eventType;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Event[");
        sb.append("name: ").append(getName()).
                append("; type: ").append(getEventType());
        sb.append("]");
        return sb.toString();
    }
    
    public boolean isPropertyChange() {
        return propertyChange;
    }
    
    public String getPropertyName() {
        if (!propertyChange) {
            return null;
        }
        String s = getName();
        return s.substring(0, s.length() - 6); // minus Change at the end.
    }
    
    public String getSymbol() {
        String s = getName();
        return "on" + Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
    
    /**
     * Returns EVENT type
     * @return EVENT
     */
    public FxDefinitionKind getKind() {
        return FxDefinitionKind.EVENT;
    }
}
