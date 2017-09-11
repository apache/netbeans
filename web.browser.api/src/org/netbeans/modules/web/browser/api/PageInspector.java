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
package org.netbeans.modules.web.browser.api;

import java.beans.PropertyChangeListener;
import org.openide.util.Lookup;

/**
 * Web-page inspector.
 *
 * @author Jan Stola
 */
public abstract class PageInspector {
    /** Feature ID used by page-inspection when it sends messages through {@code MessageDispatcher}. */
    public static final String MESSAGE_DISPATCHER_FEATURE_ID = "inspect"; // NOI18N
    /** Default {@code PageInspector}. */
    private static PageInspector DEFAULT;
    
    /** Name of the property that is fired when the page model changes. */
    public static final String PROP_MODEL = "model"; // NOI18N

    /**
     * Returns the default {@code PageInspector}. This method can return <code>null</code>
     * if no page inspector is registered. If your module cannot reasonably work without
     * a real inspector, then it should request its presence by putting
     * following into manifest:
     * <pre>
     * OpenIDE-Module-Needs: org.netbeans.modules.web.browser.api.PageInspector
     * </pre>
     * 
     * @return default {@code PageInspector} or <code>null</code>
     */
    public static synchronized PageInspector getDefault() {
        if(DEFAULT == null) {
            DEFAULT = Lookup.getDefault().lookup(PageInspector.class);
        }
        return DEFAULT;
    }

    /**
     * Starts the inspection of the web-page described by the given context.
     * 
     * @param pageContext tools for accessing the data about the web-page
     * (it is usually equal to {@code HtmlBrowser.Impl.getLookup()} of
     * the web-browser pane that displays the web-page).
     */
    public abstract void inspectPage(Lookup pageContext);
    
    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Getter for current page model.
     * @return 
     */
    public abstract Page getPage();
    
}
