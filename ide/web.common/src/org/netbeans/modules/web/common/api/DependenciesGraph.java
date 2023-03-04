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
