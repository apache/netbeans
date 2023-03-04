/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class TopLevelStructureItem implements StructureItem {

    private final ElementHandle elementHandle;

    public TopLevelStructureItem(String name, FileObject fileObject) {
        this.elementHandle = new TopElementHandle(fileObject, name);
    }

    @Override
    public String getName() {
        return elementHandle.getName();
    }

    @Override
    public String getSortText() {
        return this.elementHandle.getName();
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        return StringEscapeUtils.escapeHtml(this.elementHandle.getName());
    }

    @Override
    public ElementHandle getElementHandle() {
        return elementHandle;
    }

    @Override
    public ElementKind getKind() {
        return elementHandle.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return elementHandle.getModifiers();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public long getPosition() {
        return -1;
    }

    @Override
    public long getEndPosition() {
        return -1;
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    public abstract static class ChildrenSetStructureItem extends TopLevelStructureItem {

        private final Collection<StructureItem> items;

        public ChildrenSetStructureItem(String name, FileObject fileObject, Collection<StructureItem> items) {
            super(name, fileObject);
            this.items = items;
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return new ArrayList<>(items);
        }
    }

    public abstract static class ChildrenListStructureItem extends TopLevelStructureItem {

        private final List<StructureItem> items;

        public ChildrenListStructureItem(String name, FileObject fileObject, List<StructureItem> items) {
            super(name, fileObject);
            this.items = items;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public List<? extends StructureItem> getNestedItems() {
            return new ArrayList<>(items);
        }
    }

    public static class Rules extends ChildrenListStructureItem {

        private final Snapshot snapshot;

        public Rules(List<StructureItem> children, FeatureContext context) {
            super(NbBundle.getMessage(TopLevelStructureItem.class, "Rules"),  //NOI18N
                    context.getSnapshot().getSource().getFileObject(),
                    children);
            this.snapshot = context.getSnapshot();
        }

        //return the element range 0 - source lenght to ensure the recursive
        //leaf node search mechanism in CSL navigator will try to match
        //the rule children
        @Override
        public long getPosition() {
            return 0;
        }

        @Override
        public long getEndPosition() {
            return snapshot.getOriginalOffset(snapshot.getText().length());
        }
    }

    public static class Elements extends ChildrenSetStructureItem {
        public Elements(Collection<StructureItem> items) {
            super(NbBundle.getMessage(TopLevelStructureItem.class, "Elements"), null, items); //NOI18N
        }
    }

    public static class Classes extends ChildrenSetStructureItem {
        public Classes(Collection<StructureItem> children) {
            super(NbBundle.getMessage(TopLevelStructureItem.class, "Classes"), null, children);
        }
    }

    public static class Ids extends ChildrenSetStructureItem {
        public Ids(Collection<StructureItem> children) {
            super(NbBundle.getMessage(TopLevelStructureItem.class, "Ids"), null, children); //NOI18N
        }
    }

    public static class Namespaces extends ChildrenListStructureItem {

        public Namespaces(List<StructureItem> children) {
            super(NbBundle.getMessage(TopLevelStructureItem.class, "Namespaces"), null, children);  //NOI18N
        }

    }

    public static class AtRules extends ChildrenListStructureItem {
        public AtRules(List<StructureItem> children) {
            super(NbBundle.getMessage(TopLevelStructureItem.class, "AtRules"), null, children); //NOI18N
        }
    }

    @NbBundle.Messages("imports=Imports")
    public static class Imports extends ChildrenSetStructureItem {
        public Imports(Collection<StructureItem> children) {
            super(Bundle.imports(), null, children);
        }
    }

    private static class TopElementHandle implements ElementHandle {
        private final FileObject fileObject;
        private final String name;

        public TopElementHandle(FileObject fileObject, String name) {
            this.fileObject = fileObject;
            this.name = name;
        }

        @Override
        public FileObject getFileObject() {
            return fileObject;
        }

        @Override
        public String getMimeType() {
            return CssLanguage.CSS_MIME_TYPE;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE; //xxx fix - add mode categories to csl
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return new OffsetRange(-1, -1);
        }
    }
}
