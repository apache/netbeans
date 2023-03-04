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
package org.netbeans.modules.java.debug;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner9;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class ElementNode extends AbstractNode implements OffsetProvider {
    
    private Element element;
    private CompilationInfo info;
    
//    public static Node getTree(CompilationInfo info) {
//        return getTree(info, info.getElement(info.getTree().getTypeDecls().get(0)));
//    }
    
    public static Node getTree(CompilationInfo info, Element element) {
        List<Node> result = new ArrayList<Node>();
        
        new FindChildrenElementVisitor(info).scan(element, result);
        
        return result.get(0);
    }

    /** Creates a new instance of TreeNode */
    public ElementNode(CompilationInfo info, Element element, List<Node> nodes) {
        super(nodes.isEmpty() ? Children.LEAF: new NodeChilren(nodes));
        this.element = element;
        this.info = info;
        setDisplayName(element.getKind().toString() + ":" + element.toString()); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
    }

    public int getStart() {
        final int[] result = new int[] {-1};

        try {
            JavaSource.create(info.getClasspathInfo()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    Tree tree = info.getTrees().getTree(element);
                    if (tree != null) {
                        result[0] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0];
    }
    
    public int getEnd() {
        final int[] result = new int[] {-1};

        try {
            JavaSource.create(info.getClasspathInfo()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    Tree tree = info.getTrees().getTree(element);
                    if (tree != null) {
                        result[0] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0];
    }

    public int getPreferredPosition() {
        return -1;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        Sheet.Set ps = new Sheet.Set();
        ps.setName("origins"); // NOI18N
        ps.setDisplayName("Origins");
        ps.setShortDescription("Origins");
        ps.put(new Node.Property<?>[] {
            new ReadOnly<String>("sourcefile", String.class, "sourcefile", "sourcefile") {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    if (element instanceof ClassSymbol) {
                        JavaFileObject file = ((ClassSymbol) element).sourcefile;

                        if (file != null) {
                            return file.toUri().toString();
                        } else {
                            return "No sourcefile set";
                        }
                    } else {
                        return "Not a ClassSymbol";
                    }
                }
            },
            new ReadOnly<String>("classfile", String.class, "classfile", "classfile") {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    if (element instanceof ClassSymbol) {
                        JavaFileObject file = ((ClassSymbol) element).classfile;

                        if (file != null) {
                            return file.toUri().toString();
                        } else {
                            return "No classfile set";
                        }
                    } else {
                        return "Not a ClassSymbol";
                    }
                }
            }
        });

        sheet.put(ps);
        return sheet;
    }

    private static final class NodeChilren extends Children.Keys<Node> {
        
        public NodeChilren(List<Node> nodes) {
            setKeys(nodes);
        }
        
        protected Node[] createNodes(Node key) {
            return new Node[] {key};
        }
        
    }
    
    private static class FindChildrenElementVisitor extends ElementScanner9<Void, List<Node>> {
        
        private CompilationInfo info;
        
        public FindChildrenElementVisitor(CompilationInfo info) {
            this.info = info;
        }
        
        public Void visitPackage(PackageElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitPackage(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitType(TypeElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitType(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitVariable(VariableElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitVariable(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitExecutable(ExecutableElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitExecutable(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitTypeParameter(TypeParameterElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitTypeParameter(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }

        @Override
        public Void visitModule(ModuleElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitModule(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }

        @Override
        public Void visitUnknown(Element e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();
            p.add(new ElementNode(info, e, below));
            return null;
        }
    }
}
