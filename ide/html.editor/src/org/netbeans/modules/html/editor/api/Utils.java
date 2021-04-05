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

package org.netbeans.modules.html.editor.api;

import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author marekfukala
 */
public class Utils {

    /** returns top most joined html token seuence for the document at the specified offset. */
    public static TokenSequence<HTMLTokenId> getJoinedHtmlSequence(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        return getJoinedHtmlSequence(th, offset);
    }
    
    /** returns top most joined html token seuence for the document at the specified offset. */
    public static TokenSequence<HTMLTokenId> getJoinedHtmlSequence(TokenHierarchy th, int offset) {
        TokenSequence ts = th.tokenSequence();
        if(ts == null) {
            return null;
        }
        ts.move(offset);

        while(ts.moveNext() || ts.movePrevious()) {
            if(ts.language() == HTMLTokenId.language()) {
                return ts;
            }
            
            ts = ts.embeddedJoined();
            
            if(ts == null) {
                break;
            }

            //position the embedded ts so we can search deeper
            //XXX this seems to be wrong, the return code should be checked
            ts.move(offset);
        }

        return null;
        
    }

    /**
     * Finds and returns a tag open token in token sequence positioned inside an html tag.
     * If tokens sequence contains <div onclick="alert()"/> and is positioned on a token
     * within the tag it will return OPEN_TAG token for the "div" text
     *
     * @param ts a TokenSequence to be used
     * @return
     */
    public static Token<HTMLTokenId> findTagOpenToken(TokenSequence ts) {
        assert ts != null;

        //skip the tag close symbol if the sequence points to it
        if(ts.token().id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
            if(!ts.movePrevious()) {
                return null;
            }
        }

        do {
            Token<HTMLTokenId> t = ts.token();
            TokenId id = t.id();
            if( id == HTMLTokenId.TAG_OPEN) {
                return t;
            }

            if(id == HTMLTokenId.TAG_OPEN_SYMBOL || id == HTMLTokenId.TAG_CLOSE_SYMBOL ||
                    id == HTMLTokenId.TEXT) {
                break;
            }


        } while (ts.movePrevious());

        return null;
    }

    //and now the magic...
    //the method returns an artificial mimetype so the user can enable/disable the error checks
    //for particular content. For example the text/facelets+xhtml mimetype is returned for
    //.xhtml pages with facelets content. This allows to normally verify the plain xhtml file
    //even if their mimetype is text/html
    //sure the correct solution would be to let the mimeresolver to create different mimetype,
    //but since the resolution can be pretty complex it is not done this way
    public static String getWebPageMimeType(SyntaxAnalyzerResult result) {
        InstanceContent ic = new InstanceContent();
        ic.add(result);
        WebPageMetadata wpmeta = WebPageMetadata.getMetadata(new AbstractLookup(ic));

        if (wpmeta != null) {
            //get an artificial mimetype for the web page, this doesn't have to be equal
            //to the fileObjects mimetype.
            String mimeType = (String) wpmeta.value(WebPageMetadata.MIMETYPE);
            if (mimeType != null) {
                return mimeType;
            }
        }

        FileObject fo = result.getSource().getSourceFileObject();
        if(fo != null) {
            return fo.getMIMEType();
        } else {
            //no fileobject?
            return result.getSource().getSnapshot().getMimeType();
        }

    }
}
