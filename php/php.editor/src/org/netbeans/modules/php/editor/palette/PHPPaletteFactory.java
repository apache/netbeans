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
package org.netbeans.modules.php.editor.palette;

import java.io.IOException;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Petr Pisl
 */
public final class PHPPaletteFactory {

    public static final String PHP_PALETTE_FOLDER = "Palettes/PHP";  //NOI18N
    private static volatile PaletteController palette = null;

    private PHPPaletteFactory() {
    }

    public static PaletteController getPalette() throws IOException {
        if (palette == null) {
            palette = PaletteFactory.createPalette(PHP_PALETTE_FOLDER, new PHPPaletteActions(),
                    null, new PHPDnDHandler());
        }
        return palette;
    }

    private static class PHPDnDHandler extends DragAndDropHandler {

        public PHPDnDHandler() {
            super(true);
        }

        @Override
        public void customize(ExTransferable t, Lookup item) {
        }
    }
}
