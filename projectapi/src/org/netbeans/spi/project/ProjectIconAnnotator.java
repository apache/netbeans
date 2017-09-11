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
