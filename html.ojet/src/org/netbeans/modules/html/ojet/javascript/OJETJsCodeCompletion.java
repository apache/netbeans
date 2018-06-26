/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.javascript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.ojet.OJETContext;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.html.ojet.data.DataItem;
import org.netbeans.modules.html.ojet.data.DataItemImpl;
import org.netbeans.modules.html.ojet.data.DataProvider;
import org.netbeans.modules.html.ojet.data.DataProviderImpl;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority = 6)
public class OJETJsCodeCompletion implements CompletionProvider {

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        Document document = ccContext.getParserResult().getSnapshot().getSource().getDocument(true);
        int dOffset = ccContext.getCaretOffset();  // document offset
        ((AbstractDocument) document).readLock();
        OJETContext ojContext = OJETContext.UNKNOWN;
        try {
            ojContext = OJETContext.findContext(document, dOffset);
//            System.out.println("ojContext: " + ojContext);
            List<CompletionProposal> result = new ArrayList<>();
            switch (ojContext) {
                case COMP_CONF_COMP_NAME:
                    Collection<DataItem> components = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getComponents(), ccContext.getPrefix());
                    for (DataItem component : components) {
                        result.add(new OJETCodeCompletionItem.OJETComponentItem(component, ccContext));
                    }
                    break;
                case COMP_CONF:
                    result.add(new OJETCodeCompletionItem.OJETComponentOptionItem(new DataItemImpl("component", null), ccContext)); //NOI18N
                    break;
                case COMP_CONF_PROP_NAME:
                    String compName = OJETContext.findComponentName(document, dOffset);
                    if (compName != null && !compName.isEmpty()) {
                        Collection<DataItem> options = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getComponentOptions(compName), ccContext.getPrefix());
                        for (DataItem option : options) {
                            result.add(new OJETCodeCompletionItem.OJETComponentOptionItem(option, ccContext));
                        }
                        Collection<DataItem> events = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getComponentEvents(compName), ccContext.getPrefix());
                        for (DataItem event : events) {
                            result.add(new OJETCodeCompletionItem.OJETComponentEventItem(event, ccContext));
                        }
                        
                    }
                    break;
                case MODULE_PROP_NAME:
                    Collection<DataItem> options = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getModuleProperties(), ccContext.getPrefix());
                    for (DataItem option : options) {
                        result.add(new OJETCodeCompletionItem.OJETComponentOptionItem(option, ccContext));
                    }
                    break;
            }
            return result;
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element instanceof OJETCodeCompletionItem.DocSimpleElement) {
            return ((OJETCodeCompletionItem.DocSimpleElement) element).getDocumentation();
        }
        return null;
    }

}
