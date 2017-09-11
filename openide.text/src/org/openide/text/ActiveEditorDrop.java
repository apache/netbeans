/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
