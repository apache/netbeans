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

package org.netbeans.modules.editor.structure;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.structure.spi.DocumentModelProvider;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * Document model factory that obtains the DocumentModel instancies
 * by reading the xml layer.
 * <br>
 * The registration are read from the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/DocumentModel
 * </pre>
 *
 * @author Marek Fukala
 */
public class DocumentModelProviderFactory {
    
    public static final String FOLDER_NAME = "DocumentModel"; //NOI18N
    
    private Map<String, DocumentModelProvider> mime2provider;
    
    private static DocumentModelProviderFactory defaultProvider = null;
    
    public static DocumentModelProviderFactory getDefault() {
        if(defaultProvider == null) {
            defaultProvider = new DocumentModelProviderFactory();
        }
        return defaultProvider;
    }
    
    private DocumentModelProviderFactory() {
        mime2provider = new WeakHashMap<String, DocumentModelProvider>();
    }
    
    /* returns a DocumentModelFactory according to the layer */
    public DocumentModelProvider getDocumentModelProvider(String mimeType) {
        DocumentModelProvider provider = null; // result
        if(mimeType != null) {
            provider = mime2provider.get(mimeType);
            if (provider == null) { // not cached yet
                Lookup mimeLookup = MimeLookup.getLookup(MimePath.get(mimeType));
                Collection<? extends DocumentModelProvider> providers = 
                        mimeLookup.lookup(new Lookup.Template<DocumentModelProvider>(DocumentModelProvider.class)).allInstances();
                if(providers.size() > 1)
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Only one DocumentModelProvider can be registered for one mimetype!");
                
                if(providers.size() == 0)
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("There isn't any DocumentModelProvider registered for " + mimeType + " mimetype!"));
                
                provider = providers.size() > 0 ? (DocumentModelProvider)providers.iterator().next() : null;
                mime2provider.put(mimeType, provider);
                
            } else return provider;
        } else
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("mimeType cannot be null!"));
        
        return provider;
    }
    
    
}
