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

package org.netbeans.modules.xml.schema.model.impl.xdm;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

/**
 *
 * @author Nam Nguyen
 */
public class SyncUpdateVisitor<T extends SchemaComponent> implements SchemaVisitor, ComponentUpdater<T> {

    private Operation operation;
    private SchemaComponent parent;
    private int index;
    private boolean canAdd;
    
    public SyncUpdateVisitor(){}
    
    public boolean canAdd(SchemaComponent target, DocumentComponent child) {
        if (!(child instanceof SchemaComponent)) return false;
        update(target, (SchemaComponent) child, null);
        return canAdd;
    }

    @Override
    public void update(SchemaComponent target, SchemaComponent child, 
            Operation operation) {
        update(target, child, -1, operation);
    }

    @Override
    public void update(SchemaComponent target, SchemaComponent child, int index,
            Operation operation) {
        assert target != null;
        assert child != null;
        assert operation == null || operation == Operation.ADD || operation == Operation.REMOVE;

        this.parent = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }

    private SchemaImpl getSchema() {
        assert (parent instanceof Schema) : "Expect parent component is 'schema'"; //NOI18N
        return (SchemaImpl) parent;
    }

    @Override
    public void visit(Schema schema) {
        if (operation == null) {
            canAdd = false;
        } else {
            assert false : "Should never add or remove schema root";
        }
    }
    
    private boolean isParentSchemaRoot() {
        return parent.getModel().getRootComponent() == parent;
    }
    
    private boolean isParentRedefine() {
        return parent instanceof Redefine;
    }

    private void addChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).insertAtIndex(eventName, child, index);
    }
    
    private void removeChild(String eventName, DocumentComponent child) {
        ((AbstractComponent) parent).removeChild(eventName, child);
    }

    @Override
    public void visit(GlobalAttribute child) {
        if(operation == Operation.ADD) {
            getSchema().insertAtIndex(Schema.ATTRIBUTES_PROPERTY, child, index);
        } else if (operation == Operation.REMOVE) {
            getSchema().removeAttribute(child);
        } else {
            canAdd = isParentSchemaRoot();
        }
    }

    @Override
    public void visit(GlobalAttributeGroup child) {
        canAdd = isParentSchemaRoot() || isParentRedefine();
        if (operation == null || !canAdd) return;
        if(operation == Operation.ADD) {
            if (isParentRedefine()) {
                addChild(Redefine.ATTRIBUTE_GROUP_PROPERTY, child);
            } else {
                addChild(Schema.ATTRIBUTE_GROUPS_PROPERTY, child);
            }
        } else if (operation == Operation.REMOVE) {
            if (isParentRedefine()) {
                ((Redefine)parent).removeAttributeGroup(child);
            } else {
                getSchema().removeAttributeGroup(child);
            }
        } else {
            canAdd = isParentSchemaRoot() || isParentRedefine();
        }
    }

    @Override
    public void visit(GlobalElement child) {
        if(operation == Operation.ADD) {
            getSchema().insertAtIndex(Schema.ELEMENTS_PROPERTY, child, index);
        } else if (operation == Operation.REMOVE) {
            getSchema().removeElement(child);
        } else {
            canAdd = isParentSchemaRoot();
        }
    }

    @Override
    public void visit(GlobalGroup child) {
        canAdd = isParentSchemaRoot() || isParentRedefine();
        if (operation == null || !canAdd) return;
        if(operation == Operation.ADD) {
            if (isParentRedefine()) {
                addChild(Redefine.GROUP_DEFINITION_PROPERTY, child);
            } else {
                addChild(Schema.GROUPS_PROPERTY, child);
            }
        } else if (operation == Operation.REMOVE) {
            if (isParentRedefine()) {
                ((Redefine)parent).removeGroupDefinition(child);
            } else {
                getSchema().removeGroup(child);
            }
        } else {
            canAdd = isParentSchemaRoot() || isParentRedefine();
        }
    }

    @Override
    public void visit(GlobalSimpleType child) {
        canAdd = isParentSchemaRoot() || isParentRedefine();
        if (operation == null || !canAdd) return;
        if(operation == Operation.ADD) {
            if (isParentRedefine()) {
                addChild(Redefine.SIMPLE_TYPE_PROPERTY, child);
            } else {
                addChild(Schema.SIMPLE_TYPES_PROPERTY, child);
            }
        } else if (operation == Operation.REMOVE) {
            if (isParentRedefine()) {
                ((Redefine)parent).removeSimpleType(child);
            } else {
                getSchema().removeSimpleType(child);
            }
        } else {
            canAdd = isParentSchemaRoot() || isParentRedefine();
        }
    }

    @Override
    public void visit(GlobalComplexType child) {
        canAdd = isParentSchemaRoot() || isParentRedefine();
        if (operation == null || !canAdd) return;
        if(operation == Operation.ADD) {
            if (isParentRedefine()) {
                addChild(Redefine.COMPLEX_TYPE_PROPERTY, child);
            } else {
                addChild(Schema.COMPLEX_TYPES_PROPERTY, child);
            }
        } else if (operation == Operation.REMOVE) {
            if (isParentRedefine()) {
                ((Redefine)parent).removeComplexType(child);
            } else {
                getSchema().removeComplexType(child);
            } 
        }
    }

    @Override
    public void visit(Notation child) {
        if(operation == Operation.ADD) {
            addChild(Schema.NOTATIONS_PROPERTY, child);
        } else if (operation == Operation.REMOVE) {
            getSchema().removeNotation(child);
        } else {
            canAdd = isParentSchemaRoot();
        }
    }

    @Override
    public void visit(Import child) {
        if(operation == Operation.ADD) {
            getSchema().addExternalReference(child);
        } else if (operation == Operation.REMOVE) {
            getSchema().removeExternalReference(child);
        } else {
            canAdd = isParentSchemaRoot();
        }
    }

    @Override
    public void visit(Include child) {
        if(operation == Operation.ADD) {
            getSchema().addExternalReference(child);
        } else if (operation == Operation.REMOVE) {
            getSchema().removeExternalReference(child);
        } else {
            canAdd = isParentSchemaRoot();
        }
    }

    @Override
    public void visit(Redefine child) {
        if(operation == Operation.ADD) {
            getSchema().addExternalReference(child);
        } else if (operation == Operation.REMOVE) {
            getSchema().removeExternalReference(child);
        } else {
            canAdd = isParentSchemaRoot();
        }
    }

    @Override
    public void visit(LocalSimpleType child) {
        if (parent instanceof List) {
            List list = (List) parent;
            if (operation == Operation.ADD) {
                if (index > -1) { // from sync or paste
                    addChild(List.INLINE_TYPE_PROPERTY, child);
                } else { // from new component
                    list.setInlineType(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(List.INLINE_TYPE_PROPERTY, child);
            } else {
                canAdd = (list.getInlineType() == null);
            }
        } else if (parent instanceof Union) {
            Union union = (Union) parent;
            if (operation == Operation.ADD) {
                addChild(Union.INLINE_TYPE_PROPERTY, child);
            } else if (operation == Operation.REMOVE) {
                union.removeInlineType(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof LocalAttribute || 
		   parent instanceof GlobalAttribute) {
	    if (parent instanceof LocalAttribute) {
		LocalAttribute a = (LocalAttribute) parent;
		if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(LocalAttribute.INLINE_TYPE_PROPERTY, child);
            } else {
                a.setInlineType(child);
            }
		} else if (operation == Operation.REMOVE) {
		    removeChild(LocalAttribute.INLINE_TYPE_PROPERTY, child);
		} else {
                    canAdd = (a.getInlineType() == null);
                }
	    } else {
    		GlobalAttribute a = (GlobalAttribute) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(GlobalAttribute.INLINE_TYPE_PROPERTY, child);
                } else {
                    a.setInlineType(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(GlobalAttribute.INLINE_TYPE_PROPERTY, child);
            } else {
                canAdd = (a.getInlineType() == null);
                }
	    }
        } else if (parent instanceof SimpleRestriction) {
            SimpleRestriction sr = (SimpleRestriction) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(SimpleRestriction.INLINETYPE_PROPERTY, child);
                } else {
                    sr.setInlineType(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(SimpleRestriction.INLINETYPE_PROPERTY, child);
            } else {
                canAdd = (sr.getInlineType() == null);
            }
        } else if (parent instanceof TypeContainer) {
            TypeContainer target = (TypeContainer) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(TypeContainer.INLINE_TYPE_PROPERTY, child);
                } else {
                    target.setInlineType(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(TypeContainer.INLINE_TYPE_PROPERTY, child);
            } else {
                canAdd = (target.getInlineType() == null);
            }
        } else if (operation == null) {
            canAdd = false;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
    }

    @Override
    public void visit(All child) {
        if (parent instanceof ComplexContentRestriction ||
                parent instanceof ComplexType ||
                parent instanceof GlobalGroup) {
            // OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexType.DEFINITION_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexType.DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof GlobalGroup) {
            updateGlobalGroup(child);
        } else if (operation == null) {
            canAdd = false;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
	}
    }

    @Override
    public void visit(ComplexContentRestriction child) {
        if (parent instanceof ComplexContent) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        
        ComplexContent target = (ComplexContent) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(ComplexContent.LOCAL_DEFINITION_PROPERTY, child);
            } else {
                target.setLocalDefinition(child);
            }
        } else if (operation == Operation.REMOVE) {
            removeChild(ComplexContent.LOCAL_DEFINITION_PROPERTY, child);
        } else {
            canAdd = (target.getLocalDefinition() == null);
        }
    }

    @Override
    public void visit(AnyElement child) {
        if (parent instanceof Choice) {
            Choice target = (Choice) parent;
            if (operation == Operation.ADD)
                addChild(Choice.ANY_PROPERTY, child);
            else  if (operation == Operation.REMOVE) {
                target.removeAny(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof Sequence) {
            Sequence target = (Sequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else  if (operation == Operation.REMOVE) {
                target.removeContent(child);
            } else {
                canAdd = true;
            }
        }
    }

    @Override
    public void visit(GroupReference child) {
	if (parent instanceof ComplexType) {
	    ComplexType target = (ComplexType) parent;
	    if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(ComplexType.DEFINITION_PROPERTY, child);
            } else {
        		target.setDefinition(child);
            }
        } else if (operation == Operation.REMOVE) {
		    removeChild(ComplexType.DEFINITION_PROPERTY, child);
	    } else if (operation == null) {
            canAdd = (target.getDefinition() == null);
	    }
	} else if (parent instanceof Sequence) {
	    Sequence target = (Sequence) parent;
	    if (operation == Operation.ADD)
		target.addContent(child, index);
	    else if (operation == Operation.REMOVE) {
		target.removeContent(child);
	    } else if (operation == null) {
		canAdd = true;
	    }
	} else if (parent instanceof ComplexContentRestriction) {
	    ComplexContentRestriction target = (ComplexContentRestriction) parent;
	    if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
            } else {
                target.setDefinition(child);
            }
        } else if (operation == Operation.REMOVE) {
            removeChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
	    } else if (operation == null) {
            canAdd = (target.getDefinition() == null);
	    }
	} else if (parent instanceof ComplexExtension) {
	    ComplexExtension target = (ComplexExtension) parent;
	    if (operation == Operation.ADD)
            if (index > -1) { // from sync
                addChild(ComplexExtension.LOCAL_DEFINITION_PROPERTY, child);
            } else { // from ui
                target.setLocalDefinition(child);
            }
	    else if (operation == Operation.REMOVE) {
            removeChild(ComplexExtension.LOCAL_DEFINITION_PROPERTY, child);
	    } else if (operation == null) {
            canAdd = (target.getLocalDefinition() == null);
	    }
	    
	} else if (parent instanceof Choice) {
	    Choice target = (Choice) parent;
	    if (operation == Operation.ADD) {
            addChild(Choice.GROUP_REF_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removeGroupReference(child);
	    } else if (operation == null) {
            canAdd = true;
	    }
	} else if (operation == null) {
	    canAdd = false;
	    return;
	} else {
	    assert false: "Wrong parent "+parent.getClass().getName();
	}
    }

    @Override
    public void visit(Enumeration child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.ENUMERATION_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeEnumeration(child);
        } else {
            canAdd = true;
        }
    }

    private void updateConstraintOnCommonElement(Constraint child) {
        if (parent instanceof Element) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        Element target = (Element) parent;
        if (operation == Operation.ADD)
            addChild(Element.CONSTRAINT_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeConstraint(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(KeyRef child) {
        updateConstraintOnCommonElement(child);
    }

    @Override
    public void visit(Key child) {
        updateConstraintOnCommonElement(child);
    }

    @Override
    public void visit(Unique child) {
        updateConstraintOnCommonElement(child);
    }

    @Override
    public void visit(AttributeGroupReference child) {
        if (parent instanceof LocalAttributeContainer) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD)
            addChild(LocalAttributeContainer.ATTRIBUTE_GROUP_REFERENCE_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeAttributeGroupReference(child);
        } else {
            canAdd = true;
        }
    }

    private void updateGlobalGroup(LocalGroupDefinition child) {
        if (parent instanceof GlobalGroup) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        GlobalGroup target = (GlobalGroup) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(GlobalGroup.DEFINITION_PROPERTY, child);
            } else {
                target.setDefinition(child);
            }
        } else  if (operation == Operation.REMOVE) {
            removeChild(GlobalGroup.DEFINITION_PROPERTY, child);
        } else {
            canAdd = (target.getDefinition() == null);
        }
    }

    @Override
    public void visit(Documentation child) {
        if (parent instanceof Annotation) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        Annotation target = (Annotation) parent;
        if (operation == Operation.ADD)
            addChild(Annotation.DOCUMENTATION_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeDocumentation(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(AppInfo child) {
        if (parent instanceof Annotation) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        Annotation target = (Annotation) parent;
        if (operation == Operation.ADD)
            addChild(Annotation.APPINFO_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeAppInfo(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(Choice child) {
        if (parent instanceof Choice) {
            Choice target = (Choice) parent;
            if (operation == Operation.ADD)
                addChild(Choice.CHOICE_PROPERTY, child);
            else  if (operation == Operation.REMOVE) {
                target.removeChoice(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof ComplexExtension) {
            ComplexExtension target = (ComplexExtension) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexExtension.LOCAL_DEFINITION_PROPERTY, child);
                } else {
                    target.setLocalDefinition(child);
                }
            } else  if (operation == Operation.REMOVE) {
                removeChild(ComplexExtension.LOCAL_DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getLocalAttributes() == null);
            }
        } else if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else  if (operation == Operation.REMOVE) {
                removeChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof Sequence) {
            Sequence target = (Sequence) parent;
            if (operation == Operation.ADD) 
                target.addContent(child, index);
            else  if (operation == Operation.REMOVE) {
                target.removeContent(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                if (index > -1) { // from sync
                    addChild(ComplexType.DEFINITION_PROPERTY, child);
                } else { // from UI
                    target.setDefinition(child);
                }
            } else  if (operation == Operation.REMOVE) {
                removeChild(ComplexType.DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof GlobalGroup) {
	    updateGlobalGroup(child);
        } else if (operation == null) {
            canAdd = false;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
    }

    @Override
    public void visit(SimpleContentRestriction child) {
        if (parent instanceof SimpleContent) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleContent target = (SimpleContent) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(SimpleContent.LOCAL_DEFINITION_PROPERTY, child);
            } else {
                target.setLocalDefinition(child);
            }
        } else  if (operation == Operation.REMOVE) {
            removeChild(SimpleContent.LOCAL_DEFINITION_PROPERTY, child);
        } else {
            canAdd = (target.getLocalDefinition() == null);
        }
    }

    @Override
    public void visit(Selector child) {
        if (parent instanceof Constraint) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        Constraint target = (Constraint) parent;
        if (operation == Operation.ADD)  {
            if (index > -1) {
                addChild(Constraint.SELECTOR_PROPERTY, child);
            } else {
                target.setSelector(child);
            }
        } else  if (operation == Operation.REMOVE) {
            removeChild(Constraint.SELECTOR_PROPERTY, child);
        } else {
            canAdd = (target.getSelector() == null);
        }
    }

    @Override
    public void visit(LocalElement child) {
	if (parent instanceof Choice) {
	    Choice target = (Choice) parent;
	    if (operation == Operation.ADD) {
            addChild(Choice.LOCAL_ELEMENT_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removeLocalElement(child);
	    } else {
            canAdd = true;
	    }
	} else if (parent instanceof Sequence) {
	    Sequence target = (Sequence) parent;
	    if (operation == Operation.ADD) {
            target.addContent(child, index);
        } else  if (operation == Operation.REMOVE) {
            target.removeContent(child);
	    } else {
            canAdd = true;
	    }
	} else if (parent instanceof All) {
	    All target = (All) parent;
            if (operation == Operation.ADD) {
                addChild(All.ELEMENT_PROPERTY, child);
            } else  if (operation == Operation.REMOVE) {
                target.removeElement(child);
            } else {
                canAdd = true;
            }
        } else if (operation == null) {
            canAdd = false;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
    }

    @Override
    public void visit(ElementReference child) {
	if (parent instanceof Choice ||
	       parent instanceof All ||
	       parent instanceof Sequence) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
	
	if (parent instanceof Choice) {
            Choice target = (Choice) parent;
            if (operation == Operation.ADD)
                addChild(Choice.ELEMENT_REFERENCE_PROPERTY, child);
            else  if (operation == Operation.REMOVE) {
                target.removeElementReference(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof Sequence) {
            Sequence target = (Sequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else  if (operation == Operation.REMOVE) {
                target.removeContent(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof All) {
            All target = (All) parent;
            if (operation == Operation.ADD)
                addChild(All.ELEMENT_REFERENCE_PROPERTY, child);
            else  if (operation == Operation.REMOVE) {
                target.removeElementReference(child);
            } else {
                canAdd = true;
            }
        } 
	
    }

    @Override
    public void visit(Annotation child) {
        if (parent instanceof Annotation ||
            parent instanceof Documentation ||
            parent instanceof AppInfo) {
            if (operation == null) {
                canAdd = false;
                return;
            } else {
                assert false: "Wrong parent "+parent.getClass().getName();
                return;
            }
        }
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(SchemaComponent.ANNOTATION_PROPERTY, child);
            } else {
                parent.setAnnotation(child);
            }
        } else  if (operation == Operation.REMOVE) {
            removeChild(SchemaComponent.ANNOTATION_PROPERTY, child);
        } else {
            canAdd = (parent.getAnnotation() == null);
        }
    }

    @Override
    public void visit(ComplexExtension child) {
        if (parent instanceof ComplexContent) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        ComplexContent target = (ComplexContent) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(ComplexContent.LOCAL_DEFINITION_PROPERTY, child);
            } else {
                target.setLocalDefinition(child);
            }
        } else  if (operation == Operation.REMOVE) {
            removeChild(ComplexContent.LOCAL_DEFINITION_PROPERTY, child);
        } else {
            canAdd = (target.getLocalDefinition() == null);
        }
    }

    @Override
    public void visit(SimpleExtension child) {
        if (parent instanceof SimpleContent) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleContent target = (SimpleContent) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(SimpleContent.LOCAL_DEFINITION_PROPERTY, child);
            } else {
                target.setLocalDefinition(child);
            }
        } else if (operation == Operation.REMOVE) {
            removeChild(SimpleContent.LOCAL_DEFINITION_PROPERTY, child);
        } else {
            canAdd = (target.getLocalDefinition() == null);
        }
    }

    @Override
    public void visit(Sequence child) {
        if (parent instanceof Choice) {
            Choice target = (Choice) parent;
            if (operation == Operation.ADD) {
                addChild(Choice.SEQUENCE_PROPERTY, child);
            } else  if (operation == Operation.REMOVE) {
                removeChild(Choice.SEQUENCE_PROPERTY, child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof Sequence) {
            Sequence target = (Sequence) parent;
            if (operation == Operation.ADD)
                target.addContent(child, index);
            else  if (operation == Operation.REMOVE) {
                target.removeContent(child);
            } else {
                canAdd = true;
            }
        } else if (parent instanceof ComplexExtension) {
            ComplexExtension target = (ComplexExtension) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexExtension.LOCAL_DEFINITION_PROPERTY, child);
                } else {
                    target.setLocalDefinition(child);
                }
            } else  if (operation == Operation.REMOVE) {
                removeChild(ComplexExtension.LOCAL_DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getLocalDefinition() == null);
            }
        } else if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexType.DEFINITION_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else  if (operation == Operation.REMOVE) {
                removeChild(ComplexType.DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof GlobalGroup) {
            updateGlobalGroup(child);
        } else if (operation == null) {
            canAdd = false;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
    }

    @Override
    public void visit(MinExclusive child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.MIN_EXCLUSIVE_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeMinExclusive(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(MinInclusive child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD) {
            addChild(SimpleRestriction.MIN_INCLUSIVE_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removeMinInclusive(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(Pattern child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
            
        SimpleRestriction target = (SimpleRestriction) parent;
        
        if (operation == Operation.ADD) {
            addChild(SimpleRestriction.PATTERN_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removePattern(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(MinLength child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD) {
            addChild(SimpleRestriction.MIN_LENGTH_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removeMinLength(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(MaxLength child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD) {
            addChild(SimpleRestriction.MAX_LENGTH_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removeMaxLength(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(Whitespace child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.WHITESPACE_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeWhitespace(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(MaxInclusive child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD) {
            addChild(SimpleRestriction.MAX_INCLUSIVE_PROPERTY, child);
        } else  if (operation == Operation.REMOVE) {
            target.removeMaxInclusive(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(LocalComplexType child) {
	if (parent instanceof TypeContainer) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        
        TypeContainer target = (TypeContainer) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(TypeContainer.INLINE_TYPE_PROPERTY, child);
            } else {
                target.setInlineType(child);
            }
        } else if (operation == Operation.REMOVE) {
            removeChild(TypeContainer.INLINE_TYPE_PROPERTY, child);
        } else {
            canAdd = (target.getInlineType() == null);
        }
    }

    @Override
    public void visit(FractionDigits child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.FRACTION_DIGITS_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeFractionDigits(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(TotalDigits child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.TOTAL_DIGITS_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeTotalDigit(child);
        } else {
            canAdd = true;
        }
    }

    private void updateSimpleType(SimpleTypeDefinition child) {
        if (parent instanceof SimpleType) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleType target = (SimpleType) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(SimpleType.DEFINITION_PROPERTY, child);
            } else {
                target.setDefinition(child);
            }
        } else if (operation == Operation.REMOVE) {
            removeChild(SimpleType.DEFINITION_PROPERTY, child);
        } else {
            canAdd = (target.getDefinition() == null);
        }
    }

    @Override
    public void visit(List child) {
        updateSimpleType(child);
    }

    @Override
    public void visit(SimpleTypeRestriction child) {
        updateSimpleType(child);
    }

    @Override
    public void visit(Union child) {
        updateSimpleType(child);
    }

    @Override
    public void visit(MaxExclusive child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.MAX_EXCLUSIVE_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeMaxExclusive(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(AttributeReference child) {
        if (parent instanceof LocalAttributeContainer) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD)
            addChild(LocalAttributeContainer.LOCAL_ATTRIBUTE_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeAttributeReference(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(LocalAttribute child) {
        if (parent instanceof LocalAttributeContainer) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD)
            addChild(LocalAttributeContainer.LOCAL_ATTRIBUTE_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeLocalAttribute(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(SimpleContent child) {
        if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexType.DEFINITION_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexType.DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
    }

    @Override
    public void visit(ComplexContent child) {
        if (parent instanceof ComplexContentRestriction) {
            ComplexContentRestriction target = (ComplexContentRestriction) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexContentRestriction.DEFINITION_CHANGED_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (parent instanceof ComplexType) {
            ComplexType target = (ComplexType) parent;
            if (operation == Operation.ADD) {
                if (index > -1) {
                    addChild(ComplexType.DEFINITION_PROPERTY, child);
                } else {
                    target.setDefinition(child);
                }
            } else if (operation == Operation.REMOVE) {
                removeChild(ComplexType.DEFINITION_PROPERTY, child);
            } else {
                canAdd = (target.getDefinition() == null);
            }
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        
    }

    @Override
    public void visit(AnyAttribute child) {
        if (parent instanceof LocalAttributeContainer) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        LocalAttributeContainer target = (LocalAttributeContainer) parent;
        if (operation == Operation.ADD) {
            if (index > -1) {
                addChild(LocalAttributeContainer.ANY_ATTRIBUTE_PROPERTY, child);
            } else {
                target.setAnyAttribute(child);
            }
        } else if (operation == Operation.REMOVE) {
            removeChild(LocalAttributeContainer.ANY_ATTRIBUTE_PROPERTY, child);
        } else {
            canAdd = (target.getAnyAttribute() == null);
        }
    }

    @Override
    public void visit(Length child) {
        if (parent instanceof SimpleRestriction) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        SimpleRestriction target = (SimpleRestriction) parent;
        if (operation == Operation.ADD)
            addChild(SimpleRestriction.LENGTH_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.removeLength(child);
        } else {
            canAdd = true;
        }
    }

    @Override
    public void visit(Field child) {
        if (parent instanceof Constraint) {
            //OK
        } else if (operation == null) {
            canAdd = false;
            return;
        } else {
            assert false: "Wrong parent "+parent.getClass().getName();
        }
        Constraint target = (Constraint) parent;
        if (operation == Operation.ADD)
            addChild(Constraint.FIELD_PROPERTY, child);
        else  if (operation == Operation.REMOVE) {
            target.deleteField(child);
        } else {
            canAdd = true;
        }
    }
}
