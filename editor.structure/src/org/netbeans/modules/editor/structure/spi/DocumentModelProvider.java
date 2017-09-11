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

package org.netbeans.modules.editor.structure.spi;

import org.netbeans.modules.editor.structure.DocumentModelProviderFactory;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelModificationTransaction;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * The DocumentModelProvider is responsible for parsing the text document
 * and updating the tree structure of elements held by DocumentModel. 
 * The provider accomplished this by adding a 'requests' to add or remove 
 * an element into the transaction. For more detailed information see 
 * {@link org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelModificationTransaction}
 * <br>
 * When the model decides that there is a need to update the structure of the
 * elements (after there is a document change(s)) it calls the provider's 
 * update method. The model passes an instance of DocumentModelModificationTransaction,
 * the model itself and an array of changes made in the text document.
 * Then it's up to the provider implementation what parts of the document will
 * be reparsed and how the elements will be tailored. In principal the
 * provider can add or remove elements from the model. This is accomplished by
 * adding change requests into the transaction. Once the provider finishes its
 * work the model commits the transaction and the model structure is definitively
 * updated. 
 * <br>
 * When the text document is changed during the call of the update method, the
 * model cancels the transaction so next attempt to add something into the transaction
 * will cause DocumentModelTransactionCancelledException to be thrown. 
 * The provider should not catch this exception - it is a legal mechanism, how
 * to break no more valid model update.
 *
 * Instancies of this class are registered via layer mechanism into the 
 * IDE and associated with a particullar mime-types.
 *
 * The registration are read from the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/DocumentModel
 * </pre>
 *
 * @author Marek Fukala
 * @version 1.0
 * @see org.netbeans.modules.editor.structure.api.DocumentModel
 *
 */
@MimeLocation(subfolderName=DocumentModelProviderFactory.FOLDER_NAME)
public interface DocumentModelProvider {
    
    /** Method called by the DocumentModel when the model content needs to be updated.
     * For more info see the overall description of this class.
     * @param trans the transaction the provider puts the change requests into
     * @param model the DocumentModel instance
     * @param changes the array of changes made in the document from the last model update
     *
     * @throws DocumentModelTransactionCancelledException when the transaction passed
     * into the provider has been cancelled.
     *
     */
    public void updateModel(DocumentModelModificationTransaction trans, 
            DocumentModel model, DocumentChange[] changes) throws DocumentModelException, DocumentModelTransactionCancelledException;
    
}
