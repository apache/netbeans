/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.*;
import org.openide.util.Utilities;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import java.awt.Image;
import java.util.StringTokenizer;
import org.netbeans.modules.web.monitor.data.Constants;

public class NestedNode extends AbstractNode {

    private String resource = null;
    private String method = null;
    private int statusCode;
    private int[] index;
     
    public NestedNode(String resource, String method, int[] index, int statusCode) {
		
	super(Children.LEAF);
	this.resource = resource;
	this.method = method;
	this.index = index;
        this.statusCode = statusCode;
	setProperties();
    }


    public NestedNode(String resource, 
		      String method, 
		      Children ch, 
		      int[] index,
                      int statusCode) { 
	super(ch);
	this.resource = resource;
	this.method = method;
	this.index = index;
        this.statusCode = statusCode;
	setProperties();
    }

    public String getLongName() {
	return getName();
    }
    
    public Image getIcon(int type) {
        Image base;
	// Get icon
	if(method.equals(Constants.Http.GET)) {
            base = ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/get.gif");
	// Post icon
        } else if(method.equals(Constants.Http.POST)) {
            base = ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/post.gif"); // NOI18N
	// Other 
        } else {
            base = ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/other.gif"); // NOI18N
        }
        
        Image badge;
        if (statusCode >= 400 || statusCode < 0) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/infoBadge.gif"); // NOI18N
        } else if (statusCode >= 300) {
            badge = ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/warningBadge.gif"); // NOI18N
        } else if (statusCode >= 200) {
            return base;
        } else {
            badge = ImageUtilities.loadImage("org/netbeans/modules/web/monitor/client/icons/errorBadge.gif"); // NOI18N
        }
        return ImageUtilities.mergeImages(base, badge, 0, 0);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    public String getResource() { 
	return resource;
    }

    public int[] getIndex() { 
	return index;
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {

	return new SystemAction[] {
	};
    }

    /** Can this node be copied?
     * @return <code>true</code> in the default implementation
     */
    public boolean canCopy () {
	return false;
    }

    /** Can this node be cut?
     * @return <code>false</code> in the default implementation
     */
    public boolean canCut () {
	return false;
    }

    private void setProperties() {
	setNameString();
    }
    
    public void setNameString() {
	
	String name = null;
	if(resource.equals("/")) name = resource;  //NOI18N
	else {
	    StringTokenizer st = new StringTokenizer(resource,"/");  //NOI18N
	    while(st.hasMoreTokens()) name = st.nextToken();
	}
	setName(name); 
    }

    public String toString() {
	StringBuilder buf = new StringBuilder("NestedNode: ");  //NOI18N
	buf.append(this.getName());
	buf.append(", resource=");  //NOI18N
	buf.append(resource); 
	buf.append(", index="); //NOI18N
	for(int i=0; i<index.length; ++i) {
	    buf.append(index[i]);
	    buf.append(','); //NOI18N
	}
	return buf.toString();
    }
} // NestedNode
