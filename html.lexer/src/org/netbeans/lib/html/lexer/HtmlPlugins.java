/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.lib.html.lexer;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author marekfukala
 */
public class HtmlPlugins {
    
    private static HtmlPlugins DEFAULT;

    public static synchronized HtmlPlugins getDefault() {
        if(DEFAULT == null) {
            DEFAULT = new HtmlPlugins();
        }
        return DEFAULT;
    }
    
    private Lookup.Result<HtmlLexerPlugin> lookupResult;
    private Collection<? extends HtmlLexerPlugin> plugins;
    private String[][] data;
    
    private HtmlPlugins() {
        Lookup lookup = MimeLookup.getLookup("text/html");
        lookupResult = lookup.lookupResult(HtmlLexerPlugin.class);
        lookupResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                refresh();
            }
        });
        
        refresh();
    }
    
    private void refresh() {
        Collection<? extends HtmlLexerPlugin> allInstances = lookupResult.allInstances();
        plugins = allInstances;
        data = new String[3][allInstances.size()];
        int idx = 0;
        for(HtmlLexerPlugin fact : allInstances) {
            data[0][idx] = fact.getOpenDelimiter();
            data[1][idx] = fact.getCloseDelimiter();
            data[2][idx] = fact.getContentMimeType();
            idx++;
        }
    }
    
    public String[] getOpenDelimiters() {
        return data[0];
    }
    
    public String[] getCloseDelimiters() {
        return data[1];
    }
    
    public String[] getMimeTypes() {
        return data[2];
    }
    
    public String createAttributeEmbedding(String elementName, String attributeName) {
        for (HtmlLexerPlugin plugin : plugins) {
            String embeddingMimeType = plugin.createAttributeEmbedding(elementName, attributeName);
            if(embeddingMimeType != null) {
                return embeddingMimeType;
            }
        }
        return null;
    }
}
