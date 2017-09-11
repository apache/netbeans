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
package org.netbeans.modules.css.visual.spi;

import java.util.Collection;
import javax.swing.JComponent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Representation of the CssStyles window named panel.
 * 
 * Instance to be registered in global lookup.
 *
 * @author marekfukala
 */
public interface CssStylesPanelProvider {
    
    /**
     * The implementation decides whether it wants to include its UI for the given file.
     * 
     * @param file context file
     * @return true if the panel should be included in the CSS Styles window UI,
     * false otherwise.
     */
    public boolean providesContentFor(FileObject file);
    
    /**
     * Gets an unique system id for the panel. 
     * 
     * Not presented in UI.
     */
    public String getPanelID();

    /**
     * Gets a display name which is show in the toolbar.
     */
    public String getPanelDisplayName();
    
    /**
     * Gets the content component.
     * 
     * Called just once per IDE session when the panel content is about to be 
     * shown in the UI for the first time.
     * 
     * The implementor should listen on the lookup content and respond according upon changes.
     * An instance of {@link FileObject} is updated in the lookup as the edited file changes.
     * 
     * @param lookup instance of {@link Lookup} with some context object. 
     */
    public JComponent getContent(Lookup lookup);
    
    /**
     * Content of the obtained lookup will be added to the Css Styles window TC's lookup.
     */
    public Lookup getLookup();
    
    /**
     * Called after the component is made active (visible).
     */
    public void activated();
    
    /**
     * Called after the component is made inactive (invisible).
     */
    public void deactivated();
    
}
