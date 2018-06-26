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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation;


import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author joelle
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.MIMEResolver.class)
public class JSFConfigMimeResovler extends MIMEResolver{

    public JSFConfigMimeResovler() {
        super();
    }

    public String findMIMEType(FileObject fo) {
        //System.out.println("Trying to find FileObject MIME Type." + fo);
        String ext = fo.getExt();
        if( ext.equals("xml")){
            return "text/x-jsf+xml";
        } else if( ext.equals("jsp") || ext.equals("jspf")){
            return "text/x-jsp";
        } else if ( ext.equals("html")){
            return "text/html";
        }
        return null;
    }
    
    
    /*
     *     <file>
        <ext name="xml"/>
        <resolver mime="text/x-jsf+xml">
            <xml-rule>
            	<doctype public-id="-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN"/>
            </xml-rule>
        </resolver>
    </file>
    <file>
        <ext name="xml"/>
            <resolver mime="text/x-jsf+xml">
            <xml-rule>
                <doctype public-id="-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN"/>
            </xml-rule>
        </resolver>       
    </file>    
    <file>
        <ext name="xml"/>
            <resolver mime="text/x-jsf+xml">
            <xml-rule>
            	<element name="faces-config" ns="http://java.sun.com/xml/ns/javaee">
                    <attr name="version" text="1.2"/>
                </element>
            </xml-rule>
        </resolver>       
    </file> 
     * */

}
