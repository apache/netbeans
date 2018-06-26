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
package org.netbeans.modules.html.ojet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.ojet.data.DataItem;
import org.netbeans.modules.html.ojet.data.DataProvider;
import org.netbeans.modules.html.ojet.data.DataProviderImpl;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Petr Pisl
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/xhtml", service = HtmlExtension.class)
})
public class OJETHtmlExtension extends HtmlExtension {

    private static String DATA_BINDING = "data-bind";

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        String attribute = context.getAttributeName();
        if (DATA_BINDING.equals(attribute)) {

            Document document = context.getResult().getSnapshot().getSource().getDocument(true);
            int offset = context.getOriginalOffset();
            OJETContext ojContext = OJETContext.findContext(document, offset);
            switch (ojContext) {
                case DATA_BINDING:
                    String prefix = OJETUtils.getPrefix(ojContext, document, offset);
                    Collection<DataItem> data = DataProvider.filterByPrefix(DataProviderImpl.getInstance().getBindingOptions(), prefix);
                    List<CompletionItem> result = new ArrayList();
                    for (DataItem item : data) {
                        result.add(new OJETCompletionHtmlItem(item, OJETUtils.getPrefixOffset(ojContext, document, offset)));
                    }
                    return result;
            }
        }
        return Collections.emptyList();
    }
}
