/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.awt;

/** Provides a UI for <code>Check for Updates<code> feature, for example in <code>About</code> dialog.
 * An instance of the provider should be installed in the default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *
 * @author Jiri Rechtacek
 * @since 7.45
 */
public interface CheckForUpdatesProvider {
    
    /** Opens a wizard for installation of updates if found some available.
     * <b>Note:</b> Call it from AWT queue only.
     * 
     * @param reload if <code>true</code> then reload the content from all enabled Update Centers
     * @return <code>true</code> if all updates were successfully installed, <code>false</code> otherwise.
     */
    public boolean openCheckForUpdatesWizard(boolean reload);
    
    /** Runs a check for updates. If some updates found, shows a notification in the status line.
     * <b>Note:</b> It could be a time-consuming task, it should not be called from an event queue.
     * 
     * @param reload if <code>true</code> then reload the content from all enabled Update Centers
     * @return <code>true</code> if updates are available and users will be notified
     * in the status line, <code>false</code> if no updates found.
     */
    public boolean notifyAvailableUpdates(boolean reload);
    
    /** A description of content of enable Update Centers, assuming returns something like <i>7.1 patch1</i>.
     *  The description might contains HTML tags e.g. HTML Links.
     * 
     * @return free-form description of content or <code>null</code>
     */
    public String getContentDescription();
}
