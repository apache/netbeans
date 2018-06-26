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
            } catch (org.netbeans.modules.schema2beans.Schema2BeansException s2bX) {
                giveUp(s2bX);
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
