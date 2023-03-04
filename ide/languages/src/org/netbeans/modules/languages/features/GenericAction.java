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

package org.netbeans.modules.languages.features;

import org.netbeans.modules.languages.*;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTNode;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.api.languages.ParseException;
import org.openide.ErrorManager;


/**
 * @author Dan Prusa
 */
public class GenericAction extends BaseAction {
    
    String performerName = null;
    String enablerName = null;
    Feature performer = null;
    Feature enabler = null;
    
    public GenericAction(String name, String performerName, String enablerName) {
        super(name);
        this.performerName = performerName;
        this.enablerName = enablerName;
    }
    
    private Feature getPerformer() {
        if (performer == null) {
            performer = Feature.createMethodCallFeature (null, null, performerName);
        }
        return performer;
    }
    
    private Feature getEnabler() {
        if (enablerName == null) {
            return null;
        }
        if (enabler == null) {
            enabler = Feature.createMethodCallFeature (null, null, enablerName);
        }
        return enabler;
    }
    
    private ASTNode getASTNode(JTextComponent comp) {
        return ParserManagerImpl.getImpl (comp.getDocument ()).getAST();
    }
    
    
    public void actionPerformed(ActionEvent e, JTextComponent comp) {
        ASTNode node = getASTNode(comp);
        if (node != null) {
            getPerformer().getValue (new Object[] {node, comp});
        }
    }
    
    public boolean isEnabled() {
        JTextComponent comp = getTextComponent(null);
        if (comp == null)
            return false;
        ASTNode node = getASTNode(comp);
        if (node == null)
            return false;
        Feature em = getEnabler();
        if (em == null) {
            return super.isEnabled();
        }
        Object result = em.getValue (new Object[] {node, comp});
        return result != null && ((Boolean)result).booleanValue();
    }
    
}
