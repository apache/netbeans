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

package org.netbeans.modules.editor.fold;

import java.util.List;
import org.netbeans.api.editor.fold.FoldHierarchy;

/**
 * Provides list of fold factories that produce fold managers
 * for the given fold hierarchy.
 *
 * <p>
 * The default implementation <code>NbFoldManagerFactoryProvider</code>
 * in fact first obtains a mime-type by using
 * <code>hierarchySpi.getComponent().getEditorKit().getContentType()</code>
 * and then inspects the contents of the following folder in the system FS:<pre>
 *     Editors/<mime-type>/FoldManager
 * </pre>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class FoldManagerFactoryProvider {
    
    private static FoldManagerFactoryProvider defaultProvider;
    
    private static FoldManagerFactoryProvider emptyProvider;
    
    private static boolean forceCustom;
    
    /**
     * Get the default provider used to produce the managers.
     * <br>
     * This method gets called by <code>FoldHierarchyExecution</code>
     * when rebuilding the managers.
     */
    public static synchronized FoldManagerFactoryProvider getDefault() {
        if (defaultProvider == null) {
            defaultProvider = findDefault();
        }
        
        return defaultProvider;
    }
    
    /**
     * Return provider that provides empty list of factories.
     * <br>
     * This method may be used e.g. by <code>FoldHierarchyExecution</code>
     * if the code folding is disabled in editor options.
     */
    public static FoldManagerFactoryProvider getEmpty() {
        if (emptyProvider == null) {
            // Multiple EmptyProvider can be created as method is not synced
            // but should be no harm
            emptyProvider = new EmptyProvider();
        }
        return emptyProvider;
    }
    
    /**
     * This method enforces the use of custom provider
     * instead of the default layer-based provider.
     * <br>
     * It can be used e.g. for testing purposes.
     *
     * @param forceCustomProvider whether the instance
     *  of the {@link CustomProvider} should be used forcibly.
     */
    public static synchronized void setForceCustomProvider(boolean forceCustomProvider) {
        if (!forceCustom) {
            defaultProvider = null;
        }
        forceCustom = forceCustomProvider;
    }
    
    private static FoldManagerFactoryProvider findDefault() {
        FoldManagerFactoryProvider provider = null;

        // By default use layer-based fold manager factory registrations.
        // In case of standalone editor the custom provider
        // will be used allowing custom fold manager factories registrations
        // (public packages restrictions should not apply).
        if (!forceCustom) {
            try {
                provider = new LayerProvider();
            } catch (Throwable t) {
                // FileObject class not found -> use layer
            }
        }

        if (provider == null) {
            provider = new CustomProvider();
        }
        
        return provider;
    }
    
    /**
     * Get fold managers appropriate for the given fold hierarchy.
     *
     * @param hierarchy fold hierarchy for which the fold managers
     *  are being created.
     * @return list of <code>FoldManagerFactory</code>s to be used
     *  for the given hierarchy.
     *  <br>
     *  The order of the factories in the returned list defines
     *  priority of the folds produced by the corresponding manager
     *  (manager produced by the factory being first in the list
     *  produces the most important folds).
     *  <br>
     *  The list must not be modified by the clients.
     */
    public abstract List getFactoryList(FoldHierarchy hierarchy);

    
    /**
     * Provider giving empty list of factories.
     */
    private static final class EmptyProvider extends FoldManagerFactoryProvider {
        
        public List getFactoryList(FoldHierarchy hierarchy) {
            return java.util.Collections.EMPTY_LIST;
        }
        
    }
    
}
