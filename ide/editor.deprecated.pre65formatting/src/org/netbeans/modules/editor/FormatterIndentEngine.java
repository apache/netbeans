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
@Deprecated
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
    @Deprecated
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
                Integer.valueOf(spacesPerTab), SPACES_PER_TAB_PROP);
            
            firePropertyChange(SPACES_PER_TAB_PROP,
                Integer.valueOf(old),
                Integer.valueOf(spacesPerTab)
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

