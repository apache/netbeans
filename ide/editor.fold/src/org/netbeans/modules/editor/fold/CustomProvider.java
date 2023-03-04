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
