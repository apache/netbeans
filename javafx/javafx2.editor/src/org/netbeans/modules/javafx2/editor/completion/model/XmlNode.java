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

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;

/**
 * Elements, which do not play relevant role in the model, but are present 
 * in the XML.
 * Currently, only fx:define element is covered by this class. The instance is 
 * in the tree children list, so positions are stored, and the instance can
 * be manipulated. But does not appear in regular model. Attributes of such
 * elements are not modeled at all.
 * <p/>
 * Special attributes, like fx:id, fx:value etc are also modeled by XmlNode,
 * which is present int he children list of the NodeInfo. This allows
 * to insert/manipulate with such attributes programmatically.
 * 
 * @author sdedic
 */
class XmlNode extends FxNode {
    private String tagName;

    public XmlNode(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public Kind getKind() {
        return Kind.Element;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitElement(this);
    }

    @Override
    public String getSourceName() {
        return tagName;
    }

    @Override
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        // nop
    }
    
}
