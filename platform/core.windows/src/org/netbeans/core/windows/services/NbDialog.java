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

import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

import java.awt.*;
import java.awt.event.ActionListener;

// XXx Before as org.netbeans.core.NbDialog

/** Default implementation of Dialog created from DialogDescriptor.
*
* @author Ian Formanek
*/
final class NbDialog extends NbPresenter {
    static final long serialVersionUID =-4508637164126678997L;

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Frame owner) {
        super (d, owner, d.isModal ());
    }

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Dialog owner) {
        super (d, owner, d.isModal ());
    }

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog(DialogDescriptor d, Window owner) {
        super(d, owner, d.isModal() ? Dialog.DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
    }

    /** Getter for help.
    */
    @Override
    protected HelpCtx getHelpCtx () {
        return ((DialogDescriptor)descriptor).getHelpCtx ();
    }

    /** Options align.
    */
    @Override
    protected int getOptionsAlign () {
        return ((DialogDescriptor)descriptor).getOptionsAlign ();
    }

    /** Getter for button listener or null
    */
    @Override
    protected ActionListener getButtonListener () {
        return ((DialogDescriptor)descriptor).getButtonListener ();
    }

    /** Closing options.
    */
    @Override
    protected Object[] getClosingOptions () {
        return ((DialogDescriptor)descriptor).getClosingOptions ();
    }
}
