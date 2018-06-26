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
        if(JspColoringData.PROP_PARSING_IN_PROGRESS.equals(evt.getPropertyName()) && newValue.booleanValue())
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
