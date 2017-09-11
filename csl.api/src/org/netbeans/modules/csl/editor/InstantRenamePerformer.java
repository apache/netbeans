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
package org.netbeans.modules.csl.editor;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;

/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 * @author Jan Lahoda
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {
    
    private SyncDocumentRegion region;
    private Document doc;
    private JTextComponent target;
    
    private AttributeSet attribs = null;
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;
    
    private AttributeSet attribsSlave = null;
    private AttributeSet attribsSlaveLeft = null;
    private AttributeSet attribsSlaveRight = null;
    private AttributeSet attribsSlaveMiddle = null;
    private AttributeSet attribsSlaveAll = null;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target, Set<OffsetRange> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
	
	for (OffsetRange h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.getStart(), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.getEnd(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
            mainRegion = current;
	    } else {
            regions.add(current);
	    }
	}
	
	if (mainRegion == null) {
        Logger.getLogger(InstantRenamePerformer.class.getName()).warning("No highlight contains the caret (" + caretOffset + "; highlights=" + highlights + ")"); //NOI18N
        // Attempt to use another region - pick the one closest to the caret
        if (regions.size() > 0) {
            mainRegion = regions.get(0);
            int mainDistance = Integer.MAX_VALUE;
            for (MutablePositionRegion r : regions) {
                int distance = caretOffset < r.getStartOffset() ? (r.getStartOffset()-caretOffset) : (caretOffset-r.getEndOffset());
                if (distance < mainDistance) {
                    mainRegion = r;
                    mainDistance = distance;
                }
            }
        } else {
            return;
        }
	}
	
	regions.add(0, mainRegion);
	
	region = new SyncDocumentRegion(doc, regions);
	
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).addPostModificationDocumentListener(this);
        }
        
	target.addKeyListener(this);
	
        target.putClientProperty("NetBeansEditor.navigateBoundaries", mainRegion); // NOI18N
	target.putClientProperty(InstantRenamePerformer.class, this);
	
	requestRepaint();
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
    }


    private static InstantRenamePerformer getPerformerFromComponent(JTextComponent target) {
        return (InstantRenamePerformer)target.getClientProperty(InstantRenamePerformer.class);
    }

    public static void performInstantRename(JTextComponent target, Set<OffsetRange> highlights, int caretOffset) throws BadLocationException {
        //check if there is already an instant rename action in progress
        InstantRenamePerformer performer = getPerformerFromComponent(target);
        if(performer != null) {
            //cancel the old one
            performer.release();
        }

	new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync) {
            return;
        }
	
	inSync = true;
	region.sync(0);
	inSync = false;
	requestRepaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync) {
            return;
        }
	
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
	inSync = true;
	region.sync(0);
	inSync = false;
	requestRepaint();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
	if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) 
            || (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)) {
	    release();
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    void release() {
        target.putClientProperty("NetBeansEditor.navigateBoundaries", null); // NOI18N
	target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).removePostModificationDocumentListener(this);
        }
	target.removeKeyListener(this);
	target = null;

	region = null;
	requestRepaint();
        
	doc = null;
    }

    private void requestRepaint() {
        if (region == null) {
            OffsetsBag bag = getHighlightsBag(doc);
            bag.clear();
        } else {
            // Compute attributes
            if (attribs == null) {
                // read the attributes for the master region
                attribs = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext"); //NOI18N
                Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                attribsLeft = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsRight = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.RightBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsMiddle = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsAll = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.RightBorderLineColor, foreground,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );

                // read the attributes for the slave regions
                attribsSlave = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext-slave"); //NOI18N
                Color slaveForeground = (Color) attribsSlave.getAttribute(StyleConstants.Foreground);
                Color slaveBackground = (Color) attribsSlave.getAttribute(StyleConstants.Background);
                attribsSlaveLeft = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveRight = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.RightBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveMiddle = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveAll = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.RightBorderLineColor, slaveForeground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
            }
            
            OffsetsBag nue = new OffsetsBag(doc);
            for(int i = 0; i < region.getRegionCount(); i++) {
                int startOffset = region.getRegion(i).getStartOffset();
                int endOffset = region.getRegion(i).getEndOffset();
                int size = region.getRegion(i).getLength();
                if (size == 1) {
                    nue.addHighlight(startOffset, endOffset, i == 0 ? attribsAll : attribsSlaveAll);
                } else if (size > 1) {
                    nue.addHighlight(startOffset, startOffset + 1, i == 0 ? attribsLeft : attribsSlaveLeft);
                    nue.addHighlight(endOffset - 1, endOffset, i == 0 ? attribsRight : attribsSlaveRight);
                    if (size > 2) {
                        nue.addHighlight(startOffset + 1, endOffset - 1, i == 0 ? attribsMiddle : attribsSlaveMiddle);
                    }
                }
            }

            OffsetsBag bag = getHighlightsBag(doc);
            bag.setHighlights(nue);
        }
    }
    
//    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.red);
    
    private static AttributeSet getSyncedTextBlocksHighlight(String name) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors(name) : null;
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }

    private static AttributeSet createAttribs(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set.";

        List<Object> list = new ArrayList<Object>();
        
        for(int i = keyValuePairs.length / 2 - 1; i >= 0 ; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            if (attrKey != null && attrValue != null) {
                list.add(attrKey);
                list.add(attrValue);
            }
        }
        
        return AttributesUtilities.createImmutable(list.toArray());
    }
    
    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InstantRenamePerformer.class);
        
        if (bag == null) {
            doc.putProperty(InstantRenamePerformer.class, bag = new OffsetsBag(doc));
            
            Object stream = DataLoadersBridge.getDefault().getFileObject(doc);
            
            if (stream instanceof FileObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "Instant Rename Highlights Bag", new Object[] {(FileObject) stream, bag}); //NOI18N
            }
        }
        
        return bag;
    }
}
