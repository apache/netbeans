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

package org.netbeans.modules.cnd.mixeddev.java;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class AbstractJavaToCppHyperlinkProvider implements HyperlinkProviderExt {    
    
    private static final String JAVA_HYPERLINK_PROVIDER = "JavaHyperlinkProvider"; // NOI18N
    
    private static HyperlinkProviderExt delegate;        
    
    
    protected abstract String[] getCppNames(Document doc, int offset);
    
    protected abstract boolean navigate(Document doc, int offset);    
    
    
    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        HyperlinkProviderExt defaultProvider = getDelegate();
        if (defaultProvider != null) {
            return defaultProvider.isHyperlinkPoint(doc, offset, type);
        }
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        HyperlinkProviderExt defaultProvider = getDelegate();
        if (defaultProvider != null) {
            return defaultProvider.getHyperlinkSpan(doc, offset, type);
        }
        return JavaContextSupport.getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(final Document doc, final int offset, final HyperlinkType type) {
        final AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                if (!navigate(doc, offset)) {
                    HyperlinkProviderExt defaultProvider = getDelegate();
                    if (defaultProvider != null) {
                        defaultProvider.performClickAction(doc, offset, type);
                    }
                }
            }
        }, NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.go_to_declaration"), cancel, false);  // NOI18N
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        String cppNames[] = getCppNames(doc, offset);
        if (cppNames != null && cppNames.length > 0) {
            String msg = NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.search_for_msg"); // NOI18N
            StringBuilder sb = new StringBuilder();
            if (cppNames.length > 1) {
                for (String cppName : cppNames) {
                    sb.append("<br>"); // NOI18N
                    sb.append(cppName);
                }
            } else {
                sb.append(cppNames[0]);
            }
            return MessageFormat.format(msg, new Object[]{sb.toString()});
        }
        HyperlinkProviderExt defaultProvider = getDelegate();
        if (defaultProvider != null) {
            return defaultProvider.getTooltipText(doc, offset, type);
        }
        return NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.cannot_navigate_msg"); // NOI18N
    }    
    
    
    private synchronized HyperlinkProviderExt getDelegate() {
        if (delegate == null) {
            MimePath mimePath = MimePath.parse("text/x-java");  // NOI18N
            Collection<? extends HyperlinkProviderExt> providers = MimeLookup.getLookup(mimePath).lookupAll(HyperlinkProviderExt.class);
            for(HyperlinkProviderExt provider : providers) {
                if (provider.getClass().getName().endsWith(JAVA_HYPERLINK_PROVIDER)) {
                    delegate = provider;
                    break;
                }
            }
        }
        return delegate;
    }        
}
