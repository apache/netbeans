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

package org.netbeans.modules.editor.lib2;

import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.text.JTextComponent;

/** This is provider of implementation. This package (org.netbeans.editor) 
 * represent editor core which can be used independently on the rest of NetBeans.
 * However this core needs access to higher level functionality like access
 * to localized bundles, access to settings storage, etc. which can be implemented
 * differently by the applications which uses this editor core. For this purpose
 * was created this abstract class and it can be extended with any other methods which
 * are more and more often required by core editor. Example implementation
 * of this provider can be found in org.netbeans.modules.editor package
 * 
 * @author David Konecny
 * @since 10/2001
 */
public interface EditorImplementationProvider {

    /** Returns ResourceBundle for the given class.*/
    public ResourceBundle getResourceBundle(String localizer);

    /** This is temporary method which allows core editor to access
     * glyph gutter action. These actions are then used when user clicks
     * on glyph gutter. In next version this should be removed and redesigned
     * as suggested in issue #16762 */
    public Action[] getGlyphGutterActions(JTextComponent target);

    /** Activates the given component or one of its ancestors.
     * @return whether the component or one of its ancestors was succesfuly activated
     * */
    public boolean activateComponent(JTextComponent c);
}
