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

package org.netbeans.modules.java.api.common.project.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


/**
 * Action for showing Javadoc. The action looks up
 * the {@link ShowJavadocAction.JavadocProvider} in the
 * activated node's Lookup and delegates to it.
 * @author Tomas Zezula
 */
final class ShowJavadocAction extends NodeAction {
    
    private static final Logger LOG = Logger.getLogger(ShowJavadocAction.class.getName()); 

    /**
     * Implementation of this interfaces has to be placed
     * into the node's Lookup to allow {@link ShowJavadocAction}
     * on the node.
     */
    public static interface JavadocProvider {

        /**
         * Checks if the node can provide Javaodc
         * @return true if the action should be enabled
         */
        public abstract boolean hasJavadoc ();

        /**
         * Opens javadoc page in the browser
         */
        public abstract void showJavadoc ();
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length!=1) {
            return;
        }
        JavadocProvider jd = activatedNodes[0].getLookup().lookup(JavadocProvider.class);
        if (jd == null) {
            return;
        }
        jd.showJavadoc();
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length!=1) {
            return false;
        }
        JavadocProvider jd = activatedNodes[0].getLookup().lookup(JavadocProvider.class);
        if (jd == null) {
            return false;
        }
        return jd.hasJavadoc();
    }

    public String getName() {
        return NbBundle.getMessage(ShowJavadocAction.class,"CTL_ShowJavadoc");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (ShowJavadocAction.class);
    }

    public boolean asynchronous () {
        return false;
    }

    /**
     * Opens the IDE default browser with given URL
     * @param javadoc URL of the javadoc page
     * @param displayName the name of file to be displayed, typically the package name for class
     * or project name for project.
     */
    static void showJavaDoc (URL javadoc, String displayName) {
        if (javadoc!=null) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(javadoc);
        }
        else {
            StatusDisplayer.getDefault().setStatusText(MessageFormat.format(NbBundle.getMessage(ShowJavadocAction.class,
                    "TXT_NoJavadoc"), new Object[] {displayName}));   //NOI18N
        }
    }

    /**
     * Locates a javadoc page by a relative name and an array of javadoc roots
     * @param resource the relative name of javadoc page
     * @param urls the array of javadoc roots
     * @return the URL of found javadoc page or null if there is no such a page.
     */
    static  URL findJavadoc (String resource, URL urls[]) {
        for (int i = 0; i < urls.length; i++) {
            String base = urls[i].toExternalForm();
            if (!base.endsWith("/")) { // NOI18N
                base+="/"; // NOI18N
            }
            try {
                final URL u = new URL(base+resource);
                if (u.getProtocol().startsWith("http")) {   //NOI18N
                    try {
                        final InputStream in = u.openStream();
                        in.close();
                        return u;
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, "Ignoring non existent URL: ", u.toExternalForm()); //NOI18N
                        //continue
                    }
                } else {
                    final FileObject fo = URLMapper.findFileObject(u);
                    if (fo != null) {
                        return u;
                    }
                }
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, "Cannot create URL for " + base + resource + ". " + ex.toString());
                continue;
            }
        }
        return null;
    }
}
