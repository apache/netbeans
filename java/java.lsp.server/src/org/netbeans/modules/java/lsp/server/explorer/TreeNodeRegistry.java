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
package org.netbeans.modules.java.lsp.server.explorer;

import java.awt.Image;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import org.netbeans.modules.java.lsp.server.explorer.api.ResourceData;
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
    ResourceData imageContents(URI imageUri);
    
    CompletionStage<TreeViewProvider> createProvider(String id);
    
    public class ImageDataOrIndex {
        /**
         * Base URL of the image, if it could be extracted from the Image object.
         */
        public URI          baseURI;
        
        /**
         * URLs(?) of additional image constituents.
         */
        public String[]     composition;
        
        public String       contentType;
        public byte[]       imageData;

        public ImageDataOrIndex(URI imageURI) {
            this.baseURI = imageURI;
        }

        public ImageDataOrIndex baseURL(URL u) {
            try {
                baseURI = u == null ? null : u.toURI();
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(ex);
            }
            return this;
        }
        
        public ImageDataOrIndex baseURL(URI u) {
            this.baseURI = u;
            return this;
        }
        
        public ImageDataOrIndex composition(String[] composition) {
            this.composition = composition;
            return this;
        }
        
        public ImageDataOrIndex imageData(String contentType, byte[] imageData) {
            this.contentType = contentType;
            this.imageData = imageData;
            return this;
        }
    }
}
