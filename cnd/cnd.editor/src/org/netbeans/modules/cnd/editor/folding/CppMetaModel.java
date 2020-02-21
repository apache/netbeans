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
