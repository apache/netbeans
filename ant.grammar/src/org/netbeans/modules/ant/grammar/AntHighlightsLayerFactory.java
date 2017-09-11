/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ant.grammar;

import java.awt.Color;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * Highlights Ant property values.
 */
@MimeRegistration(service=HighlightsLayerFactory.class, mimeType="text/x-ant+xml")
public class AntHighlightsLayerFactory implements HighlightsLayerFactory {

    public @Override HighlightsLayer[] createLayers(final Context context) {
        AttributeSet _attrs = MimeLookup.getLookup("text/x-jsp").lookup(FontColorSettings.class).getTokenFontColors("expression-language"); // NOI18N
        final AttributeSet attrs;
        if (_attrs == null) {
            // Fallback from web.core.syntax/src/org/netbeans/modules/web/core/syntax/resources/fontsColors.xml:
            SimpleAttributeSet _sattrs = new SimpleAttributeSet();
            _sattrs.addAttribute(StyleConstants.Background, new Color(0xe3f2e1));
            attrs = _sattrs;
        } else {
            attrs = _attrs;
        }
        return new HighlightsLayer[] {HighlightsLayer.create("ant", ZOrder.SYNTAX_RACK.forPosition(10), true,
                new AntHighlightsContainer((AbstractDocument) context.getDocument(), attrs))}; // NOI18N
    }

}
