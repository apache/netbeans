/**
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

var PRODUCT_INFOS = new Array();

var BUNDLES = new Array();

var product_uids           = new Array();
var product_versions       = new Array();
var product_display_names  = new Array();
var product_notes          = new Array();
var product_descriptions   = new Array();
var product_download_sizes = new Array();
var product_platforms      = new Array();

var group_products         = new Array();
var group_display_names    = new Array();
var group_descriptions     = new Array();


var INFO_ICON   = getImagesLocation() + "info_icon.gif";
var INFO_ICON_H = getImagesLocation() + "info_icon_h.gif";

var CHECKED_WHITE_SRC = getImagesLocation() + "checked_badge_white.gif";
var CHECKED_BEIGE_SRC = getImagesLocation() + "checked_badge_beige.gif";
var WARNING_WHITE_SRC = getImagesLocation() + "warning_badge_white.gif";
var WARNING_BEIGE_SRC = getImagesLocation() + "warning_badge_beige.gif";
var CHECKED_WHITE_NA_SRC = getImagesLocation() + "checked_badge_notavailable.gif";

var IMAGE_CHECKED_WHITE_NOT_AVAILABLE = '<img src="' + CHECKED_WHITE_NA_SRC + '"/>';

var DOWNLOAD_BUTTON_NORMAL_SRC    = getImagesLocation() + DOWNLOAD_BUTTON_NORMAL;
var DOWNLOAD_BUTTON_DISABLED_SRC  = getImagesLocation() + DOWNLOAD_BUTTON_DISABLED;
var DOWNLOAD_BUTTON_HIGHLIGHT_SRC = getImagesLocation() + DOWNLOAD_BUTTON_HIGHLIGHT;

var DOWNLOAD_IMG = '<img onmouseover="this.src=&quot;' + DOWNLOAD_BUTTON_HIGHLIGHT_SRC + '&quot;" onmouseout="this.src=&quot;' + DOWNLOAD_BUTTON_NORMAL_SRC + '&quot;" src="' + DOWNLOAD_BUTTON_NORMAL_SRC + '" style="cursor: pointer; border: 0;"/>';
var DOWNLOAD_IMG_DISABLED = '<img src="' + DOWNLOAD_BUTTON_DISABLED_SRC + '" style="border: 0;"/>';


var IMAGE_CHECKED_WHITE = '<img src="' + CHECKED_WHITE_SRC + '"/>';
var IMAGE_WARNING_WHITE = '<img src="' + WARNING_WHITE_SRC + '"/>';
var IMAGE_CHECKED_BEIGE = '<img src="' + CHECKED_BEIGE_SRC + '"/>';
var IMAGE_WARNING_BEIGE = '<img src="' + WARNING_BEIGE_SRC + '"/>';

var NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG_NUMBER = "1";

var download_tabs_number = 0;
var last_selected_lang = 0;
var MORE_LANG_ID = "more";

function handle_keyup(event) {
    //if (event.keyCode == 13) {
    //    download('standard');
    //}
}

function initialize() {
 if (document.images) {
     download_on = new Image();
     download_on.src  = DOWNLOAD_BUTTON_NORMAL_SRC;
     download_off= new Image();
     download_off.src = DOWNLOAD_BUTTON_DISABLED_SRC;
     download_hl = new Image();
     download_hl.src  = DOWNLOAD_BUTTON_HIGHLIGHT_SRC;
     info = new Image();
     info.src  = INFO_ICON;
     info_h = new Image();
     info_h.src  = INFO_ICON_H;
     chw = new Image();
     chw.src  = CHECKED_WHITE_SRC;
     chb = new Image();
     chb.src  = CHECKED_BEIGE_SRC;
     ww = new Image();
     ww.src  = WARNING_WHITE_SRC;
     wb = new Image();
     wb.src  = WARNING_BEIGE_SRC;
     chwna = new Image();
     chwna.src = CHECKED_WHITE_NA_SRC;
 }
}

function write_languages() {
   var added = false;
   for(var i=0; i < LANGUAGES.length; i++ ) {
        if(!isMainLanguage(LANGUAGES[i].id) && !added) {
		added = true;
		document.write('<option class="community_separator" value="' + MORE_LANG_ID + '">' + (isCommunityBuild() ? SUN_SUPPORTED_SEP : COMMUNITY_CONTRIBUTED_SEP) + '</option>');
	}
   	document.write('<option value="' + LANGUAGES[i].id + '">' + LANGUAGES[i].name + '</option>');
   }
}

function write_platforms() {
   for(var i=0; i < PLATFORM_IDS.length; i++ ) {
   	document.write('<option value="' + PLATFORM_IDS[i] + '">' + PLATFORM_LONG_NAMES[i] + '</option>');
   }
}

function write_components() {
    for (var i = 0; i < group_products.length; i++) {
        // skip the first group name as it goes to the title of the table
        if (i != 0) {
			document.write('<tr class="row_hover bottom_border top_border">');
			document.write('    <th class="onhover_change left">' + ((i==1) ? BUNDLED_SERVERS_GROUP_NAME : group_display_names[i]) + '</th>');

			for(var k=0;k<BUNDLE_IDS.length;k++) {
				document.write('    <th class="onhover_change ' + (k%2==0 ? 'beige ': '') + 'left_border' + (k==(BUNDLE_IDS.length - 1) ? ' right_border': '') + '"></th>');
                        }

			document.write('</tr>');
        }

        for (var j = 0; j < group_products[i].length; j++) {
		var uid = group_products[i][j];
                var index = "";
                for(var k=0;k<product_uids.length;k++) {
                   if (product_uids[k] == uid) {
                      index = k;
                      break;
                   }
                }
		if (is_product_hidden(uid)) {
		    continue;
		}

		document.write('<tr class="row_hover"' + (j % 2 ? ' class="even"' : '') + '>');

		document.write('    <td class="onhover_change left">');

		document.write('<div id="product_' + index + '_description" class="pop_up">' + '<span style="font-weight:bold">'+ product_display_names[index] + '</span><br><br>' + product_descriptions[index] + '</div>');
		document.write('<img src="' + INFO_ICON + '" onmouseover="this.src=&quot;' + INFO_ICON_H + '&quot;;show_description(' + index + ');" onmouseout="this.src=&quot;' + INFO_ICON +  '&quot;;hide_description(' + index + ');"></img>');
		//document.write('<span id="product_' + index + '_display_name" onmouseover="show_description(' + index + ');" onmouseout="hide_description(' + index + ');"><a class="product_display_name">' + product_display_names[index] + '</a></span>');
		document.write('<span id="product_' + index + '_display_name""><a class="product_display_name">' + product_display_names[index] + '</a></span>');
		if (product_notes[j] != '') {
			document.write('<br><span class="product_note">' + product_notes[index] + '</span>');
		}
		document.write('	</td>');

		for(var k=0;k<BUNDLE_IDS.length;k++) {
		    document.write('    <td class="onhover_change ' + (k%2==0 ? 'beige ' : '') + 'left_border' + (k == (BUNDLE_IDS.length - 1) ? ' right_border' : '') + '" id="product_' + index + '_' + BUNDLE_IDS[k] + '"></td>');
                }

		document.write('</tr>');
        }
    }
}

function parseList(list) {
   var obj = new Array();
   var idx = -1;
   var rest = list;
   while (rest!="") {
      idx = rest.indexOf(", ");
      if(idx !=-1) {
         obj[obj.length] = rest.substring(0, idx);
         rest = rest.substring(idx + 2, rest.length);
      } else {
        obj[obj.length] = rest;
        rest = "";
      }
   }

   return obj;
}

function add_product_info(uid, version, name, note, description, size, platforms) {
    product_uids[product_uids.length] = uid;
    product_versions[product_versions.length] = version;
    product_display_names[product_display_names.length] = name;
    product_notes[product_notes.length] = note;
    product_descriptions[product_descriptions.length] = description;
    product_download_sizes[product_download_sizes.length] = size;
    product_platforms[product_platforms.length] = parseList(platforms);
}

function add_group_info(products, name, description) {
    group_products[group_products.length] = parseList(products);
    group_display_names[group_display_names.length] = name;
    group_descriptions[group_descriptions.length] = description;
}

function add_bundle_info(uid, products) {
    var index = BUNDLES.length;
    BUNDLES[index] = new Object;
    BUNDLES[index].uid = uid;
    BUNDLES[index].products = parseList(products);
}

function getHeader(community) {
    return (community ?
            (NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG + '<span class="community_note_number">' + NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG_NUMBER + '</span>') :
             NETBEANS_DOWNLOAD_BUNDLES_MSG);
}

function write_table_header() {
    document.write('<tr class="no_hover">');
    document.write('<td class="no_border no_padding"></td>');
    document.write('<td class="no_border no_padding" colspan="6">');
    document.write('<table class="components_table">');
    document.write('<tr class="no_hover">');
    document.write('	<td id="download_page_header" class="no_hover header" colspan="6">' + getHeader(isCommunityBuild()) + '</td>');
    document.write('</tr></table>');
    document.write('</td>');
    document.write('</tr>');
    document.write('<br><br>');
    document.write('<tr class="no_hover">');
    document.write('<th class="left no_border bottom_border wide bottom">' + NETBEANS_PACKS_MSG + '<a class="star">*</a></th>');
    for(var i=0;i<BUNDLE_IDS.length;i++) {
        var id = BUNDLE_IDS[i];
        document.write('<td class="no_border bottom_border" id="'+ id + '_bundle_name"> <a class="bundle_display_name">' + getBundleLongName(id) + '</a></td>');
    }
    document.write('</tr>');
}

function get_download_button(id, enabled, bit) {
   bit = bit || '';
   if(enabled) {
    if(id!=='')
       var img = DOWNLOAD_IMG;
       if(bit.length>0) {
           img=DOWNLOAD_IMG.replace(/download/g,"download_"+bit);
       }
       return '<a href="javascript: download(\'' + id + '\',\''+bit+'\')"     id="' + id + '_name"> ' + img + '</a>';
   } else {
       return DOWNLOAD_IMG_DISABLED;
   }
}

function write_table_footer() {
    document.write('<tr class="column_hover">');
    document.write('<th class="no_hover left no_border  wide bottom">&nbsp;</th>');
    for(var i=0;i<BUNDLE_IDS.length;i++) {
        var id = BUNDLE_IDS[i];
        document.write('<td class="no_border download_button"  id="' + id + '_link">' + get_download_button (id, true) + '</td>');
    }
    document.write('</tr>');
}

function write_components_sizes() {
    document.write('<tr class="no-hover">');
    document.write('<td class="no_border"></td>');
    for(var i=0;i<BUNDLE_IDS.length;i++) {
        var id = BUNDLE_IDS[i];
            document.write('<td class="no_border" id="' + id + '_size"></td>');
    }

    document.write('</tr>');
}

function show_description(index) {
    document.getElementById('product_' + index + '_description').style.visibility = 'visible';
}

function hide_description(index) {
    document.getElementById('product_' + index + '_description').style.visibility = 'hidden';
}


function highlight(){
	var table = document.getElementById("components_table");
	var tr = table.getElementsByTagName("tr");
	var cells =  new Array();
        for (var i=0;i<tr.length;i++){
		var arr = new Array();
		for(var j=0;j<tr[i].childNodes.length;j++){
			var node = tr[i].childNodes[j];
			if(node.nodeType==1) arr.push(node);
		}
		cells.push(arr);
	}

	for (var i=0;i<tr.length;i++){
		var arr = cells[i];
		for (var j=0;j<arr.length;j++){
			arr[j].row = i;
			arr[j].col = j;
			if(arr[j].innerHTML == "&nbsp;" || arr[j].innerHTML == "") arr[j].className += " empty";
			arr[j].css = arr[j].className;
			arr[j].hlCol = false;
			arr[j].hlRow = false;
			var classname = tr[i].className;
			if( classname.indexOf("column_hover") != -1)  {
				arr[j].hlCol = true;
			}
			if(classname.indexOf("row_hover") != -1) {
				arr[j].hlRow = true;
			}
                        if(arr[j].className.indexOf("no_hover") != -1) {
				arr[j].hlRow = false;
				arr[j].hlCol = false;
			}
			if(arr[j].hlRow || arr[j].hlCol) {
				arr[j].onmouseover = function(){
					over(table,cells,this);
				}
				arr[j].onmouseout = function(){
					out(table,cells,this);
				}
			}
		}
	}
}

function over(table,cells,obj){
	if (!obj.hlCol && !obj.hlRow) obj.className = obj.css + " over";
	if(!(obj.col == 0 && obj.className.indexOf("empty") != -1)) {
		if(obj.hlRow) highlightRow(table,cells,obj.row);
		if(obj.hlCol) highlightCol(table,cells,obj.col);
	}
}
function out(table,cells,obj){
	if (!obj.hlCol && !obj.hlRow) obj.className = obj.css;
	unhighlightRow(table,cells,obj.row);
	unhighlightCol(table,cells,obj.col);
}

function highlightCol(table,cells,col){
	var css = "over";
	for (var i=0;i<cells.length;i++){
		var arr = cells[i];
		if(col<arr.length) {
			var obj = arr[col];
			if (obj.className.indexOf("onhover_change")!=-1) {
				obj.className = obj.css + " " + css;
			}
		}
	}
}

function unhighlightCol(table,cells,col){
	for (var i=0;i<cells.length;i++){
		var arr = cells[i];
		if(col<arr.length) {
		    var obj = arr[col];
		    obj.className = obj.css;
		}
	}
}

function highlightRow(table,cells,row){
	var css =  "over";
	var tr = table.getElementsByTagName("tr")[row];
	for (var i=0;i<tr.childNodes.length;i++){
		var obj = tr.childNodes[i];
		obj.className = obj.css + " " + css;
	}
}

function unhighlightRow(table,cells,row){
	var tr = table.getElementsByTagName("tr")[row];
	for (var i=0;i<tr.childNodes.length;i++){
		var obj = tr.childNodes[i];
		obj.className = obj.css;
	}
}



function detect_platform() {
    var agent = navigator.userAgent;
    var platform;
    if (agent.indexOf("Windows") != -1) {
        platform='windows';
        document.getElementById("platform_select").selectedIndex = 0;
    }
    if (agent.indexOf("Linux") != -1) {
        platform='linux';
        document.getElementById("platform_select").selectedIndex = 1;
    }
    if (agent.indexOf("Intel Mac OS") != -1) {
        platform='mac';
        document.getElementById("platform_select").selectedIndex = 2;
    }
    if (agent.indexOf("PPC Mac OS") != -1) {
        platform='mac';
        document.getElementById("platform_select").selectedIndex = 2;
    }
    if (agent.indexOf("SunOS i86pc") != -1) {
        platform='solaris';
        document.getElementById("platform_select").selectedIndex = 3;
    }
    if (agent.indexOf("SunOS sun4") != -1) {
        platform='solaris';
        document.getElementById("platform_select").selectedIndex = 3;
    }
    if (agent.indexOf("BSD") != -1) {
        platform='linux';
        document.getElementById("platform_select").selectedIndex = 3;
    }
    return platform;
}

function getZipUrl() {
    var langselect = document.getElementById("language_select");
    var lang_id = langselect.options[langselect.selectedIndex].value;
    var build_location = get_build_location(lang_id);
    return get_build_info(true).USE_HTML_ZIP_LISTING ? ("javascript: open_zip_link()") : (build_location + "zip/");
}
function select_language() {
    var language = get_language_id();
    if(!isMainLanguage(language) && !isCommunityBuild()) {
       language = "en";
    }
    var select = document.getElementById("language_select");
    var languageOptions = select.options;
    for(var i=0;i<languageOptions.length;i++) {
        if(languageOptions[i].value == language) select.selectedIndex = i;
    }
    last_selected_lang = select.selectedIndex;
}



function is_product_hidden(product_uid) {
    return is_product_in_bundle(product_uid, "hidden");
}
function is_product_in_bundle(product_uid, bundle_uid) {
    var hidden = false;
    var bundle;
    for(var k=0; k < BUNDLES.length; k++ ) {
       if(BUNDLES[k].uid == bundle_uid) {
           bundle = BUNDLES[k];
           break;
       }
    }
    if(!bundle) {
      return false;
    }
    for(var k=0;k<bundle.products.length;k++) {
       if(product_uid == bundle.products[k]) {
                return true;
        }
    }
    return false;
}


function update() {
    var langselect = document.getElementById("language_select");
    if(langselect.options[langselect.selectedIndex].value == MORE_LANG_ID) {
        langselect.selectedIndex = last_selected_lang;
        window.location = get_build_info(true).ADDITIONAL_BUILD_LOCATION;
    }
    last_selected_lang = langselect.selectedIndex;

    lang_id = langselect.options[langselect.selectedIndex].value;

    var select = document.getElementById("platform_select");
    var platform = select.options[select.selectedIndex].value;
    var platform_display_name = select.options[select.selectedIndex].text;


    /*For NetBeans we have native mac installer*/

    if(platform=="macosx") {
        platform = "macosx-x86";
    }

    // update the "checks" and generate error messages, if any
    var product_messages = new Array();
    for (var i = 0; i < product_uids.length; i++) {
        if (is_product_hidden(product_uids[i])) {
            continue;
        }

        // enter the default value
        product_messages[i] = null;

        if (!is_compatible(i, platform)) {
	     product_messages[i] = product_display_names[i];
        }

        for(var k=0;k<BUNDLE_IDS.length;k++) {
                var id = BUNDLE_IDS[k];

                if(is_product_in_bundle(product_uids[i], BUNDLE_IDS[k])) {
                    if (product_messages[i] == null) {
                        document.getElementById("product_" + i + "_" + id).innerHTML = IMAGE_CHECKED_WHITE;
                    } else {
                        document.getElementById("product_" + i + "_" + id).innerHTML = IMAGE_WARNING_WHITE;
                    }
                } else {
                    document.getElementById("product_" + i + "_" + id).innerHTML = '';
                }
	}


	if (product_messages[i] == null) {
		document.getElementById("product_" + i + "_display_name").innerHTML = '<a class="product_display_name">' + product_display_names[i] + "</a>";
	} else {
		document.getElementById("product_" + i + "_display_name").innerHTML = '<a class="product_display_name_no">' + product_display_names[i] + "</a>";
	}
    }

    // update the error message
    var error_message = "";
    var messages_number = 0;

    for (var i = 0; i < product_uids.length; i++) {
        if (product_messages[i] != null) {
        messages_number += 1;
        }
    }
    if (messages_number != 0 ) {

	var messages_counter = 0;

	error_message += NOTE_PREFIX;

    	for (var j = 0; j < product_uids.length; j++) {
        	if (product_messages[j] != null) {

			if ( messages_counter == 0) {
            			error_message += product_messages[j];
			} else if (messages_counter == (messages_number-1) ){
				error_message = NOTE_AND_SEP.replace('{0}', error_message).replace('{1}', product_messages[j]);
			} else {
				error_message = NOTE_COMMA_SEP.replace('{0}', error_message).replace('{1}', product_messages[j]);
			}

			messages_counter += 1;
        	}
    	}
	var na = '';
	if (messages_number == 1 ) {
		if ( platform == "zip" ) {
			na = SINGLE_NOT_AVAILABLE_ZIP;
		} else {
			na = SINGLE_NOT_AVAILABLE_BUNDLE;
		}
        } else {
		if ( platform == "zip" ) {
			na = MULTIPLE_NOT_AVAILABLE_ZIP;
		} else {
			na = MULTIPLE_NOT_AVAILABLE_BUNDLE;
		}
	}
	error_message = na.replace('{0}', error_message).replace('{1}', platform_display_name);;
    } else {
	error_message = '<br>';
    }

    // use positive wording instead of negative

    if ( platform == "zip" ) {
        error_message = NOTE_ZIP;
    }

    if (error_message != "" ) {
       error_message = NOTE_ALL;
    }


    document.getElementById("error_message").innerHTML = error_message;

    // update the sizes
    var sizes = new Array(BUNDLE_IDS.length);
    for(var i=0;i<BUNDLE_IDS.length;i++) {
      sizes[i] = 0;
    }

    for (var i = 0; i < product_uids.length; i++) {
        var plat=platform.replace('macosx-x86','macosx');
        if (!is_compatible(i, platform)) {
            continue;
        }

        for(var k=0;k<BUNDLE_IDS.length;k++) {
            if (is_product_in_bundle(product_uids[i], BUNDLE_IDS[k])) {
                var sizes=[];
                var size=0;
                if(is_file_available(plat,BUNDLE_IDS[k],lang_id,'')) {
                    sizes.push(get_file_size_mb(get_file_name(plat, BUNDLE_IDS[k], lang_id), lang_id, 0));
                }
                if(is_file_available(plat,BUNDLE_IDS[k],lang_id,'x86')) {
                    sizes.push(get_file_size_mb(get_file_name(plat, BUNDLE_IDS[k], lang_id,'x86'), lang_id, 0));
                }
                if(is_file_available(plat,BUNDLE_IDS[k],lang_id,'x64')) {
                    sizes.push(get_file_size_mb(get_file_name(plat, BUNDLE_IDS[k], lang_id,'x64'), lang_id, 0));
                }
                size=sizes.sort().join(' - ');
                document.getElementById(BUNDLE_IDS[k] + "_size").innerHTML   = FREE_SIZE_MESSAGE.replace('{0}', size);
            }

        }
    }

    for(var k=0;k<BUNDLE_IDS.length;k++) {
        var plat=platform.replace('macosx-x86','macosx');
        var exists = false;
        var id = BUNDLE_IDS[k];

        for(var j=0;j<BUNDLES.length;j++) {
            if(id == BUNDLES[j].uid) {
               exists = true;
            }
        }

        var button;

        var winButtons=[];
        if(is_file_available(plat,id,lang_id,'')) {
            winButtons.push(get_download_button(id, exists));
        }
        if(is_file_available(plat,id,lang_id,'x86')) {
            winButtons.push(get_download_button(id, exists,'x86'));
        }
        if(is_file_available(plat,id,lang_id,'x64')) {
            winButtons.push(get_download_button(id, exists,'x64'));
        }
        button=winButtons.join('<br/>');

        document.getElementById(id + "_link").innerHTML   = button;
    }

    document.getElementById("jdk_note").innerHTML = JDK_NOTE_ALL.replace('{0}',JAVA_COM_LINK).replace('{1}',JDK_DOWNLOAD_LINK).replace('{2}',NBJDK_DOWNLOAD_LINK);

    var mainLanguage = isMainLanguage(lang_id);
    var comunityBuild = (isCommunityBuild() && mainLanguage) || (!isCommunityBuild() && !mainLanguage);

    if (comunityBuild == 1) {
	document.getElementById("community_number").innerHTML  = "<a class=\"special_message_number\">" + NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG_NUMBER + "</a>";
	document.getElementById("community_message").innerHTML = "<a class=\"special_message_text\">" + COMMUNITY_MESSAGE + "</a>";
	document.getElementById("download_page_header").innerHTML = getHeader(true);
    }
    else {
	document.getElementById("community_message").innerHTML = "";
	document.getElementById("community_number").innerHTML  = "";
	document.getElementById("download_page_header").innerHTML = getHeader(false);
    }
    document.getElementById("zip_link").setAttribute("href", getZipUrl());
}

function is_compatible(index, platform) {
    if ( platform == "zip" ) {
         for (var i = 0; i < group_products.length; i++) {
          for (var j = 0; j < group_products[i].length; j++) {
	      var uid = group_products[i][j];
              for(var k=0;k<product_uids.length;k++) {
                 if (product_uids[k] == uid) {
                    if((k==index) && (i == 0) && uid != "nb-javafx" && uid != "nb-javacard") {//runtimes are not available in zip
                        return true;
                    }
                 }
              }
          }
        }
    } else {
        for (var i = 0; i < product_platforms[index].length; i++) {
            if (product_platforms[index][i] == platform) {
                return true;
            }
        }
    }

    return false;
}

function get_file_size_mb(name,lang_id, defaultValue) {
   var size = getSize(name,lang_id);
   if(size=="") {
       size = defaultValue;
   } else {
       size = Math.ceil(size / (1024 * 1024));
   }
   return size;
}

function add_download_tab(name, url) {
   if(download_tabs_number!=0) {
       document.write(" | ");
   }
   if(url) {
	writeUrl(url,name);
   } else {
	message('<span class="download_tab_active">' + name + '</span>');
   }
   download_tabs_number++;
}

function write_files_list(title,directory) {
    document.write('<h1>' + title + '</h1>');
    document.write('<ul>');

    var lang_id = get_language_id();
    if(lang_id=="") lang_id = "en";
    var lst = get_file_list(directory, lang_id);
    for(var i=0;i<lst.length;i++) {
          var item_display_name = lst[i];
          var item_link = "javascript: download_file(\'" + directory + lst[i] + "\')";
          document.write('<li><a href="' + item_link  + '">' + item_display_name + '</a></li>');
    }
    document.write('</ul><br>');
}

function open_zip_link() {
    var overriden_language=get_overridden_language();

    var language_select = document.getElementById("language_select");
    var language = language_select.options[language_select.selectedIndex].value;

    //var zip_url = getPageArtifactsLocation(!isMainLanguage(language)) + ZIP_PAGE;
    var zip_url = ZIP_PAGE;

    if(overriden_language!=DEFAULT_LANGUAGE) {
        zip_url += "?" + PAGELANG_SEP + overriden_language;
    }
    window.location = zip_url;
}

function download_file(filename) {
    var download_url = START_PAGE;
    download_url += "?filename=" + filename;

    var overriden_language=get_overridden_language();
    if(overriden_language!=DEFAULT_LANGUAGE) {
        download_url += "&" + PAGELANG_SEP + overriden_language;
    }
    if(get_build_info(true).ADD_VERSION_INFO_TO_URL == 1) {
        download_url += "&version=" + get_build_info(true).BUILD_DISPLAY_VERSION_SHORT;
    }
    window.location = download_url;
}

function download(option, bits) {
    var select = document.getElementById("platform_select");
    var platform = select.options[select.selectedIndex].value;

    var download_url = START_PAGE;
    download_url += "?platform=" + platform;

    var language_select = document.getElementById("language_select");
    var language = language_select.options[language_select.selectedIndex].value;
    download_url += "&lang=" + language;
    download_url += "&option=" + option;
    if(bits) {
        download_url+= '&bits='+bits;
    }
    var email = document.getElementById("emailfield").value;
    if(email!="" && email.indexOf(".")!=-1 && email.indexOf("@")!=-1 && email.indexOf("&")==-1 && email.indexOf("?")==-1) {
	var monthly = (document.getElementById("monthlycb").checked ? 1 : 0);
	var weekly  = (document.getElementById("weeklycb").checked ? 1 : 0);
	var contact = (document.getElementById("contactcb").checked ? 1 : 0);
	if(monthly==1 || weekly ==1 || contact==1) {
		download_url+= "&email="   + email;
		download_url+= "&monthly=" + monthly;
		download_url+= "&weekly="  + weekly;
		download_url+= "&contact=" + contact;
        }
    }
    var overriden_language=get_overridden_language();
    if(overriden_language!=DEFAULT_LANGUAGE) {
        download_url += "&" + PAGELANG_SEP + overriden_language;
    }
    if(get_build_info(language).ADD_VERSION_INFO_TO_URL == 1) {
        download_url += "&version=" + get_build_info(language).BUILD_DISPLAY_VERSION_SHORT;
    }
    window.location = download_url;
}