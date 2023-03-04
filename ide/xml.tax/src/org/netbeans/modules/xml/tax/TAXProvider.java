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

package org.netbeans.modules.xml.tax;

import org.netbeans.modules.xml.DTDDataObject;
import org.netbeans.modules.xml.XMLDataObject;
import org.netbeans.modules.xml.XMLDataObjectLook;
import org.netbeans.modules.xml.cookies.CookieFactory;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service=XMLDataObject.XMLCookieFactoryCreator.class),
    @ServiceProvider(service=DTDDataObject.DTDCookieFactoryCreator.class)
})
public final class TAXProvider implements XMLDataObject.XMLCookieFactoryCreator, DTDDataObject.DTDCookieFactoryCreator {

    @Override public CookieFactory createCookieFactory(DataObject obj) {
        return new TreeEditorCookieImpl.CookieFactoryImpl((XMLDataObjectLook) obj);
    }

}
