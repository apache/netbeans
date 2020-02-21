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
package org.netbeans.modules.cnd.modelutil;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 */
public final class CsmFontColorManager {

    private final Map<String, FontColorProviderImpl> providers = new HashMap<String, FontColorProviderImpl>();

    public void addListener(String mimeType, FontColorChangeListener listener) {
        getCreateProvider(mimeType).addListener(listener);
    }
    
    /* package */ Color getColor(FontColorProvider.Entity color) {
        // completion is not aware of document type, use C++
        AttributeSet as = getCreateProvider(MIMENames.CPLUSPLUS_MIME_TYPE).getColor(color);
        return isUnitTestsMode ? Color.red : (Color)as.getAttribute(StyleConstants.ColorConstants.Foreground);
    }

    public AttributeSet getColorAttributes(String mimeType, FontColorProvider.Entity name) {
        // completion is not aware of document type
        AttributeSet as = getCreateProvider(mimeType).getColor(name);
        return as;
    }
    
    private FontColorProviderImpl getCreateProvider(String mimeType) {
        synchronized (providers) {
            FontColorProviderImpl fcp = providers.get(mimeType);
            if (fcp == null) {
                fcp = new FontColorProviderImpl(mimeType);
                providers.put(mimeType, fcp);
            }
            return fcp;
        }
    }
    
    public FontColorProvider.Entity getSemanticEntityByAttributeSet(String mimeType, AttributeSet set) {
        for (FontColorProvider.Entity entity : FontColorProvider.Entity.values()) {
            if (getColorAttributes(mimeType, entity).equals(set)) {
                return entity;
            }
        }
        return null;
    }

    public static CsmFontColorManager instance() {
        return Instantiator.instance;
    }

    private static class Instantiator {

        public final static CsmFontColorManager instance = new CsmFontColorManager();
    }

    private final boolean isUnitTestsMode;
    
    private CsmFontColorManager() {
        isUnitTestsMode = CndUtils.isUnitTestMode();
    }

    public interface FontColorChangeListener extends EventListener {

        void stateChanged(FontColorProvider fcp);
    }

    private static class FontColorProviderImpl implements FontColorProvider, LookupListener {

        private final String mimeType;
        private final List<WeakReference<FontColorChangeListener>> listeners = new ArrayList<WeakReference<FontColorChangeListener>>();
        private FontColorSettings fcs;
        private final Object lock = new Object();
        private final Lookup.Result<FontColorSettings> result;

        public FontColorProviderImpl(String mimeType) {
            this.mimeType = mimeType;
            Lookup lookup = MimeLookup.getLookup(MimePath.get(mimeType));
            result = lookup.lookupResult(FontColorSettings.class);
            fcs = result.allInstances().iterator().next();
            result.addLookupListener(this);
        }

        public void addListener(FontColorChangeListener listener) {
            synchronized (listeners) {
                listeners.add(new WeakReference<FontColorChangeListener>(listener));
            }
            listener.stateChanged(this);
        }

        @Override
        public AttributeSet getColor(Entity color) {
            synchronized(lock) {
                final AttributeSet tokenFontColors = fcs.getTokenFontColors(color.getResourceName());
                assert tokenFontColors != null : "There is no color for "+color.getResourceName();
                return tokenFontColors;
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            synchronized(lock) {
                fcs = result.allInstances().iterator().next();
            }
            synchronized (listeners) {
                for (ListIterator<WeakReference<FontColorChangeListener>> it = listeners.listIterator(); it.hasNext();) {
                    WeakReference<FontColorChangeListener> wrcl = it.next();
                    FontColorChangeListener cl = wrcl.get();
                    if (cl != null) {
                        cl.stateChanged(this);
                    } else {
                        it.remove();
                    }
                }
            }
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }
}
