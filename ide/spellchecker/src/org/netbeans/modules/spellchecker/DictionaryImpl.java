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
package org.netbeans.modules.spellchecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.spellchecker.spi.dictionary.Dictionary;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.Action;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  Jan Lahoda
 */
public class DictionaryImpl implements Dictionary {
    
    private static final RequestProcessor WORKER = new RequestProcessor(DictionaryImpl.class.getName(), 1, false, false);
    private List<String> dictionary = null;
    private StringBuffer dictionaryText = null;
    private final File source;
    private final Project p;
    private final AuxiliaryConfiguration ac;
    private final Locale locale;
    private final Comparator<String> dictionaryComparator;

    public DictionaryImpl(File source, Locale locale) {
        this.source = source;
        this.p = null;
        this.ac = null;
        this.locale = locale;
        this.dictionaryComparator = prepareDictionaryComparator(locale);
        loadDictionary(source);
    }

    public DictionaryImpl(Project p, AuxiliaryConfiguration ac, Locale locale) {
        this.source = null;
        this.p  = p;
        this.ac = ac;
        this.locale = locale;
        this.dictionaryComparator = prepareDictionaryComparator(locale);
        loadDictionary(ac);
    }
    
    private Comparator<String> prepareDictionaryComparator(final Locale locale) {
        return new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.toLowerCase(locale).compareTo(s2.toLowerCase(locale));
            }
        };
    }

    private void loadDictionary(File source) {
        if (!source.canRead())
            return ;
        
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8));

            String line = null;

            while ((line = reader.readLine()) != null) {
                addEntryImpl(line);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        
        getDictionary().sort(dictionaryComparator);
   }
    
    private static final String WORDLIST = "spellchecker-wordlist";
    private static final String NAMESPACE = "http://www.netbeans.org/ns/spellchecker-wordlist/1";
    
    private void loadDictionary(final AuxiliaryConfiguration ac) {
        ProjectManager.mutex().readAccess(new Action<Void>() {
            public Void run() {
                Element conf = ac.getConfigurationFragment(WORDLIST, NAMESPACE, true);

                if (conf == null) {
                    return null;
                }
                
                NodeList childNodes = conf.getChildNodes();

                for (int cntr = 0; cntr < childNodes.getLength(); cntr++) {
                    Node n = childNodes.item(cntr);

                    if ("word".equals(n.getLocalName())) {
                        addEntryImpl(n.getTextContent());
                    }
                }
                return null;
            }
        });
        
        getDictionary().sort(dictionaryComparator);
    }
    
    public int findLesser(String word) {
        word = word.toLowerCase(locale);
        List<String> dict = getDictionary();
        
        int lower = 0;
        int upper = dict.size() - 1;
        
        boolean last = false;
        
        while (true) {
            if (lower == upper)
                break;
            
            if (last)
                break;
            
            if ((upper - lower) == 1)
                last = true;
            
            int current = (lower + upper) / 2;
            String currentObj = dict.get(current);
            
            int result = currentObj.toLowerCase(locale).compareTo(word);
            
            if (result == 0)
                return current;
            
            if (result < 0) {
                lower = current + 1;
            }
            
            if (result > 0) {
                upper = current - 1;
            }
        }
        
        if (dict.get(lower).toLowerCase(locale).compareTo(word) == 0)
            return lower;
        else
            return (lower + 1) < dict.size() ? lower + 1 : lower;
    }
    
    public ValidityType findWord(String word) {
        if (getDictionary().isEmpty()) return ValidityType.INVALID;
        String str = getDictionary().get(findLesser(word));
        String lWord = word.toLowerCase(locale);
//            System.err.println("str=" + str);
        if (str.startsWith(word) || str.startsWith(lWord)) {
            if (str.equals(word) || str.equals(lWord))
                return ValidityType.VALID;
            else
                return ValidityType.PREFIX_OF_VALID;
        } else
            return ValidityType.INVALID;
    }
    
    protected synchronized List<String> getDictionary() {
        if (dictionary == null)
            dictionary = new ArrayList<String>();
        
//            System.err.println("returning dictionary=" + System.identityHashCode(dictionary));
        return dictionary;
    }
    
    protected synchronized StringBuffer getDictionaryText() {
        if (dictionaryText == null) {
            dictionaryText = new StringBuffer();
            dictionaryText.append('\n');
            
            for (String e : getDictionary()) {
                dictionaryText.append(e);
                dictionaryText.append('\n');
            }
        }
        
        return dictionaryText;
    }
    
    private void addEntryImpl(String entry) {
        getDictionary().add(entry);
    }
    
    private void dumpToFile(List<String> dictionary) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source), StandardCharsets.UTF_8));

            for (String s : dictionary) {
                writer.append(s);
                writer.append('\n');
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    private void dumpToProject(final List<String> dictionary) {
        ProjectManager.mutex().writeAccess(new Action<Void>(){
            public Void run() {
                Element conf = null;
                Document doc = createXmlDocument();
                
                if (doc != null) {
                    conf = doc.createElementNS(NAMESPACE, WORDLIST);
                }
                
                if (conf == null) {
                    return null;
                }

                for (String s : dictionary) {
                    Element e = conf.getOwnerDocument().createElementNS(NAMESPACE, "word");

                    e.appendChild(conf.getOwnerDocument().createTextNode(s));
                    conf.appendChild(e);
                }

                ac.putConfigurationFragment(conf, true);
                return null;
            }
        });
        
        WORKER.post(new Runnable() {
            @Override public void run() {
                try {
                    ProjectManager.getDefault().saveProject(p);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    private Document createXmlDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            return null;
        }
    }
    
    public synchronized void addEntry(String entry) {
        List<String> dictionary = getDictionary();
        int index = Collections.binarySearch(dictionary, entry, dictionaryComparator);
        
        if (index >= 0)
            return ;
        
        index = -index - 1;
        
        dictionary.add(index, entry);
        dictionaryText = null;
        
        if (source != null) {
            dumpToFile(dictionary);
        } else {
            dumpToProject(dictionary);
        }
    }
    
    public List<String> completions(String word) {
        if ("".equals(word))
            return Collections.emptyList();
        
        int start = findLesser(word);
        
//            if (!((String )getDictionary().get(start)).equalsIgnoreCase(word)) {
//                start++;
//            }
        
        int end   = findLesser(word.substring(0, word.length() - 1) + (char) (word.charAt(word.length() - 1) + 1));
        
        return getDictionary().subList(start, end/* + 1*/);
    }
    
    private static class Pair {
        private int distance;
        private String proposedWord;
        
        public Pair(String proposedWord, int distance) {
            this.distance = distance;
            this.proposedWord = proposedWord;
        }
    }
    
    private static class SimilarComparator implements Comparator<Pair> {
        
        public int compare(Pair p1, Pair p2) {
            if (p1.distance < p2.distance)
                return (-1);
            
            if (p1.distance > p2.distance)
                return 1;
            
            return 0;
        }
        
    }
    
    private static int MINIMAL_SIMILAR_COUNT = 3;
    
    public List<String> getSimilarWords(String word) {
        if (getDictionary().isEmpty()) return Collections.<String>emptyList();
        List<Pair> proposal = dynamicProgramming(word, getDictionaryText(), 5);
        List<String> result   = new ArrayList<String>();
        
        //future:
//            if (Character.isLowerCase(word.charAt(0)))
//                return result;
        
        proposal.sort(new SimilarComparator());
        
        Iterator words = proposal.iterator();
        int      proposedCount = 0;
        int      lastDistance = 0;
        
        while (words.hasNext()) {
            Pair pair = (Pair) words.next();
            
            if (proposedCount >= MINIMAL_SIMILAR_COUNT && lastDistance != pair.distance)
                continue;
            
            result.add(pair.proposedWord);
            proposedCount++;
            lastDistance = pair.distance;
        }
        
        return result;
    }
    
    private static List<Pair> dynamicProgramming(String pattern, CharSequence text, int distance) {
        List<Pair> result = new ArrayList<Pair>();
        pattern = pattern.toLowerCase();
        
        int[] old = new int[pattern.length() + 1];
        int[] current = new int[pattern.length() + 1];
        int[] oldLength = new int[pattern.length() + 1];
        int[] length = new int[pattern.length() + 1];
        
        for (int cntr = 0; cntr < old.length; cntr++) {
            old[cntr] = distance + 1;//cntr;
            oldLength[cntr] = (-1);
        }
        
        current[0] = old[0] = oldLength[0] = length[0] = 0;
        
        int currentIndex = 0;
        
        while (currentIndex < text.length()) {
            for (int cntr = 0; cntr < pattern.length(); cntr++) {
                int insert = old[cntr + 1] + 1;
                int delete = current[cntr] + 1;
                int replace = old[cntr] + ((pattern.charAt(cntr) == text.charAt(currentIndex)) ? 0 : 1);
                
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
            
            if (current[pattern.length()] <= distance) {
                int start = currentIndex - length[pattern.length()] + 1;
                int end   = currentIndex + 1;
                
                end = end >= text.length() ? text.length() - 1 : end;
                
                if ((start == 0 || text.charAt(start - 1) == '\n') && text.charAt(end) == '\n') {
                    String occurence = text.subSequence(start, end).toString();
                    
                    if (occurence.indexOf('\n') == (-1) && !pattern.equals(occurence)) {
                        result.add(new Pair(occurence, current[pattern.length()]));
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
        
        return result;
    }

    public ValidityType validateWord(CharSequence word) {
        return findWord(word.toString());
    }

    public List<String> findValidWordsForPrefix(CharSequence word) {
        return Collections.emptyList();
    }

    public List<String> findProposals(CharSequence word) {
        return getSimilarWords(word.toString());
    }

}
