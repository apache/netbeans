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

package org.openide.text;

import java.awt.datatransfer.DataFlavor;
import javax.swing.text.JTextComponent;

/**
 * ActiveEditorDrop with artificial DataFlavor. Drag and drop initiator sometimes needs
 * to be notified about a target component, where the drop operation was performed.
 * Initiator should implement this interface and use the required artificial DataFlavor.
 * Component that will support drop operation of the ActiveEditorDrop should call handleTransfer
 * method.
 * <br>
 * Sample usage of the client: <br>
 *   <pre>
 *   private class MyDrop extends StringSelection implements ActiveEditorDrop {
 *       
 *       public MyDrop(String text){
 *           super(text); //NOI18N
 *       }
 *       
 *       public boolean isDataFlavorSupported(DataFlavor f) {
 *           return super.isDataFlavorSupported(f) || ActiveEditorDrop.FLAVOR == f;
 *       }
 *       
 *       public final DataFlavor[] getTransferDataFlavors() {
 *           DataFlavor delegatorFlavor[] = super.getTransferDataFlavors();
 *           int delegatorFlavorLength = delegatorFlavor.length;
 *           DataFlavor newArray[] = new DataFlavor[delegatorFlavorLength + 1];
 *           System.arraycopy(delegatorFlavor, 0, newArray, 0, delegatorFlavorLength);
 *           newArray[delegatorFlavorLength] = ActiveEditorDrop.FLAVOR;
 *           return newArray;
 *       }
 *       
 *       public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
 *           if (flavor == ActiveEditorDrop.FLAVOR) {
 *               return this;
 *           }
 *           return super.getTransferData(flavor);
 *       }
 *       
 *       public boolean handleTransfer(JTextComponent targetComponent) {
 *          // your implementation
 *       }
 *   }
 *   </pre>
 *
 *   or simplified solution: <br>
 *   <pre>
 *
 *   private class MyDrop implements ActiveEditorDrop, Transferable {
 * 
 *      public MyDrop(){
 *      }
 *      
 *      public boolean isDataFlavorSupported(DataFlavor f) {
 *          return ActiveEditorDrop.FLAVOR == f;
 *      }
 *      
 *      public final DataFlavor[] getTransferDataFlavors() {
 *          DataFlavor delegatorFlavor[] = new DataFlavor[1];
 *          delegatorFlavor[0] = ActiveEditorDrop.FLAVOR;
 *          return delegatorFlavor;
 *      }
 *      
 *      public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
 *          return (flavor == ActiveEditorDrop.FLAVOR) ? this : null;
 *      }
 *      
 *      public boolean handleTransfer(JTextComponent targetComponent) {
 *          //your implementation
 *      }
 *      
 *  }
 *
 *   </pre>
 *   
 *
 * @author Martin Roskanin
 * @since org.openide.text 6.5 
 */
public interface ActiveEditorDrop {
    
    /**
     * Active editor DataFlavor used for communication between DragSource and DragTarget.
     * This DataFlavor should be used for case where target component is instance
     * of JTextComponent.
     */
    DataFlavor FLAVOR = QuietEditorPane.constructActiveEditorDropFlavor();

    /**
     * A method called from the drop target that supports the artificial DataFlavor.
     * @param targetComponent a Component where drop operation occured.
     * @return true if implementor allowed a drop operation into the targetComponent
     */
    boolean handleTransfer(JTextComponent targetComponent);
    
}
