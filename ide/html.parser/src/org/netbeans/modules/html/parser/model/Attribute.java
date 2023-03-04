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
package org.netbeans.modules.html.parser.model;

import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author marekfukala
 */
public enum Attribute {

    //GENERATED SECTION>>>
    //global attributes //NOI18N
    ACCESSKEY(new Link("accesskey", "interaction.html#the-accesskey-attribute")),
    CLASS(new Link("class", "dom.html#classes")),
    CONTENTEDITABLE(new Link("contenteditable", "interaction.html#attr-contenteditable")),
    CONTEXTMENU(new Link("contextmenu", "forms.html#attr-contextmenu")),
    DIR(new Link("dir", "dom.html#the-dir-attribute")),
    DRAGGABLE(new Link("draggable", "interaction.html#the-draggable-attribute")),
    DROPZONE(new Link("dropzone", "interaction.html#the-dropzone-attribute")),
    HIDDEN(new Link("hidden", "interaction.html#the-hidden-attribute")),
    ID(new Link("id", "dom.html#the-id-attribute")),
    ITEMID(new Link("itemid", "microdata.html#attr-itemid")),
    ITEMPROP(new Link("itemprop", "microdata.html#names:-the-itemprop-attribute")),
    ITEMREF(new Link("itemref", "microdata.html#attr-itemref")),
    ITEMSCOPE(new Link("itemscope", "microdata.html#attr-itemscope")),
    ITEMTYPE(new Link("itemtype", "microdata.html#attr-itemtype")),
    LANG(new Link("lang", "dom.html#attr-lang")),
    SPELLCHECK(new Link("spellcheck", "interaction.html#attr-spellcheck")),
    STYLE(new Link("style", "dom.html#the-style-attribute")),
    TABINDEX(new Link("tabindex", "interaction.html#attr-tabindex")),
    TITLE(new Link("title", "dom.html#attr-title")),
    TRANSLATE(new Link("translate", "dom.html#attr-translate")),
    //event attributes
    ONABORT(new Link("onabort", "webappapis.html#handler-onabort")),
    ONAUTOCOMPLETE(new Link("onautocomplete", "webappapis.html#handler-onautocomplete")),
    ONAUTOCOMPLETEERROR(new Link("onautocompleteerror", "webappapis.html#handler-onautocompleteerror")),
    ONAFTERPRINT(new Link("onafterprint", "webappapis.html#handler-window-onafterprint")),
    ONBEFOREPRINT(new Link("onbeforeprint", "webappapis.html#handler-window-onbeforeprint")),
    ONBEFOREUNLOAD(new Link("onbeforeunload", "webappapis.html#handler-window-onbeforeunload")),
    ONBLUR(new Link("onblur", "webappapis.html#handler-onblur")),
    ONCANCEL(new Link("oncancel", "webappapis.html#handler-oncancel")),
    ONCANPLAY(new Link("oncanplay", "webappapis.html#handler-oncanplay")),
    ONCANPLAYTHROUGH(new Link("oncanplaythrough", "webappapis.html#handler-oncanplaythrough")),
    ONCHANGE(new Link("onchange", "webappapis.html#handler-onchange")),
    ONCLICK(new Link("onclick", "webappapis.html#handler-onclick")),
    ONCLOSE(new Link("onclose", "webappapis.html#handler-onclose")),
    ONCONTEXTMENU(new Link("oncontextmenu", "webappapis.html#handler-oncontextmenu")),
    ONCUECHANGE(new Link("oncuechange", "webappapis.html#handler-oncuechange")),
    ONDBLCLICK(new Link("ondblclick", "webappapis.html#handler-ondblclick")),
    ONDRAG(new Link("ondrag", "webappapis.html#handler-ondrag")),
    ONDRAGEND(new Link("ondragend", "webappapis.html#handler-ondragend")),
    ONDRAGENTER(new Link("ondragenter", "webappapis.html#handler-ondragenter")),
    ONDRAGEXIT(new Link("ondragexit", "webappapis.html#handler-ondragexit")),
    ONDRAGLEAVE(new Link("ondragleave", "webappapis.html#handler-ondragleave")),
    ONDRAGOVER(new Link("ondragover", "webappapis.html#handler-ondragover")),
    ONDRAGSTART(new Link("ondragstart", "webappapis.html#handler-ondragstart")),
    ONDROP(new Link("ondrop", "webappapis.html#handler-ondrop")),
    ONDURATIONCHANGE(new Link("ondurationchange", "webappapis.html#handler-ondurationchange")),
    ONEMPTIED(new Link("onemptied", "webappapis.html#handler-onemptied")),
    ONENDED(new Link("onended", "webappapis.html#handler-onended")),
    ONERROR(new Link("onerror", "webappapis.html#handler-onerror")),
    ONFOCUS(new Link("onfocus", "webappapis.html#handler-onfocus")),
    ONHASHCHANGE(new Link("onhashchange", "webappapis.html#handler-window-onhashchange")),
    ONINPUT(new Link("oninput", "webappapis.html#handler-oninput")),
    ONINVALID(new Link("oninvalid", "webappapis.html#handler-oninvalid")),
    ONKEYDOWN(new Link("onkeydown", "webappapis.html#handler-onkeydown")),
    ONKEYPRESS(new Link("onkeypress", "webappapis.html#handler-onkeypress")),
    ONKEYUP(new Link("onkeyup", "webappapis.html#handler-onkeyup")),
    ONLANGUAGECHANGE(new Link("onlanguagechange", "webappapis.html#handler-window-onlanguagechange")),
    ONLOAD(new Link("onload", "webappapis.html#handler-onload")),
    ONLOADEDDATA(new Link("onloadeddata", "webappapis.html#handler-onloadeddata")),
    ONLOADEDMETADATA(new Link("onloadedmetadata", "webappapis.html#handler-onloadedmetadata")),
    ONMESSAGE(new Link("onmessage", "webappapis.html#handler-window-onmessage")),
    ONLOADSTART(new Link("onloadstart", "webappapis.html#handler-onloadstart")),
    ONMOUSEDOWN(new Link("onmousedown", "webappapis.html#handler-onmousedown")),
    ONMOUSEENTER(new Link("onmouseenter", "webappapis.html#handler-onmouseenter")),
    ONMOUSEMOVE(new Link("onmousemove", "webappapis.html#handler-onmousemove")),
    ONMOUSEOUT(new Link("onmouseout", "webappapis.html#handler-onmouseout")),
    ONMOUSEOVER(new Link("onmouseover", "webappapis.html#handler-onmouseover")),
    ONMOUSEUP(new Link("onmouseup", "webappapis.html#handler-onmouseup")),
    ONMOUSEWHEEL(new Link("onmousewheel", "webappapis.html#handler-onmousewheel")),
    ONOFFLINE(new Link("onoffline", "webappapis.html#handler-window-onoffline")),
    ONONLINE(new Link("ononline", "webappapis.html#handler-window-ononline")),
    ONPAGEHIDE(new Link("onpagehide", "webappapis.html#handler-window-onpagehide")),
    ONPAGESHOW(new Link("onpageshow", "webappapis.html#handler-window-onpageshow")),
    ONPAUSE(new Link("onpause", "webappapis.html#handler-onpause")),
    ONPLAY(new Link("onplay", "webappapis.html#handler-onplay")),
    ONPLAYING(new Link("onplaying", "webappapis.html#handler-onplaying")),
    ONPOPSTATE(new Link("onpopstate", "webappapis.html#handler-window-onpopstate")),
    ONPROGRESS(new Link("onprogress", "webappapis.html#handler-onprogress")),
    ONRATECHANGE(new Link("onratechange", "webappapis.html#handler-onratechange")),
    ONRESET(new Link("onreset", "webappapis.html#handler-onreset")),
    ONRESIZE(new Link("onresize", "webappapis.html#handler-onresize")),
    ONSCROLL(new Link("onscroll", "webappapis.html#handler-onscroll")),
    ONSEEKED(new Link("onseeked", "webappapis.html#handler-onseeked")),
    ONSEEKING(new Link("onseeking", "webappapis.html#handler-onseeking")),
    ONSELECT(new Link("onselect", "webappapis.html#handler-onselect")),
    ONSHOW(new Link("onshow", "webappapis.html#handler-onshow")),
    ONSTALLED(new Link("onstalled", "webappapis.html#handler-onstalled")),
    ONSTORAGE(new Link("onstorage", "webappapis.html#handler-window-onstorage")),
    ONSUBMIT(new Link("onsubmit", "webappapis.html#handler-onsubmit")),
    ONSUSPEND(new Link("onsuspend", "webappapis.html#handler-onsuspend")),
    ONTIMEUPDATE(new Link("ontimeupdate", "webappapis.html#handler-ontimeupdate")),
    ONTOGGLE(new Link("ontoggle", "webappapis.html#handler-ontoggle")),
    ONUNLOAD(new Link("onunload", "webappapis.html#handler-window-onunload")),
    ONVOLUMECHANGE(new Link("onvolumechange", "webappapis.html#handler-onvolumechange")),
    ONWAITING(new Link("onwaiting", "webappapis.html#handler-onwaiting")),
    //properietary attributes
    ATTR_STYLE_TYPE(new Link("attr-style-type", "semantics.html#attr-style-type")),
    ATTR_MENUITEM_DEFAULT(new Link("attr-menuitem-default", "forms.html#attr-menuitem-default")),
    ATTR_OL_TYPE(new Link("attr-ol-type", "semantics.html#attr-ol-type")),
    ATTR_STYLE_MEDIA(new Link("attr-style-media", "semantics.html#attr-style-media")),
    ATTR_INPUT_PLACEHOLDER(new Link("attr-input-placeholder", "forms.html#attr-input-placeholder")),
    ATTR_BUTTON_TYPE(new Link("attr-button-type", "forms.html#attr-button-type")),
    ATTR_IMG_ISMAP(new Link("attr-img-ismap", "embedded-content.html#attr-img-ismap")),
    ATTR_EMBED_TYPE(new Link("attr-embed-type", "embedded-content.html#attr-embed-type")),
    ATTR_AREA_COORDS(new Link("attr-area-coords", "embedded-content.html#attr-area-coords")),
    ATTR_IMG_SRCSET(new Link("attr-img-srcset", "embedded-content.html#attr-img-srcset")),
    ATTR_FORM_AUTOCOMPLETE(new Link("attr-form-autocomplete", "forms.html#attr-form-autocomplete")),
    ATTR_INPUT_CHECKED(new Link("attr-input-checked", "forms.html#attr-input-checked")),
    ATTR_FS_FORMENCTYPE(new Link("attr-fs-formenctype", "forms.html#attr-fs-formenctype")),
    ATTR_TABLE_SORTABLE(new Link("attr-table-sortable", "tables.html#attr-table-sortable")),
    ATTR_HYPERLINK_TYPE(new Link("attr-hyperlink-type", "semantics.html#attr-hyperlink-type")),
    ATTR_META_CONTENT(new Link("attr-meta-content", "semantics.html#attr-meta-content")),
    ATTR_IMG_ALT(new Link("attr-img-alt", "embedded-content.html#attr-img-alt")),
    ATTR_TEXTAREA_PLACEHOLDER(new Link("attr-textarea-placeholder", "forms.html#attr-textarea-placeholder")),
    ATTR_MEDIA_MUTED(new Link("attr-media-muted", "embedded-content.html#attr-media-muted")),
    ATTR_META_HTTP_EQUIV(new Link("attr-meta-http-equiv", "semantics.html#attr-meta-http-equiv")),
    ATTR_KEYGEN_CHALLENGE(new Link("attr-keygen-challenge", "forms.html#attr-keygen-challenge")),
    ATTR_INPUT_MAX(new Link("attr-input-max", "forms.html#attr-input-max")),
    ATTR_SELECT_REQUIRED(new Link("attr-select-required", "forms.html#attr-select-required")),
    ATTR_INPUT_SRC(new Link("attr-input-src", "forms.html#attr-input-src")),
    ATTR_LINK_SIZES(new Link("attr-link-sizes", "semantics.html#attr-link-sizes")),
    ATTR_INPUT_REQUIRED(new Link("attr-input-required", "forms.html#attr-input-required")),
    ATTR_IFRAME_SRC(new Link("attr-iframe-src", "embedded-content.html#attr-iframe-src")),
    ATTR_OBJECT_TYPE(new Link("attr-object-type", "embedded-content.html#attr-object-type")),
    ATTR_CANVAS_WIDTH(new Link("attr-canvas-width", "scripting.html#attr-canvas-width")),
    ATTR_INPUT_MULTIPLE(new Link("attr-input-multiple", "forms.html#attr-input-multiple")),
    ATTR_OPTION_LABEL(new Link("attr-option-label", "forms.html#attr-option-label")),
    ATTR_MENU_LABEL(new Link("attr-menu-label", "forms.html#attr-menu-label")),
    ATTR_OBJECT_DATA(new Link("attr-object-data", "embedded-content.html#attr-object-data")),
    ATTR_DIM_WIDTH(new Link("attr-dim-width", "embedded-content.html#attr-dim-width")),
    ATTR_LI_VALUE(new Link("attr-li-value", "semantics.html#attr-li-value")),
    ATTR_MENUITEM_DISABLED(new Link("attr-menuitem-disabled", "forms.html#attr-menuitem-disabled")),
    ATTR_PROGRESS_MAX(new Link("attr-progress-max", "forms.html#attr-progress-max")),
    ATTR_SCRIPT_SRC(new Link("attr-script-src", "scripting.html#attr-script-src")),
    HANDLER_WINDOW_ONSTORAGE(new Link("handler-window-onstorage", "webappapis.html#handler-window-onstorage")),
    ATTR_MAP_NAME(new Link("attr-map-name", "embedded-content.html#attr-map-name")),
    ATTR_OL_REVERSED(new Link("attr-ol-reversed", "semantics.html#attr-ol-reversed")),
    ATTR_MOD_DATETIME(new Link("attr-mod-datetime", "semantics.html#attr-mod-datetime")),
    ATTR_HYPERLINK_HREF(new Link("attr-hyperlink-href", "semantics.html#attr-hyperlink-href")),
    ATTR_INPUT_MINLENGTH(new Link("attr-input-minlength", "forms.html#attr-input-minlength")),
    ATTR_METER_MAX(new Link("attr-meter-max", "forms.html#attr-meter-max")),
    ATTR_LINK_HREFLANG(new Link("attr-link-hreflang", "semantics.html#attr-link-hreflang")),
    ATTR_SOURCE_TYPE(new Link("attr-source-type", "embedded-content.html#attr-source-type")),
    ATTR_FAE_FORM(new Link("attr-fae-form", "forms.html#attr-fae-form")),
    ATTR_HYPERLINK_DOWNLOAD(new Link("attr-hyperlink-download", "semantics.html#attr-hyperlink-download")),
    ATTR_INPUT_MAXLENGTH(new Link("attr-input-maxlength", "forms.html#attr-input-maxlength")),
    ATTR_FS_ENCTYPE(new Link("attr-fs-enctype", "forms.html#attr-fs-enctype")),
    ATTR_INPUT_TYPE(new Link("attr-input-type", "forms.html#attr-input-type")),
    ATTR_METER_VALUE(new Link("attr-meter-value", "forms.html#attr-meter-value")),
    ATTR_MEDIA_PRELOAD(new Link("attr-media-preload", "embedded-content.html#attr-media-preload")),
    ATTR_FE_NAME(new Link("attr-fe-name", "forms.html#attr-fe-name")),
    ATTR_SOURCE_SRC(new Link("attr-source-src", "embedded-content.html#attr-source-src")),
    ATTR_SCRIPT_ASYNC(new Link("attr-script-async", "scripting.html#attr-script-async")),
    ATTR_FE_DIRNAME(new Link("attr-fe-dirname", "forms.html#attr-fe-dirname")),
    ATTR_VIDEO_POSTER(new Link("attr-video-poster", "embedded-content.html#attr-video-poster")),
    ATTR_MEDIA_LOOP(new Link("attr-media-loop", "embedded-content.html#attr-media-loop")),
    HANDLER_WINDOW_ONPOPSTATE(new Link("handler-window-onpopstate", "webappapis.html#handler-window-onpopstate")),
    ATTR_FS_FORMMETHOD(new Link("attr-fs-formmethod", "forms.html#attr-fs-formmethod")),
    HANDLER_WINDOW_ONUNLOAD(new Link("handler-window-onunload", "webappapis.html#handler-window-onunload")),
    ATTR_MENUITEM_TYPE(new Link("attr-menuitem-type", "forms.html#attr-menuitem-type")),
    ATTR_AREA_ALT(new Link("attr-area-alt", "embedded-content.html#attr-area-alt")),
    ATTR_TRACK_DEFAULT(new Link("attr-track-default", "embedded-content.html#attr-track-default")),
    ATTR_DATA_VALUE(new Link("attr-data-value", "semantics.html#attr-data-value")),
    ATTR_INPUT_STEP(new Link("attr-input-step", "forms.html#attr-input-step")),
    ATTR_FS_NOVALIDATE(new Link("attr-fs-novalidate", "forms.html#attr-fs-novalidate")),
    ATTR_INPUT_VALUE(new Link("attr-input-value", "forms.html#attr-input-value")),
    ATTR_META_CHARSET(new Link("attr-meta-charset", "semantics.html#attr-meta-charset")),
    ATTR_METER_OPTIMUM(new Link("attr-meter-optimum", "forms.html#attr-meter-optimum")),
    ATTR_HYPERLINK_REL(new Link("attr-hyperlink-rel", "semantics.html#attr-hyperlink-rel")),
    ATTR_MOD_CITE(new Link("attr-mod-cite", "semantics.html#attr-mod-cite")),
    ATTR_INPUT_SIZE(new Link("attr-input-size", "forms.html#attr-input-size")),
    ATTR_OL_START(new Link("attr-ol-start", "semantics.html#attr-ol-start")),
    ATTR_SCRIPT_CHARSET(new Link("attr-script-charset", "scripting.html#attr-script-charset")),
    ATTR_MEDIA_CONTROLS(new Link("attr-media-controls", "embedded-content.html#attr-media-controls")),
    ATTR_FORM_NAME(new Link("attr-form-name", "forms.html#attr-form-name")),
    ATTR_MENUITEM_ICON(new Link("attr-menuitem-icon", "forms.html#attr-menuitem-icon")),
    HANDLER_WINDOW_ONBEFOREUNLOAD(new Link("handler-window-onbeforeunload", "webappapis.html#handler-window-onbeforeunload")),
    HANDLER_WINDOW_ONOFFLINE(new Link("handler-window-onoffline", "webappapis.html#handler-window-onoffline")),
    ATTR_FE_AUTOFOCUS(new Link("attr-fe-autofocus", "forms.html#attr-fe-autofocus")),
    PING(new Link("ping", "semantics.html#ping")),
    ATTR_TDTH_HEADERS(new Link("attr-tdth-headers", "tables.html#attr-tdth-headers")),
    HANDLER_WINDOW_ONONLINE(new Link("handler-window-ononline", "webappapis.html#handler-window-ononline")),
    ATTR_BUTTON_VALUE(new Link("attr-button-value", "forms.html#attr-button-value")),
    ATTR_IFRAME_SRCDOC(new Link("attr-iframe-srcdoc", "embedded-content.html#attr-iframe-srcdoc")),
    ATTR_INPUT_MIN(new Link("attr-input-min", "forms.html#attr-input-min")),
    ATTR_PARAM_NAME(new Link("attr-param-name", "embedded-content.html#attr-param-name")),
    ATTR_BASE_HREF(new Link("attr-base-href", "semantics.html#attr-base-href")),
    ATTR_TIME_DATETIME(new Link("attr-time-datetime", "semantics.html#attr-time-datetime")),
    ATTR_OBJECT_TYPEMUSTMATCH(new Link("attr-object-typemustmatch", "embedded-content.html#attr-object-typemustmatch")),
    HANDLER_WINDOW_ONAFTERPRINT(new Link("handler-window-onafterprint", "webappapis.html#handler-window-onafterprint")),
    ATTR_FS_FORMNOVALIDATE(new Link("attr-fs-formnovalidate", "forms.html#attr-fs-formnovalidate")),
    ATTR_OUTPUT_FOR(new Link("attr-output-for", "forms.html#attr-output-for")),
    ATTR_MEDIA_CROSSORIGIN(new Link("attr-media-crossorigin", "embedded-content.html#attr-media-crossorigin")),
    ATTR_FS_METHOD(new Link("attr-fs-method", "forms.html#attr-fs-method")),
    ATTR_INPUT_ALT(new Link("attr-input-alt", "forms.html#attr-input-alt")),
    HANDLER_WINDOW_ONPAGEHIDE(new Link("handler-window-onpagehide", "webappapis.html#handler-window-onpagehide")),
    ATTR_BUTTON_MENU(new Link("attr-button-menu", "forms.html#attr-button-menu")),
    ATTR_TEXTAREA_READONLY(new Link("attr-textarea-readonly", "forms.html#attr-textarea-readonly")),
    ATTR_MENUITEM_CHECKED(new Link("attr-menuitem-checked", "forms.html#attr-menuitem-checked")),
    ATTR_OPTION_VALUE(new Link("attr-option-value", "forms.html#attr-option-value")),
    ATTR_STYLE_SCOPED(new Link("attr-style-scoped", "semantics.html#attr-style-scoped")),
    ATTR_MENU_TYPE(new Link("attr-menu-type", "forms.html#attr-menu-type")),
    ATTR_CANVAS_HEIGHT(new Link("attr-canvas-height", "scripting.html#attr-canvas-height")),
    ATTR_METER_LOW(new Link("attr-meter-low", "forms.html#attr-meter-low")),
    ATTR_FE_DISABLED(new Link("attr-fe-disabled", "forms.html#attr-fe-disabled")),
    ATTR_MEDIA_MEDIAGROUP(new Link("attr-media-mediagroup", "embedded-content.html#attr-media-mediagroup")),
    ATTR_IFRAME_NAME(new Link("attr-iframe-name", "embedded-content.html#attr-iframe-name")),
    ATTR_MEDIA_SRC(new Link("attr-media-src", "embedded-content.html#attr-media-src")),
    ATTR_DIALOG_OPEN(new Link("attr-dialog-open", "forms.html#attr-dialog-open")),
    ATTR_METER_HIGH(new Link("attr-meter-high", "forms.html#attr-meter-high")),
    ATTR_TDTH_COLSPAN(new Link("attr-tdth-colspan", "tables.html#attr-tdth-colspan")),
    ATTR_IMG_SRC(new Link("attr-img-src", "embedded-content.html#attr-img-src")),
    ATTR_TRACK_KIND(new Link("attr-track-kind", "embedded-content.html#attr-track-kind")),
    ATTR_FE_AUTOCOMPLETE(new Link("attr-fe-autocomplete", "forms.html#attr-fe-autocomplete")),
    ATTR_TEXTAREA_ROWS(new Link("attr-textarea-rows", "forms.html#attr-textarea-rows")),
    ATTR_TRACK_LABEL(new Link("attr-track-label", "embedded-content.html#attr-track-label")),
    ATTR_OBJECT_NAME(new Link("attr-object-name", "embedded-content.html#attr-object-name")),
    ATTR_SELECT_SIZE(new Link("attr-select-size", "forms.html#attr-select-size")),
    ATTR_INPUT_READONLY(new Link("attr-input-readonly", "forms.html#attr-input-readonly")),
    ATTR_TEXTAREA_MINLENGTH(new Link("attr-textarea-minlength", "forms.html#attr-textarea-minlength")),
    HANDLER_WINDOW_ONHASHCHANGE(new Link("handler-window-onhashchange", "webappapis.html#handler-window-onhashchange")),
    ATTR_LINK_TYPE(new Link("attr-link-type", "semantics.html#attr-link-type")),
    ATTR_SCRIPT_CROSSORIGIN(new Link("attr-script-crossorigin", "scripting.html#attr-script-crossorigin")),
    ATTR_INPUT_ACCEPT(new Link("attr-input-accept", "forms.html#attr-input-accept")),
    ATTR_TRACK_SRCLANG(new Link("attr-track-srclang", "embedded-content.html#attr-track-srclang")),
    ATTR_Q_CITE(new Link("attr-q-cite", "semantics.html#attr-q-cite")),
    ATTR_FORM_ACCEPT_CHARSET(new Link("attr-form-accept-charset", "forms.html#attr-form-accept-charset")),
    ATTR_MEDIA_AUTOPLAY(new Link("attr-media-autoplay", "embedded-content.html#attr-media-autoplay")),
    ATTR_OPTION_SELECTED(new Link("attr-option-selected", "forms.html#attr-option-selected")),
    ATTR_COL_SPAN(new Link("attr-col-span", "tables.html#attr-col-span")),
    ATTR_FE_INPUTMODE(new Link("attr-fe-inputmode", "forms.html#attr-fe-inputmode")),
    ATTR_LINK_CROSSORIGIN(new Link("attr-link-crossorigin", "semantics.html#attr-link-crossorigin")),
    ATTR_SCRIPT_DEFER(new Link("attr-script-defer", "scripting.html#attr-script-defer")),
    ATTR_AREA_SHAPE(new Link("attr-area-shape", "embedded-content.html#attr-area-shape")),
    ATTR_TEXTAREA_COLS(new Link("attr-textarea-cols", "forms.html#attr-textarea-cols")),
    ATTR_TDTH_ROWSPAN(new Link("attr-tdth-rowspan", "tables.html#attr-tdth-rowspan")),
    ATTR_FS_TARGET(new Link("attr-fs-target", "forms.html#attr-fs-target")),
    ATTR_FS_FORMTARGET(new Link("attr-fs-formtarget", "forms.html#attr-fs-formtarget")),
    ATTR_HTML_MANIFEST(new Link("attr-html-manifest", "semantics.html#attr-html-manifest")),
    HANDLER_WINDOW_ONLANGUAGECHANGE(new Link("handler-window-onlanguagechange", "webappapis.html#handler-window-onlanguagechange")),
    ATTR_TH_ABBR(new Link("attr-th-abbr", "tables.html#attr-th-abbr")),
    ATTR_KEYGEN_KEYTYPE(new Link("attr-keygen-keytype", "forms.html#attr-keygen-keytype")),
    ATTR_DIM_HEIGHT(new Link("attr-dim-height", "embedded-content.html#attr-dim-height")),
    ATTR_LINK_REL(new Link("attr-link-rel", "semantics.html#attr-link-rel")),
    ATTR_TH_SCOPE(new Link("attr-th-scope", "tables.html#attr-th-scope")),
    ATTR_LINK_MEDIA(new Link("attr-link-media", "semantics.html#attr-link-media")),
    ATTR_MENUITEM_RADIOGROUP(new Link("attr-menuitem-radiogroup", "forms.html#attr-menuitem-radiogroup")),
    ATTR_LABEL_FOR(new Link("attr-label-for", "forms.html#attr-label-for")),
    ATTR_TEXTAREA_MAXLENGTH(new Link("attr-textarea-maxlength", "forms.html#attr-textarea-maxlength")),
    ATTR_IFRAME_SANDBOX(new Link("attr-iframe-sandbox", "embedded-content.html#attr-iframe-sandbox")),
    ATTR_BLOCKQUOTE_CITE(new Link("attr-blockquote-cite", "semantics.html#attr-blockquote-cite")),
    ATTR_FS_FORMACTION(new Link("attr-fs-formaction", "forms.html#attr-fs-formaction")),
    HANDLER_WINDOW_ONMESSAGE(new Link("handler-window-onmessage", "webappapis.html#handler-window-onmessage")),
    ATTR_OPTGROUP_DISABLED(new Link("attr-optgroup-disabled", "forms.html#attr-optgroup-disabled")),
    ATTR_MENUITEM_COMMAND(new Link("attr-menuitem-command", "forms.html#attr-menuitem-command")),
    ATTR_IFRAME_SEAMLESS(new Link("attr-iframe-seamless", "embedded-content.html#attr-iframe-seamless")),
    ATTR_COLGROUP_SPAN(new Link("attr-colgroup-span", "tables.html#attr-colgroup-span")),
    ATTR_FIELDSET_DISABLED(new Link("attr-fieldset-disabled", "forms.html#attr-fieldset-disabled")),
    ATTR_FS_ACTION(new Link("attr-fs-action", "forms.html#attr-fs-action")),
    ATTR_SELECT_MULTIPLE(new Link("attr-select-multiple", "forms.html#attr-select-multiple")),
    HANDLER_WINDOW_ONPAGESHOW(new Link("handler-window-onpageshow", "webappapis.html#handler-window-onpageshow")),
    ATTR_MENUITEM_LABEL(new Link("attr-menuitem-label", "forms.html#attr-menuitem-label")),
    ATTR_OPTGROUP_LABEL(new Link("attr-optgroup-label", "forms.html#attr-optgroup-label")),
    ATTR_OPTION_DISABLED(new Link("attr-option-disabled", "forms.html#attr-option-disabled")),
    ATTR_IMG_CROSSORIGIN(new Link("attr-img-crossorigin", "embedded-content.html#attr-img-crossorigin")),
    ATTR_META_NAME(new Link("attr-meta-name", "semantics.html#attr-meta-name")),
    ATTR_BASE_TARGET(new Link("attr-base-target", "semantics.html#attr-base-target")),
    ATTR_HYPERLINK_HREFLANG(new Link("attr-hyperlink-hreflang", "semantics.html#attr-hyperlink-hreflang")),
    ATTR_DETAILS_OPEN(new Link("attr-details-open", "forms.html#attr-details-open")),
    ATTR_IFRAME_ALLOWFULLSCREEN(new Link("attr-iframe-allowfullscreen", "embedded-content.html#attr-iframe-allowfullscreen")),
    ATTR_TH_SORTED(new Link("attr-th-sorted", "tables.html#attr-th-sorted")),
    HANDLER_WINDOW_ONBEFOREPRINT(new Link("handler-window-onbeforeprint", "webappapis.html#handler-window-onbeforeprint")),
    ATTR_PROGRESS_VALUE(new Link("attr-progress-value", "forms.html#attr-progress-value")),
    ATTR_SCRIPT_TYPE(new Link("attr-script-type", "scripting.html#attr-script-type")),
    ATTR_METER_MIN(new Link("attr-meter-min", "forms.html#attr-meter-min")),
    ATTR_PARAM_VALUE(new Link("attr-param-value", "embedded-content.html#attr-param-value")),
    ATTR_HYPERLINK_USEMAP(new Link("attr-hyperlink-usemap", "embedded-content.html#attr-hyperlink-usemap")),
    ATTR_INPUT_LIST(new Link("attr-input-list", "forms.html#attr-input-list")),
    ATTR_TEXTAREA_REQUIRED(new Link("attr-textarea-required", "forms.html#attr-textarea-required")),
    ATTR_TRACK_SRC(new Link("attr-track-src", "embedded-content.html#attr-track-src")),
    ATTR_HYPERLINK_TARGET(new Link("attr-hyperlink-target", "semantics.html#attr-hyperlink-target")),
    ATTR_INPUT_PATTERN(new Link("attr-input-pattern", "forms.html#attr-input-pattern")),
    ATTR_LINK_HREF(new Link("attr-link-href", "semantics.html#attr-link-href")),
    ATTR_TEXTAREA_WRAP(new Link("attr-textarea-wrap", "forms.html#attr-textarea-wrap")),
    ATTR_EMBED_SRC(new Link("attr-embed-src", "embedded-content.html#attr-embed-src"));

    //<<END OF THE GENERATED SECTION

    public static final String BASE_URL = "http://www.whatwg.org/specs/web-apps/current-work/multipage/";//NOI18N

    public static Collection<Attribute> GLOBAL_ATTRIBUTES = EnumSet.range(ACCESSKEY, TRANSLATE);
    public static Collection<Attribute> EVENT_ATTRIBUTES = EnumSet.range(ONABORT, ONWAITING);

    private static final Pattern ENUM_NAME_PATTERN = Pattern.compile(".*?-.*?-(.*)");

    private Link link;
    private String name;

    private Attribute() {
    }

    private Attribute(Link link) {
        this.link = link;
    }

    public static String attributeId2EnumName(String attributeId) {
        return attributeId.replace('-', '_').toUpperCase();
    }
    
    static String parseName(String id) {
        Matcher matcher = ENUM_NAME_PATTERN.matcher(id);
        return matcher.matches() ? matcher.group(1) : id;
    }

    public synchronized String getName() {
        if(name == null) {
            name = parseName(getAttributeId());
        }
        return name;
    }


    public URL getHelpUrl() {
        return link.getUrl(BASE_URL);
    }

    public String getHelpLink() {
        return link.getLink();
    }

    public String getAttributeId() {
        return link.getName();
    }
}
