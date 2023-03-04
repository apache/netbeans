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

package org.netbeans.modules.web.jsf.api.palette;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.web.jsf.palette.items.JsfForm;
import org.netbeans.modules.web.jsf.palette.items.JsfFormFromEntity;
import org.netbeans.modules.web.jsf.palette.items.JsfTable;
import org.netbeans.modules.web.jsf.palette.items.JsfTableFromEntity;
import org.netbeans.modules.web.jsf.palette.items.MetaData;
import org.netbeans.modules.web.jsfapi.api.JsfUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Internal API for web.jsf.editor, it allows to get and insert the palette items
 * programatically.
 *
 * @author marekfukala
 */
public final class PaletteItemsProvider {

    private static Collection<PaletteItem> ITEMS;

    public static final synchronized Collection<PaletteItem> getPaletteItems() {
            if(ITEMS == null) {
                ITEMS = new ArrayList<PaletteItem>();
                ITEMS.add(new MetaData());
                ITEMS.add(new JsfForm());
                ITEMS.add(new JsfFormFromEntity());
                ITEMS.add(new JsfTable());
                ITEMS.add(new JsfTableFromEntity());
            }
        return ITEMS;
    }

}
