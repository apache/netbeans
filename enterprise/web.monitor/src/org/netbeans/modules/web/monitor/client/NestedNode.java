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
