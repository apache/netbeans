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
package org.netbeans.modules.web.clientproject.api.json;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.web.clientproject.api.util.WatchedFile;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Class representing any JSON file on a disk.
 * @since 1.88
 */
public final class JsonFile {

    private static final Logger LOGGER = Logger.getLogger(JsonFile.class.getName());

    private static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {

        @Override
        public Map createObjectContainer() {
            return new LinkedHashMap();
        }

        @Override
        public List creatArrayContainer() {
            return new ArrayList();
        }

    };

    private final WatchedFile watchedFile;
    private final WatchedFields watchedFields;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final ChangeListener watchedFileChangeListener = new WatchedFileChangeListener();

    // @GuardedBy("this")
    private Map<String, Object> content;
    private volatile boolean contentInited = false;


    /**
     * Creates new JSON file. JSON file does not need to exist on the disk in the moment.
     * <p>
     * <em>Note:</em> If listening for {@link WatchedFields#all() all field changes}, property name and values are always {@code null}
     * and only one property change event is fired if content changes.
     * @param fileName name of the JSON file, e.g. "config.json"
     * @param directory directory where the JSON file is
     * @param watchedFields content fields that are watched for changes
     */
    public JsonFile(String fileName, FileObject directory, WatchedFields watchedFields) {
        Parameters.notNull("watchedFields", watchedFields); // NOI18N
        watchedFile = WatchedFile.create(fileName, directory);
        this.watchedFields = watchedFields.freeze();
        watchedFile.addChangeListener(WeakListeners.change(watchedFileChangeListener, watchedFile));
    }

    /**
     * Cleans up, it means to clear cached content, to remove file listener etc.
     */
    public void cleanup() {
        contentInited = false;
        clear(false);
    }

    /**
     * Adds listener to content changes.
     * @param listener listener to be added
     * @see WatchedFields
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        initContent();
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes listener to content changes.
     * @param listener listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Checks if the JSON file exists on disk.
     * @return {@code true} if the JSON file exists on disk, {@code false} otherwise
     */
    public boolean exists() {
        return watchedFile.exists();
    }

    /**
     * Gets JSON file.
     * @return JSON file
     */
    public File getFile() {
        return watchedFile.getFile();
    }

    /**
     * Gets full path of the JSON file.
     * @return full path of the JSON file
     */
    public String getPath() {
        return getFile().getAbsolutePath();
    }

    /**
     * Refreshes the file (when it was modified externally).
     */
    public void refresh() {
        clear(false);
        FileUtil.toFileObject(getFile()).refresh();
    }

    /**
     * Returns <b>shallow</b> copy of the content.
     * <p>
     * <b>WARNING:</b> Do not modify the content directly! Use {@link #setContent(List, Object)} instead!
     * @return <b>shallow</b> copy of the data
     * @see #setContent(List, Object)
     */
    @CheckForNull
    public synchronized Map<String, Object> getContent() {
        initContent();
        if (content != null) {
            return new LinkedHashMap<>(content);
        }
        File file = getFile();
        if (!file.isFile()) {
            return null;
        }
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            content = (Map<String, Object>) parser.parse(reader, CONTAINER_FACTORY);
        } catch (ParseException ex) {
            LOGGER.log(Level.INFO, file.getAbsolutePath(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, file.getAbsolutePath(), ex);
        }
        if (content == null) {
            return null;
        }
        return new LinkedHashMap<>(content);
    }

    /**
     * Gets specific content value, for the given field hierarchy.
     * @param <T> the type of the returned content value, e.g. String or Integer
     * @param valueType the type of the returned content value, e.g. {@code String.class} or {@code Integer.class}
     * @param fieldHierarchy hierarchy of fields, e.g. {@code meta, author} for <tt>content['meta']['author']</tt>
     * @return specific content value, for the given field hierarchy
     */
    @CheckForNull
    public <T> T getContentValue(Class<T> valueType, String... fieldHierarchy) {
        return getContentValue(getContent(), valueType, fieldHierarchy);
    }

    protected <T> T getContentValue(Map<String, Object> content, Class<T> valueType, String... fieldHierarchy) {
        Map<String, Object> subdata = content;
        if (subdata == null) {
            return null;
        }
        for (int i = 0; i < fieldHierarchy.length; ++i) {
            String field = fieldHierarchy[i];
            if (i == fieldHierarchy.length - 1) {
                Object value = subdata.get(field);
                if (value == null) {
                    return null;
                }
                if (valueType.isAssignableFrom(value.getClass())) {
                    return valueType.cast(value);
                }
                return null;
            }
            subdata = (Map<String, Object>) subdata.get(field);
            if (subdata == null) {
                return null;
            }
        }
        return null;
    }

    /**
     * Set new value of the given field.
     * <p>
     * <b>Warning:</b> This method must be called in a background thread. This method
     * also first waits 2 seconds before it starts changing the content (to get
     * correct document content after possible reload (typically external change)).
     * @param fieldHierarchy field (together with its hierarchy) to be changed
     * @param value new value of all type, e.g. new project name
     * @throws IOException if any error occurs
     */
    public void setContent(List<String> fieldHierarchy, Object value) throws IOException {
        assert fieldHierarchy != null;
        assert !fieldHierarchy.isEmpty();
        assert value != null;
        assert !EventQueue.isDispatchThread();
        assert exists();
        // #256712 - after discussion with Mila, this is perhaps the best what we can do now:
        // simply wait 2 seconds for possible reload to finish
        // it can happen when external tool (npm, bower etc.) modifies the given file and
        // right after it this method is called - sometimes, old content of the file
        // can be still present
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            // noop
        }
        setContentInternal(fieldHierarchy, value);
    }

    private synchronized void setContentInternal(final List<String> fieldHierarchy, final Object value) throws IOException {
        initContent();
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(getFile()));
        EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
        assert editorCookie != null : "No EditorCookie for " + dataObject;
        boolean modified = editorCookie.isModified();
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {
            document = editorCookie.openDocument();
        }
        assert document != null;
        final StyledDocument documentRef = document;
        NbDocument.runAtomic(document, new Runnable() {
            @Override
            public void run() {
                setContent(documentRef, fieldHierarchy, value);
            }
        });
        if (!modified) {
            editorCookie.saveDocument();
        }
        clear(true);
    }

    void setContent(Document document, List<String> fieldHierarchy, Object value) {
        String text;
        try {
            text = document.getText(0, document.getLength());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            assert false;
            return;
        }
        List<String> fields = new ArrayList<>(fieldHierarchy);
        int fieldIndex = -1;
        int closestFieldIndex = -1;
        int level = -1;
        int searchInLevel = 0;
        String field = null;
        for (int i = 0; i < text.length(); i++) {
            if (field == null) {
                field = "\"" + JSONValue.escape(fields.get(searchInLevel)) + "\""; // NOI18N
            }
            char ch = text.charAt(i);
            if (ch == '{') {
                level++;
                continue;
            } else if (ch == '}') {
                level--;
                continue;
            } else if (Character.isWhitespace(ch)) {
                continue;
            }
            if (level != searchInLevel) {
                continue;
            }
            if (ch == '"'
                    && text.substring(i).startsWith(field)) {
                // match
                closestFieldIndex = i;
                searchInLevel++;
                if (searchInLevel >= fields.size()) {
                    fieldIndex = i;
                    break;
                }
                i += field.length();
                field = null;
            }
        }
        assert field != null;
        if (fieldIndex == -1) {
            // remove found fields
            while (searchInLevel > 0) {
                fields.remove(0);
                searchInLevel--;
            }
            // insert missing fields
            insertNewField(document, fields, value, text, closestFieldIndex);
            return;
        }
        int colonIndex = -1;
        for (int i = fieldIndex + field.length(); i < text.length(); ++i) {
            char ch = text.charAt(i);
            switch (ch) {
                case ' ':
                    // noop
                    break;
                case ':':
                    colonIndex = i;
                    break;
                default:
                    // unexpected
                    return;
            }
            if (colonIndex != -1) {
                break;
            }
        }
        if (colonIndex == -1) {
            return;
        }
        int valueStartIndex = -1;
        for (int i = colonIndex + 1; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (!Character.isWhitespace(ch)) {
                valueStartIndex = i;
                break;
            }
        }
        if (valueStartIndex == -1) {
            return;
        }
        char valueFirstChar = text.charAt(valueStartIndex);
        int valueEndIndex = -1;
        for (int i = valueStartIndex + 1; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (valueFirstChar == '"') {
                if (ch == '"') {
                    valueEndIndex = i + 1;
                }
            } else if (Character.isDigit(ch)
                    || ch == '.') {
                // number
                continue;
            } else {
                valueEndIndex = i;
            }
            if (valueEndIndex != -1) {
                break;
            }
        }
        if (valueEndIndex == -1) {
            return;
        }
        insertValue(document, valueStartIndex, valueEndIndex, JSONValue.toJSONString(value), !(value instanceof String) && !(value instanceof Number));
    }

    private void insertNewField(Document document, List<String> fieldHierarchy, Object value, String text, int index) {
        int startIndex = index;
        boolean commaBefore;
        if (startIndex == -1) {
            startIndex = text.lastIndexOf('}'); // NOI18N
            if (startIndex != -1) {
                for (;;) {
                    char ch = text.charAt(--startIndex);
                    if (!Character.isWhitespace(ch)) {
                        startIndex++;
                        break;
                    }
                }
            }
            commaBefore = true;
        } else {
            startIndex = text.indexOf('{', startIndex); // NOI18N
            if (startIndex != -1) {
                startIndex++;
            }
            commaBefore = false;
        }
        if (startIndex == -1) {
            startIndex = text.length();
        }
        StringBuilder sb = new StringBuilder();
        if (commaBefore) {
            sb.append(','); // NOI18N
            sb.append('\n'); // NOI18N
        }
        int braces = -1;
        for (String field : fieldHierarchy) {
            if (braces > -1) {
                sb.append('{'); // NOI18N
                sb.append('\n'); // NOI18N
            }
            sb.append('"'); // NOI18N
            sb.append(JSONValue.escape(field));
            sb.append('"'); // NOI18N
            sb.append(':'); // NOI18N
            braces++;
        }
        sb.append(JSONValue.toJSONString(value));
        for (int i = 0; i < braces; i++) {
            sb.append('}'); // NOI18N
            sb.append('\n'); // NOI18N
        }
        if (!commaBefore) {
            sb.append(','); // NOI18N
        }
        insertValue(document, startIndex, -1, sb.toString(), true);
    }

    private void insertValue(Document document, int valueStartIndex, int valueEndIndex, String value, boolean format) {
        try {
            if (valueEndIndex != -1) {
                document.remove(valueStartIndex, valueEndIndex - valueStartIndex);
            }
            document.insertString(valueStartIndex, value, null);
            if (format) {
                reformat(document, valueStartIndex, valueStartIndex + value.length());
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            if (LOGGER.isLoggable(Level.FINE)) {
                try {
                    LOGGER.log(Level.FINE, "[JSON]: [{0}]", document.getText(0, document.getLength()));
                } catch (BadLocationException ex1) {
                    LOGGER.log(Level.FINE, "Cannot get document text", ex1);
                }
                LOGGER.log(Level.FINE, "start, end: {0}, {1}", new Object[] {valueStartIndex, valueEndIndex});
                LOGGER.log(Level.FINE, "[new text]: [{0}]", value);
            }
            assert false;
        }
    }

    private void reformat(Document document, int startOffset, int endOffset) throws BadLocationException {
        Reformat reformat = Reformat.get(document);
        reformat.lock();
        try {
            reformat.reformat(startOffset, endOffset);
        } finally {
            reformat.unlock();
        }
    }

    private void initContent() {
        if (contentInited) {
            return;
        }
        // read the file so we can listen on changes and fire proper events
        contentInited = true;
        getContent();
    }

    void clear(boolean fireChanges) {
        Map<String, Object> oldContent;
        Map<String, Object> newContent = null;
        synchronized (this) {
            oldContent = content;
            if (content != null) {
                LOGGER.log(Level.FINE, "Clearing cached content of {0}", watchedFile);
                content = null;
            }
            if (fireChanges) {
                newContent = getContent();
            }
        }
        if (fireChanges) {
            fireChanges(oldContent, newContent);
        }
    }

    private void fireChanges(@NullAllowed Map<String, Object> oldContent, @NullAllowed Map<String, Object> newContent) {
        if (watchedFields == WatchedFields.ALL) {
            if (!Objects.equals(oldContent, newContent)) {
                propertyChangeSupport.firePropertyChange(null, null, null);
            }
            return;
        }
        List<Pair<String, String[]>> data = watchedFields.getData();
        assert data != null;
        for (Pair<String, String[]> watchedField : data) {
            String propertyName = watchedField.first();
            String[] field = watchedField.second();
            Object oldValue = getContentValue(oldContent, Object.class, field);
            Object newValue = getContentValue(newContent, Object.class, field);
            if (!Objects.equals(oldValue, newValue)) {
                propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            }
        }
    }

    //~ Inner classes

    /**
     * Information about watched fields of the JSON file.
     */
    public static final class WatchedFields {

        private static final WatchedFields ALL = new WatchedFields((List<Pair<String, String[]>>) null);

        private final List<Pair<String, String[]>> data;

        private volatile boolean frozen = false;


        private WatchedFields(List<Pair<String, String[]>> data) {
            this.data = data;
        }

        /**
         * Creates new instance which can listen just on some specific content changes.
         * @return new instance which can listen just on some specific content changes
         * @see #add(String, String...)
         */
        public static WatchedFields create() {
            return new WatchedFields(new ArrayList<Pair<String, String[]>>());
        }

        /**
         * Creates new instance which listens on all content changes, not just specific ones.
         * @return new instance which listens on all content changes, not just specific ones
         */
        public static WatchedFields all() {
            return ALL;
        }

        /**
         * Adds new field to be watched.
         * @param propertyName property name to be fired, e.g. {@code PROP_AUTHOR}
         * @param fieldHierarchy hierarchy of fields, e.g. {@code meta, author} for <tt>content['meta']['author']</tt>
         * @return self
         */
        public WatchedFields add(@NonNull String propertyName, @NonNull String... fieldHierarchy) {
            if (data == null) {
                throw new IllegalStateException("Listening to all changes already");
            }
            if (frozen) {
                throw new IllegalStateException("Cannot add no more fields");
            }
            Parameters.notWhitespace("propertyName", propertyName); // NOI18N
            Parameters.notNull("fieldHierarchy", fieldHierarchy); // NOI18N
            int length = fieldHierarchy.length;
            if (length == 0) {
                throw new IllegalArgumentException("No field given");
            }
            String[] field = new String[length];
            System.arraycopy(fieldHierarchy, 0, field, 0, length);
            data.add(Pair.of(propertyName, field));
            return this;
        }

        @CheckForNull
        List<Pair<String, String[]>> getData() {
            if (data == null) {
                return null;
            }
            return new CopyOnWriteArrayList<>(data);
        }

        WatchedFields freeze() {
            frozen = true;
            return this;
        }

    }

    private final class WatchedFileChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            clear(true);
        }

    }

}
