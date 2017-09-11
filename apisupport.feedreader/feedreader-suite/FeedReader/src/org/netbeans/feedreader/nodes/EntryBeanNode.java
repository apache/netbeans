/*
 * Copyright (c) 2010, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.netbeans.feedreader.nodes;

import com.sun.syndication.feed.synd.SyndEntry;
import java.beans.IntrospectionException;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.nodes.BeanNode;
import org.openide.nodes.FilterNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/** Wrapping the children in a FilterNode */
public class EntryBeanNode extends FilterNode {

    private final SyndEntry entry;

    public EntryBeanNode(SyndEntry entry) throws IntrospectionException {
        super(new BeanNode<SyndEntry>(entry), Children.LEAF,
                Lookups.fixed(entry, new EntryOpenCookie(entry)));
        this.entry = entry;
    }

    /** Using HtmlDisplayName ensures any HTML in RSS entry titles are properly handled, escaped, entities resolved, etc. */
    @Override
    public String getHtmlDisplayName() {
        return entry.getTitle();
    }

    /** Making a tooltip out of the entry's description */
    @Override
    public String getShortDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Author: ").append(entry.getAuthor()).append("; ");
        if (entry.getPublishedDate() != null) {
            sb.append("Published: ").append(entry.getPublishedDate().toString());
        }
        return sb.toString();
    }

    /** Providing the Open action on a feed entry */
    @Override
    public Action[] getActions(boolean popup) {
        return new Action[]{SystemAction.get(OpenAction.class)};
    }

    @Override
    public Action getPreferredAction() {
        return getActions(false)[0];
    }

}
