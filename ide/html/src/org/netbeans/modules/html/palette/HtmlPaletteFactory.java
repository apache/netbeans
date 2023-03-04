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
package org.netbeans.modules.html.palette;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.html.palette.api.HtmlPaletteFolderProvider;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Libor Kotouc, mfukala@netbeans.org
 */
public final class HtmlPaletteFactory {

    //palette folder to palettecontroller map
    private static Map<String, PaletteController> PALETTES = new HashMap<String, PaletteController>();

    public static PaletteController getHtmlPalette() throws IOException {
        return getOrCreatePalette("HTMLPalette");
    }
    
    public static PaletteController getXhtmlPalette() throws IOException {
        return getOrCreatePalette("XHTMLPalette");
    }
    
    public static PaletteController getPalette(String mimeType) throws IOException {
        if("text/html".equals(mimeType)) {
            return getHtmlPalette();
        } else if("text/xhtml".equals(mimeType)) {
            return getXhtmlPalette();
        } else {
            return null;
        }
    }
    
    private static PaletteController getOrCreatePalette(String paletteFolder) throws IOException {
        PaletteController palette = PALETTES.get(paletteFolder);
        if(palette == null) {
            palette = PaletteFactory.createPalette(paletteFolder, new HtmlPaletteActions(), null, new HtmlDragAndDropHandler());
            PALETTES.put(paletteFolder, palette);
        }

        return palette;
    }

    private static class HtmlDragAndDropHandler extends DragAndDropHandler {

        public HtmlDragAndDropHandler() {
            super(true);
        }

        @Override
        public void customize(ExTransferable t, Lookup item) {
            //do nothing
        }
    }

}
