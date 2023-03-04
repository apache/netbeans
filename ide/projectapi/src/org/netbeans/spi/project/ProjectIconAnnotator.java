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

package org.netbeans.spi.project;

import java.awt.Image;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;

/**
 * Permits third parties to alter a project's icon.
 * You can use it for example to add a badge to the project's icon.
 * Implementations should be registered in global lookup. Example:
 * <pre>
 * &#64;ServiceProvider(service=ProjectIconAnnotator.class)
 * public class SampleProjectIconAnnotator implements ProjectIconAnnotator {
 *     private final ChangeSupport pcs = new ChangeSupport(this);
 *     private boolean enabled;
 *     public &#64;Override Image annotateIcon(Project p, Image orig, boolean openedNode) {
 *         return enabled ?
 *             ImageUtilities.mergeImages(ImageUtilities.addToolTipToImage(orig, "Annotated!"),
 *                                        ImageUtilities.loadImage(".../badge.png"), 16, 0) :
 *             original;
 *     }
 *     public &#64;Override void addChangeListener(ChangeListener listener) {
 *         pcs.addChangeListener(listener);
 *     }
 *     public &#64;Override void removeChangeListener(ChangeListener listener) {
 *         pcs.removeChangeListener(listener);
 *     }
 *     void setEnabled(boolean enabled) {
 *         this.enabled = enabled;
 *         pcs.fireChange();
 *     }
 * }
 * </pre>
 * @author petrdvorak
 * @since 1.33
 * @see ProjectInformation#getIcon
 * @see <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/LogicalViewProvider.html#createLogicalView()"><code>LogicalViewProvider.createLogicalView</code></a>
 */
public interface ProjectIconAnnotator {

    /**
     * Makes any changes to a project's icon.
     * Return {@code original} if you do not intend to change the original icon at this time.
     * @param p project whose icon is to be annotated
     * @param original the original icon (might have been processed by an earlier annotator)
     * @param openedNode true for an expanded node; false for a collapsed node or icon used in another way
     * @return annotated project icon
     */
    Image annotateIcon(Project p, Image original, boolean openedNode);

    /**
     * Adds a listener to changes in badging.
     * @param listener a listener to add
     */
    void addChangeListener(ChangeListener listener);


    /**
     * Removes a listener to changes in badging.
     * @param listener a listener to remove
     */
    void removeChangeListener(ChangeListener listener);

}
