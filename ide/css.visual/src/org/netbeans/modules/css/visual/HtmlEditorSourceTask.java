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
