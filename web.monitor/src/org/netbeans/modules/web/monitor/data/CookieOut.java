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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.web.monitor.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import javax.servlet.http.Cookie;

public class CookieOut extends BaseBean
{

    static Vector comparators = new Vector();

    public CookieOut()
    {
	this(Common.USE_DEFAULT_VALUES);
    }

    public CookieOut(Cookie cookie) {
	super(CookieOut.comparators, new
	org.netbeans.modules.schema2beans.Version(1, 0, 5));

	// Note - the XML beans library does not treat NMTOKENS as
	// special - they have to be set as strings! 
	setAttributeValue("name", cookie.getName()); //NOI18N
	setAttributeValue("value", cookie.getValue()); //NOI18N
	setAttributeValue("maxAge", //NOI18N
			  String.valueOf(cookie.getMaxAge()));
	setAttributeValue("version", //NOI18N
			  String.valueOf(cookie.getVersion()));

	String domain = "";  //NOI18N
	try { 
	    domain = cookie.getDomain();
	} 
	catch(NullPointerException ne) {} 
	if(domain != null) {
	    if(domain.trim().equals("")) //NOI18N
		setAttributeValue("domain", ""); //NOI18N
	    else 
		setAttributeValue("domain", domain);  //NOI18N
	}

	String path = "";  //NOI18N
	try { 
	    path = cookie.getPath();
	} 
	catch(NullPointerException ne) {} 
	if(path != null) {
	    if(path.trim().equals("")) //NOI18N
		setAttributeValue("path", ""); //NOI18N
	    else 
		setAttributeValue("path", path); //NOI18N
	}

	String comment = "";  //NOI18N
	try { 
	    comment = cookie.getComment();
	} 
	catch(NullPointerException ne) {} 
	if(comment != null) {
	    if(comment.trim().equals("")) //NOI18N
		setAttributeValue("comment", ""); //NOI18N
	    else 
		setAttributeValue("comment", comment);  //NOI18N
	}
	
	int version = cookie.getVersion();   
	// XML Beans...
	if(version != 0) setAttributeValue("version", //NOI18N
					   String.valueOf(version));  
      
	try { 
	    if(cookie.getSecure()) 
		// XMLBeans library... 
		setAttributeValue("secure", //NOI18N
				  String.valueOf(cookie.getSecure())); 
	    
	}
	catch(Exception exc) {}
    }


    public CookieOut(int options)
    {
	super(CookieOut.comparators, new org.netbeans.modules.schema2beans.Version(1, 0, 5));
	// Properties (see root bean comments for the bean graph)
	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options)
    {

    }

    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

    //
    static public void addComparator(BeanComparator c)
    {
	CookieOut.comparators.add(c);
    }

    //
    static public void removeComparator(BeanComparator c)
    {
	CookieOut.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
	String s;
	BaseBean n;
    }

    public String dumpBeanNode()
    {
	StringBuffer str = new StringBuffer();
	str.append("CookieOut\n"); //NOI18N
	this.dump(str, "\n  "); //NOI18N
	return str.toString();
    }
}

