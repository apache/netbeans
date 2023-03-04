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
