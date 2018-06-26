/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
