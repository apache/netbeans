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
package org.netbeans.modules.web.inspect.webkit;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.api.dom.NodeAnnotator;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Annotator for {@DOMNode}s.
 *
 * @author Jan Stola
 */
@ServiceProvider(service=NodeAnnotator.Impl.class)
public class DOMNodeAnnotator implements NodeAnnotator.Impl {
    /** The default instance of this class. */
    private static DOMNodeAnnotator INSTANCE;
    /** Maps node ID to an associated badge. */
    private final Map<Integer,Image> badges = new HashMap<Integer,Image>();

    /**
     * Returns the default {@DOMNodeAnnotator}.
     * 
     * @return the default {@DOMNodeAnnotator}.
     */
    public static DOMNodeAnnotator getDefault() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(DOMNodeAnnotator.class);
        }
        return INSTANCE;
    };

    @Override
    public void annotate(Node node, Image badge) {
        int nodeId = node.getNodeId();
        if (badge == null) {
            badges.remove(nodeId);
        } else {
            badges.put(node.getNodeId(), badge);
        }
        Page page = PageInspector.getDefault().getPage();
        if (page instanceof WebKitPageModel) {
            WebKitPageModel pageModel = (WebKitPageModel)page;
            DOMNode domNode = pageModel.getNode(node.getNodeId());
            if (domNode != null) {
                domNode.updateIcon();
            }
        }
    }

    /**
     * Annotates the icon of the given node. If the node is associated with
     * some badge then this badge is merge into the node's icon. The node's
     * icon is left unmodified otherwise.
     * 
     * @param node node whose icon should be annotated.
     * @param originalImage original (not annotated) icon of the node.
     * @return annotated icon of the node.
     */
    public Image annotateIcon(Node node, Image originalImage) {
        Image image = originalImage;
        Image badge = badges.get(node.getNodeId());
        if (badge != null) {
            int x = image.getWidth(null)-badge.getWidth(null);
            int y = image.getHeight(null)-badge.getHeight(null);
            image = ImageUtilities.mergeImages(image, badge, x, y);
        }
        return image;
    }

}
