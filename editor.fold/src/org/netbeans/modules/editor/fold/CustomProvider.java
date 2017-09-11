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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/**
 * Fold manager factory provider that allows factories
 * to be registered explicitly.
 * <br>
 * It can be used for standalone editor.
 *
 * @author Miloslav Metelka, Martin Roskanin
 */
public class CustomProvider extends FoldManagerFactoryProvider {

    private static final String FOLDER_NAME = "FoldManager"; //NOI18N
    
    private final Map mime2factoryList = new HashMap();
    
    CustomProvider() {
    }
    
    /**
     * Register the fold manager factory so that it will be used for the code folding
     * for the given mime-type.
     *
     * @param mimeType mime-type for which the factory is being registered.
     * @param factory fold manager factory to be registered.
     *  <br>
     *  The factory added sooner will have higher priority regarding
     *  the fold appearance.
     */
    public void registerFactory(String mimeType, FoldManagerFactory factory) {
        assert (mimeType != null) && (factory != null);

        synchronized (mime2factoryList) {
            List factoryList = (List)mime2factoryList.get(mimeType);
            if (factoryList == null) {
                factoryList = new ArrayList();
                mime2factoryList.put(mimeType, factoryList);
            }
            factoryList.add(factory);
        }
    }
    
    /**
     * Register multiple factories for the given mime-type
     * in the order as they are present in the array.
     */
    public void registerFactories(String mimeType, FoldManagerFactory[] factories) {
        synchronized (mime2factoryList) {
            for (int i = 0; i < factories.length; i++) {
                registerFactory(mimeType, factories[i]);
            }
        }
    }
    
    public void removeAllFactories(String mimeType) {
        synchronized (mime2factoryList) {
            mime2factoryList.put(mimeType, null);
        }
    }

    public void removeAllFactories() {
        synchronized (mime2factoryList) {
            mime2factoryList.clear();
        }
    }

    public List getFactoryList(FoldHierarchy hierarchy) {
        List factoryList = null; // result
        JTextComponent editorComponent = hierarchy.getComponent();
        EditorKit kit = editorComponent.getUI().getEditorKit(editorComponent);
        if (kit != null) {
            String mimeType = kit.getContentType();
            if (mimeType != null) {
                synchronized (mime2factoryList) {
                    factoryList = (List)mime2factoryList.get(mimeType);
                }
            }
        }
        
        if (factoryList == null) {
            return Collections.EMPTY_LIST;
        }
        return factoryList;
    }
    
}
