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

package org.netbeans.modules.xml.text.completion;

import java.awt.Container;
import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.HintContext;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.completion.XMLCompletionQuery;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;

/**
 * Finds SyntaxNode at the Carat position in the text editor
 * and asks grammars to provide customizer and properties.
 * The customizer and properties are added to the selected node in the editors
 * TopComponent.
 *
 * @author  asgeir@dimonsoftware.com
 */
public final class NodeSelector {
    
    /** Listener on caret movements */
    private CaretListener caretListener;
    
    /** Timer which countdowns the "update selected element node" time. */ // NOI18N
    private Timer timerSelNodes;
    
    /** The last caret offset position. */
    private int lastCaretOffset = -1;
    
    /** Default delay between cursor movement and updating selected element nodes. */
    private static final int SELECTED_NODES_DELAY = 200;
    
    private JEditorPane pane;
    
    private XMLSyntaxSupport syntaxSupport;
    
    private Node originalUINode;
    
    HintContext hintContext;
    
    /** Creates a new instance of NodeSelector */
    public NodeSelector(final JEditorPane pane) {
        this.pane = pane;
        
        caretListener = new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                restartTimerSelNodes(e.getDot());
            }
        };
        
        timerSelNodes = new Timer(100, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (lastCaretOffset == -1 && pane != null) {
                    Caret caret = pane.getCaret();
                    if (caret != null)
                        lastCaretOffset = caret.getDot();
                }
                selectElementsAtOffset(lastCaretOffset);
            }
        });
        timerSelNodes.setInitialDelay(100);
        timerSelNodes.setRepeats(false);
        timerSelNodes.restart();
        
        pane.addCaretListener(caretListener);

        //!!! It should also listen on current TopComponent
    }
    
    /** Restart the timer which updates the selected nodes after the specified delay from
     * last caret movement.
     */
    void restartTimerSelNodes(int pos) {
        timerSelNodes.setInitialDelay(SELECTED_NODES_DELAY);
        lastCaretOffset = pos;
        timerSelNodes.restart();
    }
    
    /** Selects element at the given position. */
    synchronized void selectElementsAtOffset(final int offset) {
        if (syntaxSupport == null) {
            Document doc = pane.getDocument();
            if (doc instanceof BaseDocument) {
                syntaxSupport = XMLSyntaxSupport.getSyntaxSupport((BaseDocument)doc);
            }
            if (syntaxSupport == null) {
                return;
            }
        }
        
        Container parent = pane.getParent();
        while (parent != null && !(parent instanceof TopComponent)){
            parent = parent.getParent();
        }
        if (parent == null) {
            return;
        }
        
        TopComponent topComp = (TopComponent)parent;
        Node activeNodes[] = topComp.getActivatedNodes();
        if (activeNodes == null || activeNodes.length == 0) {
            return; // No nodes active
        }
        
        if (originalUINode == null) {
            originalUINode = activeNodes[0];
        }

        //it must be called from separate thread, it may the block UI thread
        
        GrammarQuery grammarQuery = XMLCompletionQuery.getPerformer(pane.getDocument(), syntaxSupport);
        if (grammarQuery == null) {
            return;
        }
        
        SyntaxQueryHelper helper = null;
        try {
            helper = new SyntaxQueryHelper(syntaxSupport, offset);
        } catch (BadLocationException e) {
            topComp.setActivatedNodes(new Node[]{new DelegatingNode(originalUINode, null, null)});
            return;
        }
        
        Node newUiNode = new DelegatingNode(originalUINode, grammarQuery, helper.getContext());
        
        topComp.setActivatedNodes(new Node[]{newUiNode});
    }
    
    private class DelegatingNode extends FilterNode {
        
        GrammarQuery grammarQuery;
        
        HintContext hintContext;
        
        Sheet propSheet;
        
        public DelegatingNode(Node peer, GrammarQuery grammarQuery, HintContext hintContext) {
            super(peer);
            this.grammarQuery = grammarQuery;
            this.hintContext = hintContext;
        }
        
        public java.awt.Component getCustomizer() {
            if (grammarQuery == null || hintContext == null) {
                return super.getCustomizer();
            } else {
                return grammarQuery.getCustomizer(hintContext);
            }
        }
        
        public boolean hasCustomizer() {
            if (grammarQuery == null || hintContext == null) {
                return super.hasCustomizer();
            } else {
                return grammarQuery.hasCustomizer(hintContext);
            }
        }
        
        public Node.PropertySet[] getPropertySets() {
            if (propSheet == null) {
                propSheet = Sheet.createDefault();
                Sheet.Set nodePropertySet = propSheet.get(Sheet.PROPERTIES);
                
                if (grammarQuery != null && hintContext != null) {
                    Node.Property[] nodeProperties = grammarQuery.getProperties(hintContext);
                    if (nodeProperties != null && nodeProperties.length > 0) {
                        // The GrammarQuery controls the properties
                        nodePropertySet.put(nodeProperties);
                        return propSheet.toArray();
                    }
                }
                
                // By default, we try to create properties from the attributes of the
                // selected element.
                org.w3c.dom.Element attributeOwningElem = null;
                if (hintContext != null) {
                    if (hintContext.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        attributeOwningElem = (org.w3c.dom.Element)hintContext;
                    } else if (hintContext.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
                        attributeOwningElem = (org.w3c.dom.Element)((org.w3c.dom.Attr)hintContext).getOwnerElement();
                    }
                }
                
                if (attributeOwningElem != null) {
                    // We have a selected element that might have attributes
                    org.w3c.dom.NamedNodeMap attributes = attributeOwningElem.getAttributes();
                    for (int ind = 0; ind < attributes.getLength(); ind++) {
                        org.w3c.dom.Node node = attributes.item(ind);
                        nodePropertySet.put(new AttributeProperty(attributeOwningElem, node.getNodeName()));
                    }
                    
                }
            }
            
            return propSheet.toArray();
        }
    }

    /**
     * It models attribute as node property.
     */
    private class AttributeProperty extends org.openide.nodes.PropertySupport {
        private final String propName;
        private final org.w3c.dom.Element ownerElem;
        private boolean canWrite = true;

        public AttributeProperty(org.w3c.dom.Element ownerElem, String propName) {
            super(propName, String.class, propName, propName, true, true);
            this.ownerElem = ownerElem;
            this.propName = propName;
        }
        
        public void setValue(Object value) {
            try {
                ownerElem.setAttribute(propName, (String)value);
            } catch (DOMException ex) {
                canWrite = false;
            }
        }
        
        public Object getValue() {
            try {
                Attr attrNode = ownerElem.getAttributeNode(propName);
                return attrNode != null ? attrNode.getValue() : null;
            } catch (DOMException ex) {
                // #29618 lifetime problem
                canWrite = false;
                return NbBundle.getMessage(NodeSelector.class, "BK0001");
            }
        }

        /**
         * Writeability can change during lifetime.
         * So nobody can cache this flag.
         */
        public boolean canWrite() {
            return canWrite;
        }

    }
}
