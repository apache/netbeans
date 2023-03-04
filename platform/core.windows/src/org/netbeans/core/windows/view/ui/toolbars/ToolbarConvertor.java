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

package org.netbeans.core.windows.view.ui.toolbars;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.settings.Convertor;
import org.netbeans.spi.settings.Saver;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Creates ToolbarConfiguration instances from XML files registered in XML layer.
 *
 * @author S. Aubrecht
 */
public final class ToolbarConvertor extends Convertor {
    /** xml element for row */
    private static final String TAG_CONFIGURATION = "Configuration"; // NOI18N
    /** xml element for row */
    private static final String TAG_ROW = "Row"; // NOI18N
    /** xml element for toolbar */
    private static final String TAG_TOOLBAR = "Toolbar"; // NOI18N
    /** xml attribute for toolbar name */
    private static final String ATT_TOOLBAR_NAME = "name"; // NOI18N
    /** xml attribute for toolbar horizontal alignment */
    private static final String ATT_TOOLBAR_ALIGNMENT = "align"; // NOI18N
    /** xml attribute for toolbar visible */
    private static final String ATT_TOOLBAR_VISIBLE = "visible"; // NOI18N
    /** xml attribute for toolbar draggable */
    private static final String ATT_TOOLBAR_DRAGGABLE = "draggable"; // NOI18N

    public static ToolbarConvertor create() {
        return new ToolbarConvertor();
    }

    @Override
    public Object read(Reader r) throws IOException, ClassNotFoundException {
        Lookup lkp = findContext(r);
        FileObject fo = lkp.lookup(FileObject.class);
        String displayName = fo.getFileSystem().getDecorator().annotateName(fo.getName(), Collections.singleton(fo));
        try {
            XMLReader reader = XMLUtil.createXMLReader(true);
            ToolbarParser parser = new ToolbarParser();
            reader.setContentHandler(parser);
            reader.setErrorHandler(parser);
            reader.setEntityResolver(EntityCatalog.getDefault());
            reader.parse(new InputSource(r));
            return parser.createToolbarConfiguration( fo.getName(), displayName );
        } catch( SAXException saxE ) {
            IOException ioE = new IOException();
            ioE.initCause(saxE);
            throw ioE;
        }
    }

    @Override
    public void write(Writer writer, Object inst) throws IOException {
        if( !(inst instanceof ToolbarConfiguration) )
            return;

        BufferedWriter w = new BufferedWriter(writer);
        w.write("<?xml version=\"1.0\"?>"); //NOI18N
        w.newLine();

        w.write("<!DOCTYPE Configuration PUBLIC \"-//NetBeans IDE//DTD toolbar 1.1//EN\" \"http://www.netbeans.org/dtds/toolbar1_1.dtd\">"); //NOI18N
        w.newLine();

        w.write("<"); w.write(TAG_CONFIGURATION); w.write(">"); //NOI18N
        w.newLine();

        for( List<? extends ToolbarConstraints> row : ((ToolbarConfiguration)inst).getSnapshot() ) {
            w.write("\t<"); w.write(TAG_ROW); w.write(">"); //NOI18N
            w.newLine();

            for( ToolbarConstraints tc : row ) {
                w.write("\t\t<"); w.write(TAG_TOOLBAR); //NOI18N
                w.write(" "); //NOI18N
                w.write(ATT_TOOLBAR_NAME); w.write("=\""); w.write(tc.getName()); w.write("\""); //NOI18N
                w.write(" "); //NOI18N
                w.write(ATT_TOOLBAR_VISIBLE); w.write("=\""); w.write(tc.isVisible() ? "true" : "false"); w.write("\""); //NOI18N
                w.write(" "); //NOI18N
                w.write(ATT_TOOLBAR_DRAGGABLE); w.write("=\""); w.write(tc.isDraggable() ? "true" : "false"); w.write("\""); //NOI18N
                w.write(" "); //NOI18N
                w.write(ATT_TOOLBAR_ALIGNMENT); w.write("=\""); w.write(tc.getAlign().toString()); w.write("\""); //NOI18N
                w.write("/>"); //NOI18N
                w.newLine();

            }

            w.write("\t</"); w.write(TAG_ROW); w.write(">"); //NOI18N
            w.newLine();
        }

        w.write("</"); w.write(TAG_CONFIGURATION); w.write(">"); //NOI18N
        w.newLine();
        w.close();
    }

    @Override
    public void registerSaver(Object inst, Saver s) {
        if( inst instanceof ToolbarConfiguration ) {
            ((ToolbarConfiguration)inst).setSaverCallback(s);
        }
    }

    @Override
    public void unregisterSaver(Object inst, Saver s) {
        if( inst instanceof ToolbarConfiguration ) {
            ((ToolbarConfiguration)inst).setSaverCallback(null);
        }
    }

    private static class ToolbarParser extends DefaultHandler {

        private final List<ToolbarRow> rows = new ArrayList<ToolbarRow>(10);
        private ToolbarRow currentRow;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if( TAG_ROW.equals(qName) ) {
                currentRow = new ToolbarRow();
            } else if( TAG_TOOLBAR.equals(qName) ) {
                String barName = attributes.getValue(ATT_TOOLBAR_NAME);
                if( null == barName || barName.trim().length() == 0 )
                    return;

                boolean visible = !"false".equals( attributes.getValue(ATT_TOOLBAR_VISIBLE) ); //NOI18N

                ToolbarConstraints.Align align = ToolbarConstraints.Align.fromString( attributes.getValue(ATT_TOOLBAR_ALIGNMENT) );

                boolean draggable = !"false".equals( attributes.getValue(ATT_TOOLBAR_DRAGGABLE) ); //NOI18N

                //#154332 - HACK always dock quick search toolbar to the right
                //needed when importing toolbar settings from nb 6.5
                if( "QuickSearch".equals( barName ) ) {
                    align = ToolbarConstraints.Align.right;
                }
                ToolbarConstraints tc = new ToolbarConstraints(barName, align, visible, draggable);
                if( null != currentRow )
                    currentRow.addConstraint(tc);
            }
        }


        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if( TAG_ROW.equals(qName) ) {
                if( null != currentRow && !currentRow.isEmpty() ) {
                    rows.add(currentRow);
                }
                currentRow = null;
            }
        }

        private ToolbarConfiguration createToolbarConfiguration(String name, String displayName) {
            return new ToolbarConfiguration(name, displayName, rows);
        }
    }
}
