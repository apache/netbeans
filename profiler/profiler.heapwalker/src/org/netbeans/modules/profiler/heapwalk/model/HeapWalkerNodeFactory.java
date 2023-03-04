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

package org.netbeans.modules.profiler.heapwalk.model;


import org.openide.util.NbBundle;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.lib.profiler.heap.ArrayItemValue;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.lib.profiler.heap.Value;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "HeapWalkerNodeFactory_NoneString=<none>",
    "HeapWalkerNodeFactory_NoFieldsString=<no fields>",
    "HeapWalkerNodeFactory_NoReferencesString=<no references>",
    "HeapWalkerNodeFactory_NoItemsString=<no items>",
    "HeapWalkerNodeFactory_SearchingString=<Searching...>",
    "HeapWalkerNodeFactory_OutOfMemoryString=<out of memory>",
    "HeapWalkerNodeFactory_ArrayContainerNameString=<items {0}-{1}>",
    "HeapWalkerNodeFactory_ArrayContainerValueString=({0} items)"
})
public class HeapWalkerNodeFactory {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int ITEMS_COLLAPSE_UNIT_SIZE = 500;
    public static final int ITEMS_COLLAPSE_THRESHOLD = 2000;
    public static final int ITEMS_COLLAPSE_UNIT_THRESHOLD = 5000;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static HeapWalkerNode createArrayItemContainerNode(final ArrayNode array, final int startIndex, final int endIndex) {
        return new AbstractHeapWalkerNode(array) {
            protected String computeName() {
                return Bundle.HeapWalkerNodeFactory_ArrayContainerNameString(startIndex, endIndex);
            }

            protected String computeType() {
                return BrowserUtils.getArrayItemType(array.getType());
            }

            protected String computeValue() {
                return Bundle.HeapWalkerNodeFactory_ArrayContainerValueString((endIndex - startIndex + 1));
            }

            protected String computeSize() {
                return "-"; // NOI18N
            }

            protected String computeRetainedSize() {
                return "-"; // NOI18N
            }

            protected Icon computeIcon() {
                return null;
            }

            public boolean isLeaf() {
                return false;
            }

            protected ChildrenComputer getChildrenComputer() {
                return new ChildrenComputer() {
                    public HeapWalkerNode[] computeChildren() {
                        int itemsCount = endIndex - startIndex + 1;
                        HeapWalkerNode[] children = new HeapWalkerNode[itemsCount];

                        boolean primitiveArray = array instanceof PrimitiveArrayNode;
                        List values = primitiveArray ? ((PrimitiveArrayInstance) (array.getInstance())).getValues()
                                                     : ((ObjectArrayInstance) (array.getInstance())).getValues();

                        for (int i = 0; i < itemsCount; i++) {
                            if (primitiveArray) {
                                children[i] = createPrimitiveArrayItemNode((PrimitiveArrayNode) array, startIndex + i,
                                                                           (String) values.get(startIndex + i));
                            } else {
                                children[i] = createObjectArrayItemNode((ObjectArrayNode) array, startIndex + i,
                                                                        (Instance) values.get(startIndex + i));
                            }
                        }

                        return children;
                    }
                };
            }
        };
    }

    public static ClassNode createClassNode(JavaClass javaClass, String name, HeapWalkerNode parent) {
        return new ClassNode(javaClass, name, parent, (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode());
    }

    public static HeapWalkerNode createFieldNode(FieldValue fieldValue, HeapWalkerNode parent) {
        if (fieldValue instanceof ObjectFieldValue) {
            Instance instance = ((ObjectFieldValue) fieldValue).getInstance();

            if (instance instanceof PrimitiveArrayInstance) {
                return new PrimitiveArrayFieldNode((ObjectFieldValue) fieldValue, parent);
            } else if (instance instanceof ObjectArrayInstance) {
                return new ObjectArrayFieldNode((ObjectFieldValue) fieldValue, parent);
            } else {
                return new ObjectFieldNode((ObjectFieldValue) fieldValue, parent);
            }
        } else {
            return new PrimitiveFieldNode(fieldValue, parent);
        }
    }

    public static HeapWalkerInstanceNode createInstanceNode(Instance instance, String name, HeapWalkerNode parent) {
        int mode = (parent == null) ? HeapWalkerNode.MODE_FIELDS : parent.getMode();

        if (instance instanceof PrimitiveArrayInstance) {
            return new PrimitiveArrayNode((PrimitiveArrayInstance) instance, name, parent, mode);
        } else if (instance instanceof ObjectArrayInstance) {
            return new ObjectArrayNode((ObjectArrayInstance) instance, name, parent, mode);
        } else {
            return new ObjectNode(instance, name, parent, mode);
        }
    }

    public static HeapWalkerNode createNoFieldsNode(HeapWalkerNode parent) {
        return new AbstractHeapWalkerNode(parent) {
            protected String computeName() {
                return Bundle.HeapWalkerNodeFactory_NoFieldsString();
            }

            protected String computeType() {
                return Bundle.HeapWalkerNodeFactory_NoneString();
            }

            protected String computeValue() {
                return Bundle.HeapWalkerNodeFactory_NoneString();
            }

            protected String computeSize() {
                return "-"; // NOI18N
            }

            protected String computeRetainedSize() {
                return "-"; // NOI18N
            }

            protected Icon computeIcon() {
                return null;
            }
        };
    }
    
    public static boolean isNoFieldsNode(HeapWalkerNode node) {
        return Bundle.HeapWalkerNodeFactory_NoFieldsString().equals(node.getName());
    }

    public static HeapWalkerNode createNoItemsNode(HeapWalkerNode parent) {
        return new AbstractHeapWalkerNode(parent) {
            protected String computeName() {
                return Bundle.HeapWalkerNodeFactory_NoItemsString();
            }

            protected String computeType() {
                return Bundle.HeapWalkerNodeFactory_NoneString();
            }

            protected String computeValue() {
                return Bundle.HeapWalkerNodeFactory_NoneString();
            }

            protected String computeSize() {
                return "-"; // NOI18N
            }

            protected String computeRetainedSize() {
                return "-"; // NOI18N
            }

            protected Icon computeIcon() {
                return null;
            }
        };
    }
    
    public static boolean isNoItemsNode(HeapWalkerNode node) {
        return Bundle.HeapWalkerNodeFactory_NoItemsString().equals(node.getName());
    }

    public static HeapWalkerNode createNoReferencesNode(HeapWalkerNode parent) {
        return new AbstractHeapWalkerNode(parent) {
            protected String computeName() {
                return Bundle.HeapWalkerNodeFactory_NoReferencesString();
            }

            protected String computeType() {
                return Bundle.HeapWalkerNodeFactory_NoneString();
            }

            protected String computeValue() {
                return Bundle.HeapWalkerNodeFactory_NoneString();
            }

            protected String computeSize() {
                return "-"; // NOI18N
            }

            protected String computeRetainedSize() {
                return "-"; // NOI18N
            }

            protected Icon computeIcon() {
                return null;
            }
        };
    }
    
    public static boolean isNoReferencesNode(HeapWalkerNode node) {
        return Bundle.HeapWalkerNodeFactory_NoReferencesString().equals(node.getName());
    }

    public static HeapWalkerNode createOOMNode(HeapWalkerNode parent) {
        return new AbstractHeapWalkerNode(parent) {
            protected String computeName() {
                return Bundle.HeapWalkerNodeFactory_OutOfMemoryString();
            }

            protected String computeType() {
                return ""; // NOI18N
            }

            protected String computeValue() {
                return ""; // NOI18N
            }

            protected String computeSize() {
                return ""; // NOI18N
            }

            protected String computeRetainedSize() {
                return ""; // NOI18N
            }

            protected Icon computeIcon() {
                return Icons.getIcon(GeneralIcons.ERROR);
            }
        };
    }
    
    public static boolean isOOMNode(HeapWalkerNode node) {
        return Bundle.HeapWalkerNodeFactory_OutOfMemoryString().equals(node.getName());
    }

    public static HeapWalkerNode createObjectArrayItemNode(ObjectArrayNode array, int itemIndex, Instance instance) {
        if (instance instanceof PrimitiveArrayInstance) {
            return new PrimitiveArrayNode.ArrayItem(itemIndex, (PrimitiveArrayInstance) instance, array);
        } else if (instance instanceof ObjectArrayInstance) {
            return new ObjectArrayNode.ArrayItem(itemIndex, (ObjectArrayInstance) instance, array);
        } else {
            return new ObjectNode.ArrayItem(itemIndex, instance, array);
        }
    }

    public static HeapWalkerNode createPrimitiveArrayItemNode(PrimitiveArrayNode array, int itemIndex, String value) {
        return new PrimitiveFieldNode.ArrayItem(itemIndex, BrowserUtils.getArrayItemType(array.getType()), value, array);
    }

    public static HeapWalkerNode createProgressNode(HeapWalkerNode parent) {
        return new AbstractHeapWalkerNode(parent) {
            protected String computeName() {
                return Bundle.HeapWalkerNodeFactory_SearchingString();
            }

            protected String computeType() {
                return ""; // NOI18N
            }

            protected String computeValue() {
                return ""; // NOI18N
            }

            protected String computeSize() {
                return ""; // NOI18N
            }

            protected String computeRetainedSize() {
                return ""; // NOI18N
            }

            protected Icon computeIcon() {
                return BrowserUtils.ICON_PROGRESS;
            }
        };
    }
    
    public static boolean isProgressNode(HeapWalkerNode node) {
        return Bundle.HeapWalkerNodeFactory_SearchingString().equals(node.getName());
    }

    public static HeapWalkerNode[] createReferences(InstanceNode parent) {
        HeapWalkerNode[] referenceNodes = null;
        List references = parent.getReferences();

        referenceNodes = HeapPatterns.processReferencePatterns(parent, references);
        if (referenceNodes != null) return referenceNodes;

        if (references.size() == 0) {
            // Instance has no fields
            referenceNodes = new HeapWalkerNode[1];
            referenceNodes[0] = createNoReferencesNode(parent);
        } else {
            // Instance has at least one field
            referenceNodes = new HeapWalkerNode[references.size()];

            for (int i = 0; i < referenceNodes.length; i++) {
                referenceNodes[i] = createReferenceNode((Value)
                                    references.get(i), parent);
            }
        }

        return referenceNodes;
    }

    public static HeapWalkerNode createReferenceNode(Value value, HeapWalkerNode parent) {
//        HeapWalkerNode referenceNode = HeapPatterns.processReferencePatterns(value, parent);
//        if (referenceNode != null) return referenceNode;
        
        if (value instanceof ObjectFieldValue) {
            return new ObjectFieldNode((ObjectFieldValue) value, parent);
        } else if (value instanceof ArrayItemValue) {
            ArrayItemValue arrayValue = (ArrayItemValue) value;

            return new ObjectArrayNode.ArrayItem(arrayValue.getIndex(), (ObjectArrayInstance) arrayValue.getDefiningInstance(),
                                                 parent);
        } else {
            return null;
        }
    }

    public static ClassNode createRootClassNode(JavaClass javaClass, String name, final Runnable refresher,
                                                final Runnable repainter, int mode, final Heap heap) {
        return new ClassNode.RootNode(javaClass, name, null, mode) {
            public void refreshView() {
                refresher.run();
            }

            public GCRoot getGCRoot(Instance inst) {
                return heap.getGCRoot(inst);
            }

            public JavaClass getJavaClassByID(long javaclassId) {
                return heap.getJavaClassByID(javaclassId);
            }
            
            public String getDetails(Instance instance) {
                return DetailsSupport.getDetailsString(instance, heap);
            }

            public void repaintView() {
                repainter.run();
            }
        };
    }

    public static HeapWalkerNode createRootInstanceNode(Instance instance, String name, final Runnable refresher,
                                                                final Runnable repainter, int mode, final Heap heap) {
        if (instance instanceof PrimitiveArrayInstance) {
            return new PrimitiveArrayNode.RootNode((PrimitiveArrayInstance) instance, name, null, mode) {
                public void refreshView() {
                    refresher.run();
                }

                public GCRoot getGCRoot(Instance inst) {
                    return heap.getGCRoot(inst);
                }

                public JavaClass getJavaClassByID(long javaclassId) {
                    return heap.getJavaClassByID(javaclassId);
                }
                
                public String getDetails(Instance instance) {
                    return DetailsSupport.getDetailsString(instance, heap);
                }

                public void repaintView() {
                    repainter.run();
                }
            };
        } else if (instance instanceof ObjectArrayInstance) {
            return new ObjectArrayNode.RootNode((ObjectArrayInstance) instance, name, null, mode) {
                public void refreshView() {
                    refresher.run();
                }

                public GCRoot getGCRoot(Instance inst) {
                    return heap.getGCRoot(inst);
                }

                public JavaClass getJavaClassByID(long javaclassId) {
                    return heap.getJavaClassByID(javaclassId);
                }
                
                public String getDetails(Instance instance) {
                    return DetailsSupport.getDetailsString(instance, heap);
                }

                public void repaintView() {
                    repainter.run();
                }
            };
        } else {
            return new ObjectNode.RootNode(instance, name, null, mode) {
                public void refreshView() {
                    refresher.run();
                }

                public GCRoot getGCRoot(Instance inst) {
                    return heap.getGCRoot(inst);
                }

                public JavaClass getJavaClassByID(long javaclassId) {
                    return heap.getJavaClassByID(javaclassId);
                }
                
                public String getDetails(Instance instance) {
                    return DetailsSupport.getDetailsString(instance, heap);
                }

                public void repaintView() {
                    repainter.run();
                }
            };
        }
    }
    
    public static boolean isMessageNode(HeapWalkerNode node) {
        return isNoFieldsNode(node) ||
               isNoItemsNode(node) ||
               isNoReferencesNode(node) ||
               isNoReferencesNode(node) ||
               isOOMNode(node) ||
               isProgressNode(node);
    }
}
