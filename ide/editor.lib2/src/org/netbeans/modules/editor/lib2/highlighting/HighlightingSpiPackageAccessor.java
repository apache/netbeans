/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author vita
 */
public abstract class HighlightingSpiPackageAccessor {
    
    private static HighlightingSpiPackageAccessor ACCESSOR = null;
    
    public static synchronized void register(HighlightingSpiPackageAccessor accessor) {
        assert ACCESSOR == null : "Can't register two package accessors!";
        ACCESSOR = accessor;
    }
    
    public static synchronized HighlightingSpiPackageAccessor get() {
        // Trying to wake up HighlightsLayer ...
        try {
            Class<?> clazz = Class.forName(HighlightsLayer.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        assert ACCESSOR != null : "There is no package accessor available!";
        return ACCESSOR;
    }
    
    /** Creates a new instance of HighlightingSpiPackageAccessor */
    protected HighlightingSpiPackageAccessor() {
    }
    
    public abstract HighlightsLayerFactory.Context createFactoryContext(Document document, JTextComponent component);
    
    public abstract List<? extends HighlightsLayer> sort(Collection<? extends HighlightsLayer> layers) throws TopologicalSortException;
    
    public abstract HighlightsLayerAccessor getHighlightsLayerAccessor(HighlightsLayer layer);
    
    public abstract int getZOrderRack(ZOrder zOrder);
}
