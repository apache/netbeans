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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.editor.structure.api;

/**
 * Implementations of this interface may register itself into the DocumentModel 
 * and then listen to changes of the model state.
 * 
 *<br>
 * Allows to listen on following changes of the model state:
 * <ul>
 * <li>The underlaying document has changed.
 * <li>DocumentModel started to scan the underlying document for changes. 
 * (The old model data are available until next step is reached.)
 * <li>The document model update started. 
 * Model is locked for reading since this event. 
 * <li>The document model update finished. 
 * New model data are accessible now.
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 * @since 1.14
 *
 * @see DocumentModel
 * @see DocumentModelListener
 * @see DocumentElement
 * @see DocumentElementListener
 * 
 */
public interface DocumentModelStateListener {

     /** Called when the underlying javax.swing.Document has changed. */
    public void sourceChanged();
    
    /** Indicates the model started to scan the underlying document for changes 
     * happened since last scan and update of the model.
     * The old model elements can be still accessed.
     */
    public void scanningStarted();
    
    /** Called when the DocumentModel update has started. 
     * The model elements are locked for reading until the updateFinished() method 
     * notifies that the model update finished.
     */
    public void updateStarted();
    
    /** Called when the DocumentModel update has finished. 
     * New model data are available now.
     */
    public void updateFinished();
    
}
