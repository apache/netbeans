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

package org.netbeans.modules.cnd.editor.folding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

final class CppMetaModel implements PropertyChangeListener {

    // TODO: need to get reparse time from settings
    private final int reparseDelay = 1000;
    
    /** map of all files we're interested in */
    private final Map<String,CppFile> map = new ConcurrentHashMap<String,CppFile>();

    private final Collection<ParsingListener> listeners = new ConcurrentLinkedQueue<ParsingListener>();

    private static final CppMetaModel instance = new CppMetaModel();
    static {
        TopComponent.getRegistry().addPropertyChangeListener(instance);
    }

    private static RequestProcessor cppParserRP;

    private static final Logger log = Logger.getLogger(CppMetaModel.class.getName());

    private CppMetaModel() {
	//log.log(Level.FINE, "CppMetaModel: Constructor");
    }

    public static CppMetaModel getDefault() {
    	return instance;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())){
            checkClosed(evt.getNewValue());
        }
    }

    private void checkClosed(Object o){
        if (o instanceof Set<?>) {
            Set<EditorCookie> editorCookies = new HashSet<EditorCookie>();
            for(Object top : (Set<?>) o){
                if (top instanceof EditorCookie){
                    EditorCookie cookie = (EditorCookie) top;
                    if (MIMENames.isFortranOrHeaderOrCppOrC(
                            DocumentUtilities.getMimeType(cookie.getDocument()))) {
                        editorCookies.add(cookie);
                    }
                }
            }
            checkClosed(editorCookies);
        }
    }

    private void checkClosed(Set<EditorCookie> editors){
        Set<String> opened = new HashSet<String>();
        for (EditorCookie editor : editors) {
            Document doc = editor.getDocument();
            if (doc != null) {
                String tittle = (String) doc.getProperty(Document.TitleProperty);
                opened.add(tittle);
            }
        }
        List<String> toDelete = new ArrayList<String>();
        for(String title : map.keySet()){
            if (!opened.contains(title)){
                toDelete.add(title);
            }
        }
        for(String title : toDelete){
            map.remove(title);
        }
        if (map.size() == 0){
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    // Helper methods for awhile...
    private static synchronized RequestProcessor getCppParserRP() {
	if (cppParserRP == null) {
	    cppParserRP = new RequestProcessor("CPP Parser", 1); // NOI18N
	}
	return cppParserRP;
    }

    // we need to provide mechanism for handling only most recent changes and 
    // reject the unnecessary ones, so cancel previous one and create new task 
    // using delay
    private RequestProcessor.Task task = null;
    public void scheduleParsing(final Document doc) {

	final String title = (String) doc.getProperty(Document.TitleProperty);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "CppMetaModel.scheduleParsing: Checking " + getShortName(doc) +
		" [" + Thread.currentThread().getName() + "]"); // NOI18N
        }
        if (title == null) {
            log.log(Level.INFO, "CppMetaModel.scheduleParsing: No Title for document" + getShortName(doc)); // NOI18N
            return;
        }
	final CppFile file = map.get(title);
        // try to cancel task
        if (task != null) {
            task.cancel();
        }
	if (file == null) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "CppMetaModel.scheduleParsing: Starting initial parse for " +
			getShortName(doc));
            }
	    task = getCppParserRP().post(new Runnable() {
                @Override
		public void run() {
		    CppFile file = new CppFile(title);
		    map.put(title, file);
		    file.startParsing(doc);
                    fireObjectParsed(doc);
		}
	    }, reparseDelay);
	} else if (file.needsUpdate(doc)) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "CppMetaModel.scheduleParsing: Starting update parse for " +
			getShortName(doc));
            }
	    task = getCppParserRP().post(new Runnable() {
                @Override
		public void run() {
		    file.startParsing(doc);
                    fireObjectParsed(doc);
		}
	    }, reparseDelay);
	} /*else {
	    DataObject dobj;
	    Object o = doc.getProperty(Document.StreamDescriptionProperty);
	    if (o instanceof DataObject) {
		dobj = (DataObject) o;
		log.log(Level.FINE, "CppMetaModel.scheduleParsing: Existing record for " + getShortName(doc));
	    }
	}*/
    }
    
    private void fireObjectParsed(Document doc) {
        Object o = doc.getProperty(Document.StreamDescriptionProperty);
        if (o instanceof DataObject) {
            DataObject dobj = (DataObject) o;
            // listeners is a ConcurrentLinkedQueue now. It intelligently
            // handles concurrent modification without throwing exceptions.
            for (ParsingListener listener : listeners) {
                listener.objectParsed(new ParsingEvent(dobj));
            }
        }
    }

    private String getShortName(Document doc) {
	String longname = (String) doc.getProperty(Document.TitleProperty);
        if (longname == null) {
            return doc.toString();
        }
	int slash = longname.lastIndexOf(java.io.File.separatorChar);

	if (slash != -1) {
	    return longname.substring(slash + 1);
	} else {
	    return longname;
	}
    }

    public CppFile get(String key) {
	return map.get(key);
    }

    public void addParsingListener(ParsingListener listener) {
	//log.log(Level.FINE, "CppMetaModel: addParsingListener");
//	synchronized (listeners) {
	    listeners.add(listener);
//	}
    }

    public void removeParsingListener(ParsingListener listener) {
	//log.log(Level.FINE, "CppMetaModel: removeParsingListener");
//	synchronized (listeners) {
	    listeners.remove(listener);
//	}
    }

}
