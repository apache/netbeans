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

package org.netbeans.modules.web.core.syntax;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda, Marek Fukala
 */
final class JspUpToDateStatusProvider extends UpToDateStatusProvider implements DocumentListener, PropertyChangeListener  {
    
    private UpToDateStatus upToDate;
    
    public static JspUpToDateStatusProvider get(Document doc) {
        JspUpToDateStatusProvider provider = (JspUpToDateStatusProvider) doc.getProperty(JspUpToDateStatusProvider.class);
        
        if (provider == null) {
            doc.putProperty(JspUpToDateStatusProvider.class, provider = new JspUpToDateStatusProvider(doc));
        }
        
        return provider;
    }
    
    /** Creates a new instance of AnnotationMarkProvider */
    private JspUpToDateStatusProvider(Document document) {
        upToDate = UpToDateStatus.UP_TO_DATE_OK;
        document.addDocumentListener(this);
        
        //listen to parser results
        DataObject documentDO = NbEditorUtilities.getDataObject(document);
        if(documentDO != null && documentDO.isValid()) {
            JspColoringData jspcd = JspUtils.getJSPColoringData(documentDO.getPrimaryFile());
            //jspcd.addPropertyChangeListener(this);
            if(jspcd != null) {
                jspcd.addPropertyChangeListener(WeakListeners.propertyChange(this, jspcd));
            } else {
                //coloring data is null - weird, likely some parser problem or something in the file or project is broken
                //we will ignore the state, but the up-to-date status provider won't work for this file!
                upToDate = UpToDateStatus.UP_TO_DATE_DIRTY;
                Logger.getAnonymousLogger().info("JspUtils.getJSPColoringData(document, " + documentDO.getPrimaryFile() + ") returned null!");
            }
        }
    }
    
    //the property changes are fired via JspColoringData by TagLibParseSupport
    public void propertyChange(PropertyChangeEvent evt) {
        Boolean newValue = (Boolean)evt.getNewValue();
        if(JspColoringData.PROP_PARSING_IN_PROGRESS.equals(evt.getPropertyName()) && newValue)
            setUpToDate(UpToDateStatus.UP_TO_DATE_PROCESSING);
        if(JspColoringData.PROP_PARSING_SUCCESSFUL.equals(evt.getPropertyName()))
            setUpToDate(UpToDateStatus.UP_TO_DATE_OK);
    }
    
    public synchronized UpToDateStatus getUpToDate() {
        return upToDate;
    }
    
    private void setUpToDate(UpToDateStatus upToDate) {
        UpToDateStatus oldStatus = this.upToDate;
        if(oldStatus.equals(upToDate)) return ;
        this.upToDate = upToDate;
        firePropertyChange(PROP_UP_TO_DATE, oldStatus, upToDate);
    }
    
    public synchronized void removeUpdate(DocumentEvent e) {
        setUpToDate(UpToDateStatus.UP_TO_DATE_DIRTY);
    }
    
    public synchronized void insertUpdate(DocumentEvent e) {
        setUpToDate(UpToDateStatus.UP_TO_DATE_DIRTY);
    }
    
    public void changedUpdate(DocumentEvent e) {
    }
    
}
