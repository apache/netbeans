/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
