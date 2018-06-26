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
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.openide.filesystems.FileObject;

/**
 * @author pfiala
 */
public class DDUtils {
    public static EjbJarProxy createEjbJarProxy(InputStream inputStream) throws IOException {
        return createEjbJarProxy(new InputSource(inputStream));
    }

    public static EjbJarProxy createEjbJarProxy(FileObject fo) throws IOException {
        InputStream inputStream = fo.getInputStream();
        try {
            return createEjbJarProxy(new InputSource(inputStream));
        } finally {
            inputStream.close();
        }
    }
    
    public static EjbJarProxy createEjbJarProxy(Reader reader) throws IOException {
        return createEjbJarProxy(new InputSource(reader));
    }
    
    public static EjbJarProxy createEjbJarProxy(InputSource inputSource) throws IOException {
        try {
            return (EjbJarProxy) DDProvider.getDefault().getDDRoot(inputSource);
        } catch (SAXException ex) {
            // XXX lets throw an exception here
            EjbJar ejbJar = org.netbeans.modules.j2ee.dd.impl.ejb.model_2_1.EjbJar.createGraph();
            EjbJarProxy ejbJarProxy = new EjbJarProxy(ejbJar, ejbJar.getVersion().toString());
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException) ex.getException());
            }
            return ejbJarProxy;
        }
    }
    
    
    public static void merge(EjbJarProxy ejbJarProxy, Reader reader) {
        try {
            EjbJarProxy newEjbJarProxy = createEjbJarProxy(reader);
            if (newEjbJarProxy.getStatus() == EjbJar.STATE_INVALID_UNPARSABLE) {
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                ejbJarProxy.setError(newEjbJarProxy.getError());
                return;
            }
            ejbJarProxy.merge(newEjbJarProxy, EjbJar.MERGE_UPDATE);
            ejbJarProxy.setStatus(newEjbJarProxy.getStatus());
            ejbJarProxy.setError(newEjbJarProxy.getError());
        } catch (IOException ex) {
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            // cbw if the state of the xml file transitions from
            // parsable to unparsable this could be due to a user
            // change or cvs change. We would like to still
            // receive events when the file is restored to normal
            // so lets not set the original to null here but wait
            // until the file becomes parsable again to do a merge
            //ejbJarProxy.setOriginal(null);
        } catch (Schema2BeansRuntimeException s2bre){ // see #70286    
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            ejbJarProxy.setError(new SAXParseException(null, null, s2bre));
        } catch (RuntimeException re){ // see #99047    
            if (re.getCause() instanceof Schema2BeansException){
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                ejbJarProxy.setError(new SAXParseException(null, null, (Schema2BeansException) re.getCause()));
            } else if (re instanceof IllegalArgumentException){ // see #104180
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                Logger.getLogger(DDUtils.class.getName()).log(Level.FINE, "IAE thrown during merge, see #104180.", re); //NO18N
            } else {
                throw re;
            }
        }
    }

    public static WebApp createWebApp(InputStream is, String version) throws IOException, SAXException {
        try {
            if (WebApp.VERSION_2_4.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_4.WebApp.createGraph(is);
            } else if (WebApp.VERSION_2_5.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_5.WebApp.createGraph(is);
            } else if (WebApp.VERSION_3_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_3_0.WebApp.createGraph(is);
            } else if (WebApp.VERSION_3_1.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_3_1.WebApp.createGraph(is);
            } else {
                return null;
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
    }

    public static WebApp createWebApp(FileObject fo, String version) throws IOException, SAXException {
        InputStream inputStream = fo.getInputStream();
        try {
            return createWebApp(inputStream, version);
        } finally {
            inputStream.close();
        }
    }
    
    public static AppClient createAppClient(InputStream is, String version) throws IOException, SAXException {
        try {
            if (AppClient.VERSION_1_4.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_1_4.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_5_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_5_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_6_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_6_0.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_7_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_7_0.ApplicationClient.createGraph(is);
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
        return null;
    }

    public static AppClient createAppClient(FileObject fo, String version) throws IOException, SAXException {
        InputStream inputStream = fo.getInputStream();
        try {
            return createAppClient(inputStream, version);
        } finally {
            inputStream.close();
        }
    }
}
