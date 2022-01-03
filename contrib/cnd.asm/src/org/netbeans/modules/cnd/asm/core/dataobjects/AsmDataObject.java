/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

