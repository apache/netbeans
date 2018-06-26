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

package org.netbeans.modules.j2ee.ejbjar;

import java.util.HashMap;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.spi.ejbjar.CarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.api.webmodule.WebModule;

/** A dummy provider that things that any *.foo file belongs to its web module.
 *
 * @author  Pavel Buzek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2ee.spi.ejbjar.EarProvider.class)
public class CustomProviderEar implements EarProvider {

    private HashMap cache = new HashMap ();

    public CustomProviderEar () {
    }

    public Ear findEar (FileObject file) {
        if (file.getExt ().equals ("foo")) {
            Ear em  = (Ear) cache.get (file.getParent ());
            if (em == null) {
                em = EjbJarFactory.createEar (new EM (file.getParent (), EjbProjectConstants.J2EE_14_LEVEL));
                cache.put (file.getParent (), em);
            }
            return em;
        }
        if (file.getExt ().equals ("bar")) {
            Ear em  = (Ear) cache.get (file.getParent ());
            if (em == null) {
                em = CarFactory.createEar (new EM (file.getParent (), EjbProjectConstants.J2EE_14_LEVEL));
                cache.put (file.getParent (), em);
            }
            return em;
        }
        return null;
    }
    
    private class EM implements EarImplementation {
        FileObject root;
        String ver;
        
        public EM (FileObject root, String ver) {
            this.root = root;
            this.ver = ver;
        }
        
        public String getJ2eePlatformVersion () {
            return ver;
        }
        
        public FileObject getDeploymentDescriptor () {
            return root.getFileObject ("conf/application.xml");
        }
        
        public FileObject getMetaInf () {
            return null;
        }
        
        public void addEjbJarModule(EjbJar module) {
            
        }
        public void addWebModule(WebModule module) {
            
        }
        public void addCarModule(Car module) {
            
        }        
    }
}
