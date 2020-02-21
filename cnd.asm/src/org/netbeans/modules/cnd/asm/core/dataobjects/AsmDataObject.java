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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.asm.core.dataobjects;

import java.io.IOException;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.cnd.asm.core.editor.AsmEditorSupport;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@MIMEResolver.ExtensionRegistration(
    displayName="#AsmResolver", // NOI18N
    position=458,
    extension={ "s", "S", "asm", "ASM", "as", "il" }, // NOI18N
    mimeType="text/x-asm" // NOI18N
)
public class AsmDataObject extends MultiDataObject {
    public AsmDataObject(FileObject fo, AsmDataLoader loader) throws DataObjectExistsException, IOException {
        super(fo, loader); 
        
        CookieSet cookies = getCookieSet();                       
        cookies.add(AsmEditorSupport.class, factory);
    }
    
    private AsmEditorSupport editor = null;
    
    private final CookieSet.Factory factory = new CookieSet.Factory() {
        @Override
        public <T extends Cookie> T createCookie(Class<T> klass) {
            if (editor == null) {
                editor = new AsmEditorSupport(AsmDataObject.this);
            }
            return klass.isAssignableFrom(editor.getClass()) ? klass.cast(editor) : null;
        }
    };
  
    @Messages("Source=&Source")
    @MultiViewElement.Registration(displayName = "#Source", //NOI18N 
    iconBase = AsmDataLoaderBeanInfo.IMAGE_ICON_BASE,
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
    preferredID = "asm.source", //NOI18N
    mimeType = MIMENames.ASM_MIME_TYPE,
    position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }


    public void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
    }

    public void removeSaveCookie(SaveCookie save) {
        getCookieSet().remove(save);
    }

    @Override
    protected Node createNodeDelegate() {
        return new AsmDataNode(this);
    }

    @Override
   protected int associateLookup() {
       return 1;
   }

}

