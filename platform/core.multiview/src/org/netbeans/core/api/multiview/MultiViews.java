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
