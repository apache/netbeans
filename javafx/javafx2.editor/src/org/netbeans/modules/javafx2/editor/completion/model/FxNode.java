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
import org.netbeans.modules.javafx2.editor.parser.NodeInfo;

/**
 * Basic FXML node
 * 
 * @author sdedic
 */
public abstract class FxNode {
    private boolean error;
    
    private FxModel model;
    
    public enum Kind {
        /**
         * Source model
         */
        Source,
        
        /**
         * Import directive
         */
        Import,
        
        /**
         * Language directive
         */
        Language,

        /**
         * fx:include element
         */
        Include,
        
        /**
         * Instance, either new or copy
         */
        Instance,
        
        /**
         * Reference to an instance
         */
        Reference,
        
        /**
         * Property
         */
        Property,
        
        /**
         * Event handler
         */
        Event,
        
        /**
         * Script fragment
         */
        Script,
        
        /**
         * XML namespace declaration
         */
        Namespace,
        
        /**
         * Element pseudo-node
         */
        Element,
        
        /**
         * Attribute pseudo-node
         */
        Attribute,
        
        /**
         * Erroneous Node
         */
        Error,
    }
    
    private NodeInfo    info = NodeInfo.newNode();

    FxNode() {
        if (this instanceof FxModel) {
            this.model = (FxModel)this;
        }
    }
    
    void attach(NodeInfo info) {
        this.info = info;
    }
    
    NodeInfo i() {
        return info;
    }
    
    public abstract Kind    getKind();
    
    public abstract void accept(FxNodeVisitor v);
    
    void detachChild(FxNode child) {
    }
    
    public abstract String getSourceName();

    /**
     * True if the Node is badly broken. No further errors should be reported
     * on this Node. Usually set by an attributing step, when the Node is considered
     * broken enough to be removed from further processing.
     * 
     * @return true, if the node is broken and should be skipped.
     */
    public boolean isBroken() {
        return error;
    }
    
    void markError() {
        this.error = true;
    }
    
    void addChild(FxNode child) {
    }
    
    void setModel(FxModel model) {
        this.model = model;
    }
    
    public FxModel  getRoot() {
        return model;
    }
    
    abstract void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info);
}
