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

package org.netbeans.modules.cnd.makefile.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import org.netbeans.modules.cnd.api.makefile.MakefileElement;
import org.netbeans.modules.cnd.api.makefile.MakefileRule;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.ImageUtilities;

/**
 *
 */
public final class MakefileStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult parseResult) {
        if (parseResult instanceof MakefileParseResult) {
            MakefileParseResult makefileParseResult = (MakefileParseResult) parseResult;
            List<MakefileTargetItem> list = new ArrayList<MakefileTargetItem>();
            for (MakefileElement element : makefileParseResult.getElements()) {
                if (element.getKind() == MakefileElement.Kind.RULE) {
                    MakefileRule rule = (MakefileRule) element;
                    for (String target : rule.getTargets()) {
                        list.add(new MakefileTargetItem(target, rule));
                    }
                }
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        return Collections.emptyMap();
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }


    private static class MakefileTargetItem implements StructureItem {

        private static final ImageIcon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/script/resources/TargetIcon.gif", false); // NOI18N
        private final String target;
        private final MakefileRule rule;

        public MakefileTargetItem(String target, MakefileRule rule) {
            this.target = target;
            this.rule = rule;
        }

        @Override
        public String getName() {
            return target;
        }

        @Override
        public String getSortText() {
            return target;
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            final boolean bold = MakefileUtils.isPreferredTarget(target);
            final boolean shaded = !MakefileUtils.isRunnableTarget(target);
            if (bold) {
                formatter.emphasis(true);
            }
            if (shaded) {
                formatter.appendHtml("<font color=\""); // NOI18N
                formatter.appendHtml("!textInactiveText"); // NOI18N
                formatter.appendHtml("\">"); // NOI18N
            }
            formatter.appendText(target);
            if (shaded) {
                formatter.appendHtml("</font>"); // NOI18N
            }
            if (bold) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }

        @Override
        public ElementHandle getElementHandle() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return Collections.emptyList();
        }

        @Override
        public long getPosition() {
            return rule.getStartOffset();
        }

        @Override
        public long getEndPosition() {
            return rule.getEndOffset();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return ICON;
        }
    }
}
