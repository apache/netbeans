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

package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.GraphContainerKey;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Storage for include graph.
 */
public class GraphContainer extends ProjectComponent implements CsmProgressListener {

    // empty stub
    private static final GraphContainer EMPTY = new GraphContainer() {

        @Override
        public void put() {
        }

        @Override
        public void putFile(CsmFile master) {
        }
    };

    /** Creates a new instance of GraphContainer */
    public GraphContainer(ProjectBase project) {
        super(new GraphContainerKey(project.getUnitId()));
        CsmListeners.getDefault().addProgressListener(GraphContainer.this);
        put();
    }

    public GraphContainer(final RepositoryDataInput input) throws IOException {
        super(input);
        assert input != null;
        CsmListeners.getDefault().addProgressListener(GraphContainer.this);
        readUIDToNodeLinkMap(input, graph);
    }

    // only for EMPTY static field
    private GraphContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null);
    }

    public static GraphContainer empty() {
        return EMPTY;
    }

    /**
     * save file graph.
     * called after (re)parse.
     */
    public void putFile(CsmFile master){
        CsmUID<CsmFile> key = UIDCsmConverter.fileToUID(master);
        if (key != null) {
            graphLock.writeLock().lock();
            try {
                NodeLink node = graph.get(key);
                if (node != null){
                    Set<CsmUID<CsmFile>> outLink = node.getOutLinks();
                    for (CsmUID<CsmFile> out : outLink){
                        NodeLink pair = graph.get(out);
                        if (pair != null){
                            pair.removeInLink(key);
                        }
                    }
                    outLink.clear();
                } else {
                    node = new NodeLink();
                    graph.put(key,node);
                }
                for (CsmInclude include : master.getIncludes()){
                    CsmFile to = include.getIncludeFile();
                    if (to != null) {
                        CsmUID<CsmFile> out = UIDCsmConverter.fileToUID(to);
                        NodeLink pair = graph.get(out);
                        if (pair == null){
                            pair = new NodeLink();
                            graph.put(out,pair);
                        }
                        node.addOutLink(out);
                        pair.addInLink(key);
                    }
                }
            } finally {
                graphLock.writeLock().unlock();
            }
        }
        put();
    }

    /**
     * remove file graph.
     * called after remove, delelete.
     */
    public void removeFile(CsmFile master){
        CsmUID<CsmFile> key = UIDCsmConverter.fileToUID(master);
        if (key != null) {
            graphLock.writeLock().lock();
            try {
                NodeLink node = graph.get(key);
                if (node != null){
                    Set<CsmUID<CsmFile>> inLink = node.getInLinks();
                    for (CsmUID<CsmFile> in : inLink){
                        NodeLink pair = graph.get(in);
                        if (pair != null){
                            pair.removeOutLink(key);
                        }
                    }
                    inLink.clear();
                    Set<CsmUID<CsmFile>> outLink = node.getOutLinks();
                    for (CsmUID<CsmFile> out : outLink){
                        NodeLink pair = graph.get(out);
                        if (pair != null){
                            pair.removeInLink(key);
                        }
                    }
                    outLink.clear();
                    graph.remove(key);
                }
            } finally {
                graphLock.writeLock().unlock();
            }
        }
    	put();
    }

    /**
     * gets all direct or indirect included files into the  referenced file.
     */
    public Set<CsmUID<CsmFile>> getIncludedFilesUids(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<>();
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(referencedFile);
        if (keyFrom != null) {
            graphLock.readLock().lock();
            try {
                getIncludedFiles(res, keyFrom);
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return res;
    }

    /**
     * gets all direct or indirect included files into the  referenced file.
     */
    public Set<CsmFile> getIncludedFiles(CsmFile referencedFile){
        return convertToFiles(getIncludedFilesUids(referencedFile));
    }

    private final LinkedList<WeakReference<HotSpotFile>> hotSpot = new LinkedList<>();
    private final static int HOT_SPOT_SIZE = 10;

    public boolean isFileIncluded(CsmFile sourceFile, CsmFile headerFile) {
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(sourceFile);
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(headerFile);
        if (keyFrom != null && keyTo != null) {
            synchronized (hotSpot) {
                Iterator<WeakReference<HotSpotFile>> iterator = hotSpot.iterator();
                while(iterator.hasNext()) {
                    WeakReference<HotSpotFile> ref = iterator.next();
                    HotSpotFile file = ref.get();
                    if (file != null) {
                        if (file.from.equals(keyFrom)) {
                            return file.to.contains(keyTo);
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
            Set<CsmUID<CsmFile>> res = new HashSet<>();
            Map<Integer, GraphContainer> map = new HashMap<>();
            try {
                gatherIncludedFiles(map, res, keyFrom);
            } finally {
                for(GraphContainer current : map.values()){
                    current.graphLock.readLock().unlock();
                }
            }
            synchronized (hotSpot) {
                if (hotSpot.size() > HOT_SPOT_SIZE) {
                    hotSpot.removeFirst();
                }
                hotSpot.addLast(new WeakReference<>(new HotSpotFile(keyFrom, res)));
                return res.contains(keyTo);
            }
        }
        return false;
    }

    private void gatherIncludedFiles(Map<Integer, GraphContainer> map, Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyFrom) {
        GraphContainer current = map.get(UIDUtilities.getProjectID(keyFrom));
        if (current == null) {
            CsmFile file = UIDCsmConverter.UIDtoFile(keyFrom);
            if (file == null || !file.isValid()) {
                return;
            }
            ProjectBase prj = (ProjectBase) file.getProject();
            if (prj == null || !prj.isValid()) {
                return;
            }
            current = prj.getGraphStorage();
            if (current == null) {
                return;
            }
            map.put(UIDUtilities.getProjectID(keyFrom), current);
            current.graphLock.readLock().lock();
        }
        NodeLink node = current.graph.get(keyFrom);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getOutLinks()){
                if (res.add(uid)){
                    gatherIncludedFiles(map, res, uid);
                }
            }
        }
    }

    /**
     * gets all files that direct or indirect include the referenced file.
     */
    public Set<CsmUID<CsmFile>> getParentFilesUids(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                getParentFiles(res, keyTo);
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return res;
    }

    /**
     * gets all files that direct or indirect include the referenced file.
     */
    public Set<CsmFile> getParentFiles(CsmFile referencedFile){
        return convertToFiles(getParentFilesUids(referencedFile));
    }

    /**
     * gets all files that direct or indirect include the referenced file.
     * return files that not included into other files.
     * If set empty then return set with the referenced file.
     */
    public ParentFiles getTopParentFiles(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> parent = new HashSet<>();
        Set<CsmUID<CsmFile>> top = new HashSet<>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                getParentFiles(parent, keyTo);
                if (parent.isEmpty()) {
                    parent.add(keyTo);
                }
                for(CsmUID<CsmFile> uid : parent){
                    NodeLink link = graph.get(uid);
                    if (link != null && link.getInLinks().isEmpty()){
                        top.add(uid);
                    }
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return new ParentFiles(top, parent);
    }

    /**
     * gets all files that direct or indirect include referenced file.
     * If set empty then return set with the referenced file.
     */
    public CoherenceFiles getCoherenceFiles(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> parent = new HashSet<>();
        Set<CsmUID<CsmFile>> coherence = new HashSet<>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                getParentFiles(parent, keyTo);
                if (parent.isEmpty()) {
                    parent.add(keyTo);
                }
                coherence.addAll(parent);
                for(CsmUID<CsmFile> uid : parent){
                    getIncludedFiles(coherence, uid);
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return new CoherenceFiles(parent, coherence);
    }

    /**
     *  Returns set files that direct include referenced file.
     */
    public Set<CsmUID<CsmFile>> getInLinksUids(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                NodeLink node = graph.get(keyTo);
                if (node != null) {
                    for(CsmUID<CsmFile> uid : node.getInLinks()){
                        res.add(uid);
                    }
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return res;
    }

    /**
     *  Returns set files that direct include referenced file.
     */
    public Set<CsmFile> getInLinks(CsmFile referencedFile){
        return convertToFiles(getInLinksUids(referencedFile));
    }

    /**
     *  Returns set of direct included files in the referenced file.
     */
    public Set<CsmUID<CsmFile>> getOutLinksUids(CsmFile referencedFile){
        Set<CsmUID<CsmFile>> res = new HashSet<>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(referencedFile);
        if (keyTo != null) {
            graphLock.readLock().lock();
            try {
                NodeLink node = graph.get(keyTo);
                if (node != null) {
                    for(CsmUID<CsmFile> uid : node.getOutLinks()){
                        res.add(uid);
                    }
                }
            } finally {
                graphLock.readLock().unlock();
            }
        }
        return res;
    }

    /**
     *  Returns set of direct included files in the referenced file.
     */
    public Set<CsmFile> getOutLinks(CsmFile referencedFile){
        return convertToFiles(getOutLinksUids(referencedFile));
    }

    public static Set<CsmFile> convertToFiles(Set<CsmUID<CsmFile>> res) {
        Set<CsmFile> res2= new HashSet<>();
        for(CsmUID<CsmFile> uid : res) {
            CsmFile file = UIDCsmConverter.UIDtoFile(uid);
            if (file != null && file.isValid()) {
                res2.add(file);
            }
        }
        return res2;
    }

    /*
     * method called in synchronized block
     */
    private void getIncludedFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyFrom){
        NodeLink node = graph.get(keyFrom);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getOutLinks()){
                if (res.add(uid)){
                    getIncludedFiles(res, uid);
                }
            }
        }
    }

    /*
     * method called in synchronized block
     */
    private void getParentFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyTo){
        NodeLink node = graph.get(keyTo);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getInLinks()){
                if (res.add(uid)){
                    getParentFiles(res, uid);
                }
            }
        }
    }

    public void clear() {
        graphLock.writeLock().lock();
        try {
            graph.clear();
        } finally {
            graphLock.writeLock().unlock();
        }
        put();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        // need a write lock
        graphLock.writeLock().lock();
        try {
            writeUIDToNodeLinkMap(output, graph);
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    private static void writeUIDToNodeLinkMap (
            final RepositoryDataOutput output, final Map<CsmUID<CsmFile>,NodeLink> aMap) throws IOException {

        assert output != null;
        assert aMap != null;

        UIDObjectFactory uidFactory = UIDObjectFactory.getDefaultFactory();
        assert uidFactory != null;

        output.writeInt(aMap.size());

        final Set<Entry<CsmUID<CsmFile>,NodeLink>> entrySet = aMap.entrySet();
        final Iterator<Entry<CsmUID<CsmFile>,NodeLink>> setIterator = entrySet.iterator();

        while (setIterator.hasNext()) {
            final Entry<CsmUID<CsmFile>,NodeLink> anEntry = setIterator.next();
            assert anEntry != null;

            uidFactory.writeUID(anEntry.getKey(), output);
            anEntry.getValue().write(output);
        }
    }

    private static void readUIDToNodeLinkMap (
            final RepositoryDataInput input, Map<CsmUID<CsmFile>,NodeLink> aMap) throws IOException {

        assert input != null;
        assert aMap != null;
        UIDObjectFactory uidFactory = UIDObjectFactory.getDefaultFactory();
        assert uidFactory != null;

        aMap.clear();

        final int size = input.readInt();

        for (int i = 0; i < size; i++) {
            final CsmUID<CsmFile> uid = uidFactory.readUID(input);
            final NodeLink        link = new NodeLink(input);

            assert uid != null;
            assert link != null;

            aMap.put(uid, link);
        }

    }

    private final Map<CsmUID<CsmFile>,NodeLink> graph = new HashMap<>();
    private final ReadWriteLock graphLock = new ReentrantReadWriteLock();

    @Override
    public void projectParsingStarted(CsmProject project) {
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
    }

    @Override
    public void projectLoaded(CsmProject project) {
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
    }

    @Override
    public void fileParsingFinished(CsmFile sourceFile) {
        synchronized (hotSpot) {
            hotSpot.clear();
        }
    }

    @Override
    public void parserIdle() {
    }

    @Override
    public void fileRemoved(CsmFile file) {
    }

    private static class NodeLink implements SelfPersistent, Persistent {

        final Set<CsmUID<CsmFile>> in;
        final Set<CsmUID<CsmFile>> out;

        private NodeLink(){
            in = new HashSet<>();
            out = new HashSet<>();
        }

        private NodeLink(final RepositoryDataInput input) throws IOException {
            assert input != null;

            final UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
            assert factory != null;
            int collSize = input.readInt();
            if (collSize < 0) {
                in = new HashSet<>(0);
            } else {
                in = new HashSet<>(collSize);
            }
            factory.readUIDCollection(in, input, collSize);
            collSize = input.readInt();
            if (collSize < 0) {
                out = new HashSet<>(0);
            } else {
                out = new HashSet<>(collSize);
            }
            factory.readUIDCollection(out, input, collSize);
        }

        private void addInLink(CsmUID<CsmFile> inLink){
            in.add(inLink);
        }
        private void removeInLink(CsmUID<CsmFile> inLink){
            in.remove(inLink);
        }
        private Set<CsmUID<CsmFile>> getInLinks(){
            return in;
        }
        private void addOutLink(CsmUID<CsmFile> inLink){
            out.add(inLink);
        }
        private void removeOutLink(CsmUID<CsmFile> inLink){
            out.remove(inLink);
        }
        private Set<CsmUID<CsmFile>> getOutLinks(){
            return out;
        }

        @Override
        public void write(final RepositoryDataOutput output) throws IOException {
            assert output != null;
            assert in != null;
            assert out != null;

            final UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
            assert factory != null;

            factory.writeUIDCollection(in, output, false);
            factory.writeUIDCollection(out, output, false);
        }
    }

    public static final class ParentFiles {
        private final Set<CsmUID<CsmFile>> comilationUnits;
        private final Set<CsmUID<CsmFile>> parentFiles;
        private ParentFiles(Set<CsmUID<CsmFile>> comilationUnits, Set<CsmUID<CsmFile>> parentFiles){
            this.comilationUnits = comilationUnits;
            this.parentFiles = parentFiles;
        }
        public Set<CsmFile> getCompilationUnits(){
            return convertToFiles(comilationUnits);
        }
        public Set<CsmUID<CsmFile>> getCompilationUnitsUids(){
            return comilationUnits;
        }
        public Set<CsmFile> getParentFiles(){
            return convertToFiles(parentFiles);
        }
        public Set<CsmUID<CsmFile>> getParentFilesUids(){
            return parentFiles;
        }
    }

    public static final class CoherenceFiles {
        private final Set<CsmUID<CsmFile>> coherenceFiles;
        private final Set<CsmUID<CsmFile>> parentFiles;
        private CoherenceFiles(Set<CsmUID<CsmFile>> parentFiles, Set<CsmUID<CsmFile>> coherenceFiles){
            this.parentFiles = parentFiles;
            this.coherenceFiles = coherenceFiles;
        }
        public Set<CsmFile> getCoherenceFiles(){
            return convertToFiles(coherenceFiles);
        }
        public Set<CsmUID<CsmFile>> getCoherenceFilesUids(){
            return coherenceFiles;
        }
        public Set<CsmFile> getParentFiles(){
            return convertToFiles(parentFiles);
        }
        public Set<CsmUID<CsmFile>> getParentFilesUids(){
            return parentFiles;
        }
    }

    private static final class HotSpotFile {
         private final CsmUID<CsmFile> from;
         private final Set<CsmUID<CsmFile>> to;
         private HotSpotFile(CsmUID<CsmFile> from, Set<CsmUID<CsmFile>> to) {
             this.from = from;
             this.to = to;
         }
    }
}
