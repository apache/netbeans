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
package org.netbeans.modules.localhistory.store;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author tomas
 */
public class Store {
               
    public void logStore() throws Exception {        
        StoreMap storeMap = StoreMap.create(new File("/data/work/workspaces/nb60/var/filehistory"));
        log(storeMap);           
    }
    
    private void log(StoreMap storeMap) throws Exception {
        for(Map.Entry<File, StoreEntries> entry : storeMap.entrySet()) {            
            logS(entry.getKey().getAbsolutePath());   
            logData(entry.getValue().getData());
            logHistory(entry.getValue().getHistory());
            logFiles(entry.getValue().getFiles());
        }                        
    }    
    
    private void logData(DataStoreEntry data) throws Exception {                        
        if(data == null) return;
        logS("\t" + data.toString());        
    }

    private void logFiles(FilesStoreEntry files) {
        if(files == null || files.size() < 1) return;        
        logS("");        
        for(FileEntry entry : files) {
            logS("\t" + entry.toString());
        }                
    }
       
    private void logHistory(HistoryStoreEntry history) throws Exception {        
        if(history == null || history.size() < 1) return;        
        logS("");        
        for(HistoryEntry entry : history) {
            logS("\t" + entry.toString());
        }        
    }        
    
    private void logS(String str) {
        System.out.println(str);
    }        

    protected static String formatStatus(Object obj) {
        int status = (Integer) obj;
        return status == 0 ? "DELETED" : "TOUCHED";
    }
    protected static String formatFile(Object obj) {
        boolean isFile = (Boolean) obj;
        return isFile ? "File" : "Directory";
    }
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");                        
    protected static String formatTimestamp(Object obj) {
        long ts = (Long) obj;
        return dateFormat.format(new Date(ts));
    }        
        
    private static Object getGetterValue(Object obj, String methodName) {
        try {
            Method m = obj.getClass().getDeclaredMethod(methodName, new Class[]{});
            m.setAccessible(true);        
            return m.invoke(obj, new Object[] {});        
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }                

    private static Object storage;    
    private static Object getStorage() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if(storage == null) {
            storage = LocalHistoryStoreFactory.getInstance().createLocalHistoryStorage();                
        }    
        return storage;
    }
    
    private static class StoreEntry {
        static final String DATA    = "data";
        static final String HISTORY = "history";
        static final String LABELS  = "labels";
        static final String FILES    = "file";
        private final String type;        
        public StoreEntry(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }        
    }     
    private static class DataStoreEntry extends StoreEntry {
        private Object storeDataFile;
        public DataStoreEntry(File dataFile) throws Exception {
            super(DATA);            
            Method readStoreData = getStorage().getClass().getDeclaredMethod("readStoreData", new Class[] {File.class, boolean.class});
            readStoreData.setAccessible(true);            
            storeDataFile = readStoreData.invoke(getStorage(), new Object[]{dataFile, false});                                           
        }
        int getStatus() {
            return (Integer) getGetterValue(storeDataFile, "getStatus");
        }        
        long getLastModified() {
            return (Long) getGetterValue(storeDataFile, "getLastModified");
        }
        String getAbsolutePath() {
            return (String) getGetterValue(storeDataFile, "getAbsolutePath");
        }    
        boolean isFile() {
            return (Boolean) getGetterValue(storeDataFile, "isFile");
        }
        public String toString() {
            return formatTimestamp(getLastModified()) + " " +
                   formatStatus(getStatus()) + " " + 
                   formatFile(isFile()) + " " +
                   getAbsolutePath();
        }
    }
    private abstract static class ListStoreEntry<E> extends StoreEntry implements List<E> {
        private List<E> list = new ArrayList<E>();

        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        public Object[] toArray() {
            return list.toArray();
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return list.subList(fromIndex, toIndex);
        }

        public int size() {
            return list.size();
        }

        public E set(int index, E element) {
            return list.set(index, element);
        }

        public boolean retainAll(Collection<?> c) {
            return list.retainAll(c);
        }

        public boolean removeAll(Collection<?> c) {
            return list.removeAll(c);
        }

        public E remove(int index) {
            return list.remove(index);
        }

        public boolean remove(Object o) {
            return list.remove(o);
        }

        public ListIterator<E> listIterator(int index) {
            return list.listIterator(index);
        }

        public ListIterator<E> listIterator() {
            return list.listIterator();
        }

        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        public Iterator<E> iterator() {
            return list.iterator();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        public int hashCode() {
            return list.hashCode();
        }

        public E get(int index) {
            return list.get(index);
        }

        public boolean equals(Object o) {
            return list.equals(o);
        }

        public boolean containsAll(Collection<?> c) {
            return list.containsAll(c);
        }

        public boolean contains(Object o) {
            return list.contains(o);
        }

        public void clear() {
            list.clear();
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            return list.addAll(index, c);
        }

        public boolean addAll(Collection<? extends E> c) {
            return list.addAll(c);
        }

        public void add(int index, E element) {
            list.add(index, element);
        }

        public boolean add(E e) {
            return list.add(e);
        }
        public ListStoreEntry(String type) {
            super(type);
        }               
    }    
    private static class HistoryEntry {
        private final Object entry;
        public HistoryEntry(Object entry) {                        
            this.entry = entry;
        }
        int getStatus() {
            return (Integer) getGetterValue(entry, "getStatus");
        }        
        long getTimestamp() {
            return (Long) getGetterValue(entry, "getTimestamp");
        }
        String getFrom() {
            return (String) getGetterValue(entry, "getFrom");
        }    
        String getTo() {
            return (String) getGetterValue(entry, "getTo");
        }
        @Override
        public String toString() {
            return formatTimestamp(getTimestamp()) + " " + formatStatus(getStatus()) + " " +  getFrom() + " -> " + getTo();                                    
        }        
    }        
    private static class HistoryStoreEntry extends ListStoreEntry<HistoryEntry> {                
        private ArrayList<HistoryEntry> historyList;
        public HistoryStoreEntry(File historyFile) throws Exception {
            super(HISTORY);            
            Method readStoreData = getStorage().getClass().getDeclaredMethod("readHistory", new Class[] {File.class});
            readStoreData.setAccessible(true);            
            List l = (List) readStoreData.invoke(getStorage(), new Object[]{historyFile});                                       
            historyList = new ArrayList<HistoryEntry>(l.size());
            for(Object obj : l) {
                historyList.add(new HistoryEntry(obj));
            }                    
        }                                 
    }        
    private static class FileEntry {
            private final File file;
            private final long ts;            
            public FileEntry(File file) {
                this.file = file;
                ts = Long.parseLong(file.getName());
            }
            public long getTs() {
                return ts;
            }
            @Override
            public String toString() {
                return formatTimestamp(getTs()) + " " + getTs();
            }
            
        }
    private static class FilesStoreEntry extends ListStoreEntry<FileEntry> {     
        private List<FileEntry> files;
        public FilesStoreEntry() {
            super(FILES);              
        }
    }        
    private static class LabelsStoreEntry extends StoreEntry {        
        public LabelsStoreEntry(File dataFile) {
            super(LABELS);                                            
        }        
    }            
    private static class StoreEntries extends HashMap<String, StoreEntry> {        
        void createEntry(File file) throws Exception {
            if(file.getName().equals(StoreEntry.DATA)) {
                putData(new DataStoreEntry(file));
            } else if(file.getName().equals(StoreEntry.HISTORY)) {
                putHistory(new HistoryStoreEntry(file));
            } else if(file.getName().equals(StoreEntry.LABELS)) {
                putLabels(new LabelsStoreEntry(file));
            } else {
                FilesStoreEntry files = getFiles();
                if(files == null) {
                    files = new FilesStoreEntry();
                }
                files.add(new FileEntry(file));
                putFiles(files);                
            }                 
        }
        static StoreEntries create(File storeFolder) throws Exception  {
            StoreEntries ret = new StoreEntries();
            File[] files = storeFolder.listFiles();
            for(File file : files) {
                ret.createEntry(file);
            }            
            return ret;
        }
        void putData(DataStoreEntry data) {
            put(StoreEntry.DATA, data);
        }
        DataStoreEntry getData() {
            return (DataStoreEntry) get(StoreEntry.DATA);
        }
        void putHistory(HistoryStoreEntry data) {
            put(StoreEntry.HISTORY, data);
        }
        HistoryStoreEntry getHistory() {
            return (HistoryStoreEntry) get(StoreEntry.HISTORY);
        }        
        void putLabels(LabelsStoreEntry data) {
            put(StoreEntry.LABELS, data);
        }
        LabelsStoreEntry getLabels() {
            return (LabelsStoreEntry) get(StoreEntry.LABELS);
        }                
        void putFiles(FilesStoreEntry data) {
            put(StoreEntry.FILES, data);
        }
        FilesStoreEntry getFiles() {
            return (FilesStoreEntry) get(StoreEntry.FILES);
        }                        
    }
    private static class StoreMap extends HashMap<File, StoreEntries> {
        static StoreMap create(File storeRootFile) throws Exception {
            StoreMap map = new StoreMap();
            File[] topLevelFiles = storeRootFile.listFiles();                
            if(topLevelFiles == null || topLevelFiles.length == 0) {
                return map;
            }
            for(File topLevelFile : topLevelFiles) {                        
                File[] secondLevelFiles = topLevelFile.listFiles();
                if(secondLevelFiles == null || secondLevelFiles.length == 0) {
                    // XXX add empty
                    continue;
                }            
                for(File secondLevelFile : secondLevelFiles) {                                                                   
                    map.put(secondLevelFile, StoreEntries.create(secondLevelFile));
                }                     
            }         
            return map;
        }
    };    
}
