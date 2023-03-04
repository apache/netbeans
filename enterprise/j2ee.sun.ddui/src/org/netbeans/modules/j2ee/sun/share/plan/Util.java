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

package org.netbeans.modules.j2ee.sun.share.plan;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;

import java.util.jar.JarOutputStream;
import java.io.InputStream;

import java.util.jar.JarEntry;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

/** Utility functions for deployment plan objects
 * @author vkraemer
 */
public class Util {

    /** Creates a new instance of Util */
    private Util() {
    }
    
    /** Compile a textual plan into its jar file form
     *
     * Converts an xml file that conforms to the deployment-plan.dtd
     * and changes it into a jar file, suitable for the SJS8.0PE
     * DeploymentManager implementation.
     * @param plan The textual deployment plan
     * @param jar The resulting jar file
     * @throws IOException in case of trouble
     */    
    public static void convert(InputStream plan, JarOutputStream jar) throws java.io.IOException  {
        DeploymentPlan dp = null;
        Throwable cause = null;

        Document doc = null; 
        // read in the stream content as an xml document...
        try {
             doc = GraphManager.createXmlDocument(plan, false);
        }
        catch (RuntimeException re) {
            giveUp(re);
        }

        // try to treat that document as a deployment-plan
        try {
            dp = DeploymentPlan.createGraph(doc);
        }
        catch (org.netbeans.modules.schema2beans.Schema2BeansException s2be) {
            // this may happen if the plan is from a a web app
            cause = s2be;
        }
        if (null == dp) {
            // try to correct for a webmod plan, which is just the sun-web.xml
            SunWebApp swa = null;
            try {
                // treat the document as a sun-web-app
                swa = DDProvider.getDefault().getWebDDRoot(doc);
                dp = DeploymentPlan.createGraph();
                FileEntry fe = new FileEntry();
                fe.setName("sun-web.xml");
                java.io.StringWriter strWriter = new java.io.StringWriter();
                swa.write(strWriter);
                fe.setContent(strWriter.toString());
                dp.addFileEntry(fe);
            } catch(DDException ex) {
                giveUp(ex);
            } catch (java.beans.PropertyVetoException pv) {
                giveUp(pv);
            }
        }
        
        int index = dp.sizeFileEntry();
        for (int i = 0; i < index; i++) {
            FileEntry fe = dp.getFileEntry(i);
            String name = fe.getUri();
            if (null == name)
                name = hashify(fe.getName());
            else
                name += "." + hashify(fe.getName());
            JarEntry ent = new JarEntry(name);
            jar.putNextEntry(ent);
            String content = fe.getContent();
            jar.write(content.getBytes());
            jar.closeEntry();
        }
    }
    
    private static void giveUp(Throwable s2be) throws java.io.IOException  {
        java.io.IOException ioe = new java.io.IOException("plan file issue");
        ioe.initCause(s2be);
        throw ioe;
    }
    
    private static String hashify(String path) {
        return path.replace('/','#');
    }
            
}
