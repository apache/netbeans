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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.beans.PropertyEditor;

import javax.swing.JFileChooser;
import org.openide.ErrorManager;
import org.openide.nodes.PropertySupport;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;

/**
 * Bridge to NB property sheet style properties.
 */

public class OptionPropertySupport extends PropertySupport {
    private final Option option;
    private final OptionSetOwner optionSetOwner;
    private final String base;
    private final FileSystem fileSystem;

    public OptionPropertySupport(OptionSetOwner optionSetOwner,
				 Option option,
				 String base) {
        this(optionSetOwner, option, base, 
                FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()));
    }
    
    // By rights we should extend PropertySupport<T> and subclass
    // OptionPropertySupport into Boolean and Object versions instead of
    // the dynamic ?: based on option.getType().
    // I tried that but it got too complicated with OptionDirectoryEditor
    // which has a reference to us.
    @SuppressWarnings("unchecked")
    public OptionPropertySupport(OptionSetOwner optionSetOwner,
				 Option option,
				 String base,
                                 FileSystem fileSystem) {
	super(option.getName(),
	      (option.getType() == Option.Type.CHECK_BOX)? Boolean.class:
						      Object.class,
	      option.getDisplayName(),
	      option.getShortDescription(),
	      true,
	      true);
	this.option = option;
	this.optionSetOwner = optionSetOwner;
	this.base = base;
	this.fileSystem = fileSystem;
    }

    // interface PropertySupport
    @Override
    public boolean canRead() {
	return true;
    }

    // interface PropertySupport
    @Override
    public boolean canWrite() {
	return option.canWrite(optionSetOwner.getOptions());
    }

    // interface Node.Property
    @Override
    public void setValue(Object o) {
	OptionValue ov = optionSetOwner.getOptions().byType(option);
	if (ov != null) {
	    if (option.getType() == Option.Type.CHECK_BOX && o instanceof Boolean) {
		Boolean b = (Boolean) o;
		ov.set(b == Boolean.TRUE? "on": "off"); // NOI18N
	    } else {
		ov.set((String)o);
	    }
	} else {
	    ErrorManager.getDefault().log(ErrorManager.WARNING,
			 String.format("setValue(): null OptionValue for %s", // NOI18N
				       option.getName()));
	}
    }

    // interface Node.Property
    @Override
    public Object getValue() {
	OptionValue ov = optionSetOwner.getOptions().byType(option);
	if (ov != null) {
	    if (option.getType() == Option.Type.CHECK_BOX) {
		if (ov.get().equalsIgnoreCase("on")) // NOI18N
		    return Boolean.TRUE;
		else if (ov.get().equalsIgnoreCase("off")) // NOI18N
		    return Boolean.FALSE;
		else
		    return null;
	    } else {
		return ov.get();
	    }
	} else {
	    ErrorManager.getDefault().log(ErrorManager.WARNING,
			 String.format("getValue(): null OptionValue for %s", // NOI18N
				       option.getName()));
	    return null;
	}
    }


    // interface Node.Property
    @Override
    public PropertyEditor getPropertyEditor() {

	PropertyEditor propertyEditor = null;

	switch (option.getType()) {
	    case CHECK_BOX:
		// We pass a Boolean.class above
		propertyEditor = super.getPropertyEditor();
		break;
	    case DIRECTORY:
		propertyEditor = new OptionDirectoryEditor(this, base, JFileChooser.DIRECTORIES_ONLY, fileSystem);
		break;
            case DIRECTORIES:
		propertyEditor = new OptionDirectoriesEditor(this, base, fileSystem);
		break;
	    case FILE:
		propertyEditor = new OptionDirectoryEditor(this, base, JFileChooser.FILES_ONLY);
		break;
	    case TEXT_AREA:
	    case RADIO_BUTTON:
	    case COMBO_BOX:
		propertyEditor = new OptionEnumEditor(this);
		break;
	    default:
		break;
	}

	return propertyEditor;
    }

    // interface Node.Property
    @Override
    public boolean supportsDefaultValue() {
	return option.getDefaultValue() != null;
    }

    // interface Node.Property
    @Override
    public boolean isDefaultValue() {
	if (!supportsDefaultValue())
	    return true;
	OptionValue ov = optionSetOwner.getOptions().byType(option);
	if (ov != null)
	    return IpeUtils.sameString(ov.get(), option.getDefaultValue());
	else
	    return true;
    }

    // interface Node.Property
    @Override
    public void restoreDefaultValue() {
	if (!supportsDefaultValue())
	    return;
	setValue(option.getDefaultValue());
    }

    // interface Node.Property
    @Override
    public String getHtmlDisplayName() {
	if (isDefaultValue()) {
	    return getDisplayName();
	} else {
	    return "<b>" + getDisplayName() + "</b>"; // NOI18N
	}
    }

    String[] getValues() {
	return option.getValues();
    }

    Validity getValidity(String text) {
	return option.getValidity(text);
    }
    
    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canAutoComplete")) { //NOI18N
            return Boolean.FALSE;
        }
        return super.getValue(attributeName);
    }
    
}
