package org.black.kotlin.run.output;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CompilerOutputParser {
    
    public static void parseCompilerMessagesFromReader(MessageCollector messageCollector, final Reader reader) {
        final StringBuilder stringBuilder = new StringBuilder();
        Reader wrappingReader = new Reader() {

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                int read = reader.read(cbuf, off, len);
                stringBuilder.append(cbuf, off, len);
                return read;
            }

            @Override
            public void close() throws IOException {
            }
        };
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(wrappingReader), new CompilerOutputSAXHandler(messageCollector));
        }
        catch (Throwable e) {
            String message = stringBuilder.toString();
            messageCollector.report(CompilerMessageSeverity.ERROR, message, CompilerMessageLocation.NO_LOCATION);
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
            }
        }
    }
    
    private static class CompilerOutputSAXHandler extends DefaultHandler {
        private static final Map<String, CompilerMessageSeverity> CATEGORIES = new HashMap<String, CompilerMessageSeverity>();
        
        static {
            CATEGORIES.put("error", CompilerMessageSeverity.ERROR);
            CATEGORIES.put("warning", CompilerMessageSeverity.WARNING);
            CATEGORIES.put("logging", CompilerMessageSeverity.LOGGING);
            CATEGORIES.put("output", CompilerMessageSeverity.OUTPUT);
            CATEGORIES.put("exception", CompilerMessageSeverity.EXCEPTION);
            CATEGORIES.put("info", CompilerMessageSeverity.INFO);
            CATEGORIES.put("messages", CompilerMessageSeverity.INFO); 
        }
             

        private final MessageCollector messageCollector;

        private final StringBuilder message = new StringBuilder();
        private final Stack<String> tags = new Stack<String>();
        private String path;
        private int line;
        private int column;

        public CompilerOutputSAXHandler(MessageCollector messageCollector) {
            this.messageCollector = messageCollector;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            tags.push(qName);

            message.setLength(0);

            String rawPath = attributes.getValue("path");
            path = rawPath == null ? null : rawPath;
            line = safeParseInt(attributes.getValue("line"), -1);
            column = safeParseInt(attributes.getValue("column"), -1);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (tags.size() == 1) {
                String message = new String(ch, start, length);
                if (!message.trim().isEmpty()) {
                    messageCollector.report(CompilerMessageSeverity.ERROR, "Unhandled compiler output: " + message, CompilerMessageLocation.NO_LOCATION);
                }
            }
            else {
                message.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (tags.size() == 1) {
                return;
            }
            String qNameLowerCase = qName.toLowerCase();
            CompilerMessageSeverity category = CATEGORIES.get(qNameLowerCase);
            if (category == null) {
                messageCollector.report(CompilerMessageSeverity.ERROR, "Unknown compiler message tag: " + qName, CompilerMessageLocation.NO_LOCATION);
                category = CompilerMessageSeverity.INFO;
            }
            String text = message.toString();

            messageCollector.report(category, text, CompilerMessageLocation.create(path, line, column, null));
            
            tags.pop();
        }
        
        private static int safeParseInt(String value, int defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value.trim());
            }
            catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }
}
