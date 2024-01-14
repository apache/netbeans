/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.junit.ui.wizards;

import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Displays folders and Java source files under a source node.
 * @author Marian Petras, Jesse Glick
 */
public class JavaChildren extends FilterNode.Children {

    private static final String JAVA_MIME_TYPE = "text/x-java";         //NOI18N

    public JavaChildren(Node parent) {
        super(parent);
    }

    @Override
    protected Node[] createNodes(Node originalNode) {
        Node newNode;
        
        FileObject primaryFile = originalNode.getLookup().lookup(FileObject.class);
        if (primaryFile == null) {
            newNode = copyNode(originalNode);
        } else {
            if (primaryFile.isFolder()) {
                newNode = new FilterNode(originalNode, new JavaChildren(originalNode));
            } else if (primaryFile.getMIMEType().equals(JAVA_MIME_TYPE)) {
                newNode = new FilterNode(originalNode, Children.LEAF);
                newNode.setDisplayName(primaryFile.getName());
            } else {
                newNode = null;
            }
        }

        return (newNode != null) ? new Node[] {newNode}
                                 : new Node[0];
    }
    
}
