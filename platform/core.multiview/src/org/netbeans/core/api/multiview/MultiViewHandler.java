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

package org.netbeans.core.api.multiview;

import org.netbeans.core.multiview.MultiViewHandlerDelegate;
import org.netbeans.core.spi.multiview.MultiViewDescription;

/**
 * A handler for the  multiview's {@link org.openide.windows.TopComponent}, obtainable via
 * {@link org.netbeans.core.spi.multiview.MultiViewFactory}, that allows
 * examination of Component's content and programatic changes in visible/activated elements.
 * @author  mkleint
 */
public final class MultiViewHandler {

    static {
        AccessorImpl.createAccesor();
    }

    private MultiViewHandlerDelegate del;

    MultiViewHandler(MultiViewHandlerDelegate delegate) {
        del = delegate;
    }
    /**
     * Returns the array of <code>MultiViewPerspective</code>s that the {@link org.openide.windows.TopComponent} is composed of.
     * @return array of defined perspectives.
     */
    public MultiViewPerspective[] getPerspectives() {
        return del.getDescriptions();
    }
    
    /**
     * Returns the currently selected <code>MultiViewPerspective</code> in the {@link org.openide.windows.TopComponent}.
     * It's element can be either visible or activated.
     * @return selected perspective
     */
    public MultiViewPerspective getSelectedPerspective() {
        return del.getSelectedDescription();
    }
    
    /**
     * Requests focus for the <code>MultiViewPerspective</code> passed as parameter, if necessary
     * will switch from previously selected <code>MultiViewPerspective</code>
     * @param desc the new active selection
     */
    public void requestActive(MultiViewPerspective desc) {
        del.requestActive(desc);
    }
    
    /**
     * Changes the visible <code>MultiViewPerspective</code> to the one passed as parameter.
     * @param desc the new selection
     *
     */
    
    public void requestVisible(MultiViewPerspective desc) {
        del.requestVisible(desc);
    }
    
    /**
     * Adds another multiview element to an existing multiview TopComponent. 
     * Such elements are not persisted.
     * @param descr The description of the element to be added.
     * @param position Position of the new element or -1 to append the element to the end.
     * @since 1.38
     */
    public void addMultiViewDescription(MultiViewDescription descr, int position) {
        del.addMultiViewDescription( descr, position );
    }
    
    /**
     * Removes multiview element that was added at runtime.
     * @param descr The description of the element that was previously passed to 
     * addMultiViewDescription method.
     * @since 1.38
     * @see #addMultiViewDescription(org.netbeans.core.spi.multiview.MultiViewDescription, int) 
     */
    public void removeMultiViewDescription(MultiViewDescription descr) {
        del.removeMultiViewDescription( descr );
    }
 
    
 
}
