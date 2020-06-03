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
