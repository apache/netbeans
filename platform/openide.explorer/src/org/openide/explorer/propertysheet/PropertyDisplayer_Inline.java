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
/*
 * PropertyDisplayer_Inline.java
 * Refactored from PropertyDisplayer.Inline to avoid making the class public
 * Created on December 13, 2003, 7:22 PM
 */
package org.openide.explorer.propertysheet;


/** Basic interface for a property displayer which displays an inline
 * editor component, including methods for the various UI options
 * appropriate to such a component
 * @author Tim Boudreau
 */
interface PropertyDisplayer_Inline extends PropertyDisplayer {
    /** Set whether the instance should the custom editor button if
     * the property's property editor supports a custom editor */
    public void setShowCustomEditorButton(boolean val);

    /** Determine if the custom editor button should be shown for properties
     * whose editors support custom editors.  The default should be true */
    public boolean isShowCustomEditorButton();

    /** Set whether components should have borders as is typical for use
     * in a panel, or should show no borders, as is appropriate for uses
     * in tables and trees.  This may also affect behavior on focus -
     * components with popup windows should show their popup immediately
     * on receiving focus if table ui has been set to true */
    public void setTableUI(boolean val);

    /** Determine if the instance will show borders.  The default should
     * be false */
    public boolean isTableUI();

    /** Set a threshold for editors which would normally be rendered as
     * a combo box - below the threshold number of possible values, a
     * panel containing radio buttons should be used; above it a combo
     * box should be used */
    public void setRadioButtonMax(int max);

    /** Get the threshold number of components above which a combo box
     * should be used, and below which a panel containing radio buttons
     * should be used.  Default should be 0. */
    public int getRadioButtonMax();

    /** Sets the policy for using labels.  Certain types of inline editors,
     * notably checkboxes and radio button panels, are most usable if they
     * show the title of the property (in the case of radio buttons, using
     * TitledBorder, and in the case of checkboxes, as the checkbox
     * caption (which also serves to indicate keyboard focus in many look
     * and feels.  If this value is true, the component supplied will
     * display the title of a component if the component is one that can
     * benefit from this.  Setting this property to true does not guarantee
     * that a title will be displayed - that depends on the type of editor
     * used - for cases like combo boxes, proper alignment of labels with
     * combo boxes cannot be done automatically (taking into account the
     * other components on the form), but for cases like checkboxes and
     * radio buttons it is useful.  If the component is indeed displaying
     * a lable, isTitleDisplayed() will return true. */
    public void setUseLabels(boolean val);

    /** Determine if, in appropriate cases, the component should show the
     * title of the property being edited.  The default is false. */
    public boolean isUseLabels();

    /** Returns true if isUseLabels() is true and the displayed component
     * does indeed show the title */
    public boolean isTitleDisplayed();

    /** Returns true if the displayer is set to use radio buttons for
     * boolean properties instead of a checkbox */
    public boolean isRadioBoolean();

    /** Set whether boolean properties should be represented by a checkbox
     * or a pair of radio buttons */
    public void setRadioBoolean(boolean b);

    /**
     * If this ever becomes API, delete this method and find another way.
     * @return The ReusablePropertyEnv instance this displayer reconfigures on
     *  the fly to display different properties
     */
    public ReusablePropertyEnv getReusablePropertyEnv();
}
