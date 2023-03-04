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
package org.netbeans.modules.spring.beans.loader;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.xml.api.XmlFileEncodingQueryImpl;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

/**
 *
 * @author Andrei Badea et al.
 */
@MIMEResolver.NamespaceRegistration(
    displayName="",
    position=440,
    mimeType="text/x-springconfig+xml",
    elementName="beans",
    elementNS="http://www.springframework.org/schema/beans",
    doctypePublicId={
        "-//SPRING//DTD BEAN 2.0//EN",
        "-//SPRING//DTD BEAN//EN"
    }
)
public class SpringXMLConfigDataObject extends MultiDataObject {

    public SpringXMLConfigDataObject(FileObject pf, SpringXMLConfigDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        InputSource in = DataObjectAdapters.inputSource(this);
        cookies.add(new CheckXMLSupport(in));
        cookies.add(new ValidateXMLSupport(in));
        cookies.assign(FileEncodingQueryImplementation.class, XmlFileEncodingQueryImpl.singleton());

        registerEditor(SpringConstants.CONFIG_MIME_TYPE, true);
    }

    @Override
    protected Node createNodeDelegate() {
        return new SpringXMLConfigDataNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Messages("Source=&Source")
    @MultiViewElement.Registration(displayName = "#Source",
            iconBase = "org/netbeans/modules/spring/beans/resources/spring.png",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            mimeType = SpringConstants.CONFIG_MIME_TYPE,
            preferredID = "spring.xml.config",
            position = 1)
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
}
