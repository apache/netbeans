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
package org.netbeans.modules.profiler.heapwalk.details.basic;

import org.netbeans.modules.profiler.heapwalk.details.api.ExportAction;
import org.netbeans.modules.profiler.heapwalk.details.api.StringDecoder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.lib.profiler.results.ExportDataDumper;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import static org.netbeans.modules.profiler.heapwalk.details.basic.ArrayValueView.Type.*;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ArrayValueView_Truncated=... <truncated>",                                     // NOI18N
    "ArrayValueView_Value=Value:",                                                  // NOI18N
    "ArrayValueView_Items=Array items:",                                            // NOI18N
    "ArrayValueView_All=Show All",                                                  // NOI18N
    "ArrayValueView_Save=Save to File",                                             // NOI18N
    "ArrayValueView_OutOfMemory=Out of memory - value too long."                    // NOI18N
})
final class ArrayValueView extends DetailsProvider.View implements Scrollable, ExportAction.ExportProvider {
    
    private static final int MAX_PREVIEW_LENGTH = 256;
    private static final int MAX_ARRAY_ITEMS = 1000;
    private static final int MAX_CHARARRAY_ITEMS = 500000;
    private static final String TRUNCATED = Bundle.ArrayValueView_Truncated();
    enum Type {STRING, STRING_BUILDER, PRIMITIVE_ARRAY};
    
    private final String className;
    
    private JTextArea view;
    private JButton all;
    
    private String caption;
    private Heap heap;
    private List<String> values;
    private byte coder = -1;
    private String separator;
    private int offset;
    private int count;
    private boolean truncated;
    private boolean chararray;
    private boolean bytearray;
    private String instanceIdentifier;
    private Type type;
    
    protected ArrayValueView(String className, Instance instance, Heap heap) {
        super(instance, heap);
        this.className = className;
    }

    protected void computeView(Instance instance, Heap h) {
        
        if (StringDetailsProvider.STRING_MASK.equals(className)) {                  // String
            separator = "";                                                         // NOI18N
            offset = DetailsUtils.getIntFieldValue(instance, "offset", 0);          // NOI18N
            count = DetailsUtils.getIntFieldValue(instance, "count", -1);           // NOI18N
            coder = DetailsUtils.getByteFieldValue(instance, "coder", (byte) -1);   // NOI18N
            values = DetailsUtils.getPrimitiveArrayFieldValues(instance, "value");  // NOI18N
            caption = Bundle.ArrayValueView_Value();
            heap = h;
            type = STRING;
        } else if (StringDetailsProvider.BUILDERS_MASK.equals(className)) {         // AbstractStringBuilder+
            separator = "";                                                         // NOI18N
            offset = 0;
            count = DetailsUtils.getIntFieldValue(instance, "count", -1);           // NOI18N
            coder = DetailsUtils.getByteFieldValue(instance, "coder", (byte) -1);   // NOI18N
            values = DetailsUtils.getPrimitiveArrayFieldValues(instance, "value");  // NOI18N
            caption = Bundle.ArrayValueView_Value();
            heap = h;
            type = STRING_BUILDER;
        } else if (instance instanceof PrimitiveArrayInstance) {                    // Primitive array
            chararray = "char[]".equals(instance.getJavaClass().getName());         // NOI18N
            bytearray = "byte[]".equals(instance.getJavaClass().getName());         // NOI18N
            separator = chararray ? "" : ", ";                                      // NOI18N
            offset = 0;
            values = DetailsUtils.getPrimitiveArrayValues(instance);
            count = values == null ? 0 : values.size();
            caption = Bundle.ArrayValueView_Items();
            type = PRIMITIVE_ARRAY;
        }
        instanceIdentifier=instance.getJavaClass().getName()+"#"+instance.getInstanceNumber(); // NOI18N
        final String preview = getString(true);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                setBackground(UIUtils.getProfilerResultsBackground());
                setOpaque(true);
                
                removeAll();
                
                JLabel l = new JLabel(caption, JLabel.LEADING);
                l.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
                add(l, BorderLayout.NORTH);
                
                view = new JTextArea();
                l.setLabelFor(view);
                view.setEditable(false);
                view.setLineWrap(true);
                view.setWrapStyleWord(true);
                view.setText(preview);
                try { view.setCaretPosition(0); } catch (IllegalArgumentException e) {}
                
                JScrollPane viewScroll = new JScrollPane(view,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                add(viewScroll, BorderLayout.CENTER);
                
                JPanel p = new JPanel(new GridBagLayout());
                p.setOpaque(false);
                
                all = htmlButton(Bundle.ArrayValueView_All(), truncated && count < (chararray ? MAX_CHARARRAY_ITEMS : MAX_ARRAY_ITEMS), new Runnable() {
                    public void run() { showAll(); }
                });
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.insets = new Insets(3, 0, 0, 5);
                p.add(all, c);
                
                JButton save = htmlButton(Bundle.ArrayValueView_Save(), !preview.isEmpty(), new Runnable() {
                    public void run() {
                        new ExportAction(ArrayValueView.this).actionPerformed(null);
                    }
                });
                c = new GridBagConstraints();
                c.gridx = 1;
                c.insets = new Insets(3, 0, 0, 0);
                p.add(save, c);
                
                JPanel f = new JPanel(null);
                f.setOpaque(false);
                c = new GridBagConstraints();
                c.gridx = 2;
                c.weightx = 1;
                c.fill = GridBagConstraints.HORIZONTAL;
                p.add(f, c);
                
                add(p, BorderLayout.SOUTH);
                
                revalidate();
                doLayout();
                repaint();
            }
        });
    }
    
    private void showAll() {
        all.setEnabled(false);
        view.setEnabled(false);
        BrowserUtils.performTask(new Runnable() {
            public void run() {
                String _preview = null;
                try {
                    _preview = getString(false);
                } catch (OutOfMemoryError e) {
                    ProfilerDialogs.displayError(Bundle.ArrayValueView_OutOfMemory());
                    return;
                }
                
                final String preview = _preview;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            view.setText(preview);
                            try { view.setCaretPosition(0); } catch (IllegalArgumentException e) {}
                            view.setEnabled(true);
                        } catch (OutOfMemoryError e) {
                            ProfilerDialogs.displayError(Bundle.ArrayValueView_OutOfMemory());
                        }
                    }
                });
            }
        });
    }
    
    
    private String getString(boolean preview) {
        if (values == null) return "";                                              // NOI18N
        StringDecoder decoder = new StringDecoder(heap, coder, values);
        int valuesCount = count < 0 ? decoder.getStringLength() - offset : count;            
        int separatorLength = separator == null ? 0 : separator.length();
        int estimatedSize = (int)Math.min((long)valuesCount * (2 + separatorLength), MAX_PREVIEW_LENGTH + TRUNCATED.length());
        StringBuilder value = new StringBuilder(estimatedSize);
        int lastValue = offset + valuesCount - 1;
        for (int i = offset; i <= lastValue; i++) {
            value.append(decoder.getValueAt(i));
            if (preview && value.length() >= MAX_PREVIEW_LENGTH) {
                value.append(TRUNCATED);
                truncated = true;
                break;
            }
            if (separator != null && i < lastValue) value.append(separator);
        }
        return value.toString();
    }
    
    
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        // Scroll almost one screen
        Container parent = getParent();
        if (!(parent instanceof JViewport)) return 50;
        return (int)(((JViewport)parent).getHeight() * 0.95f);
    }

    public boolean getScrollableTracksViewportHeight() {
        // Allow dynamic vertical enlarging of the panel but request the vertical scrollbar when needed
        Container parent = getParent();
        if (!(parent instanceof JViewport)) return false;
        return getMinimumSize().height < ((JViewport)parent).getHeight();
    }

    public boolean getScrollableTracksViewportWidth() {
        // Allow dynamic horizontal enlarging of the panel but request the vertical scrollbar when needed
        Container parent = getParent();
        if (!(parent instanceof JViewport)) return false;
        return getMinimumSize().width < ((JViewport)parent).getWidth();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }
    
    
    private static JButton htmlButton(final String text, final boolean enabled, final Runnable handler) {
        JButton b = new JButton() {
            public void setEnabled(boolean b) {
                setText(!b ? text : "<html><nobr><a href='#'>" + text + "</a></nobr></html>"); // NOI18N
                setCursor(!b ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                super.setEnabled(b);
            }
        };
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setEnabled(enabled);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { handler.run(); }
        });
        return b;
    }

    @Override
    public void exportData(int exportedFileType, ExportDataDumper eDD) {
        String comma = ","; // NOI18N
        if (values != null) {
            StringDecoder decoder = new StringDecoder(heap, coder, values);
            int valuesCount = count < 0 ? decoder.getStringLength() - offset : count;            
            int lastValue = offset + valuesCount - 1;
            for (int i = offset; i <= lastValue; i++) {
                String value = decoder.getValueAt(i);
                
                switch (exportedFileType) {
                    case ExportAction.MODE_CSV:
                        eDD.dumpData(value);
                        eDD.dumpData(comma);
                        break;
                    case ExportAction.MODE_TXT:
                        eDD.dumpData(value);
                        break;
                    case ExportAction.MODE_BIN:
                        byte b = Byte.valueOf(value);
                        eDD.dumpByte(b);
                        break;
                    default:
                        throw new IllegalArgumentException(); //Illegal export type
                }
            }
        }
        eDD.close();
    }

    @Override
    public String getViewName() {
        return instanceIdentifier;
    }

    @Override
    public boolean hasRawData() {
        return type == PRIMITIVE_ARRAY;
    }

    @Override
    public boolean hasBinaryData() {
        return bytearray;
    }

    @Override
    public boolean hasText() {
        switch (type) {
            case STRING:
            case STRING_BUILDER:
                return true;
            case PRIMITIVE_ARRAY:
                return chararray;
        }
        throw new IllegalArgumentException(type.toString());
    }

    @Override
    public boolean isExportable() {
        return hasText() || hasBinaryData() || hasRawData();
    }
    
}
