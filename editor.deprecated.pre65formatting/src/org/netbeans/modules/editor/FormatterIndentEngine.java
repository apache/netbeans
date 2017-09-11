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

package org.netbeans.modules.editor;

import java.awt.Toolkit;
import java.io.*;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.ExtFormatter;
import org.openide.ErrorManager;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Indent engine that delegates to formatter
 *
 * @author Miloslav Metelka
 *
 * @deprecated Please use Editor Indentation API instead, for details see
 *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
 */

public abstract class FormatterIndentEngine extends IndentEngine {

    public static final String EXPAND_TABS_PROP = "expandTabs"; //NOI18N

    public static final String SPACES_PER_TAB_PROP = "spacesPerTab"; //NOI18N

    static final long serialVersionUID = -3408217516931076216L;

    /** Formatter to delegate to. It's checked before use and if it's null
     * the createFormatter() is called to initialize it.
     */
    private transient ExtFormatter formatter;

    private String[] acceptedMimeTypes;

    /** Get the formatter to which this indentation engine delegates. */
    public ExtFormatter getFormatter() {
        if (formatter == null) {
            formatter = createFormatter();
            // Fallback if no formatter is registered (can happen with new formatting api)
            if (formatter == null) {
                formatter = new ExtFormatter(BaseKit.class);
            }
        }
        return formatter;
    }

    /** Create the formatter. */
    protected abstract ExtFormatter createFormatter();

    public Object getValue(String settingName) {
        return getFormatter().getSettingValue(settingName);
    }

    public void setValue(String settingName, Object newValue, String propertyName) {
        Object oldValue = getValue(settingName);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }

        getFormatter().setSettingValue(settingName, newValue);
        
        if (propertyName != null){
            firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * @deprecated use {@link #setValue(java.lang.String, java.lang.Object, java.lang.String)} instead 
     * with properly specified propertyName
     */
    public void setValue(String settingName, Object newValue) {
        setValue(settingName, newValue, null);
    }
    
    
    public int indentLine(Document doc, int offset) {
        return getFormatter().indentLine(doc, offset);
    }

    public int indentNewLine(Document doc, int offset) {
        return getFormatter().indentNewLine(doc, offset);
    }

    public Writer createWriter(Document doc, int offset, Writer writer) {
        return getFormatter().createWriter(doc, offset, writer);
    }

    protected @Override boolean acceptMimeType(String mimeType) {
        if (acceptedMimeTypes != null) {
            for (int i = acceptedMimeTypes.length - 1; i >= 0; i--) {
                if (acceptedMimeTypes[i].equals(mimeType)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isExpandTabs() {
        return getFormatter().expandTabs();
    }

    public void setExpandTabs(boolean expandTabs) {
        boolean old = getFormatter().expandTabs();
        // Must call setter because of turning into custom property
        getFormatter().setExpandTabs(expandTabs);
        if (old != expandTabs) {
            setValue(SimpleValueNames.EXPAND_TABS,
                Boolean.valueOf(expandTabs), EXPAND_TABS_PROP);
            
            firePropertyChange(EXPAND_TABS_PROP,
                old ? Boolean.TRUE : Boolean.FALSE,
                expandTabs ? Boolean.TRUE : Boolean.FALSE
            );
        }
        
    }

    public int getSpacesPerTab() {
        return getFormatter().getSpacesPerTab();
    }

    public void setSpacesPerTab(int spacesPerTab) {
        if (spacesPerTab <= 0) {
            IllegalArgumentException iae = new IllegalArgumentException("Invalid argument"); //NOI18N
            ErrorManager errMan = Lookup.getDefault().lookup(ErrorManager.class);

            if (errMan != null) {
                Toolkit.getDefaultToolkit().beep();
                errMan.annotate(iae, ErrorManager.USER, iae.getMessage(), NbBundle.getMessage(FormatterIndentEngine.class, "MSG_NegativeValue"), null, null); //NOI18N
            } else {
                throw iae;
            }
        }

        int old = getFormatter().getSpacesPerTab();
        getFormatter().setSpacesPerTab(spacesPerTab);
        if (old != spacesPerTab) {
            setValue(SimpleValueNames.SPACES_PER_TAB,
                new Integer(spacesPerTab), SPACES_PER_TAB_PROP);
            
            firePropertyChange(SPACES_PER_TAB_PROP,
                new Integer(old),
                new Integer(spacesPerTab)
            );
        }
    }

    public void setAcceptedMimeTypes(String[] mimes) {
        this.acceptedMimeTypes = mimes;
    }

    public String[] getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }
    
    

    // Serialization ------------------------------------------------------------

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField(EXPAND_TABS_PROP, Boolean.TYPE),
        new ObjectStreamField(SPACES_PER_TAB_PROP, Integer.TYPE)
    };
    
    private void readObject(java.io.ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = ois.readFields();
        setExpandTabs(fields.get(EXPAND_TABS_PROP, true));
        setSpacesPerTab(fields.get(SPACES_PER_TAB_PROP, 4));
    }

    private void writeObject(java.io.ObjectOutputStream oos)
    throws IOException, ClassNotFoundException {
        ObjectOutputStream.PutField fields = oos.putFields();
        fields.put(EXPAND_TABS_PROP, isExpandTabs());
        fields.put(SPACES_PER_TAB_PROP, getSpacesPerTab());
        oos.writeFields();
    }

}

