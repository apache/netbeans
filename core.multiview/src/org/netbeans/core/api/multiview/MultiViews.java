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

package org.netbeans.core.api.multiview;

import java.io.Serializable;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.core.multiview.MultiViewCloneableTopComponent;
import org.netbeans.core.multiview.MultiViewTopComponent;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElement.Registration;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/** Factory class for handling multi views.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
 public final class MultiViews {

    /** Factory class, no instances. */
    private MultiViews() {
    }

    /**
     * For advanced manipulation with Multiview component, the handler can be requested
     * @return handle that one can use for manipulation with multiview component.
     */
    public static MultiViewHandler findMultiViewHandler(TopComponent tc) {
        if ( tc != null) {
            if (tc instanceof MultiViewTopComponent) {
                return new MultiViewHandler(((MultiViewTopComponent)tc).getMultiViewHandlerDelegate());
            }
            if (tc instanceof MultiViewCloneableTopComponent) {
                return new MultiViewHandler(((MultiViewCloneableTopComponent)tc).getMultiViewHandlerDelegate());
            }
        }
        return null;
    }
 
    /** Factory method to create multiview for a given mime type. The list
     * of {@link MultiViewElement}s to display is taken from {@link MimeLookup#getLookup}.
     * The <code>context</code> parameter has to be Serializable, so the top component
     * can be persisted and later, when deserialized, it can again recreate the 
     * {@link Lookup}. Suitable candidate for an object that implements both
     * {@link Serializable} as well as {@link org.openide.util.Lookup.Provider} is 
     * <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html">DataObject</a>.
     * To register your elements into particular mime type see {@link Registration}.
     * 
     * @param context lookup provider representing the object to displayed in the multiview
     * @param mimeType the mime type to seek for elements in
     * @return multiview component
     * @since 1.24
     */
    public static <T extends Serializable & Lookup.Provider> TopComponent createMultiView(
        String mimeType, T context
    ) {
        MultiViewTopComponent tc = new MultiViewTopComponent();
        tc.setMimeLookup(mimeType, context);
        return tc;
    }

    /** Factory method to create cloneable multiview for a given mime type. 
     * The way to obtain individual elements is the same as in 
     * {@link #createMultiView}
     * 
     * @param context lookup representing the object to be displayed in the multiview
     * @param mimeType the mime type to seek for elements in
     * @return cloneable multiview component also implementing {@link Pane} interface
     * @since 1.24
     */
    public static <T extends Serializable & Lookup.Provider> CloneableTopComponent createCloneableMultiView(
            String mimeType, T context
    ) {
        MultiViewCloneableTopComponent tc = new MultiViewCloneableTopComponent();
        tc.setMimeLookup(mimeType, context);
        return tc;
    }
}
