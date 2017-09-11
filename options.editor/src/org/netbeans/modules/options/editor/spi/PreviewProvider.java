/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options.editor.spi;

import javax.swing.JComponent;

/**
 * This interface should be implemented by all option customizers that wish
 * to provide a preview of changes in the options they maintain.
 *
 * <p>An example is the Editor -> Formatting panel in Tools-Options, which presents
 * customizers for many mime types and these customizers have to share the same
 * preview area. Each of those customizers should provide implementation of this
 * interface in order to supply the contents of the preview area.
 *
 * <p>Typically option customizers are supplied in form of <code>OptionsPanelController</code>,
 * which provides the customizer's UI component and allows to control it. If such
 * a customizer wishes to provide a preview component it should do so by
 * letting its <code>OptionsPanelController</code> implementation to also implement
 * the <code>PreviewProvider</code> interface.
 *
 * @author Vita Stejskal
 */
public interface PreviewProvider {

    /**
     * Gets the component that will be used for previewing changes in the
     * associated options panel.
     *
     * @return The preview component.
     */
    public JComponent getPreviewComponent();

    /**
     * Refreshes the preview component. The implementation should use the current
     * option values to refrfesh the preview component.
     *
     * <p>Normally option panels are responsible for refreshing their preview
     * components whenever the options that they maintain are changed. This method
     * take out this obligation. It is here for the options UI infrastructure
     * to be able to enforece the preview refresh.
     */
    public void refreshPreview();

}
