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

package org.netbeans.modules.websvc.saas.model.wsdl.impl;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.WeakHashMap;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModelerFactory {
    
    private static WsdlModelerFactory factory;
    WeakHashMap<URL, WeakReference<WsdlModeler>> modelers;
    
    /** Creates a new instance of WsdlModelerFactory */
    private WsdlModelerFactory() {
        modelers = new WeakHashMap<URL, WeakReference<WsdlModeler>>(5);
    }
    
    /**
    * Accessor method for WsdlModelerFactory singleton
    * @return WsdlModelerFactory object
    */
    public static synchronized WsdlModelerFactory getDefault() {
        if (factory==null) factory = new WsdlModelerFactory();
        return factory;
    }
    
    /** Get WsdlModeler for particular WSDL
     */
    public WsdlModeler getWsdlModeler(URL wsdlUrl) {
        WsdlModeler modeler = null;
        synchronized (modelers) {
            modeler = getFromCache(wsdlUrl);
            if (modeler!=null) {
                return modeler;
            }
            modeler = new WsdlModeler(wsdlUrl);
            modelers.put(wsdlUrl, new WeakReference<WsdlModeler>(modeler));
        }
        return modeler;
    }
    
    private WsdlModeler getFromCache (URL url) {
        if (url == null) {
            return null;
        }
        WeakReference wr = modelers.get(url);
        if (wr == null) {
            return null;
        }
        WsdlModeler modeler = (WsdlModeler) wr.get();
        if (modeler == null) {
            modelers.remove(url);
        }
        return modeler;
    }
    
    int mapLength() {
        return modelers.size();
    }
    
    
}
