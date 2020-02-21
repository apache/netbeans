/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
