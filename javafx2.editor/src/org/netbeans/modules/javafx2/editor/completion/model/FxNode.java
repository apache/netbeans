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
