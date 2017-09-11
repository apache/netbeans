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

package org.netbeans.core.spi.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.multiview.MultiViewElementCallbackDelegate;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


    /** Requester type of class, allowing implementors of MultiViewElement
     * to send requests back to enclosing component and window system. Enclosing
     * component or other part of window system will set the instance of this class to elements upon
     * instantiation or deserialization of the element to receive requests properly. 
     * Implementors of MultiViewElement shall not attempt to serialize
     * the passed callback instance.
     * 
     */
    public final class MultiViewElementCallback {
        
        static {
            AccessorImpl.createAccesor();
        }
        
        private MultiViewElementCallbackDelegate delegate;
        
        MultiViewElementCallback(MultiViewElementCallbackDelegate del) {
            delegate = del;
        }
        
        /** Activates this multi view element in enclosing multi view component
         * context, if enclosing multi view top component is opened.
         */
        public void requestActive() {
            delegate.requestActive();
        }

        /** Selects this multi view element in enclosing component context,
         * if component is opened, but does not activate it 
         * unless enclosing component is in active mode already.
         */
        public void requestVisible () {
            delegate.requestVisible();
        }
        
        /**
         * Creates the default TopComponent actions as defined by the Window System.
         * Should be used by the element when constructing it's own getActions() return value.
         */
        public Action[] createDefaultActions() {
            return delegate.createDefaultActions();
        }
        
        /**
         * Update the multiview's topcomponent title.
         */
        public void updateTitle(String title) {
            delegate.updateTitle(title);
        }
        
        /**
         * Element can check if it's currently the selected element.
         */
        public boolean isSelectedElement() {
            return delegate.isSelectedElement();
        }
        
        /**
         * Returns the enclosing Multiview's topcomponent.
         */
        public TopComponent getTopComponent() {
            return delegate.getTopComponent();
        }
        
    } // end of ActionRequestListener
    
