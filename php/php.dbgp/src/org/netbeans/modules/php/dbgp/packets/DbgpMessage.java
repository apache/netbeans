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
package org.netbeans.modules.php.dbgp.packets;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author ads
 *
 */
public abstract class DbgpMessage {

    private static final Logger LOGGER = Logger.getLogger(DbgpMessage.class.getName());
    private static final String INIT = "init"; // NOI18N
    private static final String RESPONSE = "response"; // NOI18N
    private static final String STREAM = "stream"; // NOI18N
    static final String ISO_CHARSET = "ISO-8859-1"; // NOI18N
    private static final int MAX_PACKET_SIZE = 1024;
    protected static final String HTML_APOS = "&apos;"; // NOI18N
    protected static final String HTML_QUOTE = "&quot;"; // NOI18N
    protected static final String HTML_AMP = "&amp"; // NOI18N
    protected static final String HTML_LT = "&lt"; // NOI18N
    protected static final String HTML_GT = "&gt"; // NOI18N
    private static final java.util.Map<String, Character> ENTITIES = new HashMap<>();
    private static DocumentBuilder builder;
    private static AtomicInteger myMaxDataSize = new AtomicInteger(MAX_PACKET_SIZE);
    private Node myNode;

    static {
        ENTITIES.put(HTML_APOS, '\'');
        ENTITIES.put(HTML_QUOTE, '"');
        ENTITIES.put(HTML_AMP, '&');
        ENTITIES.put(HTML_LT, '<');
        ENTITIES.put(HTML_GT, '>');
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setEntityResolver(new StubResolver());
        } catch (ParserConfigurationException e) {
            log(e);
        }
    }

    DbgpMessage(Node node) {
        myNode = node;
    }

    public static DbgpMessage create(InputStream inputStream, String projectEncoding) throws SocketException {
        try {
            int size = getDataSize(inputStream);
            if (size < 0) {
                notifyPacketError(null);
                Logger.getLogger(DbgpMessage.class.getName()).log(Level.FINE, "Got {0} as data size", size); // NOI18N
                return null;
            }
            byte[] bytes = getContent(inputStream, size);
            Node node = getNode(bytes, projectEncoding);
            logDebugInfo(bytes);
            return create(node);
        } catch (SocketException e) {
            throw e;
        } catch (IOException e) {
            log(e);
        }
        return null;
    }

    public abstract void process(DebugSession session, DbgpCommand command);

    public static int getMaxDataSize() {
        return myMaxDataSize.get();
    }

    public static void setMaxDataSize(int size) {
        int maxSize = myMaxDataSize.get();
        if (maxSize != size) {
            myMaxDataSize.compareAndSet(maxSize, size);
        }
    }

    public static DbgpMessage create(Node node) {
        if (node == null) {
            return null;
        }
        String rootName = node.getNodeName();
        switch (rootName) {
            case INIT:
                return new InitMessage(node);
            case STREAM:
                return MessageBuilder.createStream(node);
            case RESPONSE:
                return MessageBuilder.createResponse(node);
        }
        return null;
    }

    protected static void log(IOException e) {
        Logger.getLogger(DbgpMessage.class.getName()).log(Level.SEVERE, null, e);
    }

    protected static String getNodeValue(Node node) {
        NodeList list = node.getChildNodes();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (child instanceof Text) {
                builder.append(child.getNodeValue());
            } else if (child instanceof CDATASection) {
                builder.append(child.getNodeValue());
            }
        }
        return replaceHtmlEntities(builder.toString());
    }

    protected static String getAttribute(Node node, String attrName) {
        Node attr = node.getAttributes().getNamedItem(attrName);
        return attr == null ? null : replaceHtmlEntities(attr.getNodeValue());
    }

    protected static boolean getBoolean(Node node, String attrName) {
        String value = getAttribute(node, attrName);
        if (value == null) {
            return false;
        }
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected static Node getChild(Node node, String nodeName) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (nodeName.equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }

    protected static List<Node> getChildren(Node node, String nodeName) {
        List<Node> result = new LinkedList<>();
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (nodeName.equals(child.getNodeName())) {
                result.add(child);
            }
        }
        return result;
    }

    protected Node getNode() {
        return myNode;
    }

    private static void log(ParserConfigurationException e) {
        Logger.getLogger(DbgpMessage.class.getName()).log(Level.SEVERE, null, e);
    }

    private static void logDebugInfo(byte[] bytes) {
        try {
            if (bytes != null) {
                Logger.getLogger(DbgpMessage.class.getName()).log(Level.FINE, new String(bytes, ISO_CHARSET));
            }
        } catch (UnsupportedEncodingException e) {
            assert false;
        }
    }

    /*
     * Notify user about unexpected format of received packet.
     */
    @NbBundle.Messages("DbgpMessage.packet.error=Error occured during communication with Xdebug.\n\n"
            + "Report issue, provide steps to reproduce and attach IDE and ideally also Xdebug log.\n\n"
            + "Xdebug 3: Add xdebug.log=/log_path/xdebug.log to your php.ini.\n"
            + "Xdebug 2: Add xdebug.remote_log=/log_path/xdebug.log to your php.ini.")
    private static void notifyPacketError(Exception e) {
        if (e != null) {
            LOGGER.log(Level.INFO, null, e);
        }
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.DbgpMessage_packet_error(), NotifyDescriptor.ERROR_MESSAGE));
    }

    private static byte[] getContent(InputStream inputStream, int size)
            throws IOException {
        byte[] bytes = new byte[size];
        int count = 0;
        try {
            while (count < size) {
                int awaitedBytes = size - count;
                int length = awaitedBytes < getMaxDataSize() ? awaitedBytes : getMaxDataSize();
                count += inputStream.read(bytes, count, length);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        if (count != size) {
            notifyPacketError(null);
            LOGGER.log(Level.INFO, "Read {0} bytes from socket input stream, but expected {1} bytes", new Object[]{count, size});
            return null;
        }
        int nullByte = inputStream.read();
        assert nullByte == 0;
        return bytes;
    }

    private static int getDataSize(InputStream inputStream) throws IOException {
        List<Integer> list = new LinkedList<>();
        int next;
        while ((next = inputStream.read()) > 0) {
            list.add(next);
        }
        byte[] bytes = new byte[list.size()];
        int i = 0;
        for (Integer integer : list) {
            byte byt = integer.byteValue();
            bytes[i++] = byt;
        }
        String str = new String(bytes, ISO_CHARSET);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Remove invalid XML characters from the string
     *
     * @param text The string containing xml message.
     * @return
     */
    private static String removeNonXMLCharacters(String text) {
        StringBuilder out = new StringBuilder();
        StringBuilder errorMessage = null;
        int codePoint;
        int index = 0;
        while (index < text.length()) {
            codePoint = text.codePointAt(index);
            if ((codePoint == 0x9)
                    || (codePoint == 0xA)
                    || (codePoint == 0xD)
                    || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                    || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                    || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
                out.append(Character.toChars(codePoint));
            } else {
                if (errorMessage == null) {
                    errorMessage = new StringBuilder();
                    errorMessage.append("The message from xdebug contains invalid XML characters: ");  //NOI18N
                } else {
                    errorMessage.append(", ");      //NOI18N
                }
                errorMessage.append(codePoint);
            }
            index += Character.charCount(codePoint);
        }
        if (errorMessage != null) {
            errorMessage.append("\nPlease mentioned it in http://netbeans.org/bugzilla/show_bug.cgi?id=179309."); //NOI18N
            LOGGER.warning(errorMessage.toString());
        }
        return out.toString();
    }

    private static Node getNode(byte[] bytes, String projectEncoding) throws IOException {
        if (builder == null || bytes == null) {
            return null;
        }
        String original = new String(bytes, projectEncoding);
        String inputWithoutNullChars = null;
        try {
            // this is basically workaround for a bug in xdebug, where xdebug
            // includes invalid xml characters into the message.
            String input = removeNonXMLCharacters(original);
            inputWithoutNullChars = input.replace("&#0;", ""); //NOI18N
            InputSource is = new InputSource(new StringReader(inputWithoutNullChars));
            is.setEncoding(projectEncoding);
            Document doc = builder.parse(is);
            return doc.getDocumentElement();
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, "Possible invalid XML - ORIGINAL:\n\n{0}\n\nAFTER REPLACE:\n\n{1}", new Object[]{original, inputWithoutNullChars});
            return null;
        }
    }

    private static String replaceHtmlEntities(String str) {
        if (str.indexOf("&") == -1) {
            return str;
        } else {
            for (Entry<String, Character> entry : ENTITIES.entrySet()) {
                String entity = entry.getKey();
                Character ch = entry.getValue();
                str = str.replace(entity, ch + "");
            }
            return str;
        }
    }

    private static class StubResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            return null;
        }

    }

    public static final class NoneDbgpMessage extends DbgpMessage {

        public NoneDbgpMessage(Node node) {
            super(node);
        }

        @Override
        public void process(DebugSession session, DbgpCommand command) {
        }

    }

}
