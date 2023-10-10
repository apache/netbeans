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
package org.netbeans.spi.options;

import java.beans.PropertyChangeListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.options.OptionsPanelControllerAccessor;
import org.netbeans.modules.options.advanced.AdvancedPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * PanelController creates visual representation of one Options Dialog
 * category, and manages communication between Options Dialog and this
 * panel.
 */
public abstract class OptionsPanelController {

    /**
     * Property name constant.
     */
    public static final String PROP_VALID = "valid";

    /**
     * Property name constant.
     */
    public static final String PROP_CHANGED = "changed";

    /**
     * Property name constant.
     */
    public static final String PROP_HELP_CTX = "helpCtx";

    static {
        OptionsPanelControllerAccessor.DEFAULT = new OptionsPanelControllerAccessor() {

            @Override
            public void setCurrentSubcategory(OptionsPanelController controller, String subpath) {
                controller.setCurrentSubcategory(subpath);
            }
            
        };
    }

    /**
     * Creates an advanced tabbed controller, just like Miscellaneous section.
     * @param subpath path to folder under OptionsDialog folder containing 
     * instances of AdvancedOption class. Path is composed from registration 
     * names divided by slash. E.g. "MyCategory" for the following registration:
     * <pre style="background-color: rgb(255, 255, 153);">
     * &lt;folder name="OptionsDialog"&gt;
     *     &lt;file name="MyCategory.instance"&gt;
     *         &lt;attr name="instanceClass" stringvalue="org.foo.MyCategory"/&gt;
     *         &lt;attr name="position" intvalue="900"/&gt;
     *     &lt;/file&gt;
     *     &lt;folder name="MyCategory"&gt;
     *         &lt;file name="SubCategory1.instance"&gt;
     *             &lt;attr name="instanceClass" stringvalue="org.foo.Subcategory1"/&gt;
     *         &lt;/file&gt;
     *         &lt;file name="SubCategory2.instance"&gt;
     *             &lt;attr name="instanceClass" stringvalue="org.foo.Subcategory2"/&gt;
     *         &lt;/file&gt;
     *     &lt;/file&gt;
     * &lt;/folder&gt;</pre>
     * @return OptionsPanelController a controller wrapping all AdvancedOption instances found in the folder
     * @since 1.8
     * @deprecated Use {@link ContainerRegistration} instead.
     */
    @Deprecated
    public static final OptionsPanelController createAdvanced(String subpath) {
        return new AdvancedPanelController(subpath);
    }

    /**
     * Component should load its data here. You should not do any 
     * time-consuming operations inside the constructor, because it 
     * blocks initialization of OptionsDialog. Initialization 
     * should be implemented in update method.
     * This method is called after {@link #getComponent} method.
     * Update method can be called more than one time for the same instance 
     * of JComponent obtained from {@link #getComponent} call.
     */
    public abstract void update ();

    /**
     * This method is called off EDT when Options Dialog "OK" or "Apply" button is pressed.
     */
    public abstract void applyChanges ();

    /**
     * This method is called when Options Dialog "Cancel" button is pressed.
     */
    public abstract void cancel ();

    /**
     * Should return <code>true</code> if some option value in this 
     * category is valid.
     * 
     * 
     * @return <code>true</code> if some option value in this 
     * category is valid
     */
    public abstract boolean isValid ();

    /**
     * Should return <code>true</code> if some option value in this 
     * category has been changed.
     * 
     * 
     * @return <code>true</code> if some option value in this 
     * category has been changed
     */
    public abstract boolean isChanged ();

    /**
     * Each option category can provide some lookup. Options Dialog master
     * lookup is composed from these individual lookups. Master lookup
     * can be obtained from {@link #getComponent} call. This lookup is designed
     * to support communication anong individual panels in one Options
     * Dialog.
     * 
     * There is no guarantee that this method will be called from AWT thread.
     * 
     * @return lookup provided by this Options Dialog panel
     */
    public Lookup getLookup () {
        return Lookup.EMPTY;
    }

    /**
     * Handle successful search in some panel in options window.
     * By default no action is performed. Each implementor should make special
     * actions, for example to choose a specific sub-panel, if this is required.
     *
     * <p>Note that if the search is cleared (user presses <code>Esc</code> or <code>Enter</code> with empty text)
     * this method is called with <code>null</code> as values for both the parameters, giving the
     * implementors the chance to undo the filtering done is some previous invocation.
     * @param searchText the text the user has entered in the search box in the options window.
     * @param matchedKeywords the list of matched keywords for a specific panel in the options window.
     * @since 1.30
     */
    public void handleSuccessfulSearch(String searchText, List<String> matchedKeywords) {
    }

    /**
     * Returns visual component representing this options category.
     * This method is called before {@link #update} method.
     * 
     * @param masterLookup master lookup composed from lookups provided by 
     *        individual OptionsPanelControllers 
     *        - {@link OptionsPanelController#getLookup}
     * @return visual component representing this options category
     */
    public abstract JComponent getComponent (Lookup masterLookup);

    /**
     * Enables to handle selection of current subcategory. It is called from
     * {@link org.netbeans.api.options.OptionsDisplayer#open(java.lang.String)},
     * if some subpath is defined.
     * @param subpath path of subcategories to be selected. Path is 
     * composed from registration names divided by slash.
     * @see org.netbeans.api.options.OptionsDisplayer
     * @since 1.8
     */
    protected void setCurrentSubcategory(String subpath) {
    }

    /**
     * Enables to handle selection of subcategory. It is meant to be called from
     * a composite OptionspanelController and  delegates to
     * {@link #setCurrentSubcategory(java.lang.String)}.
     * @param subpath path of subcategory to be selected. Path is 
     * composed from registration names divided by slash.
     * @since 1.38
     */
    public final void setSubcategory(String subpath) {
        setCurrentSubcategory(subpath);
    }

    /**
     * 
     * Get current help context asociated with this panel.
     * 
     * 
     * @return current help context
     */
    public abstract HelpCtx getHelpCtx ();

    /**
     * Registers new listener.
     * 
     * 
     * @param l a new listener
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Unregisters given listener.
     * 
     * 
     * @param l a listener to be removed
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);

    /**
     * Registers a simple panel at the top level of the Options dialog.
     * Should be placed on a {@link OptionsPanelController} instance.
     * @see OptionsCategory
     * @since org.netbeans.modules.options.api/1 1.14
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TopLevelRegistration {
        /**
         * Optional path that can be used in {@link OptionsDisplayer#open(String)}.
         * Typically this should be a reference to a compile-time constant to which other code can refer.
         */
        String id() default "";
        /** Label shown on the button. You may use {@code #key} syntax. */
        String categoryName();
        /** Path to icon for the button. */
        String iconBase();
        /**
         * Optional keywords (separated by commas) for use with Quick Search (must also specify {@link #keywordsCategory}).
         * You may use {@code #key} syntax.
         */
        String keywords() default "";
        /** Keyword category for use with Quick Search (must also specify {@link #keywords}). */
        String keywordsCategory() default "";
        /** Position relative to other top-level panels. */
        int position() default Integer.MAX_VALUE;
    }

    /**
     * Registers a subpanel inside a top-level container panel in the Options dialog.
     * Should be placed on a {@link OptionsPanelController} instance.
     * @see AdvancedOption
     * @since org.netbeans.modules.options.api/1 1.14
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SubRegistration {
        /**
         * Optional path that can be used (with {@link #location}) in {@link OptionsDisplayer#open(String)}.
         * Typically this should be a reference to a compile-time constant to which other code can refer.
         */
        String id() default "";
        /**
         * Location of this panel inside some top-level panel matching {@link ContainerRegistration#id}.
         * If unspecified, placed in the Miscellaneous panel.
         * Typically this should be a reference to a compile-time constant also used for the container's ID.
         */
        String location() default "Advanced";
        /** Label shown on the tab. You may use {@code #key} syntax. */
        String displayName();
        /**
         * Optional keywords (separated by commas) for use with Quick Search (must also specify {@link #keywordsCategory}).
         * You may use {@code #key} syntax.
         */
        String keywords() default "";
        /** Keyword category for use with Quick Search (must also specify {@link #keywords}). */
        String keywordsCategory() default "";
        /**
         * Position relative to sibling subpanels.
         * Accepted only for non-default {@link #location} (Miscellaneous panel is sorted alphabetically).
         */
        int position() default Integer.MAX_VALUE;
    }

    /**
     * Registers a panel with child panels at the top level of the Options dialog.
     * May be placed on any package (i.e. {@code package-info.java}).
     * Register children using {@link SubRegistration}.
     * @see OptionsCategory
     * @since org.netbeans.modules.options.api/1 1.14
     */
    @Target(ElementType.PACKAGE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface ContainerRegistration {
        /**
         * Path that can be used in {@link OptionsDisplayer#open(String)} and {@link SubRegistration#location}.
         * Typically this should be a reference to a compile-time constant to which other code can refer.
         */
        String id();
        /** Label shown on the button. You may use {@code #key} syntax. */
        String categoryName();
        /** Path to icon for the button. */
        String iconBase();
        /**
         * Optional keywords (separated by commas) for use with Quick Search (must also specify {@link #keywordsCategory}).
         * You may use {@code #key} syntax.
         */
        String keywords() default "";
        /** Keyword category for use with Quick Search (must also specify {@link #keywords}). */
        String keywordsCategory() default "";
        /** Position relative to other top-level panels. */
        int position() default Integer.MAX_VALUE;
    }

    /**
     * Similar to {@link Keywords} but permits multiple registrations of
     * one class.
     *
     * @since org.netbeans.modules.options.api/1 1.29
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface KeywordsRegistration {

        /**
         * List of Keywords registrations.
         */
        Keywords[] value();
    }

    /**
     * Registers keywords for some panel in the Options dialog. Should be placed
     * on a {@link javax.swing.JPanel} instance.
     *
     * @since org.netbeans.modules.options.api/1 1.29
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Keywords {

        /**
         * Keywords for use with search inside the Options dialog. You may use
         * {@code #key} syntax. The case in not important.
         *
         * <p> Each entry in the provided array is split around comma character.
         * For example:
         *
         * <blockquote><table>
         * <caption>Split examples showing array and keywords</caption>
         * <tr><th>Provided array</th><th>Keywords</th></tr> 
         * <tr><td align=center>{ "Boo", "fOo" }</td><td><tt>{ "BOO", "FOO" }</tt></td></tr>
         * <tr><td align=center>{ "boo and", "foo" }</td><td><tt>{ "BOO AND", "FOO" }</tt></td></tr>
         * <tr><td align=center>{ "boo,and", "foo" }</td><td><tt>{ "BOO", "AND", "FOO" }</tt></td></tr> 
         * </table></blockquote>
         *
         * <p> The user's search-text is split around the space character to form words.
         * All words need to be present in a panel to yield a successful search.
         * The registered keywords {"Boo,anD", "fOo"}, for example, yield the following results with these search-texts:
         *
         * <blockquote><table> 
         * <caption>Search examples showing search-text and results</caption>
         * <tr><th>User's search-text</th><th>Result</th></tr>
         * <tr><td align=center>"boo"</td><td><tt>keyword found</tt></td></tr>
         * <tr><td align=center>"nd"</td><td><tt>keyword found</tt></td></tr>
         * <tr><td align=center>"boo and"</td><td><tt>keyword found</tt></td></tr>
         * <tr><td align=center>"boo moo"</td><td><tt>keyword NOT found</tt></td></tr>
         * </table></blockquote>
         */
        String[] keywords();

        /**
         * Keyword category for use with search inside the Options dialog.
         *
         * Location of this panel inside some top-level panel matching
         * {@link ContainerRegistration#id} or {@link SubRegistration#location}.
         * Typically this should be a reference to a compile-time constant also
         * used for the container's ID.
         *
         * If the panel is in the Miscellaneous category you must also specify {@link #tabTitle}).
         */
        String location();

        /**
         * Optional title that must be used if the panel is part of a tabbed pane, such as when it is
         * in the Editor, Fonts &amp; Colors, Java, PHP, C/C++ or Miscellaneous categories, matching the
         * {@link SubRegistration#displayName}.
         * 
         * You may use {@code #key} syntax.
         */
        String tabTitle() default "";
    }

}
