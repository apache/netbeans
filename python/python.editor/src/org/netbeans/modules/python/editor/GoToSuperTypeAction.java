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
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonAstUtils;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.python.editor.imports.PopupUtil;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class GoToSuperTypeAction extends BaseAction {
    @SuppressWarnings("deprecation")
    public GoToSuperTypeAction() {
        super("goto-super-implementation", SAVE_POSITION | ABBREV_RESET); // NOI18N

    }

    @Override
    public Class getShortDescriptionBundleClass() {
        return GoToSuperTypeAction.class;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target.getCaret() == null) {
            return;
        }

        FileObject fo = GsfUtilities.findFileObject(target);
        BaseDocument doc = (BaseDocument)target.getDocument();

        if (fo != null) {
            // Cleanup import section: Remove newlines
            // Sort imports alphabetically
            // Split multi-imports into single splits
            // Look for missing imports: Take ALL calls,
            // and ensure we have imports for all of them.
            // (This means I need to have a complete index of all the builtins)
            // Combine multiple imports (from X import A,, from X import B,  etc. into single list)
            // Move imports that I think may be unused to the end - or just comment them out?

            // For imports: Gather imports from everywhere... move others into the same section
            PythonParserResult info = null;

            Source source = Source.create(fo);
            if (source != null) {
                final PythonParserResult[] infoHolder = new PythonParserResult[1];
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            infoHolder[0] = (PythonParserResult) resultIterator.getParserResult();
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
                info = infoHolder[0];
            }
            if (info != null && PythonAstUtils.getRoot(info) != null) {
                // Figure out if we're on a method, and if so, locate the nearest
                // method it is overriding.
                // Otherwise, if we're on a class (anywhere, not just definition),
                // go to the super class.
                PythonDeclarationFinder finder = new PythonDeclarationFinder();
                int offset = target.getCaretPosition();
                DeclarationLocation location = finder.getSuperImplementations(info, offset);
                if (location == DeclarationLocation.NONE) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    if (location.getAlternativeLocations().size() > 0 &&
                            !PopupUtil.isPopupShowing()) {
                        // Many alternatives - pop up a dialog and make the user choose
                        if (chooseAlternatives(doc, offset, location.getAlternativeLocations())) {
                            return;
                        }
                    }

                    GsfUtilities.open(location.getFileObject(), location.getOffset(), null);
                }
            }
        }
    }

    // COPY FROM GSF's GOTOSUPPORT!!!
    private static boolean chooseAlternatives(Document doc, int offset, List<AlternativeLocation> alternatives) {
        Collections.sort(alternatives);

        // Prune results a bit
        int MAX_COUNT = 30; // Don't show more items than this
        String previous = "";
        GsfHtmlFormatter formatter = new GsfHtmlFormatter();
        int count = 0;
        List<AlternativeLocation> pruned = new ArrayList<>(alternatives.size());
        for (AlternativeLocation alt : alternatives) {
            String s = alt.getDisplayHtml(formatter);
            if (!s.equals(previous)) {
                pruned.add(alt);
                previous = s;
                count++;
                if (count == MAX_COUNT) {
                    break;
                }
            }
        }
        alternatives = pruned;
        if (alternatives.size() <= 1) {
            return false;
        }

        JTextComponent target = findEditor(doc);
        if (target != null) {
            try {
                Rectangle rectangle = target.modelToView(offset);
                Point point = new Point(rectangle.x, rectangle.y + rectangle.height);
                SwingUtilities.convertPointToScreen(point, target);

                String caption = NbBundle.getMessage(GoToSuperTypeAction.class, "ChooseDecl");
                PopupUtil.showPopup(new DeclarationPopup(caption, alternatives), caption, point.x, point.y, true, 0);

                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return false;
    }

    /** TODO - MOVE TO UTILITTY LIBRARY */
    private static JTextComponent findEditor(Document doc) {
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        if (comp.getDocument() == doc) {
            return comp;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            if (comp.getDocument() == doc) {
                return comp;
            }
        }

        return null;
    }
    
    /**
     * Copied from csl.api.core.
     */
    private static class GsfHtmlFormatter extends HtmlFormatter {
        protected boolean isDeprecated;
        protected boolean isParameter;
        protected boolean isType;
        protected boolean isName;
        protected boolean isEmphasis;

        protected StringBuilder sb = new StringBuilder();

        public GsfHtmlFormatter() {
        }

        @Override
        public void reset() {
            textLength = 0;
            sb.setLength(0);
        }

        @Override
        public void appendHtml(String html) {
            sb.append(html);
            // Not sure what to do about maxLength here... but presumably
        }

        @Override
        public void appendText(String text, int fromInclusive, int toExclusive) {
            for (int i = fromInclusive; i < toExclusive; i++) {
                if (textLength >= maxLength) {
                    if (textLength == maxLength) {
                        sb.append("...");
                        textLength += 3;
                    }
                    break;
                }
                char c = text.charAt(i);

                switch (c) {
                case '<':
                    sb.append("&lt;"); // NOI18N

                    break;

                case '>': // Only ]]> is dangerous
                    if ((i > 1) && (text.charAt(i - 2) == ']') && (text.charAt(i - 1) == ']')) {
                        sb.append("&gt;"); // NOI18N
                    } else {
                        sb.append(c);
                    }
                    break;

                case '&':
                    sb.append("&amp;"); // NOI18N

                    break;

                default:
                    sb.append(c);
                }

                textLength++;
            }
        }

        @Override
        public void name(ElementKind kind, boolean start) {
            assert start != isName;
            isName = start;

            if (isName) {
                sb.append("<b>");
            } else {
                sb.append("</b>");
            }
        }

        @Override
        public void parameters(boolean start) {
            assert start != isParameter;
            isParameter = start;

            if (isParameter) {
                sb.append("<font color=\"#808080\">");
            } else {
                sb.append("</font>");
            }
        }

        @Override
        public void active(boolean start) {
            emphasis(start);
        }

        @Override
        public void type(boolean start) {
            assert start != isType;
            isType = start;

            if (isType) {
                sb.append("<font color=\"#808080\">");
            } else {
                sb.append("</font>");
            }
        }

        @Override
        public void deprecated(boolean start) {
            assert start != isDeprecated;
            isDeprecated = start;

            if (isDeprecated) {
                sb.append("<s>");
            } else {
                sb.append("</s>");
            }
        }

        @Override
        public String getText() {
            assert !isParameter && !isDeprecated && !isName && !isType;

            return sb.toString();
        }

        @Override
        public void emphasis(boolean start) {
            assert start != isEmphasis;
            isEmphasis = start;

            if (isEmphasis) {
                sb.append("<b>");
            } else {
                sb.append("</b>");
            }
        }
    }
    private static class DeclarationPopup extends JPanel implements FocusListener {
    
    private String caption;
    private List<AlternativeLocation> declarations;
    
    /** Creates new form DeclarationPopup */
    public DeclarationPopup(String caption, List<AlternativeLocation> declarations) {
        this.caption = caption;
        this.declarations = declarations;
        
        initComponents();
        
        jList1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addFocusListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setFocusCycleRoot(true);
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(caption
        );
        jLabel1.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jList1.setModel(createListModel());
        jList1.setCellRenderer(new RendererImpl());
        jList1.setSelectedIndex(0);
        jList1.setVisibleRowCount(declarations.size()
        );
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jList1KeyPressed(evt);
            }
        });
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>                        
    
    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {                                    
        // TODO add your handling code here:
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 1) {
            openSelected();
        }
    }                                   
    
    private void jList1KeyPressed(java.awt.event.KeyEvent evt) {                                  
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && evt.getModifiers() == 0) {
            openSelected();
        }
    }                                 
    
    
    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration                   
    
    private void openSelected() {
        AlternativeLocation desc = (AlternativeLocation) jList1.getSelectedValue();
        
        if (desc != null) {
            DeclarationLocation location = desc.getLocation();
            if (location == DeclarationLocation.NONE) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DeclarationPopup.class, "InvalidLoc"));
                Toolkit.getDefaultToolkit().beep();
            } else {
                String invalid = location.getInvalidMessage();
                if (invalid != null) {
                    // TODO - show in the editor as an error instead?
                    StatusDisplayer.getDefault().setStatusText(invalid);
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    FileObject fileObject = location.getFileObject();
                    if (fileObject != null) {
                        UiUtils.open(fileObject,location.getOffset());
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }
        
        PopupUtil.hidePopup();
    }
    
    private ListModel createListModel() {
        DefaultListModel dlm = new DefaultListModel();
        
        for (AlternativeLocation el: declarations) {
            dlm.addElement(el);
        }
        
        return dlm;
    }
    
    private static class RendererImpl extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof AlternativeLocation) {
                AlternativeLocation desc = (AlternativeLocation) value;
                ElementHandle e = desc.getElement();
                ImageIcon icon = Icons.getElementIcon(e.getKind(), e.getModifiers());
                setIcon(icon);
                
                GsfHtmlFormatter formatter = new GsfHtmlFormatter();
                String s = desc.getDisplayHtml(formatter);
                setText("<html>"+s);
            }
            
            return c;
        }
    }
    
    @Override
    public void focusGained(FocusEvent arg0) {
        jList1.requestFocus();
        jList1.requestFocusInWindow();
    }
    
    @Override
    public void focusLost(FocusEvent arg0) {
    }
    
    }
    private static class Icons {
        private static final String ICON_BASE = "org/netbeans/modules/csl/source/resources/icons/";
        private static final String GIF_EXTENSION = ".gif";
        private static final String PNG_EXTENSION = ".png";
        private static final String WAIT = ICON_BASE + "wait" + PNG_EXTENSION;
        //private static final Map<String, Icon> icons = new HashMap<String, Icon>();

        /** Creates a new instance of Icons */
        private Icons() {
        }

    //    public static Icon getBusyIcon() {
    //        Image img = Utilities.loadImage(WAIT);
    //
    //        if (img == null) {
    //            return null;
    //        } else {
    //            return new ImageIcon(img);
    //        }
    //    }
    //
    //    public static Icon getMethodIcon() {
    //        // TODO - consider modifiers
    //        Image img =
    //            Utilities.loadImage(ICON_BASE + "method" + "Public" + PNG_EXTENSION);
    //
    //        if (img == null) {
    //            return null;
    //        } else {
    //            return new ImageIcon(img);
    //        }
    //    }
    //
    //    public static Icon getFieldIcon() {
    //        // TODO - consider modifiers
    //        Image img =
    //            Utilities.loadImage(ICON_BASE + "field" + "Public" + PNG_EXTENSION);
    //
    //        if (img == null) {
    //            return null;
    //        } else {
    //            return new ImageIcon(img);
    //        }
    //    }
    //
    //    public static Icon getClassIcon() {
    //        Image img = Utilities.loadImage(ICON_BASE + "class" + PNG_EXTENSION);
    //
    //        if (img == null) {
    //            return null;
    //        } else {
    //            return new ImageIcon(img);
    //        }
    //    }
    //
    //    public static Icon getModuleIcon() {
    //        Image img =
    //            Utilities.loadImage(ICON_BASE + "package"  + GIF_EXTENSION);
    //
    //        if (img == null) {
    //            return null;
    //        } else {
    //            return new ImageIcon(img);
    //        }
    //    }

        public static ImageIcon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {

            if ( modifiers == null ) {
                modifiers = Collections.<Modifier>emptyList();
            }

            Image img = null;

            switch( elementKind ) {
            case FILE:
                img = ImageUtilities.loadImage( ICON_BASE + "emptyfile-icon" + PNG_EXTENSION );
                break;
            case ERROR:
                img = ImageUtilities.loadImage( ICON_BASE + "error-glyph" + GIF_EXTENSION );
                break;
            case PACKAGE:
            case MODULE:
                img = ImageUtilities.loadImage( ICON_BASE + "package" + GIF_EXTENSION );
                break;
            case TEST:
                img = ImageUtilities.loadImage( ICON_BASE + "test" + PNG_EXTENSION );
                break;
            case CLASS:
            case INTERFACE:
                img = ImageUtilities.loadImage( ICON_BASE + "class" + PNG_EXTENSION );
                break;
            case TAG:
                img = ImageUtilities.loadImage( ICON_BASE + "html_element" + PNG_EXTENSION );
                break;
            case RULE:
                img = ImageUtilities.loadImage( ICON_BASE + "rule" + PNG_EXTENSION );
                break;
            case VARIABLE:
            case PROPERTY:
            case GLOBAL:
            case ATTRIBUTE:
            case FIELD:
                img = ImageUtilities.loadImage( getIconName( ICON_BASE + "field", PNG_EXTENSION, modifiers ) );
                break;
            case PARAMETER:
            case CONSTANT:
                img = ImageUtilities.loadImage(ICON_BASE + "constant" + PNG_EXTENSION );
                break;
            case CONSTRUCTOR:
                img = ImageUtilities.loadImage( getIconName( ICON_BASE + "constructor", PNG_EXTENSION, modifiers ) );
                break;
            case METHOD:
                img = ImageUtilities.loadImage( getIconName( ICON_BASE + "method", PNG_EXTENSION, modifiers ) );
                break;
            case DB:
                img = ImageUtilities.loadImage(ICON_BASE + "database" + GIF_EXTENSION);
                break;
            default:   
                    img = null;
            }

            return img == null ? null : new ImageIcon (img);
        }

        // Private Methods ---------------------------------------------------------
        private static String getIconName(String typeName, String extension, Collection<Modifier> modifiers) {

            StringBuffer fileName = new StringBuffer( typeName );

            if (modifiers.contains(Modifier.STATIC)) {
                fileName.append( "Static" );
            }
            if (modifiers.contains(Modifier.PROTECTED)) {
                return fileName.append( "Protected" ).append( extension ).toString();
            }
            if (modifiers.contains(Modifier.PRIVATE)) {
                return fileName.append( "Private" ).append( extension ).toString();
            }
            // Assume it's public
            return fileName.append( "Public" ).append( extension ).toString();
            //return fileName.append( "Package" ).append( extension ).toString();
            //return fileName.append(extension).toString();
        }
    }
}
