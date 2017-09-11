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
package org.netbeans.modules.debugger.ui.views;

import java.io.Serializable;
import org.netbeans.spi.debugger.ui.ViewLifecycle.ModelUpdateListener;

/**
 * Additional view for custom model set.
 * 
 * @author Martin Entlicher
 */
public class CustomView extends View {
    
    private transient String icon;
    private transient String displayName;
    private transient String toolTip;
    
    public CustomView(String icon, String name, String helpID, String propertiesHelpID,
                      String displayName, String toolTip) {
        super(icon, name, helpID, propertiesHelpID, null, null);
        this.icon = icon;
        this.displayName = displayName;
        this.toolTip = toolTip;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getToolTipText() {
        return toolTip;
    }
    
    public static ViewModelListener createViewModelService(String name,
                                                           String propertiesHelpID,
                                                           ModelUpdateListener mul) {
        return new ViewModelListener(name, propertiesHelpID, mul);
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper(icon, name, helpID, propertiesHelpID, displayName, toolTip);
    }
     
    /**
     * The serializing class.
     */
    private static final class ResolvableHelper implements Serializable {
        
        private String[] data;
        
        private static final long serialVersionUID = 1L;
        
        ResolvableHelper(String... data) {
            this.data = data;
        }
        
        public ResolvableHelper() {
            // Just for the purpose of deserialization
        }
        
        public Object readResolve() {
            return new CustomView(data[0], data[1], data[2], data[3], data[4], data[5]);
        }
    }
    
}
