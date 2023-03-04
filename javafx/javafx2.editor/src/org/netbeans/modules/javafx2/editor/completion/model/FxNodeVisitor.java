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

import org.netbeans.modules.javafx2.editor.parser.NodeInfo;
import java.util.Collection;
import java.util.Comparator;

/**
 *
 * @author sdedic
 */
public abstract class FxNodeVisitor {
    public void visitNode(FxNode node) {}
    
    public void visitSource(FxModel source) {
        visitNode(source);
    }
    
    public void visitLanguage(LanguageDecl decl) {
        visitNode(decl);
    }
    
    public void visitImport(ImportDecl decl) {
        visitNode(decl);
    }
    
    public void visitInclude(FxInclude decl) {
        visitNode(decl);
    }
    
    public void visitInstance(FxNewInstance decl) {
        visitNode(decl);
    }
    
    public void visitPropertyValue(PropertyValue val) {
        visitNode(val);
    }
    
    public void visitMapProperty(MapProperty p) {
        visitNode(p);
    }

    public void visitStaticProperty(StaticProperty p) {
        visitNode(p);
    }

    public void visitPropertySetter(PropertySetter p) {
        visitNode(p);
    }
    
    public void visitReference(FxReference ref) {
        visitNode(ref);
    }
    
    public void visitCopy(FxInstanceCopy copy) {
        visitNode(copy);
    }
    
    public void visitEvent(EventHandler eh) {
        visitNode(eh);
    }
    
    public void visitScript(FxScriptFragment script) {
        
    }
    
    public void visitElement(XmlNode n) {
        visitNode(n);
    }
    
    public static class ModelTreeTraversal extends FxNodeVisitor {
        protected void scan(FxNode node) {
            if (node != null) {
                node.accept(this);
            }
        }
        
        protected void scan(Collection<? extends FxNode> nodes) {
            if (nodes != null) {
                for (FxNode n : nodes) {
                    scan(n);
                }
            }
        }

        public void visitNode(FxNode node) {
            if (node == null) {
                return;
            }
            scan(node.i().getChildren());
        }
        
        public void visitCopy(FxInstanceCopy copy) {
            // do not call super
            visitBaseInstance(copy);
        }

        public void visitInclude(FxInclude incl) {
            // do not call super
            visitBaseInstance(incl);
        }

        public void visitInstance(FxNewInstance decl) {
            // do not call super
            visitBaseInstance(decl);
        }

        
        protected void visitBaseInstance(FxInstance inst) {
            visitNode(inst);
        }
    
    }
    
    public static class ModelTraversal extends FxNodeVisitor {
        protected void scan(FxNode node) {
            if (node != null) {
                node.accept(this);
            }
        }
        
        protected void scan(Collection<? extends FxNode> nodes) {
            if (nodes != null) {
                for (FxNode n : nodes) {
                    scan(n);
                }
            }
        }
        
        protected void scanImports(FxModel model) {
            scan(model.getImports());
        }
        
        @Override
        public void visitSource(FxModel model) {
            super.visitSource(model);
            scan(model.getLanguage());
            scanImports(model);
            scan(model.getDefinitions());
            scan(model.getRootComponent());
        }

        @Override
        public void visitCopy(FxInstanceCopy decl) {
            super.visitCopy(decl);
            visitBaseInstance(decl);
        }
        
        public void visitBaseInstance(FxInstance decl) {
            scan(decl.getProperties());
            scan(decl.getStaticProperties());
            scan(decl.getEvents());
            scan(decl.getScripts());
        }

        @Override
        public void visitInstance(FxNewInstance decl) {
            super.visitInstance(decl);
            visitBaseInstance(decl);
        }

        @Override
        public void visitStaticProperty(StaticProperty p) {
            super.visitStaticProperty(p);
            scan(p.getValues());
        }

        @Override
        public void visitPropertySetter(PropertySetter p) {
            super.visitPropertySetter(p);
            scan(p.getValues());
        }
    }
    
    private static final Comparator<FxNode> POSITION_COMPARATOR = new Comparator<FxNode>() {

        @Override
        public int compare(FxNode o1, FxNode o2) {
            NodeInfo n1 = o1.i();
            NodeInfo n2 = o2.i();
            
            int diff = n1.getStart() - n2.getStart();
            if (diff != 0) {
                return diff;
            }
            return 0;
        }
        
    };
}
