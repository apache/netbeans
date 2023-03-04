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

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 * Visitor to add or remove a child of a WSDL component.
 * @author Nam Nguyen
 */
public class ChildComponentUpdateVisitor<T extends WSDLComponent> implements WSDLVisitor, ComponentUpdater<T> {
    
    private Operation operation;
    private WSDLComponent parent;
    private int index;
    private boolean canAdd = false;
    
    /**
     * Creates a new instance of ChildComponentUpdateVisitor
     */
    public ChildComponentUpdateVisitor() {
    }
    
    public boolean canAdd(WSDLComponent target, Component child) {
        if (!(child instanceof WSDLComponent)) return false;
        update(target, (WSDLComponent) child, null);
        return canAdd;
    }
    
    public void update(WSDLComponent target, WSDLComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    
    public void update(WSDLComponent target, WSDLComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;

        this.parent = target;
        this.operation = operation;
        this.index = index;
        child.accept(this);
    }
    
    private void addChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).insertAtIndex(eventName, child, index);
    }
    
    private void removeChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).removeChild(eventName, child);
    }
    
    public void visit(Definitions child) {
        checkOperationOnUnmatchedParent();
    }

    public void visit(Types child) {
        if (parent instanceof Definitions) {
            if (operation == Operation.ADD) {
                addChild(Definitions.TYPES_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(Definitions.TYPES_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Binding child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                addChild(target.BINDING_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeBinding(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Message child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                addChild(target.MESSAGE_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeMessage(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Service child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                addChild(target.SERVICE_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeService(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(PortType child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                addChild(target.PORT_TYPE_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removePortType(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Import child) {
        if (parent instanceof Definitions) {
            Definitions target = (Definitions)parent;
            if (operation == Operation.ADD) {
                addChild(target.IMPORT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeImport(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Port child) {
        if (parent instanceof Service) {
            Service target = (Service)parent;
            if (operation == Operation.ADD) {
                addChild(target.PORT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removePort(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingOperation child) {
        if (parent instanceof Binding) {
            Binding target = (Binding)parent;
            if (operation == Operation.ADD) {
                addChild(target.BINDING_OPERATION_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeBindingOperation(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingInput child) {
        if (parent instanceof BindingOperation) {
            if (operation == Operation.ADD) {
                addChild(BindingOperation.BINDING_INPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(BindingOperation.BINDING_INPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingOutput child) {
        if (parent instanceof BindingOperation) {
            if (operation == Operation.ADD) {
                addChild(BindingOperation.BINDING_OUTPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(BindingOperation.BINDING_OUTPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(BindingFault child) {
        if (parent instanceof BindingOperation) {
            BindingOperation target = (BindingOperation)parent;
            if (operation == Operation.ADD) {
                addChild(BindingOperation.BINDING_FAULT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeBindingFault(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Part child) {
        if (parent instanceof Message) {
            Message target = (Message)parent;
            if (operation == Operation.ADD) {
                addChild(Message.PART_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removePart(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Documentation doc) {
        if (operation == Operation.ADD) {
            addChild(WSDLComponent.DOCUMENTATION_PROPERTY, doc);
        } else if (operation == Operation.REMOVE) {
            removeChild(WSDLComponent.DOCUMENTATION_PROPERTY, doc);
        } else if (operation == null) {
            canAdd = true;
        }
    }
    
    public void visit(Output child) {
        if (parent instanceof RequestResponseOperation || 
            parent instanceof SolicitResponseOperation ||
            parent instanceof NotificationOperation) 
        {
            if (operation == Operation.ADD) {
                addChild(org.netbeans.modules.xml.wsdl.model.Operation.OUTPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(org.netbeans.modules.xml.wsdl.model.Operation.OUTPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Input child) {
        if (parent instanceof OneWayOperation ||
            parent instanceof RequestResponseOperation ||
            parent instanceof SolicitResponseOperation) 
        {
            if (operation == Operation.ADD) {
                addChild(org.netbeans.modules.xml.wsdl.model.Operation.INPUT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                removeChild(org.netbeans.modules.xml.wsdl.model.Operation.INPUT_PROPERTY, child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(Fault child) {
        if (parent instanceof org.netbeans.modules.xml.wsdl.model.Operation) {
            org.netbeans.modules.xml.wsdl.model.Operation target = 
                (org.netbeans.modules.xml.wsdl.model.Operation)parent;
            boolean operationWithFaults = 
                parent instanceof RequestResponseOperation || 
                parent instanceof SolicitResponseOperation;

            if (operationWithFaults && operation == Operation.ADD) {
                addChild(target.FAULT_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeFault(child);
            } else if (operation == null) {
                canAdd = operationWithFaults;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    private void update(org.netbeans.modules.xml.wsdl.model.Operation child) {
        if (parent instanceof PortType) {
            PortType target = (PortType)parent;
            if (operation == Operation.ADD) {
                addChild(target.OPERATION_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                target.removeOperation(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else {
            checkOperationOnUnmatchedParent();
        }
    }
    
    public void visit(NotificationOperation child) {
        update(child);
    }
    
    public void visit(SolicitResponseOperation child) {
        update(child);
    }

    public void visit(RequestResponseOperation child) {
        update(child);
    }
    
    public void visit(OneWayOperation child) {
        update(child);
    }
    
    public void visit(ExtensibilityElement child) {
        if (parent instanceof ExtensibilityElement.UpdaterProvider) {
            ExtensibilityElement.UpdaterProvider target = (ExtensibilityElement.UpdaterProvider) parent;
            ComponentUpdater<ExtensibilityElement> updater = target.getComponentUpdater();
            if (operation != null) {
                updater.update(target, child, index, operation);
            } else {
                canAdd = false;
                if (updater instanceof ComponentUpdater.Query) {
                    canAdd = ((ComponentUpdater.Query) updater).canAdd(target, child);
                } 
            }
        } else {
            if (operation == Operation.ADD) {
                parent.addExtensibilityElement(child);
            } else if (operation == Operation.REMOVE) {
                parent.removeExtensibilityElement(child);
            } else if (operation == null) {
                canAdd = true;
                if (child instanceof ExtensibilityElement.ParentSelector) {
                    canAdd = ((ExtensibilityElement.ParentSelector)child).canBeAddedTo(parent);
                }
            }
        }
    }

    private void checkOperationOnUnmatchedParent() {
        if (operation != null) {
            // note this unmatch should be caught by validation, 
            // we don't want the UI view to go blank on invalid but still well-formed document
            //throw new IllegalArgumentException("Unmatched parent-child components"); //NO18N
        } else {
            canAdd = false;
        }
    }
}
