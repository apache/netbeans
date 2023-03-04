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
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;

import java.util.StringTokenizer;
import java.text.DateFormat;
import java.util.Date;
import java.awt.Image;
import org.netbeans.modules.web.monitor.data.Constants;

public class TransactionNode extends AbstractNode {

    String id, method, uri, name = null, timestamp = null; 
    boolean current;
    private int statusCode;
    static boolean showTimeStamp = true; 

    public TransactionNode(String id, String method, String uri, 
			   boolean current, int statusCode) {
	
	super(Children.LEAF);

	this.id = id;
	this.method = method;
	this.uri = uri;
	this.current = current;
        this.statusCode = statusCode;

	setProperties();
    }

    public TransactionNode(String id, String method, String uri, 
			   Children ch, boolean current, int statusCode) {
	
	super(ch);

	this.id = id;
	this.method = method;
	this.uri = uri;
	this.current = current;
        this.statusCode = statusCode;
        
	setProperties();
    }

    // This method is incomplete, URI may need to be truncated... 
    public String getLongName() {

	StringBuffer buf = new StringBuffer(method); 
	buf.append(" "); //NOI18N
	buf.append(uri);
	if(timestamp == null) setTimeStamp();
	buf.append(" "); //NOI18N
	buf.append(timestamp);

	return buf.toString();
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
    
    public String getID() { 
	return id;
    }

    public String getMethod() { 
	return method;
    }

    public String getURI() { 
	return uri;
    }

    public boolean isCurrent() { 
	return current;
    }

    public void setCurrent(boolean b) { 
	current = b;
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {

	if(current) {
	    return new SystemAction[] {
		SystemAction.get(SaveAction.class),
		null,
		SystemAction.get(ReplayAction.class),
		SystemAction.get(EditReplayAction.class),
		null,
		SystemAction.get(DeleteAction.class)
	    };
	}
     
	return new SystemAction[] {
	    SystemAction.get(ReplayAction.class),
	    SystemAction.get(EditReplayAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	};
    }


    public SystemAction[] getActions () {

	if(current) {
	    return new SystemAction[] {
		SystemAction.get(SaveAction.class),
		null,
		SystemAction.get(ReplayAction.class),
		SystemAction.get(EditReplayAction.class),
		null,
		SystemAction.get(DeleteAction.class)
	    };
	}
     
	return new SystemAction[] {
	    SystemAction.get(ReplayAction.class),
	    SystemAction.get(EditReplayAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	};
    }

    /** Can this node be copied?
     * @return <code>true</code> in the default implementation
     */
    public boolean canCopy () {
	return true;
    }

    /** Can this node be cut?
     * @return <code>false</code> in the default implementation
     */
    public boolean canCut () {
	return false;
    }

    /** 
     * Set whether the timestamp is shown or not
     */
    public static void toggleTimeStamp() { 
	if(showTimeStamp) showTimeStamp = false; 
	else showTimeStamp = true; 
    }

    /** 
     * Is the timestamp showing
     */
    public static boolean showTimeStamp() { 
	return showTimeStamp; 
    }

    private void setProperties() {
	setNameString();
	setShortDescription(uri);
    }
    
    public void setNameString() {
	
	String name = null;
	if(uri.equals("/")) name = uri;  //NOI18N
	else {
	    StringTokenizer st = new StringTokenizer(uri,"/");  //NOI18N
	    while(st.hasMoreTokens()) name = st.nextToken();
	}
	
	StringBuilder buf = new StringBuilder(method); 
	buf.append(' '); //NOI18N
	buf.append(name);
	if(showTimeStamp) { 
	    if(timestamp == null) setTimeStamp();
	    buf.append(' '); //NOI18N
	    buf.append(timestamp);
	}
	setName(buf.toString()); 
    }

    private void setTimeStamp() {
	
	try { 
	    long ldate = Long.valueOf(id); 
	    Date date = new Date(ldate); 

	    StringBuilder buf = new StringBuilder('['); //NOI18N
	    DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT); 
	    buf.append(df.format(date)); 
	    buf.append(" "); //NOI18N
	    df = DateFormat.getDateInstance(DateFormat.SHORT);
	    buf.append(df.format(date)); 
	    buf.append(']'); //NOI18N
	    timestamp = buf.toString();
	} 
	catch(Exception e) {} 
    } 
    
    public String toString() {
	StringBuilder buf = new StringBuilder("TransactionNode: ");  //NOI18N
	buf.append(this.getName());
	buf.append('\n');  //NOI18N
	buf.append("id=");  //NOI18N
	buf.append(id); 

	buf.append('\n');  //NOI18N
	buf.append("method=");  //NOI18N
	buf.append(method); 

	buf.append('\n');  //NOI18N
	buf.append("uri=");  //NOI18N
	buf.append(uri); 

	buf.append('\n');  //NOI18N
	buf.append("current=");  //NOI18N
	buf.append(String.valueOf(current)); 
	buf.append('\n');  //NOI18N

	return buf.toString();
    }
} // TransactionNode






