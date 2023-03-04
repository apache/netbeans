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
package org.netbeans.modules.openide.explorer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.ExTransferable.Multi;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 * Utilities to handle drag and drop events to/from other applications
 * 
 * @author S. Aubrecht
 */
public class ExternalDragAndDrop {
    
    private ExternalDragAndDrop() {
    }
    
    /**
     * The default Transferable implementation for multi-object drag and drop operations is
     * ExTransferable.Multi. However it uses a custom DataFlavor which prevents drag and drop
     * of multiple files from the IDE to other applications.
     * This method checks whether the given Multi instance contains objects that support
     * DataFlavor.javaFileListFlavor and adds a separate Transferable instance for them.
     * 
     * @param multi Multi transferable
     * 
     * @return The original Multi transferable if none of the inner transferables supports
     * javaFileListFlavor. Otherwise it returns a new ExTransferable with the original Multi
     * transferable and an additional Transferable with javaFileListFlavor that aggregates
     * all file objects from the Multi instance.
     * 
     */
    public static Transferable maybeAddExternalFileDnd( Multi multi ) {
        Transferable res = multi;
        try {
            MultiTransferObject mto = (MultiTransferObject) multi.getTransferData(ExTransferable.multiFlavor);
            final List fileList = new ArrayList<>( mto.getCount() );
            for( int i=0; i<mto.getCount(); i++ ) {
                if( mto.isDataFlavorSupported( i, DataFlavor.javaFileListFlavor ) ) {
                    List<?> list = (List<?>)mto.getTransferData( i, DataFlavor.javaFileListFlavor );
                    fileList.addAll( list );
                }
            }
            if( !fileList.isEmpty() ) {
                ExTransferable fixed = ExTransferable.create( multi );
                fixed.put( new ExTransferable.Single( DataFlavor.javaFileListFlavor ) {
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        return fileList;
                    }
                });
                res = fixed;
            }
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(ExternalDragAndDrop.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalDragAndDrop.class.getName()).log(Level.INFO, null, ex);
        }
        return res;
    }
}
