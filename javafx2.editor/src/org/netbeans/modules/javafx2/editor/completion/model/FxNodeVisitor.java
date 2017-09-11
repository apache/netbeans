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
