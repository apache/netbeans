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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;


/**
 * Describes an option, it's name, type, default value and some other stuff.
 *
 * Options are singletons but their values are defined in an OptionSet
 * several of which can be layered in OptionLayers.
 */

public abstract class Option {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final String      name;        // name of the option
    private final String      label;       // label representing the name
    private final char        mnemonic;
    private final String      tooltip;     // tooltip for the label
    private final String[]    values;      // values defined by the option
    private String[]    valueLabels; // labels representing the values in GUI
    private String[]    valueDescrs; // Descriptions of values
    private String      optionDescr; // Description of the option
    private final String      defaultValue;// default value
    private final boolean     clientOption;// is this option handled by OptionClient?
//    private final boolean     trim;		// ... the string representation on set
    private final Type         type;        // GUI represantation of the option
    private final boolean hasTooltip;
    private CatalogDynamic catalog;

    /**
     * types defining GUI representation
     */
    public static enum Type{TEXT_AREA, RADIO_BUTTON, COMBO_BOX, CHECK_BOX, DIRECTORY, DIRECTORIES, FILE};
    
    private static String yesString = null;
    private static String noString = null;

    static {
	try {
	    yesString = Catalog.get("VALUE_on"); // NOI18N
	    noString  = Catalog.get("VALUE_off"); // NOI18N
	}
	catch (Exception ex) {
            Exceptions.printStackTrace(ex);
	}
    }

    /**
     * Describe an option.
     *
     * @param name internal name of the option.
     * @param catalog CatalogDynamic to be used for accessing labels and such.
     * @param values If non-null described an enumeration of possible values.
     * @param defaultValue If non-null specifies the default value.
     *                     If null then this option doesn't have default values.
     * @param clientOption If true means that values are set through an
     *                     OptionClient.
     * @param type One of TEXT_AREA, RADIO_BUTTON, COMBO_BOX, CHECK_BOX,
     *             DIRECTORY, FILE.
     */


    protected Option(String name, CatalogDynamic catalog, String[] values,
			   String defaultValue, boolean clientOption,
			   Type type, boolean hasTooltip, boolean hasMnemonic) {
	this.name  = name;
	this.catalog = catalog;
	this.values =  values;
	this.defaultValue = defaultValue;
	this.clientOption = clientOption;
	this.type = type;
	this.label = catalog.get("LABEL_"+name); // NOI18N

        // see CR 6995039, IZ 203918
        this.tooltip = hasTooltip ? name : null;
        this.hasTooltip = hasTooltip;
        
        this.mnemonic = hasMnemonic ? catalog.getMnemonic("MNEM_"+name) : '\0'; // NOI18N

	setValueLabels();
    }

    // getters
    public String getName() { return name; }

    public String getLabel() { return label; }
    public String getDisplayName() { return label; }
    public char getMnemonic() { return mnemonic; }

    public String getLabelTip() { return tooltip; }
    public String getShortDescription() {
        return hasTooltip ? name : optionDescr;
    }
    
    public String[] getValues() {return values; }
    public String[] getValueLabels() {return valueLabels; }
    public String[] getValueDescrs() {return valueDescrs; }
    public String getOptionDescription() {return optionDescr; }
    public String getDefaultValue() {return defaultValue; }
    public boolean isClientOption() {return clientOption;}
    public Type getType() {return type;}

    public String getCurrValue(OptionSet optionSet) {
	OptionValue ov = optionSet.byType(this);
	if (ov != null)
	    return ov.get();
	else
	    return null;
    }

    /**
     * Return true if this kind of option is writable by the property editor,
     * aka editable, in the context of the given optionSet.
     * By default returns true.
     */
    protected boolean canWrite(OptionSet optionSet) {
	return true;
    }

    public void setCurrValue(OptionSet optionSet, String v) {
	OptionValue ov = optionSet.byType(this);
	if (ov != null) {
	    String oldValue = ov.get();
	    ov.set(v);
	    firePropertyChange(ov, oldValue, v);
	}
    }

    public void setEnabled(OptionLayers optionLayers, boolean enabled) {
	OptionValue ov = optionLayers.byType(this);
	if (ov != null) {
	    boolean oldValue = isEnabled(ov.owner());
	    ov.setEnabled(enabled);
	    firePropertyChange(ov, oldValue, enabled);
	}
    }

    /**
     * Return true if this option is "enabled". This is only legal
     * for "on/off" type of options. The result for other options
     * is undefined.
     */

    public boolean isEnabled(OptionSet optionSet) {
	assert isYesNoOption();
	return "on".equals(getCurrValue(optionSet)); // NOI18N
    }


    /**
     * given the value return it's corresponding valueLabel
     */
    public String getValueLabel(String value) {
	if ((values == null) || ( valueLabels == null))
	    return null;

	for (int i = 0; i < values.length; i++) {
	    if (value.equals(values[i]))
		return valueLabels[i];
	}
	return null;
    }

    /**
     * given the valueLabel return it's corresponding value
     */
    public String getValue(String valueLabel) {
	if ((values == null) || ( valueLabels == null))
	    return null;

	for (int i = 0; i < valueLabels.length; i++) {
	    if (valueLabel.equals(valueLabels[i]))
		return values[i];
	}
	return null;
    }

    /**
     * sets the labels representing the values in UI
     */
    private void setValueLabels() {
	//for the text area and check box no valueLabel is used
	if (type == Type.TEXT_AREA || type == Type.CHECK_BOX 
                || type == Type.DIRECTORY || type == Type.DIRECTORIES
                || type == Type.FILE) {
	    valueLabels = null;
	    setValueDescrs();
	    return;
	}
	else if (isYesNoOption()) {
	    if (values[0].equals("on"))  // NOI18N
		valueLabels = new String[] {yesString, noString};
	    else
		valueLabels = new String[] {noString, yesString};
	    setValueDescrs();
	    return;
	}

	valueLabels = new String[values.length];
	for (int i = 0; i < values.length; i++) {
	    if (isNumber(values[i])) {
		valueLabels[i] = values[i];
		continue;
	    }
	    try {
		valueLabels[i] = catalog.get("VALUE_"+name+"_"+values[i]); // NOI18N
	    }
	    catch (Exception ex) {
                Exceptions.printStackTrace(ex);
	    }
	}
	setValueDescrs();
    }

    private static boolean isNumber(String text) {
	try {
	    Integer.parseInt(text);
	} catch (NumberFormatException x) {
	    return false;
	}
	return true;
    }

    /** Generates accessible descriptions for options.
     *  The algorithm is as follows:
     *  1. Try to get accessible descriptions from resource bundle
     *     If resourse is found it will be the accessible descriptions
     *  2. If resourse is not presented in the resourse bundle
     *     construct accessible description using option name
     *     and option value taken from resource bundle.
     *     For example, for radio buttons "Yes" and "No" in the option
     *     which is represented in GUI as follows:
     *          Abbreviate Pathnames:  (*) Yes  ( ) No
     *     the generated accessible description will be
     *     "Abbreviate Pathnames: Yes" and "Abbreviate Pathnames: No"
     *     accordingly.
     */

    private void setValueDescrs() {
	String labTxt;
	if (type == Type.RADIO_BUTTON) {
	    valueDescrs = new String[values.length];
	    for (int i = 0; i < values.length; i++) {
		if (isNumber(values[i])) {
		    valueDescrs[i] = values[i];
		    continue;
		}
		try {
		    valueDescrs[i] = catalog.get("ACSD_"+name+"_"+values[i]); // NOI18N
		}
		catch (java.util.MissingResourceException ex) {
		    String pattern = catalog.get("ACSD_option_pattern"); // NOI18N
		    String valTxt;
		    labTxt = org.openide.awt.Actions.cutAmpersand(label);
		    if(labTxt.endsWith(":")) { // NOI18N
			    labTxt = labTxt.substring(0, labTxt.length()-1);
		    }
		    valTxt = org.openide.awt.Actions.cutAmpersand(valueLabels[i]);
		    valueDescrs[i] = Catalog.format(pattern, labTxt, valTxt);
		}
	    }
	} else {
	    try {
		optionDescr = catalog.get("ACSD_"+name); // NOI18N
	    }
	    catch (java.util.MissingResourceException ex) {
		labTxt = org.openide.awt.Actions.cutAmpersand(label);
		if(labTxt.endsWith(":")) { // NOI18N
		    labTxt = labTxt.substring(0, labTxt.length()-1);
		}
		optionDescr = labTxt;
	    }
	}
    }

    /**
     * checks if the option values are only on and off
     * and if the option is a RADIO_BOX option
     */
    public boolean isYesNoOption() {
	if (type == Type.CHECK_BOX)
	    return true;

	if ((type == Type.RADIO_BUTTON)  &&
		(values.length == 2) &&
		((values[0].equals("on")) || (values[0].equals("off"))) &&    // NOI18N
		((values[1].equals("on")) || (values[1].equals("off"))))     // NOI18N
	    return true;

	return false;
    }

    public boolean equals(Option option) {
	if (option.getName().equals(name))
	    return true;
	return false;
    }

    /**
     * Given the option and a value, return the sub option
     * this function is created for special cases where an option
     * is enabled/disabled by the behaviour of another option
     */

    public abstract Option getSubOption(String value);

    /**
     * Hack to fix width rendering
     */
    public abstract boolean verticalLayout();

    /**
     * Hack to fix width rendering
     */
    public abstract boolean overrideHasLabel();
    
    
    /**
     * return the sub option if any
     */
    public abstract Option getSubOption();


    /**
     * returns if this is a sub option or not
     */
    public abstract boolean isSubOption();

    /**
     * Returns true if this option should be persisted
     *
     * was: hard coded in perhapsToXML()
     */
    public abstract boolean persist(OptionValue value);


    /**
     * Return the well-formedness disposition of the value expressed in text.
     */
    public abstract Validity getValidity(String text);


    /**
     * Return true if the string value of this option should be trimmed on set.
     */
    public abstract boolean isTrim();

    
    
    /**
     * creates the UI for the DialogOption depending on its type property
     * returns the created Panel
     *
     * was: createOptionPanel
     */

    public OptionUI createUI() {
	OptionUI panel = null;
	switch (this.getType()) {
	    case DIRECTORIES:
            case DIRECTORY:
	    case FILE:
		panel = new DirectoryOptionUI(this);
		break;
	    case TEXT_AREA:
		panel = new TextFieldOptionUI(this);
		break;
	    case RADIO_BUTTON:
		panel = new RadioButtonOptionUI(this);
		break;
	    case COMBO_BOX:
		panel = new ComboBoxOptionUI(this);
		break;
	    case CHECK_BOX:
		panel = new CheckBoxOptionUI(this);
		break;
	    default:
		break;
	}
	return panel;
    }

    public PropertySupport createNodeProp(OptionSetOwner optionSetOwner) {
	return new OptionPropertySupport(optionSetOwner, this, null);
    }

    public PropertySupport createNodeProp(OptionSetOwner optionSetOwner, String base, FileSystem fs) {
	return new OptionPropertySupport(optionSetOwner, this, base, fs);
    }

    public PropertySupport createBasedNodeProp(OptionSetOwner optionSetOwner,
					       String base) {
	return new OptionPropertySupport(optionSetOwner, this, base);
    }




    //
    // Property change support
    // The source of the event is the OptionSet where the valkue changed.
    //

    public void addPropertyChangeListener(PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(listener);
    }

    /**
     * Was event 'e' caused by this Option?
     */
    public boolean caused(PropertyChangeEvent e) {
	return IpeUtils.sameString(e.getPropertyName(), name);
    }

    private void firePropertyChange(OptionValue ov,
				    String oldValue, String newValue) {
	PropertyChangeEvent e =
	    new PropertyChangeEvent(ov.owner(), name, oldValue, newValue);
	pcs.firePropertyChange(e);
    }

    private void firePropertyChange(OptionValue ov,
				    boolean oldValue, boolean newValue) {
	PropertyChangeEvent e =
	    new PropertyChangeEvent(ov.owner(), name, oldValue, newValue);
	pcs.firePropertyChange(e);
    }
}
