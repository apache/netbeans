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

package org.netbeans.modules.html.parser.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;

/**
 *
 * @author marekfukala
 */
public enum ElementDescriptor {

    //section generated from the whatwg specification at http://www.whatwg.org/specs/web-apps/current-work
    //by the GenerateElementsIndex unit test
    //
    //>>>>>>>>>>>>>>>>>>>>>>>>

    //NOI18N
A(
	HtmlTagType.HTML,
	new Link("a", "semantics.html#the-a-element"),
	 "Hyperlink", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_HYPERLINK_HREF, Attribute.ATTR_HYPERLINK_TARGET, Attribute.ATTR_HYPERLINK_DOWNLOAD, Attribute.PING, Attribute.ATTR_HYPERLINK_REL, Attribute.ATTR_HYPERLINK_HREFLANG, Attribute.ATTR_HYPERLINK_TYPE), 
	new Link("HTMLAnchorElement", "semantics.html#htmlanchorelement")
), 

ABBR(
	HtmlTagType.HTML,
	new Link("abbr", "semantics.html#the-abbr-element"),
	 "Abbreviation", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

ADDRESS(
	HtmlTagType.HTML,
	new Link("address", "semantics.html#the-address-element"),
	 "Contact information for a page or article element", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

AREA(
	HtmlTagType.HTML,
	new Link("area", "embedded-content.html#the-area-element"),
	 "Hyperlink or dead area on an image map", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_AREA_ALT, Attribute.ATTR_AREA_COORDS, Attribute.ATTR_AREA_SHAPE, Attribute.ATTR_HYPERLINK_HREF, Attribute.ATTR_HYPERLINK_TARGET, Attribute.ATTR_HYPERLINK_DOWNLOAD, Attribute.PING, Attribute.ATTR_HYPERLINK_REL, Attribute.ATTR_HYPERLINK_HREFLANG, Attribute.ATTR_HYPERLINK_TYPE), 
	new Link("HTMLAreaElement", "embedded-content.html#htmlareaelement")
), 

ARTICLE(
	HtmlTagType.HTML,
	new Link("article", "semantics.html#the-article-element"),
	 "Self-contained syndicatable or reusable composition", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

ASIDE(
	HtmlTagType.HTML,
	new Link("aside", "semantics.html#the-aside-element"),
	 "Sidebar for tangentially related content", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

AUDIO(
	HtmlTagType.HTML,
	new Link("audio", "embedded-content.html#the-audio-element"),
	 "Audio player", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{"source", "track"}, 
	EnumSet.of(Attribute.ATTR_MEDIA_SRC, Attribute.ATTR_MEDIA_CROSSORIGIN, Attribute.ATTR_MEDIA_PRELOAD, Attribute.ATTR_MEDIA_AUTOPLAY, Attribute.ATTR_MEDIA_MEDIAGROUP, Attribute.ATTR_MEDIA_LOOP, Attribute.ATTR_MEDIA_MUTED, Attribute.ATTR_MEDIA_CONTROLS), 
	new Link("HTMLAudioElement", "embedded-content.html#htmlaudioelement")
), 

B(
	HtmlTagType.HTML,
	new Link("b", "semantics.html#the-b-element"),
	 "Keywords", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

BASE(
	HtmlTagType.HTML,
	new Link("base", "semantics.html#the-base-element"),
	 "Base URL and default target browsing context for hyperlinks and forms", 
	EnumSet.of(ContentType.METADATA), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"head", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_BASE_HREF, Attribute.ATTR_BASE_TARGET), 
	new Link("HTMLBaseElement", "semantics.html#htmlbaseelement")
), 

BDI(
	HtmlTagType.HTML,
	new Link("bdi", "semantics.html#the-bdi-element"),
	 "Text directionality isolation", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

BDO(
	HtmlTagType.HTML,
	new Link("bdo", "semantics.html#the-bdo-element"),
	 "Text directionality formatting", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

BLOCKQUOTE(
	HtmlTagType.HTML,
	new Link("blockquote", "semantics.html#the-blockquote-element"),
	 "A section quoted from another source", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_BLOCKQUOTE_CITE), 
	new Link("HTMLQuoteElement", "semantics.html#htmlquoteelement")
), 

BODY(
	HtmlTagType.HTML,
	new Link("body", "semantics.html#the-body-element"),
	 "Document body", 
	EnumSet.of(ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"html"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.HANDLER_WINDOW_ONAFTERPRINT, Attribute.HANDLER_WINDOW_ONBEFOREPRINT, Attribute.HANDLER_WINDOW_ONBEFOREUNLOAD, Attribute.HANDLER_WINDOW_ONHASHCHANGE, Attribute.HANDLER_WINDOW_ONLANGUAGECHANGE, Attribute.HANDLER_WINDOW_ONMESSAGE, Attribute.HANDLER_WINDOW_ONOFFLINE, Attribute.HANDLER_WINDOW_ONONLINE, Attribute.HANDLER_WINDOW_ONPAGEHIDE, Attribute.HANDLER_WINDOW_ONPAGESHOW, Attribute.HANDLER_WINDOW_ONPOPSTATE, Attribute.HANDLER_WINDOW_ONSTORAGE, Attribute.HANDLER_WINDOW_ONUNLOAD), 
	new Link("HTMLBodyElement", "semantics.html#htmlbodyelement")
), 

BR(
	HtmlTagType.HTML,
	new Link("br", "semantics.html#the-br-element"),
	 "Line break, e.g. in poem or postal address", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLBRElement", "semantics.html#htmlbrelement")
), 

BUTTON(
	HtmlTagType.HTML,
	new Link("button", "forms.html#the-button-element"),
	 "Button control", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FS_FORMACTION, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMMETHOD, Attribute.ATTR_FS_FORMNOVALIDATE, Attribute.ATTR_FS_FORMTARGET, Attribute.ATTR_BUTTON_MENU, Attribute.ATTR_FE_NAME, Attribute.ATTR_BUTTON_TYPE, Attribute.ATTR_BUTTON_VALUE), 
	new Link("HTMLButtonElement", "forms.html#htmlbuttonelement")
), 

CANVAS(
	HtmlTagType.HTML,
	new Link("canvas", "scripting.html#the-canvas-element"),
	 "Scriptable bitmap canvas", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_CANVAS_WIDTH, Attribute.ATTR_CANVAS_HEIGHT), 
	new Link("HTMLCanvasElement", "scripting.html#htmlcanvaselement")
), 

CAPTION(
	HtmlTagType.HTML,
	new Link("caption", "tables.html#the-caption-element"),
	 "Table caption", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"table", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTableCaptionElement", "tables.html#htmltablecaptionelement")
), 

CITE(
	HtmlTagType.HTML,
	new Link("cite", "semantics.html#the-cite-element"),
	 "Title of a work", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

CODE(
	HtmlTagType.HTML,
	new Link("code", "semantics.html#the-code-element"),
	 "Computer code", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

COL(
	HtmlTagType.HTML,
	new Link("col", "tables.html#the-col-element"),
	 "Table column", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"colgroup", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_COL_SPAN), 
	new Link("HTMLTableColElement", "tables.html#htmltablecolelement")
), 

COLGROUP(
	HtmlTagType.HTML,
	new Link("colgroup", "tables.html#the-colgroup-element"),
	 "Group of columns in a table", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"table", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"col", "template"}, 
	EnumSet.of(Attribute.ATTR_COLGROUP_SPAN), 
	new Link("HTMLTableColElement", "tables.html#htmltablecolelement")
), 

DATA(
	HtmlTagType.HTML,
	new Link("data", "semantics.html#the-data-element"),
	 "Machine-readable equivalent", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_DATA_VALUE), 
	new Link("HTMLDataElement", "semantics.html#htmldataelement")
), 

DATALIST(
	HtmlTagType.HTML,
	new Link("datalist", "forms.html#the-datalist-element"),
	 "Container for options for combo box control", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{"option"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLDataListElement", "forms.html#htmldatalistelement")
), 

DD(
	HtmlTagType.HTML,
	new Link("dd", "semantics.html#the-dd-element"),
	 "Content for corresponding dt element(s)", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"dl", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

DEL(
	HtmlTagType.HTML,
	new Link("del", "semantics.html#the-del-element"),
	 "A removal from the document", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_MOD_CITE, Attribute.ATTR_MOD_DATETIME), 
	new Link("HTMLModElement", "semantics.html#htmlmodelement")
), 

DETAILS(
	HtmlTagType.HTML,
	new Link("details", "forms.html#the-details-element"),
	 "Disclosure control for hiding details", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"summary"}, 
	EnumSet.of(Attribute.ATTR_DETAILS_OPEN), 
	new Link("HTMLDetailsElement", "forms.html#htmldetailselement")
), 

DFN(
	HtmlTagType.HTML,
	new Link("dfn", "semantics.html#the-dfn-element"),
	 "Defining instance", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

DIALOG(
	HtmlTagType.HTML,
	new Link("dialog", "forms.html#the-dialog-element"),
	 "Dialog box or window", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_DIALOG_OPEN), 
	new Link("HTMLDialogElement", "forms.html#htmldialogelement")
), 

DIV(
	HtmlTagType.HTML,
	new Link("div", "semantics.html#the-div-element"),
	 "Generic flow container", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLDivElement", "semantics.html#htmldivelement")
), 

DL(
	HtmlTagType.HTML,
	new Link("dl", "semantics.html#the-dl-element"),
	 "Association list consisting of zero or more name-value groups", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"dt", "dd"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLDListElement", "semantics.html#htmldlistelement")
), 

DT(
	HtmlTagType.HTML,
	new Link("dt", "semantics.html#the-dt-element"),
	 "Legend for corresponding dd element(s)", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"dl", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

EM(
	HtmlTagType.HTML,
	new Link("em", "semantics.html#the-em-element"),
	 "Stress emphasis", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

EMBED(
	HtmlTagType.HTML,
	new Link("embed", "embedded-content.html#the-embed-element"),
	 "Plugin", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_EMBED_SRC, Attribute.ATTR_EMBED_TYPE, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT), 
	new Link("HTMLEmbedElement", "embedded-content.html#htmlembedelement")
), 

FIELDSET(
	HtmlTagType.HTML,
	new Link("fieldset", "forms.html#the-fieldset-element"),
	 "Group of form controls", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"legend"}, 
	EnumSet.of(Attribute.ATTR_FIELDSET_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_NAME), 
	new Link("HTMLFieldSetElement", "forms.html#htmlfieldsetelement")
), 

FIGCAPTION(
	HtmlTagType.HTML,
	new Link("figcaption", "semantics.html#the-figcaption-element"),
	 "Caption for figure", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"figure", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

FIGURE(
	HtmlTagType.HTML,
	new Link("figure", "semantics.html#the-figure-element"),
	 "Figure with optional caption", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"figcaption"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

FOOTER(
	HtmlTagType.HTML,
	new Link("footer", "semantics.html#the-footer-element"),
	 "Footer for a page or section", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

FORM(
	HtmlTagType.HTML,
	new Link("form", "forms.html#the-form-element"),
	 "User-submittable form", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_FORM_ACCEPT_CHARSET, Attribute.ATTR_FS_ACTION, Attribute.ATTR_FORM_AUTOCOMPLETE, Attribute.ATTR_FS_ENCTYPE, Attribute.ATTR_FS_METHOD, Attribute.ATTR_FORM_NAME, Attribute.ATTR_FS_NOVALIDATE, Attribute.ATTR_FS_TARGET), 
	new Link("HTMLFormElement", "forms.html#htmlformelement")
), 

H1(
	HtmlTagType.HTML,
	new Link("h1", "semantics.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
	 "", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"hgroup"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadingElement", "semantics.html#htmlheadingelement")
), 

H2(
	HtmlTagType.HTML,
	new Link("h2", "semantics.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
	 "", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"hgroup"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadingElement", "semantics.html#htmlheadingelement")
), 

H3(
	HtmlTagType.HTML,
	new Link("h3", "semantics.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
	 "", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"hgroup"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadingElement", "semantics.html#htmlheadingelement")
), 

H4(
	HtmlTagType.HTML,
	new Link("h4", "semantics.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
	 "", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"hgroup"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadingElement", "semantics.html#htmlheadingelement")
), 

H5(
	HtmlTagType.HTML,
	new Link("h5", "semantics.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
	 "", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"hgroup"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadingElement", "semantics.html#htmlheadingelement")
), 

H6(
	HtmlTagType.HTML,
	new Link("h6", "semantics.html#the-h1,-h2,-h3,-h4,-h5,-and-h6-elements"),
	 "Section heading", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"hgroup"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadingElement", "semantics.html#htmlheadingelement")
), 

HEAD(
	HtmlTagType.HTML,
	new Link("head", "semantics.html#the-head-element"),
	 "Container for document metadata", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"html"}, 
	EnumSet.of(ContentType.METADATA), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHeadElement", "semantics.html#htmlheadelement")
), 

HEADER(
	HtmlTagType.HTML,
	new Link("header", "semantics.html#the-header-element"),
	 "Introductory or navigational aids for a page or section", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

HGROUP(
	HtmlTagType.HTML,
	new Link("hgroup", "semantics.html#the-hgroup-element"),
	 "heading group", 
	EnumSet.of(ContentType.FLOW, ContentType.HEADING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"h1", "h2", "h3", "h4", "h5", "h6", "template"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

HR(
	HtmlTagType.HTML,
	new Link("hr", "semantics.html#the-hr-element"),
	 "Thematic break", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLHRElement", "semantics.html#htmlhrelement")
), 

HTML(
	HtmlTagType.HTML,
	new Link("html", "semantics.html#the-html-element"),
	 "Root element", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"head", "body"}, 
	EnumSet.of(Attribute.ATTR_HTML_MANIFEST), 
	new Link("HTMLHtmlElement", "semantics.html#htmlhtmlelement")
), 

I(
	HtmlTagType.HTML,
	new Link("i", "semantics.html#the-i-element"),
	 "Alternate voice", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

IFRAME(
	HtmlTagType.HTML,
	new Link("iframe", "embedded-content.html#the-iframe-element"),
	 "Nested browsing context", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TEXT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_IFRAME_SRC, Attribute.ATTR_IFRAME_SRCDOC, Attribute.ATTR_IFRAME_NAME, Attribute.ATTR_IFRAME_SANDBOX, Attribute.ATTR_IFRAME_SEAMLESS, Attribute.ATTR_IFRAME_ALLOWFULLSCREEN, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT), 
	new Link("HTMLIFrameElement", "embedded-content.html#htmliframeelement")
), 

IMG(
	HtmlTagType.HTML,
	new Link("img", "embedded-content.html#the-img-element"),
	 "Image", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_IMG_ALT, Attribute.ATTR_IMG_SRC, Attribute.ATTR_IMG_SRCSET, Attribute.ATTR_IMG_CROSSORIGIN, Attribute.ATTR_HYPERLINK_USEMAP, Attribute.ATTR_IMG_ISMAP, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT), 
	new Link("HTMLImageElement", "embedded-content.html#htmlimageelement")
), 

INPUT(
	HtmlTagType.HTML,
	new Link("input", "forms.html#the-input-element"),
	 "Form control", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_INPUT_ACCEPT, Attribute.ATTR_INPUT_ALT, Attribute.ATTR_FE_AUTOCOMPLETE, Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_INPUT_CHECKED, Attribute.ATTR_FE_DIRNAME, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FS_FORMACTION, Attribute.ATTR_FS_FORMENCTYPE, Attribute.ATTR_FS_FORMMETHOD, Attribute.ATTR_FS_FORMNOVALIDATE, Attribute.ATTR_FS_FORMTARGET, Attribute.ATTR_DIM_HEIGHT, Attribute.ATTR_FE_INPUTMODE, Attribute.ATTR_INPUT_LIST, Attribute.ATTR_INPUT_MAX, Attribute.ATTR_INPUT_MAXLENGTH, Attribute.ATTR_INPUT_MIN, Attribute.ATTR_INPUT_MINLENGTH, Attribute.ATTR_INPUT_MULTIPLE, Attribute.ATTR_FE_NAME, Attribute.ATTR_INPUT_PATTERN, Attribute.ATTR_INPUT_PLACEHOLDER, Attribute.ATTR_INPUT_READONLY, Attribute.ATTR_INPUT_REQUIRED, Attribute.ATTR_INPUT_SIZE, Attribute.ATTR_INPUT_SRC, Attribute.ATTR_INPUT_STEP, Attribute.ATTR_INPUT_TYPE, Attribute.ATTR_INPUT_VALUE, Attribute.ATTR_DIM_WIDTH), 
	new Link("HTMLInputElement", "forms.html#htmlinputelement")
), 

INS(
	HtmlTagType.HTML,
	new Link("ins", "semantics.html#the-ins-element"),
	 "An addition to the document", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_MOD_CITE, Attribute.ATTR_MOD_DATETIME), 
	new Link("HTMLModElement", "semantics.html#htmlmodelement")
), 

KBD(
	HtmlTagType.HTML,
	new Link("kbd", "semantics.html#the-kbd-element"),
	 "User input", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

KEYGEN(
	HtmlTagType.HTML,
	new Link("keygen", "forms.html#the-keygen-element"),
	 "Cryptographic key-pair generator form control", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_KEYGEN_CHALLENGE, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_KEYGEN_KEYTYPE, Attribute.ATTR_FE_NAME), 
	new Link("HTMLKeygenElement", "forms.html#htmlkeygenelement")
), 

LABEL(
	HtmlTagType.HTML,
	new Link("label", "forms.html#the-label-element"),
	 "Caption for a form control", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_FAE_FORM, Attribute.ATTR_LABEL_FOR), 
	new Link("HTMLLabelElement", "forms.html#htmllabelelement")
), 

LEGEND(
	HtmlTagType.HTML,
	new Link("legend", "forms.html#the-legend-element"),
	 "Caption for fieldset", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"fieldset", "template"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLLegendElement", "forms.html#htmllegendelement")
), 

LI(
	HtmlTagType.HTML,
	new Link("li", "semantics.html#the-li-element"),
	 "List item", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"ol", "ul", "menu", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_LI_VALUE), 
	new Link("HTMLLIElement", "semantics.html#htmllielement")
), 

LINK(
	HtmlTagType.HTML,
	new Link("link", "semantics.html#the-link-element"),
	 "Link metadata", 
	EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{"head", "template", "noscript"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_LINK_HREF, Attribute.ATTR_LINK_CROSSORIGIN, Attribute.ATTR_LINK_REL, Attribute.ATTR_LINK_MEDIA, Attribute.ATTR_LINK_HREFLANG, Attribute.ATTR_LINK_TYPE, Attribute.ATTR_LINK_SIZES), 
	new Link("HTMLLinkElement", "semantics.html#htmllinkelement")
), 

MAIN(
	HtmlTagType.HTML,
	new Link("main", "semantics.html#the-main-element"),
	 "Container for the dominant contents of another element", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

MAP(
	HtmlTagType.HTML,
	new Link("map", "embedded-content.html#the-map-element"),
	 "Image map", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{"area"}, 
	EnumSet.of(Attribute.ATTR_MAP_NAME), 
	new Link("HTMLMapElement", "embedded-content.html#htmlmapelement")
), 

MARK(
	HtmlTagType.HTML,
	new Link("mark", "semantics.html#the-mark-element"),
	 "Highlight", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

MENU(
	HtmlTagType.HTML,
	new Link("menu", "forms.html#the-menu-element"),
	 "Menu of commands", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"menu"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"li", "menuitem", "hr", "menu"}, 
	EnumSet.of(Attribute.ATTR_MENU_TYPE, Attribute.ATTR_MENU_LABEL), 
	new Link("HTMLMenuElement", "forms.html#htmlmenuelement")
), 

MENUITEM(
	HtmlTagType.HTML,
	new Link("menuitem", "forms.html#the-menuitem-element"),
	 "Menu command", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"menu", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_MENUITEM_TYPE, Attribute.ATTR_MENUITEM_LABEL, Attribute.ATTR_MENUITEM_ICON, Attribute.ATTR_MENUITEM_DISABLED, Attribute.ATTR_MENUITEM_CHECKED, Attribute.ATTR_MENUITEM_RADIOGROUP, Attribute.ATTR_MENUITEM_DEFAULT, Attribute.ATTR_MENUITEM_COMMAND), 
	new Link("HTMLMenuItemElement", "forms.html#htmlmenuitemelement")
), 

META(
	HtmlTagType.HTML,
	new Link("meta", "semantics.html#the-meta-element"),
	 "Text metadata", 
	EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{"head", "template", "noscript"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_META_NAME, Attribute.ATTR_META_HTTP_EQUIV, Attribute.ATTR_META_CONTENT, Attribute.ATTR_META_CHARSET), 
	new Link("HTMLMetaElement", "semantics.html#htmlmetaelement")
), 

METER(
	HtmlTagType.HTML,
	new Link("meter", "forms.html#the-meter-element"),
	 "Gauge", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.of(FormAssociatedElementsCategory.LABELABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_METER_VALUE, Attribute.ATTR_METER_MIN, Attribute.ATTR_METER_MAX, Attribute.ATTR_METER_LOW, Attribute.ATTR_METER_HIGH, Attribute.ATTR_METER_OPTIMUM), 
	new Link("HTMLMeterElement", "forms.html#htmlmeterelement")
), 

NAV(
	HtmlTagType.HTML,
	new Link("nav", "semantics.html#the-nav-element"),
	 "Section with navigational links", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

NOSCRIPT(
	HtmlTagType.HTML,
	new Link("noscript", "scripting.html#the-noscript-element"),
	 "Fallback content for script", 
	EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{"head", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

OBJECT(
	HtmlTagType.HTML,
	new Link("object", "embedded-content.html#the-object-element"),
	 "Image, nested browsing context, or plugin", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.SUBMITTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{"param"}, 
	EnumSet.of(Attribute.ATTR_OBJECT_DATA, Attribute.ATTR_OBJECT_TYPE, Attribute.ATTR_OBJECT_TYPEMUSTMATCH, Attribute.ATTR_OBJECT_NAME, Attribute.ATTR_HYPERLINK_USEMAP, Attribute.ATTR_FAE_FORM, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT), 
	new Link("HTMLObjectElement", "embedded-content.html#htmlobjectelement")
), 

OL(
	HtmlTagType.HTML,
	new Link("ol", "semantics.html#the-ol-element"),
	 "Ordered list", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"li"}, 
	EnumSet.of(Attribute.ATTR_OL_REVERSED, Attribute.ATTR_OL_START, Attribute.ATTR_OL_TYPE), 
	new Link("HTMLOListElement", "semantics.html#htmlolistelement")
), 

OPTGROUP(
	HtmlTagType.HTML,
	new Link("optgroup", "forms.html#the-optgroup-element"),
	 "Group of options in a list box", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"select", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"option"}, 
	EnumSet.of(Attribute.ATTR_OPTGROUP_DISABLED, Attribute.ATTR_OPTGROUP_LABEL), 
	new Link("HTMLOptGroupElement", "forms.html#htmloptgroupelement")
), 

OPTION(
	HtmlTagType.HTML,
	new Link("option", "forms.html#the-option-element"),
	 "Option in a list box or combo box control", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"select", "datalist", "optgroup", "template"}, 
	EnumSet.of(ContentType.TEXT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_OPTION_DISABLED, Attribute.ATTR_OPTION_LABEL, Attribute.ATTR_OPTION_SELECTED, Attribute.ATTR_OPTION_VALUE), 
	new Link("HTMLOptionElement", "forms.html#htmloptionelement")
), 

OUTPUT(
	HtmlTagType.HTML,
	new Link("output", "forms.html#the-output-element"),
	 "Calculated output value", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.RESETTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_OUTPUT_FOR, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_NAME), 
	new Link("HTMLOutputElement", "forms.html#htmloutputelement")
), 

P(
	HtmlTagType.HTML,
	new Link("p", "semantics.html#the-p-element"),
	 "Paragraph", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLParagraphElement", "semantics.html#htmlparagraphelement")
), 

PARAM(
	HtmlTagType.HTML,
	new Link("param", "embedded-content.html#the-param-element"),
	 "Parameter for object", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"object", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_PARAM_NAME, Attribute.ATTR_PARAM_VALUE), 
	new Link("HTMLParamElement", "embedded-content.html#htmlparamelement")
), 

PRE(
	HtmlTagType.HTML,
	new Link("pre", "semantics.html#the-pre-element"),
	 "Block of preformatted text", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLPreElement", "semantics.html#htmlpreelement")
), 

PROGRESS(
	HtmlTagType.HTML,
	new Link("progress", "forms.html#the-progress-element"),
	 "Progress bar", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.of(FormAssociatedElementsCategory.LABELABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_PROGRESS_VALUE, Attribute.ATTR_PROGRESS_MAX), 
	new Link("HTMLProgressElement", "forms.html#htmlprogresselement")
), 

Q(
	HtmlTagType.HTML,
	new Link("q", "semantics.html#the-q-element"),
	 "Quotation", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_Q_CITE), 
	new Link("HTMLQuoteElement", "semantics.html#htmlquoteelement")
), 

RP(
	HtmlTagType.HTML,
	new Link("rp", "semantics.html#the-rp-element"),
	 "Parenthesis for ruby annotation text", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"ruby", "template"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

RT(
	HtmlTagType.HTML,
	new Link("rt", "semantics.html#the-rt-element"),
	 "Ruby annotation text", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"ruby", "template"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

RUBY(
	HtmlTagType.HTML,
	new Link("ruby", "semantics.html#the-ruby-element"),
	 "Ruby annotation(s)", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{"rt", "rp"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

S(
	HtmlTagType.HTML,
	new Link("s", "semantics.html#the-s-element"),
	 "Inaccurate text", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

SAMP(
	HtmlTagType.HTML,
	new Link("samp", "semantics.html#the-samp-element"),
	 "Computer output", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

SCRIPT(
	HtmlTagType.HTML,
	new Link("script", "scripting.html#the-script-element"),
	 "Embedded script", 
	EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{"head"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_SCRIPT_SRC, Attribute.ATTR_SCRIPT_TYPE, Attribute.ATTR_SCRIPT_CHARSET, Attribute.ATTR_SCRIPT_ASYNC, Attribute.ATTR_SCRIPT_DEFER, Attribute.ATTR_SCRIPT_CROSSORIGIN), 
	new Link("HTMLScriptElement", "scripting.html#htmlscriptelement")
), 

SECTION(
	HtmlTagType.HTML,
	new Link("section", "semantics.html#the-section-element"),
	 "Generic document or application section", 
	EnumSet.of(ContentType.FLOW, ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

SELECT(
	HtmlTagType.HTML,
	new Link("select", "forms.html#the-select-element"),
	 "List box control", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"option", "optgroup"}, 
	EnumSet.of(Attribute.ATTR_FE_AUTOCOMPLETE, Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_SELECT_MULTIPLE, Attribute.ATTR_FE_NAME, Attribute.ATTR_SELECT_REQUIRED, Attribute.ATTR_SELECT_SIZE), 
	new Link("HTMLSelectElement", "forms.html#htmlselectelement")
), 

SMALL(
	HtmlTagType.HTML,
	new Link("small", "semantics.html#the-small-element"),
	 "Side comment", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

SOURCE(
	HtmlTagType.HTML,
	new Link("source", "embedded-content.html#the-source-element"),
	 "Media source for video or audio", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"video", "audio", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_SOURCE_SRC, Attribute.ATTR_SOURCE_TYPE), 
	new Link("HTMLSourceElement", "embedded-content.html#htmlsourceelement")
), 

SPAN(
	HtmlTagType.HTML,
	new Link("span", "semantics.html#the-span-element"),
	 "Generic phrasing container", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLSpanElement", "semantics.html#htmlspanelement")
), 

STRONG(
	HtmlTagType.HTML,
	new Link("strong", "semantics.html#the-strong-element"),
	 "Importance", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

STYLE(
	HtmlTagType.HTML,
	new Link("style", "semantics.html#the-style-element"),
	 "Embedded styling information", 
	EnumSet.of(ContentType.METADATA, ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{"head", "noscript"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_STYLE_MEDIA, Attribute.ATTR_STYLE_TYPE, Attribute.ATTR_STYLE_SCOPED), 
	new Link("HTMLStyleElement", "semantics.html#htmlstyleelement")
), 

SUB(
	HtmlTagType.HTML,
	new Link("sub", "semantics.html#the-sub-and-sup-elements"),
	 "Subscript", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

SUMMARY(
	HtmlTagType.HTML,
	new Link("summary", "forms.html#the-summary-element"),
	 "Caption for details", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"details"}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

SUP(
	HtmlTagType.HTML,
	new Link("sup", "semantics.html#the-sub-and-sup-elements"),
	 "Superscript", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

TABLE(
	HtmlTagType.HTML,
	new Link("table", "tables.html#the-table-element"),
	 "Table", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"caption", "colgroup", "thead", "tbody", "tfoot", "tr"}, 
	EnumSet.of(Attribute.ATTR_TABLE_SORTABLE), 
	new Link("HTMLTableElement", "tables.html#htmltableelement")
), 

TBODY(
	HtmlTagType.HTML,
	new Link("tbody", "tables.html#the-tbody-element"),
	 "Group of rows in a table", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"table", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"tr"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTableSectionElement", "tables.html#htmltablesectionelement")
), 

TD(
	HtmlTagType.HTML,
	new Link("td", "tables.html#the-td-element"),
	 "Table cell", 
	EnumSet.of(ContentType.SECTIONING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"tr", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_TDTH_COLSPAN, Attribute.ATTR_TDTH_ROWSPAN, Attribute.ATTR_TDTH_HEADERS), 
	new Link("HTMLTableDataCellElement", "tables.html#htmltabledatacellelement")
), 

TEMPLATE(
	HtmlTagType.HTML,
	new Link("template", "scripting.html#the-template-element"),
	 "Template", 
	EnumSet.of(ContentType.METADATA, ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.METADATA, ContentType.PHRASING), 
	new String[]{"colgroup"}, 
	EnumSet.of(ContentType.TEXT), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTemplateElement", "scripting.html#htmltemplateelement")
), 

TEXTAREA(
	HtmlTagType.HTML,
	new Link("textarea", "forms.html#the-textarea-element"),
	 "Multiline text field", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.INTERACTIVE), 
	EnumSet.of(FormAssociatedElementsCategory.LISTED, FormAssociatedElementsCategory.LABELABLE, FormAssociatedElementsCategory.SUBMITTABLE, FormAssociatedElementsCategory.RESETTABLE), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TEXT), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_FE_AUTOFOCUS, Attribute.ATTR_TEXTAREA_COLS, Attribute.ATTR_FE_DIRNAME, Attribute.ATTR_FE_DISABLED, Attribute.ATTR_FAE_FORM, Attribute.ATTR_FE_INPUTMODE, Attribute.ATTR_TEXTAREA_MAXLENGTH, Attribute.ATTR_TEXTAREA_MINLENGTH, Attribute.ATTR_FE_NAME, Attribute.ATTR_TEXTAREA_PLACEHOLDER, Attribute.ATTR_TEXTAREA_READONLY, Attribute.ATTR_TEXTAREA_REQUIRED, Attribute.ATTR_TEXTAREA_ROWS, Attribute.ATTR_TEXTAREA_WRAP), 
	new Link("HTMLTextAreaElement", "forms.html#htmltextareaelement")
), 

TFOOT(
	HtmlTagType.HTML,
	new Link("tfoot", "tables.html#the-tfoot-element"),
	 "Group of footer rows in a table", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"table", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"tr"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTableSectionElement", "tables.html#htmltablesectionelement")
), 

TH(
	HtmlTagType.HTML,
	new Link("th", "tables.html#the-th-element"),
	 "Table header cell", 
	EnumSet.of(ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"tr", "template"}, 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_TDTH_COLSPAN, Attribute.ATTR_TDTH_ROWSPAN, Attribute.ATTR_TDTH_HEADERS, Attribute.ATTR_TH_SCOPE, Attribute.ATTR_TH_SORTED, Attribute.ATTR_TH_ABBR), 
	new Link("HTMLTableHeaderCellElement", "tables.html#htmltableheadercellelement")
), 

THEAD(
	HtmlTagType.HTML,
	new Link("thead", "tables.html#the-thead-element"),
	 "Group of heading rows in a table", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"table", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"tr"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTableSectionElement", "tables.html#htmltablesectionelement")
), 

TIME(
	HtmlTagType.HTML,
	new Link("time", "semantics.html#the-time-element"),
	 "Machine-readable equivalent of date- or time-related data", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_TIME_DATETIME), 
	new Link("HTMLTimeElement", "semantics.html#htmltimeelement")
), 

TITLE(
	HtmlTagType.HTML,
	new Link("title", "semantics.html#the-title-element"),
	 "Document title", 
	EnumSet.of(ContentType.METADATA), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"head", "template"}, 
	EnumSet.of(ContentType.TEXT), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTitleElement", "semantics.html#htmltitleelement")
), 

TR(
	HtmlTagType.HTML,
	new Link("tr", "tables.html#the-tr-element"),
	 "Table row", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"table", "thead", "tbody", "tfoot", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"th", "td"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLTableRowElement", "tables.html#htmltablerowelement")
), 

TRACK(
	HtmlTagType.HTML,
	new Link("track", "embedded-content.html#the-track-element"),
	 "Timed text track", 
	EnumSet.noneOf(ContentType.class), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"audio", "video", "template"}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.of(Attribute.ATTR_TRACK_DEFAULT, Attribute.ATTR_TRACK_KIND, Attribute.ATTR_TRACK_LABEL, Attribute.ATTR_TRACK_SRC, Attribute.ATTR_TRACK_SRCLANG), 
	new Link("HTMLTrackElement", "embedded-content.html#htmltrackelement")
), 

U(
	HtmlTagType.HTML,
	new Link("u", "semantics.html#the-u-element"),
	 "Keywords", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

UL(
	HtmlTagType.HTML,
	new Link("ul", "semantics.html#the-ul-element"),
	 "List", 
	EnumSet.of(ContentType.FLOW), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.FLOW), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{"li"}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLUListElement", "semantics.html#htmlulistelement")
), 

VAR(
	HtmlTagType.HTML,
	new Link("var", "semantics.html#the-var-element"),
	 "Variable", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 

VIDEO(
	HtmlTagType.HTML,
	new Link("video", "embedded-content.html#the-video-element"),
	 "Video player", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED, ContentType.INTERACTIVE), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.of(ContentType.TRANSPARENT), 
	new String[]{"source", "track"}, 
	EnumSet.of(Attribute.ATTR_MEDIA_SRC, Attribute.ATTR_MEDIA_CROSSORIGIN, Attribute.ATTR_VIDEO_POSTER, Attribute.ATTR_MEDIA_PRELOAD, Attribute.ATTR_MEDIA_AUTOPLAY, Attribute.ATTR_MEDIA_MEDIAGROUP, Attribute.ATTR_MEDIA_LOOP, Attribute.ATTR_MEDIA_MUTED, Attribute.ATTR_MEDIA_CONTROLS, Attribute.ATTR_DIM_WIDTH, Attribute.ATTR_DIM_HEIGHT), 
	new Link("HTMLVideoElement", "embedded-content.html#htmlvideoelement")
), 

WBR(
	HtmlTagType.HTML,
	new Link("wbr", "semantics.html#the-wbr-element"),
	 "Line breaking opportunity", 
	EnumSet.of(ContentType.FLOW, ContentType.PHRASING), 
	EnumSet.noneOf(FormAssociatedElementsCategory.class), 
	EnumSet.of(ContentType.PHRASING), 
	new String[]{}, 
	EnumSet.noneOf(ContentType.class), 
	new String[]{}, 
	EnumSet.noneOf(Attribute.class), 
	new Link("HTMLElement", "dom.html#htmlelement")
), 


//MATHML elements:
//-----------------------

DIVERGENCE(
        HtmlTagType.MATHML,
        new Link("divergence", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOS(
        HtmlTagType.MATHML,
        new Link("arccos", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DIFF(
        HtmlTagType.MATHML,
        new Link("diff", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CURL(
        HtmlTagType.MATHML,
        new Link("curl", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOT(
        HtmlTagType.MATHML,
        new Link("arccot", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
OTHERWISE(
        HtmlTagType.MATHML,
        new Link("otherwise", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LAPLACIAN(
        HtmlTagType.MATHML,
        new Link("laplacian", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
REALS(
        HtmlTagType.MATHML,
        new Link("reals", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANNOTATION(
        HtmlTagType.MATHML,
        new Link("annotation", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTANUMBER(
        HtmlTagType.MATHML,
        new Link("notanumber", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LAMBDA(
        HtmlTagType.MATHML,
        new Link("lambda", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARG(
        HtmlTagType.MATHML,
        new Link("arg", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NATURALNUMBERS(
        HtmlTagType.MATHML,
        new Link("naturalnumbers", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ABS(
        HtmlTagType.MATHML,
        new Link("abs", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SEMANTICS(
        HtmlTagType.MATHML,
        new Link("semantics", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTEXT(
        HtmlTagType.MATHML,
        new Link("mtext", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CODOMAIN(
        HtmlTagType.MATHML,
        new Link("codomain", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTSUBSET(
        HtmlTagType.MATHML,
        new Link("notsubset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FALSE(
        HtmlTagType.MATHML,
        new Link("false", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COMPOSE(
        HtmlTagType.MATHML,
        new Link("compose", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PIECEWISE(
        HtmlTagType.MATHML,
        new Link("piecewise", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MFRAC(
        HtmlTagType.MATHML,
        new Link("mfrac", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FN(
        HtmlTagType.MATHML,
        new Link("fn", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PIECE(
        HtmlTagType.MATHML,
        new Link("piece", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MFRACTION(
        HtmlTagType.MATHML,
        new Link("mfraction", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LIMIT(
        HtmlTagType.MATHML,
        new Link("limit", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PLUS(
        HtmlTagType.MATHML,
        new Link("plus", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MATRIXROW(
        HtmlTagType.MATHML,
        new Link("matrixrow", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SECH(
        HtmlTagType.MATHML,
        new Link("sech", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
OR(
        HtmlTagType.MATHML,
        new Link("or", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSUBSUP(
        HtmlTagType.MATHML,
        new Link("msubsup", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCSCH(
        HtmlTagType.MATHML,
        new Link("arccsch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PRSUBSET(
        HtmlTagType.MATHML,
        new Link("prsubset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MROOT(
        HtmlTagType.MATHML,
        new Link("mroot", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMAGE(
        HtmlTagType.MATHML,
        new Link("image", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCTANH(
        HtmlTagType.MATHML,
        new Link("arctanh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TENDSTO(
        HtmlTagType.MATHML,
        new Link("tendsto", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LOG(
        HtmlTagType.MATHML,
        new Link("log", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DECLARE(
        HtmlTagType.MATHML,
        new Link("declare", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOT(
        HtmlTagType.MATHML,
        new Link("not", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MEDIAN(
        HtmlTagType.MATHML,
        new Link("median", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FLOOR(
        HtmlTagType.MATHML,
        new Link("floor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LOWLIMIT(
        HtmlTagType.MATHML,
        new Link("lowlimit", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GT(
        HtmlTagType.MATHML,
        new Link("gt", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PI(
        HtmlTagType.MATHML,
        new Link("pi", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TAN(
        HtmlTagType.MATHML,
        new Link("tan", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LN(
        HtmlTagType.MATHML,
        new Link("ln", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTABLE(
        HtmlTagType.MATHML,
        new Link("mtable", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MERROR(
        HtmlTagType.MATHML,
        new Link("merror", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MACTION(
        HtmlTagType.MATHML,
        new Link("maction", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TRANSPOSE(
        HtmlTagType.MATHML,
        new Link("transpose", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PIECEWICE(
        HtmlTagType.MATHML,
        new Link("piecewice", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INTERVAL(
        HtmlTagType.MATHML,
        new Link("interval", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
VECTORPRODUCT(
        HtmlTagType.MATHML,
        new Link("vectorproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPADDED(
        HtmlTagType.MATHML,
        new Link("mpadded", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MOMENT(
        HtmlTagType.MATHML,
        new Link("moment", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DOMAINOFAPPLICATION(
        HtmlTagType.MATHML,
        new Link("domainofapplication", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMAGINARY(
        HtmlTagType.MATHML,
        new Link("imaginary", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NEQ(
        HtmlTagType.MATHML,
        new Link("neq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LT(
        HtmlTagType.MATHML,
        new Link("lt", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MODE(
        HtmlTagType.MATHML,
        new Link("mode", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DETERMINANT(
        HtmlTagType.MATHML,
        new Link("determinant", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INVERSE(
        HtmlTagType.MATHML,
        new Link("inverse", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TIMES(
        HtmlTagType.MATHML,
        new Link("times", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
UNION(
        HtmlTagType.MATHML,
        new Link("union", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ENCODING(
        HtmlTagType.MATHML,
        new Link("encoding", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DOMAIN(
        HtmlTagType.MATHML,
        new Link("domain", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MI(
        HtmlTagType.MATHML,
        new Link("mi", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MN(
        HtmlTagType.MATHML,
        new Link("mn", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MIN(
        HtmlTagType.MATHML,
        new Link("min", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
UPLIMIT(
        HtmlTagType.MATHML,
        new Link("uplimit", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MLABELEDTR(
        HtmlTagType.MATHML,
        new Link("mlabeledtr", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SETDIFF(
        HtmlTagType.MATHML,
        new Link("setdiff", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MO(
        HtmlTagType.MATHML,
        new Link("mo", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MAX(
        HtmlTagType.MATHML,
        new Link("max", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LEQ(
        HtmlTagType.MATHML,
        new Link("leq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCTAN(
        HtmlTagType.MATHML,
        new Link("arctan", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MS(
        HtmlTagType.MATHML,
        new Link("ms", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MATRIX(
        HtmlTagType.MATHML,
        new Link("matrix", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TRUE(
        HtmlTagType.MATHML,
        new Link("true", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DIVIDE(
        HtmlTagType.MATHML,
        new Link("divide", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PRODUCT(
        HtmlTagType.MATHML,
        new Link("product", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MATH(
        HtmlTagType.MATHML,
        new Link("math", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSIN(
        HtmlTagType.MATHML,
        new Link("arcsin", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DEGREE(
        HtmlTagType.MATHML,
        new Link("degree", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SIN(
        HtmlTagType.MATHML,
        new Link("sin", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MFENCED(
        HtmlTagType.MATHML,
        new Link("mfenced", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
REM(
        HtmlTagType.MATHML,
        new Link("rem", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EXISTS(
        HtmlTagType.MATHML,
        new Link("exists", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EMPTYSET(
        HtmlTagType.MATHML,
        new Link("emptyset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FACTORIAL(
        HtmlTagType.MATHML,
        new Link("factorial", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANNOTATION_XML(
        HtmlTagType.MATHML,
        new Link("annotation-xml", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMAGINARYI(
        HtmlTagType.MATHML,
        new Link("imaginaryi", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EQ(
        HtmlTagType.MATHML,
        new Link("eq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SELECTOR(
        HtmlTagType.MATHML,
        new Link("selector", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
QUOTIENT(
        HtmlTagType.MATHML,
        new Link("quotient", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSINH(
        HtmlTagType.MATHML,
        new Link("arcsinh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
APPLY(
        HtmlTagType.MATHML,
        new Link("apply", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INTEGERS(
        HtmlTagType.MATHML,
        new Link("integers", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COTH(
        HtmlTagType.MATHML,
        new Link("coth", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGN(
        HtmlTagType.MATHML,
        new Link("malign", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COMPLEXES(
        HtmlTagType.MATHML,
        new Link("complexes", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MOVER(
        HtmlTagType.MATHML,
        new Link("mover", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ROOT(
        HtmlTagType.MATHML,
        new Link("root", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTR(
        HtmlTagType.MATHML,
        new Link("mtr", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COS(
        HtmlTagType.MATHML,
        new Link("cos", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RELN(
        HtmlTagType.MATHML,
        new Link("reln", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COT(
        HtmlTagType.MATHML,
        new Link("cot", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSUB(
        HtmlTagType.MATHML,
        new Link("msub", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FUNCTION(
        HtmlTagType.MATHML,
        new Link("function", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TANH(
        HtmlTagType.MATHML,
        new Link("tanh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MUNDEROVER(
        HtmlTagType.MATHML,
        new Link("munderover", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LCM(
        HtmlTagType.MATHML,
        new Link("lcm", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSEC(
        HtmlTagType.MATHML,
        new Link("arcsec", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SDEV(
        HtmlTagType.MATHML,
        new Link("sdev", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
POWER(
        HtmlTagType.MATHML,
        new Link("power", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTIN(
        HtmlTagType.MATHML,
        new Link("notin", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NONE(
        HtmlTagType.MATHML,
        new Link("none", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CARD(
        HtmlTagType.MATHML,
        new Link("card", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MOMENTABOUT(
        HtmlTagType.MATHML,
        new Link("momentabout", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EULERGAMMA(
        HtmlTagType.MATHML,
        new Link("eulergamma", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PARTIALDIFF(
        HtmlTagType.MATHML,
        new Link("partialdiff", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GEQ(
        HtmlTagType.MATHML,
        new Link("geq", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
COSH(
        HtmlTagType.MATHML,
        new Link("cosh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MINUS(
        HtmlTagType.MATHML,
        new Link("minus", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CSCH(
        HtmlTagType.MATHML,
        new Link("csch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGNSCOPE(
        HtmlTagType.MATHML,
        new Link("malignscope", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CONDITION(
        HtmlTagType.MATHML,
        new Link("condition", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INT(
        HtmlTagType.MATHML,
        new Link("int", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MROW(
        HtmlTagType.MATHML,
        new Link("mrow", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SINH(
        HtmlTagType.MATHML,
        new Link("sinh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FORALL(
        HtmlTagType.MATHML,
        new Link("forall", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CONJUGATE(
        HtmlTagType.MATHML,
        new Link("conjugate", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCSC(
        HtmlTagType.MATHML,
        new Link("arccsc", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MMULTISCRIPTS(
        HtmlTagType.MATHML,
        new Link("mmultiscripts", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FACTOROF(
        HtmlTagType.MATHML,
        new Link("factorof", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EXPONENTIALE(
        HtmlTagType.MATHML,
        new Link("exponentiale", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
AND(
        HtmlTagType.MATHML,
        new Link("and", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MTD(
        HtmlTagType.MATHML,
        new Link("mtd", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EXP(
        HtmlTagType.MATHML,
        new Link("exp", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MGLYPH(
        HtmlTagType.MATHML,
        new Link("mglyph", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RATIONALS(
        HtmlTagType.MATHML,
        new Link("rationals", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CN(
        HtmlTagType.MATHML,
        new Link("cn", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CEILING(
        HtmlTagType.MATHML,
        new Link("ceiling", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CSYMBOL(
        HtmlTagType.MATHML,
        new Link("csymbol", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INTERSECT(
        HtmlTagType.MATHML,
        new Link("intersect", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CI(
        HtmlTagType.MATHML,
        new Link("ci", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LOGBASE(
        HtmlTagType.MATHML,
        new Link("logbase", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MUNDER(
        HtmlTagType.MATHML,
        new Link("munder", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPHANTOM(
        HtmlTagType.MATHML,
        new Link("mphantom", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSTYLE(
        HtmlTagType.MATHML,
        new Link("mstyle", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
EQUIVALENT(
        HtmlTagType.MATHML,
        new Link("equivalent", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GRAD(
        HtmlTagType.MATHML,
        new Link("grad", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
NOTPRSUBSET(
        HtmlTagType.MATHML,
        new Link("notprsubset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SET(
        HtmlTagType.MATHML,
        new Link("set", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IMPLIES(
        HtmlTagType.MATHML,
        new Link("implies", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SUM(
        HtmlTagType.MATHML,
        new Link("sum", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSQRT(
        HtmlTagType.MATHML,
        new Link("msqrt", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCSECH(
        HtmlTagType.MATHML,
        new Link("arcsech", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
INFINITY(
        HtmlTagType.MATHML,
        new Link("infinity", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SUBSET(
        HtmlTagType.MATHML,
        new Link("subset", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SCALARPRODUCT(
        HtmlTagType.MATHML,
        new Link("scalarproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
REAL(
        HtmlTagType.MATHML,
        new Link("real", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
APPROX(
        HtmlTagType.MATHML,
        new Link("approx", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IDENT(
        HtmlTagType.MATHML,
        new Link("ident", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
VECTOR(
        HtmlTagType.MATHML,
        new Link("vector", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
BVAR(
        HtmlTagType.MATHML,
        new Link("bvar", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MEAN(
        HtmlTagType.MATHML,
        new Link("mean", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
OUTERPRODUCT(
        HtmlTagType.MATHML,
        new Link("outerproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOTH(
        HtmlTagType.MATHML,
        new Link("arccoth", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
XOR(
        HtmlTagType.MATHML,
        new Link("xor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MENCLOSE(
        HtmlTagType.MATHML,
        new Link("menclose", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
VARIANCE(
        HtmlTagType.MATHML,
        new Link("variance", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LIST(
        HtmlTagType.MATHML,
        new Link("list", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGNGROUP(
        HtmlTagType.MATHML,
        new Link("maligngroup", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
IN(
        HtmlTagType.MATHML,
        new Link("in", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSPACE(
        HtmlTagType.MATHML,
        new Link("mspace", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CARTESIANPRODUCT(
        HtmlTagType.MATHML,
        new Link("cartesianproduct", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SEC(
        HtmlTagType.MATHML,
        new Link("sec", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MALIGNMARK(
        HtmlTagType.MATHML,
        new Link("malignmark", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CSC(
        HtmlTagType.MATHML,
        new Link("csc", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SEP(
        HtmlTagType.MATHML,
        new Link("sep", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MSUP(
        HtmlTagType.MATHML,
        new Link("msup", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PRIMES(
        HtmlTagType.MATHML,
        new Link("primes", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPRESCRIPTS(
        HtmlTagType.MATHML,
        new Link("mprescripts", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ARCCOSH(
        HtmlTagType.MATHML,
        new Link("arccosh", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GCD(
        HtmlTagType.MATHML,
        new Link("gcd", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),

//SVG elements:
//-----------------------

STOP(
        HtmlTagType.SVG,
        new Link("stop", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DESC(
        HtmlTagType.SVG,
        new Link("desc", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATE(
        HtmlTagType.SVG,
        new Link("animate", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT_FACE_SRC(
        HtmlTagType.SVG,
        new Link("font-face-src", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TBREAK(
        HtmlTagType.SVG,
        new Link("tbreak", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_SET(
        HtmlTagType.SVG,
        new Link("set", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
USE(
        HtmlTagType.SVG,
        new Link("use", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LINE(
        HtmlTagType.SVG,
        new Link("line", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SWITCH(
        HtmlTagType.SVG,
        new Link("switch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
CIRCLE(
        HtmlTagType.SVG,
        new Link("circle", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DISCARD(
        HtmlTagType.SVG,
        new Link("discard", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
POLYGON(
        HtmlTagType.SVG,
        new Link("polygon", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_TITLE(
        HtmlTagType.SVG,
        new Link("title", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATETRANSFORM(
        HtmlTagType.SVG,
        new Link("animateTransform", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_TEXTAREA(
        HtmlTagType.SVG,
        new Link("textArea", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
POLYLINE(
        HtmlTagType.SVG,
        new Link("polyline", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MISSING_GLYPH(
        HtmlTagType.SVG,
        new Link("missing-glyph", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PATH(
        HtmlTagType.SVG,
        new Link("path", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LISTENER(
        HtmlTagType.SVG,
        new Link("listener", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_VIDEO(
        HtmlTagType.SVG,
        new Link("video", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
METADATA(
        HtmlTagType.SVG,
        new Link("metadata", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
G(
        HtmlTagType.SVG,
        new Link("g", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TEXT(
        HtmlTagType.SVG,
        new Link("text", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT_FACE_URI(
        HtmlTagType.SVG,
        new Link("font-face-uri", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
DEFS(
        HtmlTagType.SVG,
        new Link("defs", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
PREFETCH(
        HtmlTagType.SVG,
        new Link("prefetch", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ELLIPSE(
        HtmlTagType.SVG,
        new Link("ellipse", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG(
        HtmlTagType.SVG,
        new Link("svg", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
LINEARGRADIENT(
        HtmlTagType.SVG,
        new Link("linearGradient", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_AUDIO(
        HtmlTagType.SVG,
        new Link("audio", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATION(
        HtmlTagType.SVG,
        new Link("animation", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FOREIGNOBJECT(
        HtmlTagType.SVG,
        new Link("foreignObject", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RADIALGRADIENT(
        HtmlTagType.SVG,
        new Link("radialGradient", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT(
        HtmlTagType.SVG,
        new Link("font", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
GLYPH(
        HtmlTagType.SVG,
        new Link("glyph", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_A(
        HtmlTagType.SVG,
        new Link("a", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_IMAGE(
        HtmlTagType.SVG,
        new Link("image", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SVG_SCRIPT(
        HtmlTagType.SVG,
        new Link("script", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
RECT(
        HtmlTagType.SVG,
        new Link("rect", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
HKERN(
        HtmlTagType.SVG,
        new Link("hkern", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
MPATH(
        HtmlTagType.SVG,
        new Link("mpath", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATEMOTION(
        HtmlTagType.SVG,
        new Link("animateMotion", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
TSPAN(
        HtmlTagType.SVG,
        new Link("tspan", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
ANIMATECOLOR(
        HtmlTagType.SVG,
        new Link("animateColor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
FONT_FACE(
        HtmlTagType.SVG,
        new Link("font-face", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
SOLIDCOLOR(
        HtmlTagType.SVG,
        new Link("solidColor", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
),
HANDLER(
        HtmlTagType.SVG,
        new Link("handler", null),
        null,
        EnumSet.of(ContentType.FLOW, ContentType.PHRASING, ContentType.EMBEDDED),
        EnumSet.noneOf(FormAssociatedElementsCategory.class),
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(ContentType.class),
        new String[]{},
        EnumSet.noneOf(Attribute.class),
        null
);

    //<<<<<<<<<<<<<<<<<<<<<<<<
    //end of the generated section

    public static final String BASE_URL = "http://www.whatwg.org/specs/web-apps/current-work/multipage/";//NOI18N

    private Link name;
    
    private String description;

    private Collection<ContentType> categories;
    private Collection<FormAssociatedElementsCategory> formCategories; //for form elements only, if empty, its non-form element

    //the parents of the elements may be a content or a concrete element or combinations of those
    private Collection<ContentType> parents;
    private Collection<ElementDescriptor> parentElements;
    private String[] parentElementNames;

    //the children of the elements may be a content or a concrete element or combinations of those
    private Collection<ContentType> children;
    private Collection<ElementDescriptor> childrenElements;
    private String[] childrenElementNames;

    private Collection<Attribute> attributes;

    private Link domInterface;

    private HtmlTagType type;

    private ElementDescriptor() {
    }

    private ElementDescriptor(HtmlTagType type,
            Link name,
            String description,
            Collection<ContentType> categories,
            Collection<FormAssociatedElementsCategory> formCategories,
            Collection<ContentType> parents, 
            String[] parentElements, //due to cyclic dependencies ElementDescription cannot be used
            Collection<ContentType> children,
            String[] childrenElements, //due to cyclic dependencies ElementDescription cannot be used
            Collection<Attribute> attributes,
            Link domInterface) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.formCategories = formCategories;
        this.parentElementNames = parentElements;
        this.parents = parents;
        this.childrenElementNames = childrenElements;
        this.children = children;
        this.attributes = attributes;
        this.domInterface = domInterface;
    }

    public static ElementDescriptor forName(String name) {
        try {
            return valueOf(elementName2EnumName(name));
        } catch (IllegalArgumentException iae) {
            //no such enum member
            return null;
        }
    }

    public static ElementDescriptor forElementName(ElementName elementName) {
        return forName(elementName.getName());
    }

    public Collection<Attribute> getAttributes() {
        Collection<Attribute> withGlobal = new LinkedList<Attribute>(attributes);
        withGlobal.addAll(Attribute.GLOBAL_ATTRIBUTES);
        withGlobal.addAll(Attribute.EVENT_ATTRIBUTES);
        return withGlobal;
    }

    public HtmlTagType getTagType() {
        return type;
    }

    public Collection<ContentType> getCategoryTypes() {
        return categories;
    }

    public Collection<ContentType> getChildrenTypes() {
        return children;
    }

    public synchronized Collection<ElementDescriptor> getChildrenElements() {
        //lazy init
        if(childrenElements == null) {
            childrenElements = new ArrayList<ElementDescriptor>();
            for(String elementName : childrenElementNames) {
                childrenElements.add(forName(elementName));
            }
        }
        return childrenElements;
    }

    public String getDescription() {
        return description;
    }

    public Link getDomInterface() {
        return domInterface;
    }

    public Collection<FormAssociatedElementsCategory> getFormCategories() {
        return formCategories;
    }

    public String getName() {
        return name.getName();
    }

    public URL getHelpUrl() {
        return name.getUrl(BASE_URL);
    }
    
    public String getHelpLink() {
        return name.getLink();
    }

    public synchronized Collection<ElementDescriptor> getParentElements() {
        //lazy init
        if(parentElements == null) {
            parentElements = new ArrayList<ElementDescriptor>();
            for(String elementName : parentElementNames) {
                parentElements.add(forName(elementName));
            }
        }
        return parentElements;
    }

    public Collection<ContentType> getParentTypes() {
        return parents;
    }

    public boolean hasOptionalOpenTag() {
        return ElementDescriptorRules.OPTIONAL_OPEN_TAGS.contains(this);
    }

    public boolean hasOptionalEndTag() {
        return ElementDescriptorRules.OPTIONAL_END_TAGS.contains(this) || isEmpty();
    }

    public boolean isEmpty() {
        return children.isEmpty() && getChildrenElements().isEmpty(); //empty content model
    }

    public static String elementName2EnumName(String elementName) {
        return elementName.replace('-', '_').toUpperCase();
    }

    public static synchronized Collection<String> getAttrNamesForElement(String elementName) {
        ElementDescriptor descriptor = ElementDescriptor.forName(elementName);
        if(descriptor == null) {
            return Collections.emptyList();
        }
        Collection<String> attrNames = new LinkedList<String>();
        for(Attribute a : descriptor.getAttributes()) {
            attrNames.add(a.getName());
        }

        return attrNames;
    }

}
