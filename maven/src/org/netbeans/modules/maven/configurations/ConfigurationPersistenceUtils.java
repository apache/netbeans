/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author mkleint
 */
public final class ConfigurationPersistenceUtils {
    public static final String NAMESPACE = "http://www.netbeans.org/ns/maven-config-data/1"; 
    public static final String ROOT = "config-data"; 
    public static final String ENABLED = "enabled"; 
    public static final String ACTIVATED = "activated";
    public static final String CONFIGURATIONS = "configurations"; 
    public static final String CONFIG = "configuration"; 
    public static final String PROPERTY = "property"; 
    public static final String PROPERTY_NAME_ATTR = "name"; 
    public static final String CONFIG_PROFILES_ATTR = "profiles"; 
    public static final String CONFIG_ID_ATTR = "id"; 
    
    private ConfigurationPersistenceUtils() {}
    
    public static String readActiveConfigurationName(AuxiliaryConfiguration config) throws DOMException {
        String active = null;
        Element el = config.getConfigurationFragment(ROOT, NAMESPACE, false);
        if (el != null) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, ACTIVATED);
            if (list.getLength() > 0) {
                Element enEl = (Element)list.item(0);
                active = enEl.getTextContent();
            }
        }
        return active;
    }
    
    public static SortedSet<M2Configuration> readConfigurations(AuxiliaryConfiguration aux, FileObject projectDirectory, boolean shared) {
        Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el != null) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, CONFIG);
            if (list.getLength() > 0) {
                SortedSet<M2Configuration> toRet = new TreeSet<M2Configuration>();
                int len = list.getLength();
                for (int i = 0; i < len; i++) {
                    Element enEl = (Element)list.item(i);
                    
                    M2Configuration c = new M2Configuration(enEl.getAttribute(CONFIG_ID_ATTR), projectDirectory);
                    String profs = enEl.getAttribute(CONFIG_PROFILES_ATTR);
                    if (profs != null) {
                        String[] s = profs.split(" ");
                        List<String> prf = new ArrayList<String>();
                        for (String s2 : s) {
                            if (s2.trim().length() > 0) {
                                prf.add(s2.trim());
                            }
                        }
                        c.setActivatedProfiles(prf);
                    }
                    NodeList ps = enEl.getElementsByTagName(PROPERTY);
                    for (int y = 0; y < ps.getLength(); y++) {
                        Element propEl = (Element) ps.item(y);
                        String key = propEl.getAttribute(PROPERTY_NAME_ATTR);
                        String value = propEl.getTextContent();
                        if (key != null && value != null) {
                            c.getProperties().put(key, value);
                        }
                    }
                    toRet.add(c);
                }
                return toRet;
            }
        }
        return new TreeSet<M2Configuration>();
    }    
    
}
