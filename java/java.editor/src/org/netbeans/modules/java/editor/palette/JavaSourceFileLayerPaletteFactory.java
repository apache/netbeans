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

package org.netbeans.modules.java.editor.palette;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author geertjan
 */
public class JavaSourceFileLayerPaletteFactory {
    
    public static final String JAVA_PALETTE_FOLDER = "JavaPalette";
    private static PaletteController palette = null;
    
    public JavaSourceFileLayerPaletteFactory() {
    }
    
    public static PaletteController createPalette() {
        try {
            if (null == palette)
                palette = PaletteFactory.createPalette(
                        JAVA_PALETTE_FOLDER, 
                        new MyActions(), 
                        null, 
                        new JavaDragAndDropHandler());
            return palette;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private static class MyActions extends PaletteActions {
        
        //Add new buttons to the Palette Manager here:
        @Override
        public Action[] getImportActions() {
            return null;
        }
        
        //Add new contextual menu items to the palette here:
        @Override
        public Action[] getCustomPaletteActions() {
            return null;
        }
        
        //Add new contextual menu items to the categories here:
        @Override
        public Action[] getCustomCategoryActions(Lookup arg0) {
            return null;
        }
        
        //Add new contextual menu items to the items here:
        @Override
        public Action[] getCustomItemActions(Lookup arg0) {
            return null;
        }
        
        //Define the default action here:
        @Override
        public Action getPreferredAction(Lookup arg0) {
            return null;
        }
        
    }
    
    private static class JavaDragAndDropHandler extends DragAndDropHandler {

        public JavaDragAndDropHandler() {
            super(true);
        }

        @Override
        public void customize(ExTransferable t, Lookup item) {
            //do nothing
        }
    }
    
}
