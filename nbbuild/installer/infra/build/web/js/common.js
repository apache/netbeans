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

/* Languages and their ids which NetBeans is available*/
var LANGUAGES   = new Array();
var BUILD_INFO  = new Array();

var PLATFORM_IDS         = new Array();
var PLATFORM_LONG_NAMES  = new Array();
var PLATFORM_SHORT_NAMES = new Array();

var BUNDLE_IDS   = new Array();
var BUNDLE_LONG_NAMES = new Array();
var BUNDLE_SHORT_NAMES = new Array();

var FILES = new Array();

PLATFORM_IDS   	     [0] = "windows";
PLATFORM_IDS   	     [1] = "linux";
PLATFORM_IDS         [2] = "macosx";
PLATFORM_IDS         [3] = "zip";
/*
// Commented since NBI installers are not used for now
PLATFORM_IDS         [4] = "macosx-x86";
PLATFORM_IDS         [5] = "macosx-ppc";
*/

BUNDLE_IDS [0] = "javase";
//BUNDLE_IDS [1] = "javafx";
BUNDLE_IDS [1] = "javaee";
//BUNDLE_IDS [2] = "ruby";
BUNDLE_IDS [2] = "html";
BUNDLE_IDS [3] = "php";
BUNDLE_IDS [4] = "cpp";
BUNDLE_IDS [5] = "all";

var DEFAULT_LANGUAGE = "DEFAULT";
var PAGELANG_SEP = "pagelang=";

var OMNITURE_CODE_JS = "//netbeans.org/images_www/js/s_code_remote.js";
var GOOGLE_ANALYTICS_JS = "http://www.google-analytics.com/ga.js";

function getNameById(id,ids,names) {
    for(var i = 0 ; i < ids.length; i++) {
	if(ids[i] == id) {
		return names[i];
	}
    }
    return "";
}

function getPlatformShortName(id) {
    return getNameById(id, PLATFORM_IDS, PLATFORM_SHORT_NAMES);
}
function getPlatformLongName(id) {
    return getNameById(id, PLATFORM_IDS, PLATFORM_LONG_NAMES);
}

function getLanguageName(id) {
    var ids = new Array();
    var names = new Array();
    for(var i=0;i<LANGUAGES.length;i++) {
        ids.push(LANGUAGES[i].id);
        names.push(LANGUAGES[i].name);
    }
    return getNameById(id, ids, names);
}

function getBundleShortName(id) {
    return getNameById(id, BUNDLE_IDS, BUNDLE_SHORT_NAMES);
}
function getBundleLongName(id) {
    return getNameById(id, BUNDLE_IDS, BUNDLE_LONG_NAMES);
}

function get_overridden_language() {
    var url = "" + window.location;
    var idx = url.indexOf(PAGELANG_SEP);
    var langcode = DEFAULT_LANGUAGE;
    if(idx != -1) {
	langcode = url.substring(idx + PAGELANG_SEP.length, url.length);
    }
    return langcode;

}


function get_language_id() {
    return get_language(0);
}

function get_language_suffix() {
    return get_language(1);
}

function get_language_location() {
    var lang_suffix = get_language_suffix();
    for(var i = 0; i < LANGUAGES.length; i++) {
       if(lang_suffix == LANGUAGES[i].suffix) {
           return LANGUAGES[i].location;
       }
    }
}

function is_suitable_location(lang_id, location) {
    for(var i = 0; i < LANGUAGES.length; i++) {
       if(lang_id == LANGUAGES[i].id) {
           return LANGUAGES[i].location == location;
       }
    }
    return false;
}

function get_language(option) {
    var variants = LANGUAGES;
    var resultLanguage = "";
    if(variants) {
        var lang = (option == 0 ) ? variants[0].id : variants[0].suffix;
        var override = get_overridden_language();

        if (override != DEFAULT_LANGUAGE) lang = override;
        else if(navigator.userLanguage)  lang = navigator.userLanguage;
        else if(navigator.language) lang = navigator.language;
        lang = lang.replace("-", "_");
        for(var i=0; i < variants.length; i++ ) {
            var value = (option == 0 ) ? variants[i].id : variants[i].suffix;
            if(value && lang.toLowerCase().indexOf(value.toLowerCase())!=-1) {
                if(value.length > resultLanguage.length) {
                    resultLanguage = value;
                }
            }
        }
    }

    //no language (for id) selected - fallback to en if it is available
    if (resultLanguage == "" && option == 0) {
        for(var i=0; i < variants.length; i++ ) {
            if(variants[i].id == "en") {
                return "en";
            }
        }
    }

    return resultLanguage;
}


function load_js(script_filename) {
    document.write('<script language="javascript" type="text/javascript" src="' + script_filename + '"></script>');
}

function load_page_js_locale(name,ext) {
    load_js_locale(name, ext);
}

function useAnotherLocation() {
    var loc = get_language_location();
    return loc != getPageArtifactsLocation(false);
}

function isMainLanguage(language) {
    for(var i=0; i < LANGUAGES.length; i++ ) {
        if(LANGUAGES[i].id == language) {
            return LANGUAGES[i].location == getPageArtifactsLocation(false);
        }
    }
    return false;
}

function load_js_locale(script_filename, extension) {
    var suffix = "";
    var locale_suffix = "";
    locale_suffix = get_language_suffix();
    if(locale_suffix!="") {
	suffix = "_" + locale_suffix;
    }
    var a = useAnotherLocation();
    load_page_js(script_filename + suffix + extension,  a);
}

function load_page_img(img,add) {
    if(add) {
        document.write('<img src="' + getImagesLocation() + img + '" ' + add + '/>');
    } else {
        document.write('<img src="' + getImagesLocation() + img + '"/>');
    }
}
function load_page_css(css) {
    document.write('<link rel="stylesheet" type="text/css" href="' + getCSSLocation() + css + '" media="screen"/>');
}

function other_webpage_langs_available() {
    for(var i=0 ; i < LANGUAGES.length; i++) {
        if ( LANGUAGES[i].suffix && LANGUAGES[i].suffix.length > 0) {
            return true;
        }
    }
    return false;
}

function page_languages_sort_function(a, b){
    if (a.webpagename && b.webpagename) {
       return a.webpagename > b.webpagename ? 1 : (a.webpagename ==  b.webpagename ? 0 : -1);
    } else {
       return a.suffix > b.suffix ? 1 : (a.suffix ==  b.suffix ? 0 : -1);
    }
}

function write_page_languages() {
    var locale_suffix = get_language_suffix();

    if(other_webpage_langs_available()) {
        document.getElementById("pagelanguagesbox").style.visibility = 'visible';
    }
    var url = "" + window.location;
    var qIndex = url.indexOf("?")!=-1 ? url.indexOf("?") : url.length;
    var aIndex = url.indexOf("&")!=-1 ? url.indexOf("&") : url.length;
    var page = url.substring(0, Math.min(qIndex, aIndex));
    var get_request = url.substring(url.indexOf(page) + page.length, url.length);
    if(get_request.indexOf(PAGELANG_SEP)==-1) {
        if(get_request.indexOf("?")==-1) {
            get_request += "?";
        } else if(get_request.indexOf("&")) {
            get_request += "&";
        }
        get_request += PAGELANG_SEP;
    } else {
        var regexp =  new RegExp(PAGELANG_SEP + "[a-zA-Z]+(_[a-zA-Z]+){0,2}","g");
	get_request = get_request.replace(regexp, PAGELANG_SEP);
    }
    var languages_sorted = LANGUAGES.slice().sort(page_languages_sort_function);

    for(var i=0;i<languages_sorted.length;i++) {
	if(languages_sorted[i].webpagename && locale_suffix!=languages_sorted[i].suffix) {
            document.write('<li><a href="' + page + get_request.replace(PAGELANG_SEP, PAGELANG_SEP + languages_sorted[i].suffix) + '">' + languages_sorted[i].webpagename + '</a></li>');
        }
    }
}

function startList() {
    // source: //netbeans.org/branding/scripts/lang-pulldown.js
    if (document.all&&document.getElementById) {
        navRoot = document.getElementById("nav");
        if (navRoot!=null) { //if the language panel is active
            for (i=0; i<navRoot.childNodes.length; i++) {
                node = navRoot.childNodes[i];
                if (node.nodeName=="LI") {
                    node.onmouseover=function() {
                        this.className+=" over";
                    }
                    node.onmouseout=function() {
                        this.className=this.className.replace(" over", "");
                    }
                }
	    }
	}
    }
}

function get_file_list(dir,lang_id) {
	lst = new Array();
	if(FILES.length > 0) {
            for (var i = 0; i < FILES.length; i++) {
		if(FILES[i].name.indexOf(dir)==0 && languageCompatible(FILES[i].locales, lang_id) && is_suitable_location(lang_id, FILES[i].location)) {
			var stripped = FILES[i].name.substring(dir.length, FILES[i].name.length);
			if(stripped.indexOf('/')==-1) {
			    lst[lst.length] = stripped;
			}
		}
            }
	}
	return lst;
}

function languageCompatible(language_list, lang_id) {
    for(var i = 0; i < language_list.length; i++) {
       if(language_list[i]==lang_id) return true;
    }
    return false;
}

function get_file_info(filename,lang_id) {
    var file = null;
	if(FILES.length > 0) {
            for (var i = 0; i < FILES.length; i++) {
		if(FILES[i].name == filename && languageCompatible(FILES[i].locales, lang_id)) {
			file = FILES[i];
			break;
		}
            }
	}
	return file;
}

function getSize(filename, lang_id) {
        var file = get_file_info(filename, lang_id);
	return file!=null ? file.size : "";
}

function getMD5(filename, lang_id) {
        var file = get_file_info(filename, lang_id);
	return file!=null ? file.md5 : "";
}


function get_file_name(platform, option, language, bits) {
    //bits = !bits ? 'x86' : bits;
    var fn = "";
    if(platform=="zip") {
        fn += "zip/";
    } else {
        fn += "bundles/";
    }
    return fn + get_file_name_short(platform, option, language,bits);
}

function is_file_available(platform, option, language, bits) {
    return get_file_info(get_file_name(platform, option, language, bits), language) != null;
}

function get_build_location(lang_id) {
    var mainLanguage = isMainLanguage(lang_id);
    var location = get_build_info(mainLanguage).BUILD_LOCATION;
    if(!mainLanguage && location == "") {
       location = get_build_info(true).ADDITIONAL_BUILD_LOCATION;
    }
    return location;
}

function get_zip_files_prefix(lang_id) {
    return get_build_info(isMainLanguage(lang_id)).ZIP_FILES_PREFIX;
}

function get_bundles_files_prefix(lang_id) {
    return get_build_info(isMainLanguage(lang_id)).BUNDLE_FILES_PREFIX;
}

function get_file_name_short(platform, option, language, bits) {
    var file_name = "";
    if(platform=="zip") {
        if(option == "javadoc" || option == "platform-src" || option == "src") {
            file_name += get_zip_files_prefix(language).replace("-ml","");
        } else {
            file_name += get_zip_files_prefix(language);
        }
    } else {
        file_name += get_bundles_files_prefix(language);
    }
    if (option != "all") {
    	file_name += "-" + option;
    }

    if ( platform != "zip" ) {
   	    file_name += "-" + platform;
    }

    if(bits) {
        file_name += "-"+bits;
    } 

    if (platform == "windows") {
        file_name += ".exe";
    } else if (platform == "macosx") {
	   file_name += ".dmg";
    } else if(platform == "zip"){
	   file_name += ".zip"
    } else {
        file_name += ".sh";
    }
    return file_name;
}

function get_file_url(filename, lang_id) {
    var url  = get_build_location(lang_id);
    url += filename;
    return url;
}


function get_file_bouncer_url(platform, option, language) {
    var url = get_build_info(isMainLanguage(language)).BOUNCER_URL;
    url += "?" + "product=" + get_build_info(isMainLanguage(language)).BOUNCER_PRODUCT_PREFIX;
    if(option != "all") {
        url += "-" + option;
    }
    url += "&" + "os=" + platform;
    return url;
}

function message(msg) {
    document.write(msg);
}
function writeUrl(url,msg) {
    document.write('<a href="' + url + '">' + msg + '</a>');
}
function set_page_title(title) {
    document.title = title;
    var titleElement = document.getElementsByTagName("title");
    if(titleElement.length == 1) {
        titleElement[0].text = title;
    } else {
        document.write('<title>' + title + '</title>');
    }
}

function set_page_description(desc) {
    document.write('<meta name="description" content="' + desc + '"/>');
}

function add_file(name, size, md5, locales) {
    var index = FILES.length;
    FILES[index] = new Object;
    FILES[index].name = name;
    FILES[index].size = size;
    FILES[index].md5  = md5;
    FILES[index].locales = locales.split(",");
    FILES[index].location = currentLocation;
}

function load_files_information(additional) {
    if(!additional) {
        load_page_js("files.js", false);
    } else if(get_build_info(true).SHOW_ADDITIONAL_LANGUAGES == 1) {
        load_page_js("files.js", true);
    }
}

function add_language(id, name, suffix, webpagename) {
    var index = LANGUAGES.length;
    for(var i=0;i<index;i++) {
       if(LANGUAGES[i].id == id) {
           if(id == "en" && isCommunityBuild()) {
               var newLanguages = new Array();
               for(var j=0;j<index;j++) {
                   if(j!=i) {
                       newLanguages[newLanguages.length] = LANGUAGES[j];
                   }
               }
               LANGUAGES = newLanguages;
               index--;
           } else {
               return;
           }
       }
    }
    LANGUAGES[index] = new Object;
    LANGUAGES[index].name        = name;
    LANGUAGES[index].id          = id;
    if(suffix || webpagename) {
       LANGUAGES[index].suffix      = suffix;
       LANGUAGES[index].webpagename = webpagename;
    }
    LANGUAGES[index].location = currentLocation;
}


function load_languages(additional) {
     if(!additional) {
          load_page_js("languages.js", false);
     } else if(get_build_info(true).SHOW_ADDITIONAL_LANGUAGES == 1) {
         load_page_js("languages.js", true);
     }
}
function add_build_info(build_info) {
     var index = BUILD_INFO.length;
     BUILD_INFO[index]=build_info;
}
function get_build_info(mainLanguage) {
     var index = (!mainLanguage && BUILD_INFO.length == 2) ? 1 : 0;
     return BUILD_INFO[index];
}
function isCommunityBuild() {
     return get_build_info(true).COMMUNITY_BUILD == 1;
}

function load_build_info(additional) {
     if(!additional) {
          load_page_js("build_info.js", false);
     } else if(get_build_info(true).SHOW_ADDITIONAL_LANGUAGES == 1) {
         load_page_js("build_info.js", true);
     }
}
