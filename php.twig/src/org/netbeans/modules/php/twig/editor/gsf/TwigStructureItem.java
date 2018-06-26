/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sebastian
 */
public class TwigStructureItem implements StructureItem {
    private static final String BLOCK = "Block";  //NOI18N
    List<TwigStructureItem> blocks;
    TwigParserResult.Block item;
    Snapshot snapshot;

    public TwigStructureItem(Snapshot snapshot, TwigParserResult.Block item, List<TwigParserResult.Block> blocks) {
        this.item = item;
        this.blocks = new ArrayList<>();
        this.snapshot = snapshot;
        for (TwigParserResult.Block current : blocks) {
            if (item.getOffset() < current.getOffset()
                    && current.getOffset() + current.getLength() < item.getOffset() + item.getLength()) {
                this.blocks.add(new TwigStructureItem(snapshot, current, blocks));
            }
        }
    }

    @Override
    public String getName() {
        return BLOCK + " " + item.getExtra();
    }

    @Override
    public String getSortText() {
        return BLOCK + " " + item.getDescription();
    }

    @Override
    public String getHtml(HtmlFormatter hf) {
        return BLOCK + " " + item.getExtra(); //NOI18N
    }

    @Override
    public ElementHandle getElementHandle() {
        return new TwigElementHandle(item, snapshot);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (CharSequenceUtilities.startsWith(item.getDescription(), "*")) {
            return Collections.singleton(Modifier.STATIC);
        }
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return blocks.isEmpty();
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return blocks;
    }

    @Override
    public long getPosition() {
        return item.getOffset();
    }

    @Override
    public long getEndPosition() {
        return item.getOffset() + item.getLength();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    static class TwigElementHandle implements ElementHandle {

        TwigParserResult.Block item;
        Snapshot snapshot;

        public TwigElementHandle(TwigParserResult.Block item, Snapshot snapshot) {
            this.item = item;
            this.snapshot = snapshot;
        }

        @Override
        public FileObject getFileObject() {
            return snapshot.getSource().getFileObject();
        }

        @Override
        public String getMimeType() {
            return TwigLanguage.TWIG_MIME_TYPE;
        }

        @Override
        public String getName() {
            return BLOCK + " " + item.getExtra();
        }

        @Override
        public String getIn() {
            return BLOCK + " " + item.getExtra();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.ATTRIBUTE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            if (CharSequenceUtilities.startsWith(item.getDescription(), "*")) {
                return Collections.singleton(Modifier.STATIC);
            }
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle eh) {
            if (!(eh instanceof TwigElementHandle)) {
                return false;
            }
            if (eh.getName().equals(this.getName())) {
                return true;
            }
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult pr) {
            return new OffsetRange(item.getOffset(), item.getOffset() + item.getLength());
        }
    }
}
