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

package org.netbeans.modules.xml.wsdl.model.extensions.mime.impl;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEContent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMimeXml;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMultipartRelated;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEPart;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Header;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.ComponentUpdater.Query;

/**
 *
 * @author jyang
 */




public class MIMEComponentUpdater
        implements ComponentUpdater<ExtensibilityElement>, Query<ExtensibilityElement>, MIMEComponent.Visitor {

    private MIMEComponent parent;
    private Operation operation;
    private boolean canAdd;

    /** Creates a new instance of SOAPComponentUpdater */
    public MIMEComponentUpdater() {
    }

    public boolean canAdd(MIMEComponent target, Component child) {
        if (child instanceof MIMEComponent) {
            update(target, (MIMEComponent) child, null);
        } else if (child instanceof SOAPComponent ) {
           update(target, (SOAPComponent) child, null);
        } else if (child instanceof SOAP12Component) {
           update(target, (SOAP12Component) child, null);
        } else {
            return false;
        }
        return canAdd;
    }

    public void update(MIMEComponent target, MIMEComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    


    public void update(MIMEComponent target, MIMEComponent child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        child.accept(this);
    }
    
    public void update(MIMEComponent target, SOAPComponent child, Operation operation) {
        update(target, child, -1, operation);
    }
    


    public void update(MIMEComponent target, SOAPComponent child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        if (child instanceof SOAPBody && target instanceof MIMEPart) {
            if (parent instanceof MIMEPart) {
                MIMEPart nTarget = (MIMEPart) target;
                if (operation == Operation.ADD) {
                    nTarget.addExtensibilityElement((SOAPBody) child);
                } else if (operation == Operation.REMOVE) {
                    nTarget.removeExtensibilityElement((SOAPBody) child);
                } else {
                    canAdd = true;
                }

            }
        } else if (child instanceof SOAPHeader && target instanceof MIMEPart) {
            if (parent instanceof MIMEPart) {
                MIMEPart nTarget = (MIMEPart) target;
                if (operation == Operation.ADD) {
                    nTarget.addExtensibilityElement((SOAPHeader) child);
                } else if (operation == Operation.REMOVE) {
                    nTarget.removeExtensibilityElement((SOAPHeader) child);
                } else {
                    canAdd = true;
                }

            }
        }
    }
    
    public void update(MIMEComponent target, SOAP12Component child, int index, Operation operation) {
        parent = target;
        this.operation = operation;
        if (child instanceof SOAP12Body && target instanceof MIMEPart) {
            if (parent instanceof MIMEPart) {
                MIMEPart nTarget = (MIMEPart) target;
                if (operation == Operation.ADD) {
                    nTarget.addExtensibilityElement((SOAP12Body) child);
                } else if (operation == Operation.REMOVE) {
                    nTarget.removeExtensibilityElement((SOAP12Body) child);
                } else {
                    canAdd = true;
                }

            }
        } else if (child instanceof SOAP12Header && target instanceof MIMEPart) {
            if (parent instanceof MIMEPart) {
                MIMEPart nTarget = (MIMEPart) target;
                if (operation == Operation.ADD) {
                    nTarget.addExtensibilityElement((SOAP12Header) child);
                } else if (operation == Operation.REMOVE) {
                    nTarget.removeExtensibilityElement((SOAP12Header) child);
                } else {
                    canAdd = true;
                }

            }
        }
    }

    public void visit(MIMEContent child) {
        if (parent instanceof MIMEPart) {
            MIMEPart target = (MIMEPart) parent;
            if (operation == Operation.ADD) {
                target.addExtensibilityElement(child);
            } else if (operation == Operation.REMOVE) {
                target.removeExtensibilityElement(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(MIMEMultipartRelated child) {
        if (parent instanceof MIMEPart) {
            MIMEPart target = (MIMEPart) parent;
            if (operation == Operation.ADD) {
                target.addExtensibilityElement(child);
            } else if (operation == Operation.REMOVE) {
                target.removeExtensibilityElement(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (operation == null) {
            canAdd = false;
        }
       
    }

    public void visit(MIMEMimeXml child) {
        if (parent instanceof MIMEPart) {
            MIMEPart target = (MIMEPart) parent;
            if (operation == Operation.ADD) {
                target.addExtensibilityElement(child);
            } else if (operation == Operation.REMOVE) {
                target.removeExtensibilityElement(child);
            } else if (operation == null) {
                canAdd = true;
            }
        } else if (operation == null) {
            canAdd = false;
        }
    }

    public void visit(MIMEPart child) {
        if (parent instanceof MIMEMultipartRelated) {
            MIMEMultipartRelated target = (MIMEMultipartRelated) parent;
            if (operation == Operation.ADD) {
                target.addMIMEPart(child);
            } else if (operation == Operation.REMOVE) {
                target.removeMIMEPart(child);
            } 
        }  else if (operation == null) {
            canAdd = true;
        }
    }



    public boolean canAdd(ExtensibilityElement target, Component child) {
         if (target instanceof MIMEComponent ) {
             return canAdd((MIMEComponent)target, child);
         }
         return false;
    }

    public void update(ExtensibilityElement target, ExtensibilityElement child, Operation operation) {
        if (target instanceof MIMEComponent && child instanceof MIMEComponent) {
            update((MIMEComponent)target, (MIMEComponent)child, -1, operation);
        }
        
    }
    
    public void update(ExtensibilityElement target, ExtensibilityElement child, int index, Operation operation) {
        if (target instanceof MIMEComponent && child instanceof MIMEComponent) {
            update((MIMEComponent)target, (MIMEComponent)child, -1, operation);
        } else if (target instanceof MIMEComponent && child instanceof SOAPComponent) {
            update((MIMEComponent)target, (SOAPComponent)child, -1, operation);
        } else if (target instanceof MIMEComponent && child instanceof SOAP12Component)
            update((MIMEComponent)target, (SOAP12Component)child, -1, operation);
        }
}

