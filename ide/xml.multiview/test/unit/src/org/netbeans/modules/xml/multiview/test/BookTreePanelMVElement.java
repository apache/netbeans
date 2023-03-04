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
