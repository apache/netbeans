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

package org.netbeans.modules.java.source.usages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.IndexManagerTestUtilities;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class LucenePerformanceTest extends NbTestCase {
    
    /** Creates a new instance of LucenePerformanceTest */
    public LucenePerformanceTest (final String name) {
        super (name);
    }
    
    
    protected @Override void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
        IndexManagerTestUtilities.setDisabledLocks(true);
        //Prepare indeces        
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }   
    
    public void testPerformance () throws Exception {
        final File indexDir = new File (this.getWorkDir(),"index");
        indexDir.mkdirs();
        final Index index = IndexManager.createIndex(indexDir, DocumentUtil.createAnalyzer());
        List<Pair<Pair<BinaryName,String>,Object[]>> data = prepareData(20000,1000,50);
//        Map<String,List<String>> data = loadData(new File ("/tmp/data"));
//        storeData(new File ("/tmp/data"),data);
        long startTime = System.currentTimeMillis();
        index.store (data, Collections.<Pair<String,String>>emptySet(), DocumentUtil.documentConvertor(), DocumentUtil.queryClassWithEncConvertor(false), true);
        long endTime = System.currentTimeMillis();
        long delta = (endTime-startTime);
        System.out.println("Indexing: " + delta);
        if (delta > 60000) {            
            assertTrue("Indexing took too much time: " +delta+ "ms",false);
        }        
        
        
        Set<String> result = new HashSet<String>();
        startTime = System.currentTimeMillis();
        final Pair<StoppableConvertor<BytesRef,String>,String> filter = QueryUtil.createPackageFilter("", true);
        index.queryTerms(result, DocumentUtil.FIELD_PACKAGE_NAME, filter.second(), filter.first(), null);
        endTime = System.currentTimeMillis();
        delta = (endTime-startTime);
        System.out.println("Packages: " + delta);
        if (delta > 500) {            
            assertTrue("All packages took too much time: " +delta+ "ms",false);
        }        
        
        
        Set<ElementHandle<TypeElement>> result2 = new HashSet<ElementHandle<TypeElement>>();
        startTime = System.currentTimeMillis();
        index.query(
                result2,
                DocumentUtil.typeElementConvertor(),
                DocumentUtil.declaredTypesFieldSelector(false, false),
                null,
                Queries.createQuery(DocumentUtil.FIELD_SIMPLE_NAME,DocumentUtil.FIELD_CASE_INSENSITIVE_NAME,"",Queries.QueryKind.PREFIX));
        endTime = System.currentTimeMillis();
        delta = (endTime-startTime);
        System.out.println("All classes: " + delta);
        if (delta > 1000) {            
            assertTrue("All classes took too much time: " +delta+ "ms",false);
        }
        
        result2 = new TreeSet<ElementHandle<TypeElement>>(new Comparator<ElementHandle<TypeElement>>() {
            @Override
            public int compare(ElementHandle<TypeElement> o1, ElementHandle<TypeElement> o2) {
                return o1.getBinaryName().compareTo(o2.getBinaryName());
            }
        });
        startTime = System.currentTimeMillis(); 
        index.query(
                result2,
                DocumentUtil.typeElementConvertor(),
                DocumentUtil.declaredTypesFieldSelector(false, false),
                null,
                Queries.createQuery(DocumentUtil.FIELD_SIMPLE_NAME,DocumentUtil.FIELD_CASE_INSENSITIVE_NAME,"Class7",Queries.QueryKind.PREFIX));
        endTime = System.currentTimeMillis();
        delta = (endTime-startTime);
        System.out.println("Prefix classes: " + delta + " size: " + result.size());
        if (delta > 500) {            
            assertTrue("Some classes took too much time: " +delta+ "ms",false);
        }        
    }
    
    
    private static List<Pair<Pair<BinaryName,String>,Object[]>> prepareData (final int count, final int pkgLimit, final int refLimit) {
        final List<Pair<Pair<BinaryName,String>,Object[]>> result = new ArrayList<> ();
        final List<String> refs = new LinkedList<String>();
        final Random r = new Random (System.currentTimeMillis());
        for (int i=0; i<count; i++) {
            final int refCount = r.nextInt(refLimit);
            final List<String> l = new ArrayList<String>(refCount);            
            for (int j=0; j<refCount && refs.size()>0; j++) {
                int index = r.nextInt(refs.size());
                String s = refs.get (index) + "+++++";
                if (!l.contains(s)) {
                    l.add(s);
                }
            }
            String name = String.format("pkg%d.Class%dC",r.nextInt(pkgLimit),i);
            result.add(Pair.<Pair<BinaryName,String>,Object[]>of(
                    Pair.<BinaryName,String>of(BinaryName.create(name, ElementKind.CLASS),null),
                    new Object[]{l,null,null}));
            refs.add (name);                    
        }
        return result;
    }
    
    
    private static void storeData  (File file, Map<String, List<String>> data) throws IOException {
        PrintWriter out = new PrintWriter (new OutputStreamWriter (new FileOutputStream (file)));
        try {
            for (Map.Entry<String,List<String>> e : data.entrySet()) {
                String key = e.getKey();
                List<String> value = e.getValue();
                out.println(key);
                for (String v : value) {
                    out.println("\t"+v);
                }
            }
        } finally {
            out.close ();
        }
    }
    
    private static void storeResult  (File file, Set<String>data) throws IOException {
        PrintWriter out = new PrintWriter (new OutputStreamWriter (new FileOutputStream (file)));
        try {
            for (String s : data) {                
                out.println(s);                
            }
        } finally {
            out.close ();
        }
    }
    
    private static Map<String,List<String>> loadData (File file) throws IOException {
        assert file != null && file.exists() && file.canRead();
        final Map<String,List<String>> result = new HashMap<String,List<String>> ();
        BufferedReader in = new BufferedReader (new FileReader (file));
        try {
            String key = null;
            List<String> value = null;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.charAt(0) != '\t') {
                    if (key != null) {
                        result.put(key,value);
                    }
                    key = line;
                    value = new ArrayList<String>();
                }
                else {
                    value.add(line.substring(1));
                }
            }
        } finally {
            in.close();
        }
        return result;
    }
     
    
}
