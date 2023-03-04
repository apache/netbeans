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
package org.netbeans.modules.java.hints;

import java.io.IOException;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=EntityCatalog.class)
public final class TestEntityCatalogImpl extends EntityCatalog {

    @Override
    public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
        switch (publicID) {
            case "-//NetBeans//DTD Editor KeyBindings settings 1.1//EN":
                return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/keybindings/EditorKeyBindings-1_1.dtd"));
            case "-//NetBeans//DTD Editor Preferences 1.0//EN":
                return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/preferences/EditorPreferences-1_0.dtd"));
            case "-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN":
                return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/fontscolors/EditorFontsColors-1_1.dtd"));
        }
        return null;
    }

}
