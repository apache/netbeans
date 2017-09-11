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

package org.netbeans.api.editor.fold;

import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

import static org.netbeans.api.editor.fold.Bundle.*;

/**
 * Each {@link Fold} is identified by its fold type.
 * <br>
 * Each fold type presents a fold type acceptor as well
 * accepting just itself.
 *
 * <p/>
 * <strike>
 * As the <code>equals()</code> method is declared final
 * (delegating to <code>super</code>) the fold types
 * can directly be compared by using <code>==</code>
 * operator.
 * </strike>
  * <p/>
 * FoldTypes are MIME-type specific. Sets of FoldType, which apply
 * to a certain MIME type are collected in a {@link FoldType.Domain},
 * which can be obtained by {@link FoldUtilities#getFoldTypes}. FoldTypes for a MIME type are
 * collected with the help of {@link org.netbeans.spi.editor.fold.FoldTypeProvider}.
 * <p/>
 * Several generalized types of folds are defined in this class as constants. 
 * When creating custom FoldTypes, please check carefully whether the new FoldType is not, 
 * in fact, one of these general ones,
 * or is not a specialization of it. If a general action (e.g. auto-collapse all folds XXX) would
 * make sense for the new fold, consider to {@link #derive} it from an existing one. 
 * <p/>
 * Each FoldType has a specific FoldTemplate instance that controls how it is presented. If a
 * new Fold has same semantics as an existing one, but a different {@link FoldTemplate} is needed,
 * use {@link #override} to create a new FoldType with appropriate properties. FoldTypes
 * defined here can be <b>reused</b> in individual MIME types, if their configuration is appropriate.
 * In general, FoldTypes from more general MIME types (e.g. text/xml) can be reused in specialized ones
 * (e.g. text/x-ant+xml).
 * <p/>
 * FoldTypes can form a hierarchy of generalization. For example java Method fold can have a more general
 * 'parent', {@link #MEMBER}. 
 * <p/>
 * The generalization is designed to allow several kinds of the same concept operated separately at the
 * level of the language, but managed at once using general options or operations. 
 * If no settings are specified for Method folding, {@link #MEMBER} settings are
 * applied. Likewise, a general-purpose folding operation can work on {@link #MEMBER} fold type,
 * which will affect both FUNCTION in PHP or METHOD in java (and other fold types in other languages).
 * <p/>
 * Because of this, please be careful
 * to use {@link #isKindOf} to check whether a FoldType is of a specific type. This method respects the
 * generalization. == can be still used to check for exact type match. When using ==, javadoc comments
 * may be evaluated as different from general 'documentation' and yet different from PHP docs. Comparison using 
 * == are safe only when applied on FoldType instances defined for the same MIME type
* <p/>
 *
 * @author sdedic
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldType {
    /**
     * A generic code block. Note that other categories are defined that represent
     * method, class, member or nested symbol levels
     * @since 1.35
     */
    @NbBundle.Messages({
        "FT_Label_code-block=Code Blocks",
        "FT_display_code-block={...}",
        "FT_display_default=..."
    })
    public static final FoldType    CODE_BLOCK = create("code-block", FT_Label_code_block(), 
            new FoldTemplate(1, 1, FT_display_code_block()));

    /**
     * Documentation comment, as defined by the language
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_javadoc=Documentation")
    public static final FoldType    DOCUMENTATION = create("documentation", FT_Label_javadoc(), 
            FoldTemplate.DEFAULT);
    
    /**
     * Unspecified type of comment
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_comment=Comments")
    public static final FoldType    COMMENT = create("comment", FT_Label_comment(), 
            FoldTemplate.DEFAULT);
    
    /**
     * Initial file-level comment. Either documentation comment or comment containing
     * copyright. Defaults to 'comment'
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_initial-comment=Initial Comment")
    public static final FoldType    INITIAL_COMMENT = create("initial-comment", FT_Label_initial_comment(),
            FoldTemplate.DEFAULT);

    /**
     * Tag in markup languages
     * @since 1.35
     */
    @NbBundle.Messages({
        "FT_Label_tag=Tags and Markup",
        "FT_display_tag=<.../>"
    })
    public static final FoldType    TAG = create("tags", FT_Label_tag(), 
            new FoldTemplate(1, 1, FT_display_tag()));
    
    /**
     * Nested part; for example an embedded type, or nested namespace. Do not use
     * this category for top-level symbols. This type is different from a 'member',
     * as type may have many member types, nested types form worlds of their own
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_inner-class=Nested Blocks")
    public static final FoldType    NESTED = create("nested", FT_Label_inner_class(), 
            FoldTemplate.DEFAULT);
    
    /**
     * Member of a symbol (type, class, object)
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_member=Member")
    public static final FoldType    MEMBER = create("member", FT_Label_member(), 
            FoldTemplate.DEFAULT);

    /**
     * Various import, includes and references to sibling files.
     * 
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_import=Imports and Includes")
    public static final FoldType    IMPORT = create("import", FT_Label_import(), 
            FoldTemplate.DEFAULT);
    
    /**
     * User-defined fold, recoded using a special comment
     * @since 1.35
     */
    @NbBundle.Messages("FT_Label_user-defined=User defined")
    public static final FoldType    USER = create("user", FT_Label_user_defined(), null);

    /**
     * Construct fold type with the given description.
     *
     * @param description textual description of the fold type.
     *  Two fold types with the same description are not equal.
     * @deprecated Use the {link create} static method.
     */
    @Deprecated
    public FoldType(String description) {
        this(description, null, null, null);
    }
    
    private FoldType(String code, String label, FoldTemplate template, FoldType parent) {
        this.code = code;
        this.parent = parent;
        this.label = label;
        this.template = template != null ? template : FoldTemplate.DEFAULT;
    }

    /**
     * Creates an instance of FoldType.
     * The code, label and template parameters will be assigned to the FoldType's properties.
     * 
     * @param code code used to form, e.g. persistent keys for info related to the FoldType. Cannot be {@code null}.
     * @param label human-readable label that identifies the FoldType. Must not be {@code null}.
     * @param template describes how the fold is presented. If {@code null}, {@link FoldTemplate#DEFAULT} will be used.
     * @return FoldType instance
     * @since 1.35
     */
    public static FoldType create(String code, String label, FoldTemplate template) {
        Parameters.notWhitespace("code", code);
        Parameters.notWhitespace("label", label);
        return new FoldType(code, label, template, null);
    }
    
    /**
     * Creates a FoldType, overriding its appearance.
     * The new FoldType will use the same code, but label and/or template can be changed.
     * 
     * @param label human-readable label that describes the FoldType. If {@code null}, the original label will be used.
     * @param template human-readable label that describes the FoldType. If {@code null}, the original template will be used.
     * @return an instance of FoldType initialized according to parameters
     * @since 1.35
     */
    public FoldType override(String label, FoldTemplate template) {
        return derive(code(), label, template);
    }
    
    /**
     * Derives a FoldType which acts as a child of this instance.
     * The FoldType will be treated as a special case of this instance. If A is the returned
     * instance and B is this instance, then A.isKindOf(B) will return true.
     * <p/>
     * 
     * @param code new code for the FoldType
     * @param label human-readable label that describes the FoldType. If {@code null}, the original label will be used.
     * @param template human-readable label that describes the FoldType. If {@code null}, the original template will be used.
     * @return an instance of child FoldType
     * @since 1.35
     */
    public FoldType derive(String code, String label, FoldTemplate template) {
        Parameters.notWhitespace("code", code);
        if (template == null) {
            template = this.template;
        }
        return new FoldType(code, label, template, this);
    }
    
    /**
     * @param type
     * @return
     * @deprecated Use {@link #isKindOf} 
     */
    @Deprecated
    public boolean accepts(FoldType type) {
        return isKindOf(type);
    }
    
    @Override
    public String toString() {
        return code();
    }
    
    /**
     * Provides human-readable label for the type
     * @return 
     * @since 1.35
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Provides FoldTemplate instance for this type.
     * The FoldTemplate is the default template used when creating a Fold. Specific FoldTemplate
     * may be provided when creating the Fold instance.
     * 
     * @return fold template for the type.
     * @since 1.35
     */
    public FoldTemplate getTemplate() {
        return template;
    }
    
    /**
     * Checks whether the fold can act as the 'other' type.
     * This check respect parent relationship (see {@link #parent}). The method returns true,
     * if semantics, operations, settings,... applicable to 'other' could be also applied on
     * this FoldType.
     * 
     * @param other the type to check
     * @return true, if this FoldType should be affected
     * @since 1.35
     */
    public boolean isKindOf(FoldType other) {
        return other == this || parent != null && parent.isKindOf(other);
    }
    
    /**
     * Provides parent of this FoldType. Returns {@code null}, if no parent is defined.
     * The parent is a FoldType, which is more general and could be used as a fallback when no information
     * is provided/available on this FoldType instance.
     * 
     * @return parent instance or {@code null}
     * @since 1.35
     */
    public FoldType parent() {
        return parent;
    }
   
    /**
     * Persistable value of FoldType.
     * Use {@link FoldType.Domain#valueOf} to convert the value back to FoldType instance. The value
     * can (and is) used to compose preference setting keys.
     * 
     * @return value of FoldType.
     * @since 1.35
     */
    public String code() {
        return code;
    }

    /**
     * Represents a value set of {@link FoldType}s for one MIME type.
     * The instance collects all FoldTypes defined for a certain MIME type. "" mime type
     * represents 'global' FoldTypes.
     * <p/>
     * The instance will fire change events when the set of fold types change, e.g. as a result
     * of enabled/disabled modules (appearance of {@link FoldTypeProvider}).
     * @since 1.35
     */
    public interface Domain {
        /**
         * Enumerates FoldTypes. Returns empty collection, if no fold types are known.
         * @return fold types.
         */
        Collection<FoldType>    values();
        
        /**
         * Translates String code into FoldType. Returns null if the fold type is not found.
         * The String should have been obtained by a call to FoldType.code().
         * 
         * @param s code value
         * @return the FoldType instance.
         */
        FoldType                valueOf(String s);

        /**
         * Attaches listener to be notified when set of FoldTypes change.
         * 
         * @param l listener instance.
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Detaches the change listener
         * @param l listener instance.
         */
        void removeChangeListener(ChangeListener l);
    }
    
    private final String code;
    
    private final FoldType parent;

    /**
     * Display name for the fold type, for presentation in UI
     */
    private final String label;
    
    /**
     * The default fold template
     */
    private final FoldTemplate template;
}
