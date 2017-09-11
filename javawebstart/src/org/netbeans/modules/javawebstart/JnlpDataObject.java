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

package org.netbeans.modules.javawebstart;

import java.io.IOException;

import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
//import org.openide.text.DataEditorSupport;

import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

@MIMEResolver.ExtensionRegistration(
    mimeType="text/x-jnlp+xml",
    position=200,
    displayName="#JNLPResolver",
    extension={ "jnlp" }
)
public class JnlpDataObject extends MultiDataObject {
    
    public JnlpDataObject(FileObject pf, JnlpDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(JnlpDataLoader.REQUIRED_MIME, true);
        CookieSet cookies = getCookieSet();
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLSupport checkCookieImpl = new CheckXMLSupport(in);
        ValidateXMLSupport validateCookieImpl = new ValidateXMLSupport(in);
        cookies.add(checkCookieImpl);
        cookies.add(validateCookieImpl);
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new JnlpDataNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }    

     @Messages("Source=&Source")
     @MultiViewElement.Registration(
             displayName="#Source",
             iconBase="org/netbeans/modules/javawebstart/resources/jnlp.gif",
             persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
             mimeType=JnlpDataLoader.REQUIRED_MIME,
             preferredID="jnlp.source",
             position=1
     )
     public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
         return new MultiViewEditorElement(context);
     }

}
