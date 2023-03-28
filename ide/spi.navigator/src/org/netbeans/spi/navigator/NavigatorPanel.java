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

package org.netbeans.spi.navigator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** Navigation related view description.
 *
 * Implementors of this interface will be plugged into Navigator UI.
 * @see Registration
 * @author Dafe Simonek
 */
public interface NavigatorPanel {

    /** Name of the view which will be shown in navigator UI.
     *
     * @return Displayable name of the view
     */
    public String getDisplayName ();

    /** Description of the view, explaining main purpose of the view.
     * <em>Currently unused.</em>
     * 
     * @return String description of the view.
     */
    public String getDisplayHint ();
    
    /** JComponent representation of this view. System will ask
     * multiple times and it is strongly recommended to
     * return the same JComponent instance each call for performance
     * reasons.<p>
     *
     * This method is always called in event dispatch thread. 
     * 
     * @return JComponent representation of this view.
     */
    public JComponent getComponent ();
    
    /** Called when this panel's component is about to being displayed.
     * Right place to attach listeners to current navigation data context,
     * as clients are responsible for listening to context changes when active
     * (in the time between panelActivated - panelDeactivated calls). 
     *
     * This method is always called in event dispatch thread. 
     *
     * @param context Lookup instance representing current context to take
     * data from
     */
    public void panelActivated (Lookup context);
    
    /** Called when this panel's component is about to being hidden.
     * Right place to detach, remove listeners from data context, that 
     * were added in panelActivated impl.
     *
     * This method is always called in event dispatch thread. 
     */
    public void panelDeactivated ();
    
    
    /** Returns Lookup that will be integrated into Lookup of Navigator UI
     * TopComponent. Allows clients to achieve several things:<p>
     * 
     * 1) Specify activated Node of Navigator UI TopComponent when this panel is active.<br>
     * If returned lookup contains Nodes, they will act as activated nodes of 
     * Navigator UI TopComponent (navigator window).<br>
     * 
     * Method may return null, signalizing that default mechanism should be enabled.
     * Default mechanism chooses first Node from Utilities.actionsGlobalContext()
     * (current system-wide activated Node) as activated Node for Navigator's TopComponent.
     * <p>
     * 
     * 2) Enable support for Toolbar or main menu global actions in NavigatorPanel.<br>
     * ActionMap contained in this Lookup will be part of Utilities.actionsGlobalContext()
     * when Navigator UI TopComponent (navigator window) is active in the system. 
     *   
     * @return Lookup instance or null
     */
    public Lookup getLookup ();

    /**
     * Registers a navigator panel.
     * @since 1.22
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Registration {

        /**
         * MIME type to register under.
         * For example: {@code text/javascript}
         */
        String mimeType();

        /**
         * Optional position for this panel among others of the same type.
         */
        int position() default Integer.MAX_VALUE;

        /**
         * Label for this view to be used in a switcher UI.
         * Will be replaced with {@link NavigatorPanel#getDisplayName}
         * if and when the panel is actually shown.
         * May use {@code pkg.Bundle#key} or {@code #key} syntax.
         */
        String displayName();

    }
    
    /**
     * Used in case multiple registrations are needed in one place.
     * @since 1.22
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Registrations {
        Registration[] value();
    }

    /**
     * Dynamically create panels for files. Register in the global lookup.
     *
     * @since 1.41
     */
    interface DynamicRegistration {

        /**
         * Dynamically create {@code NavigatorPanel}s for the given file.
         *
         * @param file for which the panels should be created
         * @return a collection of {@code NavigatorPanel}s for the given file
         *         null is allowed
         */
        public @CheckForNull Collection<? extends NavigatorPanel> panelsFor(@NonNull URI file);
    }

}
