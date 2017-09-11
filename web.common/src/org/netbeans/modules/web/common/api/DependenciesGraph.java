/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 * bidirectional dependencies graph
 *
 * The aim of the class is to store a map of dependencies among files in a web like project.
 *
 * @author marekfukala
 */
public class DependenciesGraph {
    
    private static final Logger LOGGER = Logger.getLogger(DependenciesGraph.class.getSimpleName());

    private Map<FileObject, Node> file2node = new HashMap<FileObject, Node>();
    private Node sourceNode;

    public DependenciesGraph(FileObject source) {
        this.sourceNode = new Node(source);
    }

    public Node getNode(FileObject source) {
        Node node = file2node.get(source);
        if(node == null) {
            node = new Node(source);
            file2node.put(source, node);
        }
        return node;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    /**
     * Returns a collection of files where the relation type from the base file is defined by the argument.
     * 
     * @since 1.35
     * @param type defines the relation type.
     * @return 
     */
    public Collection<FileObject> getFiles(DependencyType type) {
        switch(type) {
            case REFERRING:
                return getAllReferingFiles();
            case REFERRED:
                return getAllReferedFiles();
            case REFERRING_AND_REFERRED:
                return getAllRelatedFiles();
            default:
                throw new IllegalStateException();
        }
    }
    
    /**
     * 
     * @return a collection a files which are either imported or importing the 
     * base source file for this dependencies graph
     */
    public Collection<FileObject> getAllRelatedFiles() {
        Collection<FileObject> files = new LinkedHashSet<FileObject>();
        walk(files, getSourceNode(), true, true);
        return files;
    }

    public Collection<FileObject> getAllReferedFiles() {
        Collection<FileObject> files = new LinkedHashSet<FileObject>();
        walk(files, getSourceNode(), true, false);
        return files;
    }

    public Collection<FileObject> getAllReferingFiles() {
        Collection<FileObject> files = new LinkedHashSet<FileObject>();
        walk(files, getSourceNode(), false, true);
        return files;
    }

    private void walk(Collection<FileObject> files, Node node, boolean refered, boolean refering) {
        if(files.add(node.getFile())) {
            if(refered) {
                for(Node n : node.refered) {
                    walk(files, n, refered, refering);
                }
            }
            if(refering) {
                for(Node n : node.refering) {
                    walk(files, n, refered, refering);
                }
            }
        }
    }

    public class Node {

        private FileObject source;
        private Collection<Node> refering = new LinkedHashSet<Node>();
        private Collection<Node> refered = new LinkedHashSet<Node>();

        private Node(FileObject source) {
            this.source = source;
        }

        public DependenciesGraph getDependencyGraph() {
            return DependenciesGraph.this;
        }

        public FileObject getFile() {
            return source;
        }

        public boolean addReferedNode(Node node) {
            if(refered.add(node)) {
                boolean added = node.refering.add(this);
                if(!added) {
                    //cycle in the graph!
                    LOGGER.info(String.format("A graph cycle detected when adding refered node %s to node %s",
                            node.toString(), this.toString())); //NOI18N
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }

        public boolean addReferingNode(Node node) {
            if(refering.add(node)) {
                boolean added = node.refered.add(this);
                 if(!added) {
                    //cycle in the graph!
                     LOGGER.info(String.format("A graph cycle detected when adding refering node %s to node %s",
                            node.toString(), this.toString())); //NOI18N
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }

        /**
         *
         * @return unmodifiable collection of nodes which refers to (imports) this node
         */
        public Collection<Node> getReferingNodes() {
            return Collections.unmodifiableCollection(refering);
        }

        /**
         *
         * @return unmodifiable collection of nodes which this node refers to (imports)
         */
        public Collection<Node> getReferedNodes() {
            return Collections.unmodifiableCollection(refered);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.source != null ? this.source.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "Node[" + source.getPath() + "]";
        }


        
    }
}
