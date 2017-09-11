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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.css.visual;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.util.Elements;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marek Fukala
 */
public final class HtmlEditorSourceTask extends ParserResultTask<HtmlParserResult> {

    private static final Logger LOGGER = Logger.getLogger(HtmlEditorSourceTask.class.getSimpleName());
    
    private static HtmlSourceElementHandle activeElement;

    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/html", service = TaskFactory.class)
    })
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new HtmlEditorSourceTask());
        }
    }

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        //no-op
    }

    @Override
    public void run(HtmlParserResult result, SchedulerEvent event) {
        FileObject file = result.getSnapshot().getSource().getFileObject();
        if (file == null) {
            LOGGER.log(Level.FINE, "null file, exit");
            return;
        }

        if (!file.isValid()) {
            LOGGER.log(Level.FINE, "invalid file, exit");
            return;
        }

        if (event == null) {
            LOGGER.log(Level.FINE, "run() - NULL SchedulerEvent?!?!?!");
        } else {
            if (event instanceof CursorMovedSchedulerEvent) {
                setActiveElement(result, ((CursorMovedSchedulerEvent) event).getCaretOffset());
                return ;
            } else {
                LOGGER.log(Level.FINE, "run() - !(event instanceof CursorMovedSchedulerEvent)");
            }
        }
        
        //error - clear the active element
        activeElement = null;
    }
    
    
    private void setActiveElement(HtmlParserResult result, int caretOffset) {
        Snapshot snapshot = result.getSnapshot();
        FileObject file = snapshot.getSource().getFileObject();
        int embeddedCaretOffset = snapshot.getEmbeddedOffset(caretOffset);
        if(embeddedCaretOffset == -1) {
            return ;
        }
        
        //Bug 222535 - Create rule dialog is populated with parent element
        Element findBack = result.findByPhysicalRange(embeddedCaretOffset, false);
        if(findBack != null 
                && (findBack.type() == ElementType.OPEN_TAG || findBack.type() == ElementType.CLOSE_TAG) 
                && findBack.to() == embeddedCaretOffset) {
            // Situation:
            // ... <div class="x">|  or ... </div>|
            // in this case use the element before the caret
            embeddedCaretOffset--;
        }
        
        Node node = result.findBySemanticRange(embeddedCaretOffset, true);
        if (node != null) {
            if (node.type() == ElementType.OPEN_TAG) { //may be root node!
                activeElement = new HtmlSourceElementHandle((OpenTag)node, snapshot, file);
                return ;
            }
        }
        //no active element
        activeElement = null;
    }
    
    /**
     * Gets the active (under caret) html source element from the last focused 
     * file with html content.
     * 
     */
    public static HtmlSourceElementHandle getElement() {
        return activeElement;
    }
    
}
