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
package org.netbeans.modules.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.OnStop;
import org.openide.modules.Places;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Jan Lahoda
 */
public class TrieDictionary implements Dictionary {

    private static final Logger LOG = Logger.getLogger(TrieDictionary.class.getName());
    
    private final byte[] array;
    private final ByteBuffer buffer;

    TrieDictionary(byte[] array) {
        this.array = array;
        this.buffer = null;
    }

    private TrieDictionary(File data) throws IOException {
        this.array = null;

        FileInputStream ins = new FileInputStream(data);
        FileChannel channel = ins.getChannel();
        
        try {
            this.buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
        } finally {
            channel.close();
            ins.close();
        }
    }

    public ValidityType validateWord(CharSequence word) {
        String wordString = word.toString();
        ValidityType type = validateWordImpl(wordString.toLowerCase());

        if (type != ValidityType.VALID) {
            ValidityType curr = validateWordImpl(wordString);

            if (type == ValidityType.PREFIX_OF_VALID) {
                if (curr == ValidityType.VALID) {
                    type = curr;
                }
            } else {
                type = curr;
            }
        }

        return type;
    }

    private ValidityType validateWordImpl(CharSequence word) {
        int node = findNode(word, 0, 4);

        if (node == (-1))
            return ValidityType.INVALID;

        if (readByte(node) == 0x01) {
            return ValidityType.VALID;
        }

        return ValidityType.PREFIX_OF_VALID;
    }

    public List<String> findValidWordsForPrefix(CharSequence word) {
        List<String> result = new ArrayList<String>();
        int node = findNode(word, 0, 4);
        
        if (node == (-1))
            return Collections.emptyList();
        
        return findValidWordsForPrefix(new StringBuffer(word), node, result);
    }

    public List<String> findProposals(CharSequence pattern) {
        ListProposalAcceptor result = new ListProposalAcceptor();
        
        findProposals(pattern, 2, 4, new StringBuffer(), result);
        
        return result;
    }
    
    private void findProposals(CharSequence pattern, int maxDistance, int node, StringBuffer word, ProposalAcceptor result) {
        int entries = readInt(node + 1);
        
        for (int currentEntry = 0; currentEntry < entries; currentEntry++) {
            char ac = readChar(node + 5 + currentEntry * 6);
            
            word.append(ac);
            
            int distance = distance(pattern, word);
            int targetNode = node + readInt(node + 5 + currentEntry * 6 + 2);
            
            if (distance < maxDistance) {
                if (readByte(targetNode) == 0x01) {
                    result.add(word.toString());
                }
            }
                
            if ((distance - (pattern.length() - word.length())) < maxDistance) {
                findProposals(pattern, maxDistance, targetNode, word, result);
            }
            
            word.deleteCharAt(word.length() - 1);
        }
    }
    
    private void verifyDictionary() {
        findProposals("", Integer.MAX_VALUE, 4, new StringBuffer(), NULL_ACCEPTOR);    
    }

    private List<String> findValidWordsForPrefix(StringBuffer foundSoFar, int node, List<String> result) {
        int entries = readInt(node + 1);
        
        for (int currentEntry = 0; currentEntry < entries; currentEntry++) {
            char ac = readChar(node + 5 + currentEntry * 6);
            
            foundSoFar.append(ac);
            
            int targetNode = node + readInt(node + 5 + currentEntry * 6 + 2);
            
            if (readByte(targetNode) == 0x01) {
                result.add(foundSoFar.toString());
            }
                
            findValidWordsForPrefix(foundSoFar, targetNode, result);
            
            foundSoFar.deleteCharAt(foundSoFar.length() - 1);
        }
        
        return result;
    }
    
    private int findNode(CharSequence word, int currentCharOffset, int currentNode) {
        if (word.length() <= currentCharOffset)
            return currentNode;

        char c = word.charAt(currentCharOffset);
        int entries = readInt(currentNode + 1);

        for (int currentEntry = 0; currentEntry < entries; currentEntry++) {
            char ac = readChar(currentNode + 5 + currentEntry * 6);

            if (ac == c) {
                int newNodeOffset = readInt(currentNode + 5 + currentEntry * 6 + 2);

                int newNode = currentNode + newNodeOffset;

                return findNode(word, currentCharOffset + 1, newNode);
            }
        }

        return -1;
    }

    private static final int CURRENT_TRIE_DICTIONARY_VERSION = 2;

    public static Dictionary getDictionary(String suffix, List<URL> sources) throws IOException {
        File trie = Places.getCacheSubfile("dict/dictionary" + suffix + ".trie" + CURRENT_TRIE_DICTIONARY_VERSION);
        
        return getDictionary(trie, sources);
    }

    static Dictionary getDictionary(File trie, List<URL> sources) throws IOException {
        return new FutureDictionary(trie, sources);
    }

    private static int toUnsigned(byte b) {
        if (b < 0) {
            return 256 + b;
        }

        return b;
    }

    private int readInt(int pos) {
        return (toUnsigned(readByte(pos + 0)) << 24) + (toUnsigned(readByte(pos + 1)) << 16) + (toUnsigned(readByte(pos + 2)) << 8) + toUnsigned(readByte(pos + 3));
    }

    private char readChar(int pos) {
        return (char) ((toUnsigned(readByte(pos + 0)) << 8) + toUnsigned(readByte(pos + 1)));
    }

    private byte readByte(int pos) {
        if (buffer != null) {
            return buffer.get(pos);
        } else {
            return array[pos];
        }
    }

    private static boolean compareChars(char c1, char c2) {
        return c1 == c2 || Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }
    
    private static int distance(CharSequence pattern, CharSequence word) {
        int[] old = new int[pattern.length() + 1];
        int[] current = new int[pattern.length() + 1];
        int[] oldLength = new int[pattern.length() + 1];
        int[] length = new int[pattern.length() + 1];
        
        for (int cntr = 0; cntr < old.length; cntr++) {
            old[cntr] = pattern.length() + 1;//cntr;
            oldLength[cntr] = (-1);
        }
        
        current[0] = old[0] = oldLength[0] = length[0] = 0;
        
        int currentIndex = 0;
        
        while (currentIndex < word.length()) {
            for (int cntr = 0; cntr < pattern.length(); cntr++) {
                int insert = old[cntr + 1] + 1;
                int delete = current[cntr] + 1;
                int replace = old[cntr] + (compareChars(pattern.charAt(cntr), word.charAt(currentIndex)) ? 0 : 1);
                
                if (insert < delete) {
                    if (insert < replace) {
                        current[cntr + 1] = insert;
                        length[cntr + 1] = oldLength[cntr + 1] + 1;
                    } else {
                        current[cntr + 1] = replace;
                        length[cntr + 1] = oldLength[cntr] + 1;
                    }
                } else {
                    if (delete < replace) {
                        current[cntr + 1] = delete;
                        length[cntr + 1] = length[cntr];
                    } else {
                        current[cntr + 1] = replace;
                        length[cntr + 1] = oldLength[cntr] + 1;
                    }
                }
            }
            
            currentIndex++;
            
            int[] temp = old;
            
            old = current;
            current = temp;
            
            temp = oldLength;
            
            oldLength = length;
            length = temp;
        }
        
        return old[pattern.length()];
    }
    
    private static void constructTrie(ByteArray array, List<URL> sources) throws IOException {
        SortedSet<CharSequence> data = new TreeSet<CharSequence>();

        for (URL u : sources) {
            FileObject f = URLMapper.findFileObject(u);
            u = f != null ? URLMapper.findURL(f, URLMapper.EXTERNAL) : u;
            BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream(), StandardCharsets.UTF_8));
            
            try {
                String line;
                
                while ((line = in.readLine()) != null) {
                    data.add(CharSequences.create(line));
                }
            } finally {
                //TODO: wrap in try - catch:
                in.close();
            }
        }
        
        constructTrieData(array, data);
    }
    
    private static void constructTrieData(ByteArray array, SortedSet<? extends CharSequence> data) throws IOException {
        array.put(0, CURRENT_TRIE_DICTIONARY_VERSION);
        encodeOneLayer(array, 4, 0, data);
    }

    private static int encodeOneLayer(ByteArray array, int currentPointer, int currentChar, SortedSet<? extends CharSequence> data) throws IOException {
        Map<Character, SortedSet<CharSequence>> char2Words = new TreeMap<Character, SortedSet<CharSequence>>();
        boolean representsFullWord = !data.isEmpty() && data.first().length() <= currentChar;
        Iterator<? extends CharSequence> dataIt = data.iterator();

        if (representsFullWord) {
            dataIt.next();
        }

        while (dataIt.hasNext()) {
            CharSequence word = dataIt.next();
            char c = word.charAt(currentChar);
            SortedSet<CharSequence> words = char2Words.get(c);

            if (words == null) {
                char2Words.put(c, words = new TreeSet<CharSequence>());
            }

            words.add(word);
        }

        int entries = char2Words.size();

        //write flags:
        byte flags = 0x00;

        if (representsFullWord) {
            flags = 0x01;
        }

        array.put(currentPointer, flags);
        array.put(currentPointer + 1, entries);

        int currentEntry = 0;
        int childPointer = currentPointer + 5 + entries * 6;

        for (Entry<Character, SortedSet<CharSequence>> e : char2Words.entrySet()) {
            array.put(currentPointer + 5 + currentEntry * 6, e.getKey());
            array.put(currentPointer + 5 + currentEntry * 6 + 2, childPointer - currentPointer);

            childPointer = encodeOneLayer(array, childPointer, currentChar + 1, e.getValue());

            currentEntry++;
        }

        return childPointer;
    }

    private static final RequestProcessor WORKER = new RequestProcessor(TrieDictionary.class.getName(), 1, false, false);
    private static final class FutureDictionary implements Dictionary, Runnable {
        private final File trie;
        private final List<URL> sources;
        private final AtomicReference<Dictionary> delegate = new AtomicReference<Dictionary>();
        private final AtomicReference<Task> workingTask = new AtomicReference<Task>();
        private final AtomicBoolean wasBroken = new AtomicBoolean();

        public FutureDictionary(File trie, List<URL> sources) throws IOException {
            this.trie = trie;
            this.sources = sources;
            workingTask.set(WORKER.post(this));
        }
        
        public ValidityType validateWord(CharSequence word) {
            waitDictionaryConstructed();

            Dictionary dict = delegate.get();

            if (dict != null) {
                try {
                    return dict.validateWord(word);
                } catch (IndexOutOfBoundsException ex) {
                    rebuild(ex);
                }
            }

            return ValidityType.VALID;
        }

        public List<String> findValidWordsForPrefix(CharSequence word) {
            waitDictionaryConstructed();

            Dictionary dict = delegate.get();

            if (dict != null) {
                try {
                    return dict.findValidWordsForPrefix(word);
                } catch (IndexOutOfBoundsException ex) {
                    rebuild(ex);
                }
            }

            return Collections.emptyList();
        }

        public List<String> findProposals(CharSequence word) {
            waitDictionaryConstructed();

            Dictionary dict = delegate.get();

            if (dict != null) {
                try {
                    return dict.findProposals(word);
                } catch (IndexOutOfBoundsException ex) {
                    rebuild(ex);
                }
            }

            return Collections.emptyList();
        }

        private void waitDictionaryConstructed() {
            Task t = workingTask.get();

            if (t != null) {
                t.waitFinished();
                workingTask.set(null);
            }
        }

        private void rebuild(Throwable t) {
            //the on disk cache is likely broken, attempt to fix:
            if (!wasBroken.getAndSet(true)) {
                LOG.log(Level.INFO, "An exception thrown while read dictionary cache, attempting to rebuild.", t);
                workingTask.set(WORKER.post(this));
            } else {
                LOG.log(Level.INFO, "An exception thrown while read dictionary cache for second time, giving up.", t);
                delegate.set(null);
            }
        }

        public void run() {
            trie.getParentFile().mkdirs();
            
            if (trie.canRead()) {
                //validate the dictionary:
                try {
                    TrieDictionary d = new TrieDictionary(trie);
                    d.verifyDictionary();
                    delegate.set(d);
                    return ;//valid
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Dictionary file failed validation, attempting to rebuild", ex);
                } catch (IndexOutOfBoundsException ex) {
                    LOG.log(Level.INFO, "Dictionary file failed validation, attempting to rebuild", ex);
                }
            }

            trie.delete();
            
            File temp = new File(trie.getParentFile(), "dict.temp");
            
            temp.delete();
            
            ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(TrieDictionary.class, "BuildingDictionary"));
            try {
                handle.start();
                ByteArray array = new ByteArray(temp);

                constructTrie(array, sources);
                array.close();
                LOG.log(Level.FINE, "trie file length: {0}", temp.length());
                temp.renameTo(trie);
                if (trie.canRead()) {
                    TrieDictionary d = new TrieDictionary(trie);
                    
                    delegate.set(d);

                    try {
                        d.verifyDictionary();
                    } catch (IndexOutOfBoundsException ex) {
                        LOG.log(Level.INFO, "Cannot read the dictionary file", ex);
                        wasBroken.set(true);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                workingTask.set(null);
                if (temp.exists()) {
                    LOG.log(Level.INFO, "Something went wrong during dictionary construction, the temporary file still exists - deleting.");
                    temp.delete();
                }
                handle.finish();
            }
        }
    }
    
    private static interface ProposalAcceptor {
        public boolean add(String proposal);
    }
    
    private static class ListProposalAcceptor extends ArrayList<String> implements ProposalAcceptor {}
    
    private static class NullProposalAcceptor implements ProposalAcceptor {
        @Override public boolean add(String proposal) {
            return true;
        }
    }
    
    private static final NullProposalAcceptor NULL_ACCEPTOR = new NullProposalAcceptor();

    private static class ByteArray {

        private final RandomAccessFile out;

        public ByteArray(File out) throws FileNotFoundException {
            this.out = new RandomAccessFile(out, "rw");
        }

        public void put(int pos, char what) throws IOException {
            out.seek(pos);
            out.writeChar(what);
        }

        public void put(int pos, byte what) throws IOException {
            out.seek(pos);
            out.writeByte(what);
        }

        public void put(int pos, int what) throws IOException {
            out.seek(pos);
            out.writeInt(what);
        }

        public void close() throws IOException {
            out.close();
        }
    }
    
    @OnStop
    public static final class RunOnStop implements Runnable {
        @Override public void run() {
            WORKER.shutdown();
            while (!WORKER.isTerminated()) {
                try {
                    WORKER.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
