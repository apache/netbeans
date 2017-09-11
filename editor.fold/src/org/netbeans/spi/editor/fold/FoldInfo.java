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
package org.netbeans.spi.editor.fold;

import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.openide.util.Parameters;

/**
 * The FoldInfo encapsulates the information passed to the
 * {@link FoldOperation#addToHierarchy(org.netbeans.api.editor.fold.FoldType, int, int, org.netbeans.api.editor.fold.FoldTemplate, boolean, java.lang.Object, org.netbeans.spi.editor.fold.FoldHierarchyTransaction).
 * Set of FoldInfos can be then applied to the FoldHieararchy, creating new, and removing obsolete Folds, so that 
 * Folds which prevail remain in the hierarchy. The mandatory information is start and end of the fold, and the {@link FoldType}.
 * If necessary, a {@link FoldTemplate} attached to the FoldType can be overriden for the specific Fold instance. Note though, that
 * if the FoldTemplate instance changes with the next fold update, the Fold may fire change events.
 * <p/>
 * Ultimately, it is possible to hand-override the Fold's description from the FoldTemplate provided value.
 * <p/>
 * If the FoldInfo is used to update an existing Fold, the or the FoldTemplate's properties 
 * collapsed state are  updated to the existing Fold instance. Appropriate fold change event is fired. It is not possible
 * to change the type of the fold.
 * <p/>
 * Initial folding state can be specified, for the case the fold will be created (it does not exist). If unspecified,
 * the infrastructure can assign an appropriate state based on e.g. user preferences. Collapsed state is never changed
 * for existing folds, even though FoldInfo specifies a value.
 * 
 * Use {@link FoldUtilities#update} to perform the process.
 * 
 * @author sdedic
 */
public final class FoldInfo {
    /**
     * Start of the folded region
     */
    private int start;
    
    /**
     * End of the folded region
     */
    private int end;
    
    /**
     * Tooltip contents and guarded areas of the fold. Defaults to the FoldTemplate present in the FoldType.
     */
    private FoldTemplate template;
    
    /**
     * The fold type of the fold
     */
    private FoldType type;
    
    /**
     * Determines whether the fold should be initially collapsed. Value of null means
     * the collapsed state should be computed by the infrastructure. Non-null value will
     * force the fold to expand or collapse.
     */
    private Boolean collapsed;
    
    /**
     * Extra information attached to a Fold
     */
    private Object  extraInfo;
    
    /**
     * Custom description, overriding the default one in the template
     */
    private String  description;
    
    /**
     * Creates a FoldInfo for the specified range.
     * 
     * @param start start offset
     * @param end end offset
     * @param type type of the fold
     * @return FoldInfo instance
     */
    public static FoldInfo range(int start, int end, FoldType type) {
        return new FoldInfo(start, end, type);
    }
    
    private FoldInfo(int start, int end, FoldType ft) {
        Parameters.notNull("ft", ft);
        if (start < 0) {
            throw new IllegalArgumentException("Invalid start offet: " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException("Invalid end offset: " + end + ", start is: " + start);
        }
        this.type = ft;
        this.start = start;
        this.end = end;
        this.template = ft.getTemplate();
        /*
        if ((end - start) < (template.getGuardedStart() + template.getGuardedEnd())) {
            throw new IllegalArgumentException("Invalid fold length: (endOffset=" + end // NOI18N
                + " - startOffset=" + start + ") < " // NOI18N
                + "(startGuardedLength=" + template.getGuardedStart() // NOI18N
                + " + endGuardedLength=" + template.getGuardedEnd() + ")"); // NOI18N
        }
        */
    }
    
    /**
     * Attaches FoldTemplate to the FoldInfo.
     * The instance will be used to configure or update the Fold instance in preference to {@link FoldType#getTemplate}.
     * 
     * @param t fold template
     * @return this instance
     */
    public FoldInfo withTemplate(FoldTemplate t) {
        Parameters.notNull("t", t);
        this.template = t;
        return this;
    }
    
    /**
     * Use to provide a custom description for the fold.
     * The description overrides all other ones taken from FoldTemplates. The description can use
     * content placeholder (see {@link FoldTemplate} for explanation. When {@code null} is set, the
     * description Fold reverts back to the one provided by FoldTemplates (the override is cleared).
     * 
     * @param desc description text.
     * @return this instance
     */
    public FoldInfo withDescription(String desc) {
        this.description = desc;
        return this;
    }
    
    /**
     * Attaches custom extra info to the fold.
     * The extra info will be available from {@link org.netbeans.api.editor.fold.Fold#getExtraInfo.
     * 
     * @param extraInfo custom data
     * @return this instance
     */
    public FoldInfo attach(Object extraInfo) {
        this.extraInfo = extraInfo;
        return this;
    }
    
    /**
     * Returns description override.
     * When {@code null}, information from FoldTemplates should be used.
     * 
     * @return explicit description, or {@code null}.
     */
    public String getDescriptionOverride() {
        return description;
    }
    
    /**
     * Returns the extra information attached to a fold.
     * 
     * @return data, or {@code null} if no data is present
     */
    public Object getExtraInfo() {
        return extraInfo;
    }
    
    /**
     * Records the desired collapsed state.
     * 
     * @param state the desired collapsed state
     * @return this instance.
     */
    public FoldInfo collapsed(boolean state) {
        this.collapsed = state;
        return this;
    }

    /**
     * Provides start offset of the folded content
     * @return offset into the document
     */
    public int getStart() {
        return start;
    }

    /**
     * Provides end offset of the folded content
     * @return offset into the document
     */
    public int getEnd() {
        return end;
    }

    /**
     * Provides FoldTemplate to be used with the Fold.
     * The template will be used in preference to {@link org.netbeans.api.editor.fold.FoldType#getTemplate}. 
     * {@code Null} return value means that the FoldTemplate from the FoldType is in effect.
     
     * @return FoldTemplate instance, or {@code null}
     */
    public FoldTemplate getTemplate() {
        return template;
    }

    /**
     * Provides FoldType for the fold.
     * The FoldType will be assigned to the new fold. If type of a fold (occupying the same range) changes during
     * {@link FoldOperation#update}, the fold will be destroyed and re-created. It is not possible to change FoldType
     * of a Fold.
     * 
     * @return FoldType instance, never {@code null}. 
     */
    public FoldType getType() {
        return type;
    }

    /**
     * Provides the desired collapsed state or {@code null}, if no specific
     * state is required.
     * When {@code null} is reported, the infrastructure can assign an an appropriate initial state to the fold,
     * e.g. based on user preferences. States of existing fold is never changed during update.
     * 
     * @return desired collapsed state or {@code null}
     */
    public Boolean getCollapsed() {
        return collapsed;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FoldInfo[").append(start).append(" - ").append(end).
                append(", ").append(type).append(", desc = ").append(description == null ? template.getDescription() : description).
                append(" collapsed = ").append(collapsed).append("]");
        return sb.toString();
    }
}
