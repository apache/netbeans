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

package org.netbeans.modules.web.inspect.webkit.knockout.unused;

import java.awt.EventQueue;
import org.netbeans.modules.web.inspect.webkit.DOMNode;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.openide.util.NbBundle;

/**
 * An unused binding.
 *
 * @author Jan Stola
 */
public class UnusedBinding {
    /** ID of the binding. */
    private final int id;
    /** Name of the binding. */
    private final String name;
    /** Tag name of the owner of this binding. */
    private final String nodeTagName;
    /** ID of the owner of this binding. */
    private final String nodeId;
    /** Classes (value if the {@code class} attribute) of the owner of this binding. */
    private final String nodeClasses;
    /** Determines whether the node was removed from the document. */
    private final boolean nodeRemoved;
    /** Owning page. */
    private final WebKitPageModel page;

    /**
     * Creates a new {@code UnusedBinding}.
     * 
     * @param id ID of the binding.
     * @param name name of the binding.
     * @param nodeTagName tag name of the owner of the binding.
     * @param nodeId ID of the owner of the binding.
     * @param nodeClasses classes (value of the {@code class} attribute)
     * of the owner of the binding.
     * @param nodeRemoved determines whether the node was removed from the document.
     * @param page owning page.
     */
    public UnusedBinding(int id, String name, String nodeTagName, String nodeId,
            String nodeClasses, boolean nodeRemoved, WebKitPageModel page) {
        this.id = id;
        this.name = name;
        this.nodeTagName = (nodeTagName == null) ? "" : nodeTagName; // NOI18N
        this.nodeId = nodeId;
        this.nodeClasses = nodeClasses;
        this.nodeRemoved = nodeRemoved;
        this.page = page;
    }

    /**
     * Returns the ID of this binding.
     * 
     * @return ID of this binding.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of this binding.
     * 
     * @return name of this binding.
     */
    public String getName() {
        return name;
    }

    /**
     * Determines whether the node was removed from the document.
     * 
     * @return {@code true} when the node is no longer in the document,
     * returns {@code false} otherwise.
     */
    public boolean isRemoved() {
        return nodeRemoved;
    }

    /**
     * Returns the owning page.
     * 
     * @return owning page.
     */
    public WebKitPageModel getPage() {
        return page;
    }

    /**
     * Returns (HTML) display name of the owner of this binding.
     * 
     * @return (HTML) display name of the owner of this binding.
     */
    @NbBundle.Messages({
        "UnusedBindings.comment=comment",
        "UnusedBindings.removedNode=<i>(removed)</i>"
    })
    public String getNodeDisplayName() {
        String selector = DOMNode.selector(
                nodeTagName.isEmpty() ? Bundle.UnusedBindings_comment() : nodeId,
                nodeClasses);
        String displayName = DOMNode.htmlDisplayName(nodeTagName, selector);
        if (isRemoved()) {
            displayName = displayName + " " + Bundle.UnusedBindings_removedNode(); // NOI18N
        }
        return displayName;
    }

    /**
     * Returns the owning node.
     * 
     * @return owning node (or {@code null} if the node cannot be found).
     */
    public DOMNode getNode() {
        assert !EventQueue.isDispatchThread();
        WebKitDebugging webKit = page.getWebKit();
        RemoteObject remoteObject = webKit.getRuntime().evaluate("NetBeans.ownerOfUnusedBinding("+id+")"); // NOI18N
        org.netbeans.modules.web.webkit.debugging.api.dom.Node webKitNode = null;
        if (remoteObject != null) {
            webKitNode = webKit.getDOM().requestNode(remoteObject);
        }
        DOMNode node = null;
        if (webKitNode != null) {
            node = page.getNode(webKitNode.getNodeId());
        }
        return node;
    }

}
