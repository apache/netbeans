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
package org.netbeans.modules.java.debug;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
@MimeRegistration(mimeType="text/x-java", service=HighlightsLayerFactory.class)
public class HighlightsLayerFactoryImpl implements HighlightsLayerFactory {

    private static final boolean DEBUG_MODE;
    
    static {
        boolean value = false;
        
        if (Boolean.getBoolean("org.netbeans.modules.java.debug.enable")) {
            value = true;
        } else {
            assert value = true;
        }
        
        DEBUG_MODE = value;
    }
    
    public HighlightsLayer[] createLayers(Context context) {
        if (!DEBUG_MODE) {
            return new HighlightsLayer[0];
        }
        
        return new HighlightsLayer[] {
            HighlightsLayer.create(HighlightsLayerFactoryImpl.class.getName(), ZOrder.DEFAULT_RACK, true, TreeNavigatorProviderImpl.getBag(context.getDocument()))
        };
    }

    public static Object createNavigatorPanel(FileObject f) {
        if (!DEBUG_MODE) {
            return new Object(); //fake answer
        }
        
        return createImpl(f);
    }
    
    private static Object createImpl(FileObject f) {
        if ("org-netbeans-modules-java-debug-TreeNavigatorProviderImpl".equals(f.getName())) {
            return new TreeNavigatorProviderImpl();
        }
        
        if ("org-netbeans-modules-java-debug-ElementNavigatorProviderImpl".equals(f.getName())) {
            return new ElementNavigatorProviderImpl();
        }
        
        if ("org-netbeans-modules-java-debug-ErrorNavigatorProviderImpl".equals(f.getName())) {
            return new ErrorNavigatorProviderImpl();
        }
        
        if ("org-netbeans-modules-java-debug-ClasspathNavigatorProviderImpl".equals(f.getName())) {
            return new ClasspathNavigatorProviderImpl();
        }

        //unknown:
        return new Object();
    }
    
}
