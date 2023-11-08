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
package org.netbeans.modules.cloud.oracle.adm;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Places;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Caches audit results in NB cache folder. Uses Jackson to serialize the audit summary + 
 * vulnerability items into $cache/{@link #CACHE_SUBDIR}/sXX/{@link #FILE_REPORT} files. 
 * Knowledge base IDs are mapped to segments using segments file to prevent issues with
 * characters in OCIDs.
 * 
 * @author sdedic
 */
final class AuditCache {
    private static final Logger LOG = Logger.getLogger(AuditCache.class.getName());
    
    private static final String FILE_REPORT = "audit-report.json"; // NOI18N
    private static final String CACHE_SUBDIR = "oracle-cloud-adm"; // NOI18N
    private static final String SEGMENTS_FILE = "segments"; // NOI18N
    private static final AuditCache INSTANCE = new AuditCache();
    
    private Properties segments = new Properties();
    private long loadTimestamp;
    
    private AuditCache() {
    }
    
    static AuditCache getInstance() {
        return INSTANCE;
    }
    
    Properties loadSegments() {
        File cacheDir = Places.getCacheSubdirectory(CACHE_SUBDIR);
        Path segPath = cacheDir.toPath().resolve(SEGMENTS_FILE);
        if (!Files.exists(segPath)) {
            synchronized (this) {
                segments.clear();
                return segments;
            }
        }
        Properties p;
        try {
            long ts;
            synchronized (this) {
                ts = Files.getLastModifiedTime(segPath).toMillis();
                if (ts < loadTimestamp) {
                    return segments;
                }
            }
            p = new Properties();
            p.load(Files.newBufferedReader(segPath));
            synchronized (this) {
                if (ts > loadTimestamp) {
                    this.segments = p;
                    this.loadTimestamp = ts;
                } else {
                    p = this.segments;
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Could not load segment properties", ex);
            p = this.segments;
        }
        return p;
    }
    
    
    static class ExplicitlySetFilter extends SimpleBeanPropertyFilter {
        private static final Method NO_METHOD;

        private Map<Class, Method> explicitlySetMethods = new HashMap<>();

        static {
            try {
                NO_METHOD = ExplicitlySetFilter.class.getDeclaredMethod("isExplicitlySet", Object.class, String.class);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private boolean isExplicitlySet(Object v, String name) {
            if (v == null) {
                return true;
            }
            Method m = explicitlySetMethods.get(v.getClass());
            if (m == null) {
                try {
                    m = v.getClass().getMethod("get__explicitlySet__");
                    m.setAccessible(true);
                } catch (ReflectiveOperationException ex) {
                    m = NO_METHOD;
                }
                explicitlySetMethods.put(v.getClass(), m);
            }
            if (m == NO_METHOD) {
                return true;
            }
            try {
                Set<String> props = (Set<String>)m.invoke(v);
                return props == null || props.contains(name);
            } catch (ReflectiveOperationException ex) {
                return true;
            }
        }
        @Override
        public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
            if (isExplicitlySet(elementValue, writer.getName())) {
                super.serializeAsElement(elementValue, jgen, provider, writer);
            }
        }

        @Override
        public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
            if (isExplicitlySet(pojo, writer.getName())) {
                super.serializeAsField(pojo, jgen, provider, writer);
            }
        }
    }
    public VulnerabilityReport cacheAuditResults(VulnerabilityReport report) throws IOException {
        Properties segments = loadSegments();
        String k = "knowledge.segment." + report.summary.getKnowledgeBaseId();
        
        File cacheDir = Places.getCacheSubdirectory(CACHE_SUBDIR);
        Path segPath = cacheDir.toPath().resolve(SEGMENTS_FILE);
        boolean writeSegment = !Files.exists(segPath);
        if (writeSegment) {
            synchronized (this) {
                this.segments.clear();
            }
        }
        String segName = segments.getProperty(k);
        if (segName == null) {
            int segNo = 1;
            IOException saveException = null;
            
            for (int attempts = 0; attempts < 5; attempts++) {
                while (segments.get("s" + segNo) != null) {
                    segNo++;
                }
                String sN = "s" + segNo;
                segments.put(sN, k);
                segments.put(k, sN);

                Path dirPath = cacheDir.toPath().resolve(sN);
                try {
                    Files.createDirectories(dirPath);
                    segName = sN;
                    break;
                } catch (IOException ex) {
                    saveException = ex;
                    continue;
                }
            }
            if (segName == null) {
                throw saveException;
            }
            
            try (OutputStream ostm = Files.newOutputStream(segPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                segments.store(ostm, null);
            }
        }
        
        Path dirPath = cacheDir.toPath().resolve(segName);
        Files.createDirectories(dirPath);
        Path reportData = dirPath.resolve(FILE_REPORT);
        ObjectWriter om = new ObjectMapper()
            .writer(new SimpleFilterProvider().addFilter("explicitlySetFilter", new ExplicitlySetFilter())); // NOI18N
        om.writeValue(reportData.toFile(), report);
        Path stampPath = reportData.resolveSibling("lastReadStamp"); // NOI18N
        Files.write(stampPath, Arrays.asList(""), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return report;
    }
    
    /**
     * Loads the cached audit for the given knowledgebase.
     * @param knowledgeBaseId
     * @return 
     */
    public VulnerabilityReport loadAudit(String knowledgeBaseId) throws IOException {
        Properties segments = loadSegments();
        String k = "knowledge.segment." + knowledgeBaseId;
        String segName = segments.getProperty(k);
        
        if (segName == null) {
            return null;
        }
        File cacheDir = Places.getCacheSubdirectory(CACHE_SUBDIR);
        Path dirPath = cacheDir.toPath().resolve(segName);
        
        if (!Files.exists(dirPath)) {
            synchronized (this) {
                // clean up the segment cache
                segments.remove(k);
                segments.remove(segName);
            }
        }
        Path reportData = dirPath.resolve(FILE_REPORT);
        if (!Files.exists(reportData)) {
            return null;
        }
        
        ObjectMapper om = new ObjectMapper();
        
        VulnerabilityReport res = om.readValue(reportData.toFile(), VulnerabilityReport.class);        
        
        Path stampPath = reportData.resolveSibling("lastReadStamp");
        Files.write(stampPath, Arrays.asList(""), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return res;
    }
}
