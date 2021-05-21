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

package org.netbeans.core.windows.services;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.LoaderTransfer;
import org.openide.util.datatransfer.PasteType;
import org.openide.cookies.InstanceCookie;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.*;

/**
 * PasteType for Action instances. PasteType impl. that uses {@link org.openide.loaders.DataShadow}  for copying and 
 * {@link DataObject#move(org.openide.loaders.DataFolder)} .
 *  
 * @author Radek Matous
 */ 
final class ActionPasteType {
/**
 *  
 */ 
    static PasteType getPasteType(final DataFolder targetFolder, final Transferable transfer) {
        final FileObject folder = targetFolder.getPrimaryFile();
        PasteType retVal = null;

        try {
            /*Copy/Cut/Paste is allowed just on SystemFileSystem*/ 
            if (folder.getFileSystem().isDefault()) {
                final int[] pasteOperations = new int[]{LoaderTransfer.CLIPBOARD_COPY, LoaderTransfer.CLIPBOARD_CUT};

                for (int i = 0; i < pasteOperations.length; i++) {
                    final DataObject[] dataObjects = LoaderTransfer.getDataObjects(transfer, pasteOperations[i]);
                    if (dataObjects != null) {                                                
                        if (canBePasted(dataObjects, targetFolder, pasteOperations[i])) {
                            retVal = new PasteTypeImpl(Arrays.asList(dataObjects), targetFolder, pasteOperations[i]);
                            break;
                        }
                    }
                }
            }
        } catch (FileStateInvalidException e) {/*just null is returned if folder.getFileSystem fires ISE*/}

        return retVal;
    }

    private static boolean canBePasted(final DataObject[] dataObjects, final DataFolder targetFolder, final int operation) throws FileStateInvalidException {
        final Set<DataObject> pasteableDataObjects = new HashSet<DataObject> ();
        final FileObject folder = targetFolder.getPrimaryFile();
        
        DataObject[] folderChildren = targetFolder.getChildren();
        
        for (int j = 0; j < dataObjects.length; j++) {
            final DataObject dataObject = dataObjects[j];
            final FileObject fo = dataObject.getPrimaryFile ();
            
            if (!isAction(dataObject) || !fo.getFileSystem().isDefault()) {
                break;    
            }

            final boolean isCopyPaste = operation == LoaderTransfer.CLIPBOARD_COPY && dataObject.isCopyAllowed();
            final boolean isCutPaste = operation == LoaderTransfer.CLIPBOARD_CUT && dataObject.isMoveAllowed() && 
                    !(fo.getParent() == folder);//prevents from cutting into the same folder where it was 
                            
            if (isCopyPaste || isCutPaste) {
                
                boolean isDuplicate = false;
                for( int i=0; i<folderChildren.length; i++ ) {
                    if( 0 == folderChildren[i].getName().compareTo( dataObject.getName() ) ) {
                        isDuplicate = true;
                        break;
                    }
                }
                if( !isDuplicate )
                    pasteableDataObjects.add(dataObject);                        
            }
        }
        return (pasteableDataObjects.size() == dataObjects.length);
    }

    private static boolean isAction(DataObject dataObject) {
        boolean retVal = false;
        InstanceCookie.Of ic = (InstanceCookie.Of)dataObject.getCookie(InstanceCookie.Of.class);            
        if (ic != null && ic.instanceOf(Action.class)) {
            retVal = true;    
        }
        return retVal;
    }

    private final static class PasteTypeImpl extends PasteType {
        final private DataFolder targetFolder;
        final private Collection<DataObject>  sourceDataObjects;
        final private int pasteOperation;

    
        private PasteTypeImpl(final Collection<DataObject> sourceDataObjects, final DataFolder targetFolder, final int pasteOperation) {
            this.targetFolder = targetFolder;
            this.sourceDataObjects = sourceDataObjects;
            this.pasteOperation = pasteOperation;
        }

        public Transferable paste() throws IOException {
            if (targetFolder != null) {
                for (Iterator iterator = sourceDataObjects.iterator(); iterator.hasNext();) {
                    DataObject dataObject = (DataObject) iterator.next();
                    boolean isValid = dataObject != null && dataObject.isValid();
                    
                    if (isValid && pasteOperation == LoaderTransfer.CLIPBOARD_COPY) {
                        dataObject.createShadow(targetFolder);
                    } 
                    
                    if (isValid && pasteOperation == LoaderTransfer.CLIPBOARD_CUT) {
                        dataObject.move(targetFolder);
                    }
                                        
                }                
            }
            return null;
        }
    }    
}
