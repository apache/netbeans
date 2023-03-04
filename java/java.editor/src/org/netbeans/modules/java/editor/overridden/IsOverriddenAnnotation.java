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
package org.netbeans.modules.java.editor.overridden;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Lahoda
 */
public class IsOverriddenAnnotation extends Annotation {
    
    private final StyledDocument document;
    private final Position pos;
    private final String shortDescription;
    private final AnnotationType type;
    private final List<ElementDescription> declarations;
    
    public IsOverriddenAnnotation(StyledDocument document, Position pos, AnnotationType type, String shortDescription, List<ElementDescription> declarations) {
        //#166351 -- null pos for some reason
        assert pos != null;

        this.document = document;
        this.pos = pos;
        this.type = type;
        this.shortDescription = shortDescription;
        this.declarations = declarations;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }

    public String getAnnotationType() {
        switch(type) {
            case IS_OVERRIDDEN:

                return "org-netbeans-modules-editor-annotations-is_overridden"; //NOI18N
            case HAS_IMPLEMENTATION:

                return "org-netbeans-modules-editor-annotations-has_implementations"; //NOI18N
            case IMPLEMENTS:

                return "org-netbeans-modules-editor-annotations-implements"; //NOI18N
            case OVERRIDES:

                return "org-netbeans-modules-editor-annotations-overrides"; //NOI18N
            default:

                throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        }
    }
    
    public void attach() {
        NbDocument.addAnnotation(document, pos, -1, this);
    }
    
    public void detachImpl() {
        NbDocument.removeAnnotation(document, this);
    }
    
    public String toString() {
        return "[IsOverriddenAnnotation: " + shortDescription + "]"; //NOI18N
    }
    
    public Position getPosition() {
        return pos;
    }
    
    public String debugDump(boolean includePosition) {
        List<String> elementNames = new ArrayList<String>();
        
        for(ElementDescription desc : declarations) {
            elementNames.add(desc.getDisplayName());
        }
        
        Collections.sort(elementNames);
        
        return "IsOverriddenAnnotation: type=" + type.name() + ", elements:" + elementNames.toString() + (includePosition ? ":" + pos.getOffset() : ""); //NOI18N
    }
    
    public AnnotationType getType() {
        return type;
    }

    public List<ElementDescription> getDeclarations() {
        return declarations;
    }
    
}
