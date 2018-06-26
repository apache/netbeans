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
	    long ldate = Long.valueOf(id).longValue(); 
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






