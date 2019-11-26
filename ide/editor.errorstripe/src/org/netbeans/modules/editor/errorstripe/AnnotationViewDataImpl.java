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

package org.netbeans.modules.editor.errorstripe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationType.Severity;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.errorstripe.apimodule.SPIAccessor;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbCollections;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
final class AnnotationViewDataImpl implements PropertyChangeListener, AnnotationViewData, Annotations.AnnotationsListener {
    
    private static final Logger LOG = Logger.getLogger(AnnotationViewDataImpl.class.getName());
    
    private static final String UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME = "UpToDateStatusProvider"; //NOI18N
    private static final String TEXT_BASE_PATH = "Editors/text/base/"; //NOI18N

    private AnnotationView view;
    private Reference<JTextComponent> paneRef;
    private BaseDocument document;
    
    private List<MarkProvider> markProviders = new ArrayList<MarkProvider>();
    private List<UpToDateStatusProvider> statusProviders = new ArrayList<UpToDateStatusProvider>();

    private List<PropertyChangeListener> markProvidersWeakLs = new ArrayList<>();
    private List<PropertyChangeListener> statusProvidersWeakLs = new ArrayList<>();
    
    private Collection<Mark> currentMarks = null;
    private SortedMap<Integer, List<Mark>> marksMap = null;

    private static WeakHashMap<String, Collection<? extends MarkProviderCreator>> mime2Creators = new WeakHashMap<String, Collection<? extends MarkProviderCreator>>();
    private static WeakHashMap<String, Collection<? extends UpToDateStatusProviderFactory>> mime2StatusProviders = new WeakHashMap<String, Collection<? extends UpToDateStatusProviderFactory>>();

    private static LegacyCrapProvider legacyCrap;
    
    private Annotations.AnnotationsListener weakL;
    
    /** Creates a new instance of AnnotationViewData */
    public AnnotationViewDataImpl(AnnotationView view, JTextComponent pane) {
        this.view = view;
        this.paneRef = new WeakReference<>(pane);
        this.document = null;
    }
    
    public void register(BaseDocument document) {
        this.document = document;
        
        JTextComponent pane = paneRef.get();
        if (pane != null) {
            gatherProviders(pane);
        }
        
        if (document != null) {
            if (weakL == null) {
                weakL = WeakListeners.create(Annotations.AnnotationsListener.class, this, document.getAnnotations());
                document.getAnnotations().addAnnotationsListener(weakL);
            }
        }
        
        clear();
    }
    
    public void unregister() {
        if (document != null && weakL != null) {
            document.getAnnotations().removeAnnotationsListener(weakL);
            weakL = null;
        }
        
        removeListenersFromStatusProviders();
        removeListenersFromMarkProviders();
        
        document = null;
    }

    public static void initProviders(String mimeType) {
        // Legacy mime path (text/base)
        MimePath legacyMimePath = MimePath.parse("text/base");
        legacyCrap = MimeLookup.getLookup(legacyMimePath).lookup(LegacyCrapProvider.class);
        lookupProviders(mimeType);
    }

    private static void lookupProviders(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        // Mark providers
        mime2Creators.put(mimeType, MimeLookup.getLookup(mimePath).lookupAll(MarkProviderCreator.class));
        // Status providers
        mime2StatusProviders.put(mimeType, MimeLookup.getLookup(mimePath).lookupAll(UpToDateStatusProviderFactory.class));
    }
    
    private void gatherProviders(JTextComponent pane) {
        long start = System.currentTimeMillis();

        // Collect legacy mark providers
        List<MarkProvider> newMarkProviders = new ArrayList<MarkProvider>();
        if (legacyCrap != null) {
            createMarkProviders(legacyCrap.getMarkProviderCreators(), newMarkProviders, pane);
        }
        
        // Collect mark providers
        String mimeType = DocumentUtilities.getMimeType(pane);

        if (mimeType == null) {
            mimeType = pane.getUI().getEditorKit(pane).getContentType();
        }
        
        Collection<? extends MarkProviderCreator> creators = 
            mime2Creators.get(mimeType);

        if (creators == null) { //nothing for current mimeType, probably wrong init
            lookupProviders(mimeType);
            creators = mime2Creators.get(mimeType);
        }

        createMarkProviders(creators, newMarkProviders, pane);

        removeListenersFromMarkProviders();
        this.markProviders = newMarkProviders;
        addListenersToMarkProviders();

        
        // Collect legacy status providers
        List<UpToDateStatusProvider> newStatusProviders = new ArrayList<UpToDateStatusProvider>();
        if (legacyCrap != null) {
            createStatusProviders(legacyCrap.getUpToDateStatusProviderFactories(), newStatusProviders, pane);
        }
        
        // Collect status providers
        Collection<? extends UpToDateStatusProviderFactory> factories = 
            mime2StatusProviders.get(mimeType);
        if (factories != null) {
            createStatusProviders(factories, newStatusProviders, pane);
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Factories set to null in mimeType " + mimeType); //NOI18N
            }
        }

        removeListenersFromStatusProviders();
        this.statusProviders = newStatusProviders;
        addListenersToStatusProviders();
        
        
        long end = System.currentTimeMillis();
        if (AnnotationView.TIMING_ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            AnnotationView.TIMING_ERR.log(ErrorManager.INFORMATIONAL, "gather providers took: " + (end - start));
        }
    }

    private static void createMarkProviders(Collection<? extends MarkProviderCreator> creators, List<MarkProvider> providers, JTextComponent pane) {
        for (MarkProviderCreator creator : creators) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("creator = " + creator);
            }

            MarkProvider provider = creator.createMarkProvider(pane);
            if (provider != null) {
                providers.add(provider);
            }
        }
    }

    private static void createStatusProviders(Collection<? extends UpToDateStatusProviderFactory> factories, List<UpToDateStatusProvider> providers, JTextComponent pane) {
        for(UpToDateStatusProviderFactory factory : factories) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("factory = " + factory);
            }

            UpToDateStatusProvider provider = factory.createUpToDateStatusProvider(pane.getDocument());
            if (provider != null) {
                providers.add(provider);
            }
        }
    }
    
    private void addListenersToStatusProviders() {
        for (UpToDateStatusProvider provider : statusProviders) {
            
            // removePropertyChangeListener() is non-public but present in UpToDateStatusProvider - will the weak listener removal work??
            PropertyChangeListener weakL = WeakListeners.propertyChange(this, provider);
            SPIAccessor.getDefault().addPropertyChangeListener(provider, weakL);
            markProvidersWeakLs.add(weakL);
        }
    }
        
    private void addListenersToMarkProviders() {
        for (MarkProvider provider : markProviders) {
            PropertyChangeListener weakL = WeakListeners.propertyChange(this, provider);
            provider.addPropertyChangeListener(weakL);
            statusProvidersWeakLs.add(weakL);
        }
    }

    private void removeListenersFromStatusProviders() {
        if (statusProvidersWeakLs.size() == statusProviders.size()) { // Check if the listeners were not removed already
            int lIndex = 0;
            for (UpToDateStatusProvider statusProvider : statusProviders) {
                SPIAccessor.getDefault().removePropertyChangeListener(statusProvider, statusProvidersWeakLs.get(lIndex));
            }
        }
        statusProvidersWeakLs.clear();
    }
        
    private void removeListenersFromMarkProviders() {
        if (markProvidersWeakLs.size() == markProviders.size()) { // Check if the listeners were not removed already
            int lIndex = 0;
            for (MarkProvider markProvider : markProviders) {
                markProvider.removePropertyChangeListener(markProvidersWeakLs.get(lIndex));
            }
        }
        markProvidersWeakLs.clear();
    }
    
    /*package private*/ static Collection<Mark> createMergedMarks(List<MarkProvider> providers) {
        Collection<Mark> result = new LinkedHashSet<Mark>();
        
        for(MarkProvider provider : providers) {
            result.addAll(provider.getMarks());
        }
        
        return result;
    }
    
    /*package private for tests*/synchronized Collection<Mark> getMergedMarks() {
        if (currentMarks == null) {
            currentMarks = createMergedMarks(markProviders);
        }
        
        return new ArrayList<Mark>(currentMarks);
    }
    
    /*package private*/ static List<Mark> getStatusesForLineImpl(int line, SortedMap<Integer, List<Mark>> marks) {
        List<Mark> inside = marks.get(line);
        return inside == null ? Collections.<Mark>emptyList() : inside;
    }
    
    public Mark getMainMarkForBlock(int startLine, int endLine) {
        Mark m1;
        synchronized(this) {
            m1 = getMainMarkForBlockImpl(startLine, endLine, getMarkMap());
        }
        Mark m2 = getMainMarkForBlockAnnotations(startLine, endLine);
        
        if (m1 == null)
            return m2;
        
        if (m2 == null)
            return m1;
        
        if (isMoreImportant(m1, m2))
            return m1;
        else
            return m2;
    }
    
    /*package private*/ static Mark getMainMarkForBlockImpl(int startLine, int endLine, SortedMap<Integer, List<Mark>> marks) {
        int current = startLine - 1;
        Mark found = null;
        
        while ((current = findNextUsedLine(current, marks)) != Integer.MAX_VALUE && current <= endLine) {
            for (Mark newMark : getStatusesForLineImpl(/*doc, */current, marks)) {
                if (found == null || isMoreImportant(newMark, found)) {
                    found = newMark;
                }
            }
        }
        
        return found;
    }
    
    private static boolean isMoreImportant(Mark m1, Mark m2) {
        int compared = m1.getStatus().compareTo(m2.getStatus());
        
        if (compared == 0)
            return m1.getPriority() < m2.getPriority();
        
        return compared > 0;
    }
    
    private boolean isMoreImportant(AnnotationDesc a1, AnnotationDesc a2) {
        AnnotationType t1 = a1.getAnnotationTypeInstance();
        AnnotationType t2 = a2.getAnnotationTypeInstance();
        
        int compared = t1.getSeverity().compareTo(t2.getSeverity());
        
        if (compared == 0)
            return t1.getPriority() < t2.getPriority();
        
        return compared > 0;
    }
    
    private boolean isValidForErrorStripe(AnnotationDesc a) {
        return a.getAnnotationTypeInstance().getSeverity() != AnnotationType.Severity.STATUS_NONE;
    }
    
    private Mark getMainMarkForBlockAnnotations(int startLine, int endLine) {
        AnnotationDesc foundDesc = null;

        for (AnnotationDesc desc : NbCollections.iterable(listAnnotations(startLine, endLine))) {
            if ((foundDesc == null || isMoreImportant(desc, foundDesc)) && isValidForErrorStripe(desc))
                foundDesc = desc;
        }
        
        if (foundDesc != null)
            return new AnnotationMark(foundDesc);
        else
            return null;
    }

    public int findNextUsedLine(int from) {
        int line1;
        synchronized (this) {
            line1 = findNextUsedLine(from, getMarkMap());
        }

        int line2 = document.getAnnotations().getNextLineWithAnnotation(from + 1);
        
        if (line2 == (-1))
            line2 = Integer.MAX_VALUE;
        
        return line1 < line2 ? line1 : line2;
    }
    
    /*package private*/ static int findNextUsedLine(int from, SortedMap<Integer, List<Mark>> marks) {
        SortedMap<Integer, List<Mark>> next = marks.tailMap(from + 1);
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AnnotationView.findNextUsedLine from: " + from + "; marks: " + marks + "; next: " + next); //NOI18N
        }
        
        if (next.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        
        return next.firstKey().intValue();
    }
    
    private void registerMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AnnotationView.registerMark mark: " + mark + "; from-to: " + span[0] + "-" + span[1]); //NOI18N
        }
        
        for (int line = span[0]; line <= span[1]; line++) {
            List<Mark> inside = marksMap.get(line);
            
            if (inside == null) {
                inside = new ArrayList<Mark>();
                marksMap.put(line, inside);
            }
            
            inside.add(mark);
        }
    }
    
    private void unregisterMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("AnnotationView.unregisterMark mark: " + mark + "; from-to: " + span[0] + "-" + span[1]); //NOI18N
        }
        
        for (int line = span[0]; line <= span[1]; line++) {
            List<Mark> inside = marksMap.get(line);
            
            if (inside != null) {
                inside.remove(mark);
                
                if (inside.size() == 0) {
                    marksMap.remove(line);
                }
            }
        }
    }
    
    /*package private for tests*/synchronized SortedMap<Integer, List<Mark>> getMarkMap() {
        if (marksMap == null) {
            Collection<Mark> marks = getMergedMarks();
            marksMap = new TreeMap<Integer, List<Mark>>();
            
            for (Mark mark : marks) {
                registerMark(mark);
            }
        }
        
        return marksMap;
    }

    @Override
    public Status computeTotalStatus() {
        Status targetStatus = Status.STATUS_OK;
        Collection<Mark> marks = getMergedMarks();
        
        for(Mark mark : marks) {
            Status s = mark.getStatus();
            targetStatus = Status.getCompoundStatus(s, targetStatus);
        }

        for (AnnotationDesc desc : NbCollections.iterable(listAnnotations(-1, Integer.MAX_VALUE))) {
            Status s = get(desc.getAnnotationTypeInstance());

            if (s != null)
                targetStatus = Status.getCompoundStatus(s, targetStatus);
        }
        
        return targetStatus;
    }
    
    @Override
    public UpToDateStatus computeTotalStatusType() {
        if (statusProviders.isEmpty())
            return UpToDateStatus.UP_TO_DATE_DIRTY;
        
        UpToDateStatus statusType = UpToDateStatus.UP_TO_DATE_OK;
        
        for ( UpToDateStatusProvider provider : statusProviders) {
            UpToDateStatus newType = provider.getUpToDate();
            
            if (newType.compareTo(statusType) > 0) {
                statusType = newType;
            }
        }
        
        return statusType;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("marks".equals(evt.getPropertyName())) {
            synchronized (this) {
                @SuppressWarnings("unchecked")
                Collection<Mark> nue = (Collection<Mark>) evt.getNewValue();
                @SuppressWarnings("unchecked")
                Collection<Mark> old = (Collection<Mark>) evt.getOldValue();
                
                if (nue == null && evt.getSource() instanceof MarkProvider)
                    nue = ((MarkProvider) evt.getSource()).getMarks();
                
                if (old != null && nue != null) {
                    Collection<Mark> added = new LinkedHashSet<Mark>(nue);
                    Collection<Mark> removed = new LinkedHashSet<Mark>(old);
                    
                    // own removeAll since indexof on HashSet is faster than on ArrayList and AbstractSet call indexof on smaller collection
                    for (Iterator<Mark> old_it = old.iterator(); old_it.hasNext();) {
                        added.remove(old_it.next());
                    }
                    for (Iterator<Mark> nue_it = nue.iterator(); nue_it.hasNext();) {
                        removed.remove(nue_it.next());   
                    }
                    
                    if (marksMap != null) {
                        for(Mark mark : removed) {
                            unregisterMark(mark);
                        }
                        
                        for(Mark mark : added) {
                            registerMark(mark);
                        }
                    }
                    
                    if (currentMarks != null) {
                        LinkedHashSet<Mark> copy = new LinkedHashSet<Mark>(currentMarks);
                        copy.removeAll(removed);
                        copy.addAll(added);
                        currentMarks = copy;
                    }
                    
                    view.fullRepaint();
                } else {
                    LOG.warning("For performance reasons, the providers should fill both old and new value in property changes. Problematic event: " + evt);
                    clear();
                    view.fullRepaint();
                }
                return ;
            }
        }
        
        if (UpToDateStatusProvider.PROP_UP_TO_DATE.equals(evt.getPropertyName())) {
            view.fullRepaint(false);
            return ;
        }
    }

    public synchronized void clear() {
        currentMarks = null;
        marksMap = null;
    }
    
    public int[] computeErrorsAndWarnings() {
        int errors = 0;
        int warnings = 0;
        Collection<Mark> marks = getMergedMarks();
        
        for(Mark mark : marks) {
            Status s = mark.getStatus();
            
            errors += s == Status.STATUS_ERROR ? 1 : 0;
            warnings += s == Status.STATUS_WARNING ? 1 : 0;
        }
        
        for (AnnotationDesc desc : NbCollections.iterable(listAnnotations(-1, Integer.MAX_VALUE))) {
            Status s = get(desc.getAnnotationTypeInstance());

            if (s != null) {
                errors += s == Status.STATUS_ERROR ? 1 : 0;
                warnings += s == Status.STATUS_WARNING ? 1 : 0;
            }
        }
        
        return new int[] {errors, warnings};
    }
    
    public void changedLine(int Line) {
        changedAll();
    }
    
    public void changedAll() {
        view.fullRepaint(false);
    }
    
    static Status get(Severity severity) {
        if (severity == Severity.STATUS_ERROR)
            return Status.STATUS_ERROR;
        if (severity == Severity.STATUS_WARNING)
            return Status.STATUS_WARNING;
        if (severity == Severity.STATUS_OK)
            return Status.STATUS_OK;
        
        return null;
    }
    
    static Status get(AnnotationType ann) {
        return get(ann.getSeverity());
    }

    private Iterator<? extends AnnotationDesc> listAnnotations(final int startLine, final int endLine) {
        final Annotations annotations = document.getAnnotations();

        return new Iterator<AnnotationDesc>() {
            private final List<AnnotationDesc> remaining = new ArrayList<AnnotationDesc>();
            private int line = startLine;
            private int last = (-1);
            private int unchagedLoops = 0;
            private boolean stop = false;
            @Override public boolean hasNext() {
                if (stop) return false;
                if (remaining.isEmpty()) {
                    if ((line = annotations.getNextLineWithAnnotation(line)) <= endLine && line != (-1)) {
                        if (last == line) {
                            unchagedLoops++;
                            if (unchagedLoops >= 100) {
                                LOG.log(Level.WARNING, "Please add the following info to https://netbeans.org/bugzilla/show_bug.cgi?id=188843 : Possible infinite loop in getMainMarkForBlockAnnotations, debug data: {0}, unchaged loops: {1}", new Object[]{annotations.toString(), unchagedLoops});
                                stop = true;
                                return false;
                            }
                        } else {
                            if (line < last) {
                                LOG.log(Level.WARNING, "Please add the following info to https://netbeans.org/bugzilla/show_bug.cgi?id=188843 : line < last: {0} < {1}", new Object[]{line, last});
                                stop = true;
                                return false;
                            }
                            last = line;
                            unchagedLoops = 0;
                        }

                        AnnotationDesc desc = annotations.getActiveAnnotation(line);

                        if (desc != null) {
                            remaining.add(desc);
                        }

                        if (annotations.getNumberOfAnnotations(line) > 1) {
                            AnnotationDesc[] descriptions = annotations.getPassiveAnnotationsForLine(line);

                            if (descriptions != null) {
                                remaining.addAll(Arrays.asList(descriptions));
                            }
                        }

                        line++;
                    }
                }

                return !(stop = remaining.isEmpty());
            }
            @Override public AnnotationDesc next() {
                if (hasNext()) {
                    return remaining.remove(0);
                } else {
                    throw new NoSuchElementException();
                }
            }
            @Override public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
    
    // XXX: This is here to help to deal with legacy code
    // that registered stuff in text/base. The artificial text/base
    // mime type is deprecated and should not be used anymore.
    @MimeLocation(subfolderName=UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME, instanceProviderClass=LegacyCrapProvider.class)
    public static final class LegacyCrapProvider implements InstanceProvider {

        private final List<FileObject> instanceFiles;
        private List<MarkProviderCreator> creators;
        private List<UpToDateStatusProviderFactory> factories;
        
        public LegacyCrapProvider() {
            this(null);
        }

        public LegacyCrapProvider(List<FileObject> files) {
            this.instanceFiles = files;
        }
        
        public Collection<? extends MarkProviderCreator> getMarkProviderCreators() {
            if (creators == null) {
                computeInstances();
            }
            return creators;
        }

        public Collection<? extends UpToDateStatusProviderFactory> getUpToDateStatusProviderFactories() {
            if (factories == null) {
                computeInstances();
            }
            return factories;
        }
        
        public Object createInstance(List fileObjectList) {
            ArrayList<FileObject> textBaseFilesList = new ArrayList<FileObject>();

            for(Object o : fileObjectList) {
                FileObject fileObject = null;

                if (o instanceof FileObject) {
                    fileObject = (FileObject) o;
                } else {
                    continue;
                }

                String fullPath = fileObject.getPath();
                int idx = fullPath.lastIndexOf(UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME);
                assert idx != -1 : "Expecting files with '" + UP_TO_DATE_STATUS_PROVIDER_FOLDER_NAME + "' in the path: " + fullPath; //NOI18N

                String path = fullPath.substring(0, idx);
                if (TEXT_BASE_PATH.equals(path)) {
                    textBaseFilesList.add(fileObject);
                    if (LOG.isLoggable(Level.WARNING)) {
                        LOG.warning("The 'text/base' mime type is deprecated, please move your file to the root. Offending file: " + fullPath); //NOI18N
                    }
                }
            }

            return new LegacyCrapProvider(textBaseFilesList);
        }
        
        private void computeInstances() {
            ArrayList<MarkProviderCreator> newCreators = new ArrayList<MarkProviderCreator>();
            ArrayList<UpToDateStatusProviderFactory> newFactories = new ArrayList<UpToDateStatusProviderFactory>();
            
            for(FileObject f : instanceFiles) {
                if (!f.isValid() || !f.isData()) {
                    continue;
                }
                
                try {
                    DataObject d = DataObject.find(f);
                    InstanceCookie ic = d.getLookup().lookup(InstanceCookie.class);
                    if (ic != null) {
                        if (MarkProviderCreator.class.isAssignableFrom(ic.instanceClass())) {
                            MarkProviderCreator creator = (MarkProviderCreator) ic.instanceCreate();
                            newCreators.add(creator);
                        } else if (UpToDateStatusProviderFactory.class.isAssignableFrom(ic.instanceClass())) {
                            UpToDateStatusProviderFactory factory = (UpToDateStatusProviderFactory) ic.instanceCreate();
                            newFactories.add(factory);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
            
            this.creators = newCreators;
            this.factories = newFactories;
        }
    } // End of LegacyToolbarActionsProvider class
}
