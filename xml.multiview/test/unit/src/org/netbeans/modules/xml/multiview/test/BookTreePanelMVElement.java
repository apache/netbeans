/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.nodes.*;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.xml.multiview.Error;
import javax.swing.*;
/**
 *
 * @author mkuchtiak
 */
public class BookTreePanelMVElement extends TreePanelMultiViewElement {
    private TreePanelDesignEditor comp;
    private BookDataObject dObj;
    private PanelView view;
    //private PanelFactory factory;
    
    /** Creates a new instance of DesignMultiViewElement */
    public BookTreePanelMVElement(BookDataObject dObj) {
        super(dObj);
        this.dObj=dObj;
        view = new BookView(dObj);
        comp = new TreePanelDesignEditor(view);
        setVisualEditor(comp);
    }
    
    public void componentShowing() {
        super.componentShowing();
    }

    private class BookView extends TreePanelView {
        BookView(BookDataObject dObj) {
            super();           
            Children rootChildren = new Children.Array();
            Node root = new AbstractNode(rootChildren);
            try {
                Book book = dObj.getBook();
                Node bookNode = new BookNode(book);
                
                Chapter[] chapters = book.getChapter();
                Node[] chapterNode = new Node[chapters.length];
                Children ch = new Children.Array();
                for (int i=0;i<chapters.length;i++) {
                    chapterNode[i] = new ChapterNode(chapters[i]);
                }
                ch.add(chapterNode);
                Node chaptersNode = new SectionContainerNode(ch);
                chaptersNode.setDisplayName("Chapters");
                rootChildren.add(new Node[]{bookNode,chaptersNode});
                // add panels
            } catch (java.io.IOException ex) {
                System.out.println("ex="+ex);
                root.setDisplayName("Invalid Book");
            }
            setRoot(root);
        }
        /*
        public void initComponents() {
            setLayout(new java.awt.BorderLayout());
            JPanel scrollPanel= new JPanel();
            scrollPanel.add(new JButton("Hello"));
            JScrollPane scrollPane = new javax.swing.JScrollPane();
            scrollPane.setViewportView(scrollPanel);
            //scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            //add (scrollPane, java.awt.BorderLayout.CENTER);
            add(scrollPanel, java.awt.BorderLayout.CENTER);
        }
        
        public void showSelection(Node[] node) {
            System.out.println("showSelection()");
        }
        */
        public Error validateView() {
            try {
                Book book = dObj.getBook();
                String title = book.getTitle();
                if (title==null || title.length()==0) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(book,"title"); //NOI18N
                    return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                }
                Chapter[] chapters = book.getChapter();
                for (int i=0;i<chapters.length;i++) {
                    title = chapters[i].getTitle();
                    if (title==null || title.length()==0) {
                        Error.ErrorLocation loc = new Error.ErrorLocation(chapters[i],"title");
                        return new Error(Error.MISSING_VALUE_MESSAGE, "Title", loc);
                    }
                    for (int j=0;j<chapters.length;j++) {
                        String tit = chapters[j].getTitle();
                        if (i!=j && title.equals(tit)) {
                            Error.ErrorLocation loc = new Error.ErrorLocation(chapters[i],"title");
                            return new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc);
                        }
                    }
                }
            } catch (java.io.IOException ex){}
            return null;
        }
    }
    
    static class BookNode extends org.openide.nodes.AbstractNode implements TreeNode {
        Book book;
        BookNode(Book book) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(book.getTitle());
            this.book=book;
            //setIconBase("org/netbeans/modules/web/dd/multiview/resources/class"); //NOI18N
        }
        public TreePanel getPanel() {
            return new BookTreePanel();
        }
        public String getPanelId() {
            return "book";
        }
        
        public Book getBook() {
            return book;
        }
    }
    static class ChapterNode extends org.openide.nodes.AbstractNode implements TreeNode {
        private Chapter chapter;
        ChapterNode(Chapter chapter) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(chapter.getTitle());
            this.chapter=chapter;
            //setIconBase("org/netbeans/modules/web/dd/multiview/resources/class"); //NOI18N
        }
        public TreePanel getPanel() {
            return new ChapterTreePanel();
        }
        public String getPanelId() {
            return "chapter";
        }
        
        public Chapter getChapter() {
            return chapter;
        }
    }
    
}
