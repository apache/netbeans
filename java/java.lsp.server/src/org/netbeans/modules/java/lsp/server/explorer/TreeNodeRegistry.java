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
package org.netbeans.modules.java.lsp.server.explorer;

import java.awt.Image;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import org.openide.nodes.Node;

/**
 * Provides an unique identification to a node and cannonicalizes images for LSP transfer. An instance
 * is registered in LSP session Lookup.
 * 
 * @author sdedic
 */
public interface TreeNodeRegistry {
    int registerNode(Node n, TreeViewProvider p);
    void unregisterNode(int nodeId, Node n);
    
    Node findNode(int id);
    TreeViewProvider providerOf(int id);
    ImageDataOrIndex imageOrIndex(Image im);
    
    CompletionStage<TreeViewProvider> createProvider(String id);
    
    public class ImageDataOrIndex {
        public final URI    imageURI;
        public final int    imageIndex;

        public ImageDataOrIndex(URI imageURI) {
            this.imageURI = imageURI;
            this.imageIndex = -1;
        }

        public ImageDataOrIndex(URI imageUri, int imageIndex) {
            this.imageIndex = imageIndex;
            this.imageURI = imageUri;
        }

        public ImageDataOrIndex(int imageIndex) {
            this.imageIndex = imageIndex;
            this.imageURI = null;
        }
    }
}
