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

package org.netbeans.lib.uihandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Can persist and read log records from streams.
 *
 * @author Jaroslav Tulach
 */
public final class LogRecords {
    private LogRecords() {
    }

    private static final Logger LOG = Logger.getLogger(LogRecords.class.getName());

    private static final Formatter FORMATTER = new LogFormatter();

    /** Inspects the log record and decorates its content.
     * @param r the log record
     * @param d callback to be called with inspected values
     */
    public static void decorate(LogRecord r, Decorable d) {
        Decorations.decorate(r, d);
    }

    /**
     * Writhe log record to an output stream.
     * @param os the output stream
     * @param rec the log record
     * @throws IOException when an I/O error occurs.
     */
    public static void write(OutputStream os, LogRecord rec) throws IOException {
        String formated = FORMATTER.format(rec);
        byte[] arr = formated.getBytes("utf-8");
        os.write(arr);
        os.flush();
    }

    private static class HandlerDelegate extends Handler {

        private Handler hd;
        private boolean afterLast;
        private long lastNumber;

        HandlerDelegate(Handler hd) {
            this.hd = hd;
        }

        @Override
        public void publish(LogRecord record) {
            long sn = record.getSequenceNumber();
            if (afterLast) {
                if (sn <= lastNumber) {
                    return ;
                } else {
                    afterLast = false;
                }
            }
            lastNumber = sn;
            hd.publish(record);
        }

        @Override
        public void flush() {
            hd.flush();
        }

        @Override
        public void close() throws SecurityException {
            hd.close();
        }

        @Override
        public String toString() {
            return hd.toString();
        }
        
        void setContinueAfterLast() {
            afterLast = true;
        }

    }

    /**
     * Scan log records stored in a file.
     * @param f the file to read log records from
     * @param h handler that gets the log records
     * @throws IOException when an I/O error occurs.
     */
    public static void scan(File f, Handler h) throws IOException {
        HandlerDelegate hd = new HandlerDelegate(h);
        InputStream is = null;
        List<LogRecord> errorLogRecords = new ArrayList<LogRecord>();        
        try {
            is = new FileInputStream(f);
            LogRecords.scan(is, hd, errorLogRecords);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "LogRecords scan threw {0}", ex.toString());
            if (is != null) {
                is.close();
            }
            is = null;
            //LOG.severe("Stream closed.");
            if (repairFile(f)) {
                errorLogRecords.clear();
                LOG.info("LogRecords File repaired. :-)");
                hd.setContinueAfterLast();
                is = new FileInputStream(f);
                LogRecords.scan(is, hd);
                return ;
            } else {
                LOG.info("LogRecords File NOT repaired. :-(");
            }
            LOG.severe("Throwing the original exception... :-(");
            throw ex;
        } finally {
            for (LogRecord lr : errorLogRecords) {
                LOG.log(lr);
            }
            if (is != null) {
                is.close();
            }
        }
    }
    
    /**
     * Scan log records from an input stream.
     * @param is the input stream to read log records from
     * @param h handler that gets the log records
     * @throws IOException when an I/O error occurs.
     */
    public static void scan(InputStream is, Handler h) throws IOException {
        List<LogRecord> errorLogRecords = new ArrayList<LogRecord>();
        try {
            scan(is, h, errorLogRecords);
        } finally {
            for (LogRecord lr : errorLogRecords) {
                LOG.log(lr);
            }
        }
    }
    
    private static void scan(InputStream is, Handler h, List<LogRecord> errorLogRecords) throws IOException {
        PushbackInputStream wrap = new PushbackInputStream(is, 32);
        byte[] arr = new byte[5];
        int len = wrap.read(arr);
        if (len == -1) {
            return;
        }
        wrap.unread(arr, 0, len);
        if (arr[0] == 0x1f && arr[1] == -117) {
            wrap = new PushbackInputStream(new GZIPInputStream(wrap), 32);
            len = wrap.read(arr);
            if (len == -1) {
                return;
            }
            wrap.unread(arr, 0, len);
        }
        
        if (arr[0] == '<' &&
            arr[1] == '?' &&
            arr[2] == 'x' &&
            arr[3] == 'm' &&
            arr[4] == 'l'
        ) {
            is = wrap;
        } else {
            ByteArrayInputStream header = new ByteArrayInputStream(
    "<?xml version='1.0' encoding='UTF-8'?><uigestures version='1.0'>".getBytes()
            );
            ByteArrayInputStream footer = new ByteArrayInputStream(
                "</uigestures>".getBytes()
            );
            is = new SequenceInputStream(
                new SequenceInputStream(header, wrap),
                footer
            );
        }
        
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setValidating(false);
        SAXParser p;
        try {
            try{
                f.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true); // NOI18N
            }catch (SAXNotRecognizedException snre){
                LogRecord lr = new LogRecord(Level.INFO, null);
                lr.setThrown(snre);
                errorLogRecords.add(lr);
            }
            p = f.newSAXParser();
        } catch (ParserConfigurationException ex) {
            LogRecord lr = new LogRecord(Level.SEVERE, null);
            lr.setThrown(ex);
            errorLogRecords.add(lr);
            throw new IOException(ex);
        } catch (SAXException ex) {
            LogRecord lr = new LogRecord(Level.SEVERE, null);
            lr.setThrown(ex);
            errorLogRecords.add(lr);
            throw new IOException(ex);
        }
        
        Parser parser = new Parser(h);
        try {
            p.parse(is, parser);
        } catch (SAXParseException ex) {
            LogRecord lr = new LogRecord(Level.WARNING, "Line = "+ex.getLineNumber()+", column = "+ex.getColumnNumber());
            lr.setThrown(ex);
            errorLogRecords.add(lr);
            throw new IOException(ex);
        } catch (SAXException ex) {
            LogRecord lr = new LogRecord(Level.WARNING, null);
            lr.setThrown(ex);
            errorLogRecords.add(lr);
            throw new IOException(ex);
        } catch (InternalError error){
            LogRecord lr = new LogRecord(Level.WARNING, "Input file corruption");
            lr.setThrown(error);
            errorLogRecords.add(lr);
            throw new IOException(error);
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            LogRecord lr = new LogRecord(Level.WARNING, "Input file corruption");
            lr.setThrown(ex);
            errorLogRecords.add(lr);
            throw new IOException(ex);
        } finally {
            List<SAXParseException> fatalErrors = parser.getFatalErrors();
            if (fatalErrors != null) {
                for (SAXParseException ex : fatalErrors) {
                    LogRecord lr = new LogRecord(Level.WARNING, "Fatal SAX Parse Exception: Line = "+ex.getLineNumber()+", column = "+ex.getColumnNumber());
                    lr.setThrown(ex);
                    errorLogRecords.add(lr);
                }
            }
        }
    }   

    static Level parseLevel(String lev) {
        return "USER".equals(lev) ? Level.SEVERE : Level.parse(lev);
    }
    
    private static final String RECORD_ELM_START = "<record>"; // NOI18N
    private static final String RECORD_ELM_END = "</record>"; // NOI18N
    
    private static boolean repairFile(File f) {
        boolean repaired = false;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(f, "rw");
            String line;
            String lastLine = null;
            long recordEndPos = -1l;
            long recordStartPos = -1l;
            while ((line = raf.readLine()) != null) {
                if (line.equals(RECORD_ELM_END)) {
                    recordEndPos = raf.getFilePointer();
                }
                if (line.endsWith(RECORD_ELM_START)) {
                    long pos = raf.getFilePointer();
                    long elmStart = pos - RECORD_ELM_START.length() - 1;
                    if (0 < recordEndPos && recordEndPos < elmStart) {
                        deletePart(raf, recordEndPos, elmStart);
                        long diff = elmStart - recordEndPos;
                        raf.seek(pos - diff);
                        recordEndPos -= diff;
                        elmStart -= diff;
                        repaired = true;
                    } else if (recordStartPos < 0 &&
                               (recordEndPos < 0 || recordEndPos == elmStart)) {
                        deletePart(raf, 0, elmStart);
                        raf.seek(0);
                        recordEndPos = 0l;
                        elmStart = 0l;
                        repaired = true;
                    }
                    recordStartPos = elmStart;
                }
                if (!line.trim().isEmpty()) {
                    lastLine = line;
                }
            }
            if (lastLine != null && !RECORD_ELM_END.equals(lastLine) && recordEndPos > 0) {
                deletePart(raf, recordEndPos, raf.length());
                repaired = true;
            }
            return repaired;
        } catch (IOException ioex) {
            return false;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException ex) {
            }
        }
    }
    
    /**
     * Deletes bytes from <pos1, pos2).
     */
    private static void deletePart(RandomAccessFile raf, long pos1, long pos2) throws IOException {
        if (pos1 == pos2) {
            return ;
        }
        assert pos1 < pos2 : "The first position is higher: "+pos1+", "+pos2+".";
        int buffLength = 3;
        byte[] buffer = new byte[buffLength];
        long length = raf.length();
        //long chunks = (length - pos2)/buffLength;
        while ((pos2 + buffLength) <= length) {
            raf.seek(pos2);
            raf.readFully(buffer);
            raf.seek(pos1);
            raf.write(buffer);
            pos1 += buffLength;
            pos2 += buffLength;
        }
        if (pos2 < length) {
            int l = (int) (length - pos2);
            raf.seek(pos2);
            raf.readFully(buffer, 0, l);
            raf.seek(pos1);
            raf.write(buffer, 0, l);
        }
        raf.setLength(length - (pos2 - pos1));
    }
    
    private static final class Parser extends DefaultHandler {
        private Handler callback;
        private static enum Elem {
            UIGESTURES, RECORD, DATE, MILLIS, SEQUENCE, LEVEL, THREAD,
            MESSAGE, KEY, PARAM, FRAME, CLASS, METHOD, LOGGER, EXCEPTION, LINE,
            CATALOG, MORE, FILE;
            
            public String parse(Map<Elem,String> values) {
                String v = values.get(this);
                return v;
            }
        }
        private Map<Elem,String> values = new EnumMap<Elem,String>(Elem.class);
        private Elem current;
        private FakeException currentEx;
        private Queue<FakeException> exceptions;
        private List<String> params;
        private StringBuilder chars = new StringBuilder();
        private List<SAXParseException> fatalErrors;
        
        public Parser(Handler c) {
            this.callback = c;
        }
        
        public List<SAXParseException> getFatalErrors() {
            return fatalErrors;
        }
        
        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
            callback.flush();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "uri: {0} localName: {1} qName: {2} atts: {3}", new Object[] { uri, localName, qName, atts });
            }

            try {
                current = Elem.valueOf(qName.toUpperCase());
                if (current == Elem.EXCEPTION) {
                    currentEx = new FakeException(new EnumMap<Elem,String>(values));
                }
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, "Uknown tag " + qName, ex);
                current = null;
            }
            chars = new StringBuilder();
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (current != null) {
                String v = chars.toString();
                values.put(current, v);
                if (current == Elem.PARAM) {
                    if (params == null) {
                        params = new ArrayList<String>();
                    }
                    params.add(v);
                    if (params.size() > 1500) {
                        LOG.log(Level.SEVERE,
                                "Too long params when reading a record. Deleting few. Msg: {0}", // NOI18N
                                Elem.MESSAGE.parse(values));
                        for (String p : params) {
                            LOG.fine(p);
                        }
                        params.clear();
                    }
                }
            }
            current = null;
            chars = new StringBuilder();
            
            if (currentEx != null && currentEx.values != null) {
                if ("frame".equals(qName)) { // NOI18N
                    String line = Elem.LINE.parse(values);
                    StackTraceElement elem = new StackTraceElement(
                            Elem.CLASS.parse(values),
                            Elem.METHOD.parse(values),
                            Elem.FILE.parse(values),
                            line == null ? -1 : Integer.parseInt(line)
                            );
                    currentEx.trace.add(elem);
                    values.remove(Elem.CLASS);
                    values.remove(Elem.METHOD);
                    values.remove(Elem.LINE);
                }
                if ("exception".equals(qName)) {
                    currentEx.message = values.get(Elem.MESSAGE);
                    String more = values.get(Elem.MORE);
                    if (more != null) currentEx.more = Integer.parseInt(more);
                    if (exceptions == null){
                        exceptions = new LinkedList<FakeException>();
                    }
                    exceptions.add(currentEx);
                    values = currentEx.values;
                    currentEx = null;
                }
                return;
            }
            
            if ("record".equals(qName)) { // NOI18N
                String millis = Elem.MILLIS.parse(values);
                String seq = Elem.SEQUENCE.parse(values);
                String lev = Elem.LEVEL.parse(values);
                String thread = Elem.THREAD.parse(values);
                String msg = Elem.MESSAGE.parse(values);
                String key = Elem.KEY.parse(values);
                String catalog = Elem.CATALOG.parse(values);
                
                if (lev != null) {
                    LogRecord r = new LogRecord(parseLevel(lev), key != null && catalog != null ? key : msg);
                    try {
                        r.setThreadID(parseInt(thread));
                    } catch (NumberFormatException ex) {
                        LOG.log(Level.WARNING, ex.getMessage(), ex);
                    }
                    r.setSequenceNumber(parseLong(seq));
                    r.setMillis(parseLong(millis));
                    r.setResourceBundleName(key);
                    if (catalog != null && key != null) {
                        r.setResourceBundleName(catalog);
                        if (!"<null>".equals(catalog)) { // NOI18N
                            try {
                                ResourceBundle b = NbBundle.getBundle(catalog);
                                b.getObject(key);
                                // ok, the key is there
                                r.setResourceBundle(b);
                            } catch (MissingResourceException e) {
                                LOG.log(Level.CONFIG, "Cannot find resource bundle {0} for key {1}", new Object[] { catalog, key });
                                r.setResourceBundle(new FakeBundle(key, msg));
                            }
                        } else {
                            LOG.log(Level.CONFIG, "Cannot find resource bundle <null> for key {1}", key);
                        }
                    }
                    if (params != null) {
                        r.setParameters(params.toArray());
                    }
                    if (exceptions != null) {
                        r.setThrown(createThrown(null));
                        // exceptions = null;  should be empty after poll
                    }

                    callback.publish(r);
                }

                currentEx = null;
                params = null;
                values.clear();
            }
            
        }

        private long parseLong(String str){
            if (str == null){
                return 0l;
            }
            try{
                return Long.parseLong(str);
            }catch(NumberFormatException exc){
                LOG.log(Level.INFO, exc.getMessage(), exc);
                return 0l;
            }
        }

        private int parseInt(String str){
            if (str == null){
                return 0;
            }
            try{
                return Integer.parseInt(str);
            }catch(NumberFormatException exc){
                LOG.log(Level.INFO, exc.getMessage(), exc);
                return 0;
            }
        }
        /** set first element of exceptions as a result of this calling and
         * recursively fill it's cause
         */
        private FakeException createThrown(FakeException last){
            if (exceptions.size()==0) {
                return null;
            }
            FakeException result = exceptions.poll();
            if (result.getMore() != 0) {
                assert last != null : "IF MORE IS NOT 0, LAST MUST BE SET NOT NULL";
                StackTraceElement[] trace = last.getStackTrace();
                for (int i = trace.length - result.getMore(); i < trace.length; i++){
                    result.trace.add(trace[i]);// fill the rest of stacktrace
                }
            }
            FakeException cause = createThrown(result);
            result.initCause(cause);
            return result;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            chars.append(ch, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            if (fatalErrors == null) {
                fatalErrors = new LinkedList<SAXParseException>();
            }
            fatalErrors.add(e);
            if (fatalErrors.size() > 100) {
                throw e;
            }
        }
        
    }
    
    private static final class FakeBundle extends ResourceBundle {
        private String key;
        private String value;
         
        public FakeBundle(String key, String value) {
            this.key = key;
            this.value = value;
        }

    
        @Override
        protected Object handleGetObject(String arg0) {
            if (key.equals(arg0)) {
                return value;
            } else {
                return null;
            }
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(Collections.singleton(key));
        }
    } // end of FakeBundle
    
    private static final class FakeException extends Exception {
        final List<StackTraceElement> trace = new ArrayList<StackTraceElement>();
        Map<Parser.Elem,String> values;
        String message;
        int more;
        
        public FakeException(Map<Parser.Elem,String> values) {
            this.values = values;
            more = 0;
        }
       
        @Override
        public StackTraceElement[] getStackTrace() {
            return trace.toArray(new StackTraceElement[0]);
        }

        @Override
        public String getMessage() {
            return message;
        }
        
        public int getMore(){
            return more;
        }
        
        /**
         * org.netbeans.lib.uihandler.LogRecords$FakeException: NullPointerException ...
         * is not the best message - it's better to suppress FakeException
         */
        @Override
        public String toString(){
            return message;
        }
        
    } // end of FakeException
}
