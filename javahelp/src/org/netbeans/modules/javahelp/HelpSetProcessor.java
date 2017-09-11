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

package org.netbeans.modules.javahelp;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import org.openide.loaders.DataObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.help.HelpSet;
import org.openide.cookies.InstanceCookie;

import org.openide.loaders.Environment;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;

/** An XML processor for help set references.
 * Provides an instance of javax.swing.HelpSet.
 * @author Jesse Glick
 */
public final class HelpSetProcessor implements Environment.Provider {
    
    /** "context" for merge attribute on helpsets
     */    
    private static final String HELPSET_MERGE_CONTEXT = "OpenIDE"; // NOI18N
    
    /** attribute (type Boolean) on helpsets indicating
     * whether they should be merged into the master or
     * not; by default, true
     */    
    private static final String HELPSET_MERGE_ATTR = "mergeIntoMaster"; // NOI18N
    
    public @Override Lookup getEnvironment(final DataObject obj) {
        Installer.log.log(Level.FINE, "creating help set from ref: {0}", obj.getPrimaryFile());
        return Lookups.singleton(new InstanceCookie() {
            public @Override String instanceName() {
                return obj.getName();
            }
            public @Override Class<?> instanceClass() throws IOException, ClassNotFoundException {
                return HelpSet.class;
            }
            public @Override Object instanceCreate() throws IOException, ClassNotFoundException {
                try {
                    Document doc = XMLUtil.parse(new InputSource(obj.getPrimaryFile().getURL().toString()), true, false, XMLUtil.defaultErrorHandler(), EntityCatalog.getDefault());
                    Element el = doc.getDocumentElement();
                    if (!el.getNodeName().equals("helpsetref")) { // NOI18N
                        throw new IOException();
                    }
                    String url = el.getAttribute("url"); // NOI18N
                    if (url == null || url.isEmpty()) {
                        throw new IOException("no url attr on <helpsetref>! doc.class=" + doc.getClass().getName() + " doc.documentElement=" + el);
                    }
                    String mergeS = el.getAttribute("merge"); // NOI18N
                    boolean merge = mergeS.isEmpty() || Boolean.valueOf(mergeS);
                    // Make sure nbdocs: protocol is ready:
                    Object ignore = NbDocsStreamHandler.class; // DO NOT DELETE THIS LINE
                    HelpSet hs = new HelpSet(Lookup.getDefault().lookup(ClassLoader.class), new URL(url));
                    hs.setKeyData(HELPSET_MERGE_CONTEXT, HELPSET_MERGE_ATTR, merge);
                    return hs;
                } catch (IOException x) {
                    throw x;
                } catch (Exception x) {
                    throw new IOException(x);
                }
            }
        });
    }
    
}

