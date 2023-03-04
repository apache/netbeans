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


var redirect_delay = 1000;

var lang_id="";
var option_id="";
var platform_id="";
var url = "";
var filename = "";
var string = "";
var parent_folder = "";
var bits_id = "";

function initialize() {
	    string = "" + window.location;
            var query    = string.substring(string.indexOf("?") + 1, string.length);
	    var sep = "&";
	    var email = "";
            var monthly = "0";
	    var weekly  = "0";
	    var contact = "0";
	    var email_sep    = "email=";
            var monthly_sep  = "monthly=";
            var weekly_sep   = "weekly=";
            var contact_sep  = "contact=";
            var start_page_string = (string.indexOf("?")==-1) ? string : string.substring(0, string.indexOf("?"));
            parent_folder  = start_page_string.substring(0, start_page_string.lastIndexOf("/") + 1);

	    if(query!="" && query != string/* && query.indexOf(sep)!=-1*/)  {
		    while(query!="") {
		        var lang_sep     = "lang=";
			    var platform_sep = "platform=";
			    var bits_sep = "bits=";
			    var option_sep   = "option=";
                var filename_sep = "filename=";


			    if(query.indexOf(lang_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					lang_id = query.substring(lang_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
        	        	 } else {
					lang_id = query.substring(lang_sep.length, query.length);
					query = "";
				 }
		            } else if(query.indexOf(platform_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					platform_id = query.substring(platform_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					platform_id = query.substring(platform_sep.length, query.length);
					query = "";
				 }
			    } else if(query.indexOf(option_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					option_id = query.substring(option_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					option_id = query.substring(option_sep.length, query.length);
					query = "";
				 }
				} else if(query.indexOf(bits_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					bits_id = query.substring(bits_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					bits_id = query.substring(bits_sep.length, query.length);
					query = "";
				 }
		        } else if(query.indexOf(email_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					email = query.substring(email_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					email = query.substring(email_sep.length, query.length);
					query = "";
				 }
		           } else if(query.indexOf(monthly_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					monthly = query.substring(monthly_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					monthly = query.substring(monthly_sep.length, query.length);
					query = "";
				 }
		            } else if(query.indexOf(weekly_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					weekly = query.substring(weekly_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					weekly = query.substring(weekly_sep.length, query.length);
					query = "";
				 }
		            } else if(query.indexOf(contact_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					contact = query.substring(contact_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					contact = query.substring(contact_sep.length, query.length);
					query = "";
				 }
		            } else if(query.indexOf(filename_sep)==0) {
				 if(query.indexOf(sep)!=-1) {
					filename = query.substring(filename_sep.length, query.indexOf(sep));
					query = query.substring(query.indexOf(sep) + 1, query.length);
	                	 } else {
					filename = query.substring(filename_sep.length, query.length);
					query = "";
				 }
		            } else {
				query = "";
			    }
	            }
		    if(email!="") {
			var phpRequest = SUBSCRIPTION_PHP_URL;
			phpRequest += "?" + email_sep   + email;
			phpRequest += "&" + monthly_sep + monthly;
			phpRequest += "&" + weekly_sep  + weekly;
			phpRequest += "&" + contact_sep + contact;
			phpRequest += "&timestamp=" + new Date().getTime();
			var image = new Image();
			image.src = phpRequest;
			image.style.display="none";
		    }

	            if(filename!="") {
			for(var i=0;i<PLATFORM_IDS.length;i++) {
			    for(var j=0;j<BUNDLE_IDS.length;j++) {
				var testFileName = get_file_name(PLATFORM_IDS[i], BUNDLE_IDS[j], "en");

				if(testFileName==filename) {
				    platform_id = PLATFORM_IDS[i];
				    option_id   = BUNDLE_IDS[j];
				    lang_id     = get_language_id();
				    i = PLATFORM_IDS.length;
				    j = BUNDLE_IDS.length;
				    filename = "";
                                }
                            }
                        }
		    }
		    if(lang_id=="") lang_id = get_language_id();

		    if(option_id != "" && platform_id != "") {
	    	        if (useBouncer(lang_id) == 1) {
                            url      = get_file_bouncer_url(platform_id, option_id, lang_id, bits_id);
                        } else {
                            url      = get_file_url(get_file_name(platform_id, option_id, lang_id, bits_id), lang_id);
		        }
                        filename     = get_file_name(platform_id, option_id, lang_id, bits_id);
		    } else if(filename!="") {
	    	        ADDITIONAL_BUNDLES = new Array();
			ADDITIONAL_BUNDLES[0] = "javadoc";
			ADDITIONAL_BUNDLES[1] = "src";
			ADDITIONAL_BUNDLES[2] = "platform-src";

			if (useBouncer(lang_id) == 1) {
                	    for(var i=0;i<ADDITIONAL_BUNDLES.length;i++) {
			        var testFileName = get_file_name("zip", ADDITIONAL_BUNDLES[i], lang_id, bits_id);
			            if(testFileName==filename) {
				        url      = get_file_bouncer_url("zip", ADDITIONAL_BUNDLES[i], lang_id, bits_id);
					break;
				    }
                            }
			}

			if (url == "") {
			    url      = get_file_url(filename, lang_id);
			}
		    }

            	    window.onload = delayedredirect;
            }
}

function delayedredirect() {
     setTimeout("redirect()",redirect_delay);
}
function redirect() {
     window.location = url;
}

function write_download_header() {
	document.write('<p>');
	document.write(AUTOMATIC_DOWNLOAD_MESSAGE.replace('{0}',url));
	document.write('</p>');
}

function useBouncer(lang_id) {
        return get_build_info(isMainLanguage(lang_id)).USE_BOUNCER == 1;
}

function write_download_info() {
	var size = getSize(filename,lang_id);
	var md5 = getMD5(filename,lang_id);
	var platform_display_name = getPlatformShortName(platform_id);
	var lang_display_name     = getLanguageName(lang_id);
    var option_display_name   = getBundleShortName(option_id);
    var short_name = get_file_name_short(platform_id,option_id,lang_id, bits_id);

	/* format size */
	mb = Math.floor(size / (1024 * 1024));
	mb_dec = Math.floor((size - (mb * 1024 * 1024))/ (1024 * 102));
        size = mb + ((mb_dec>0) ? ('.' + mb_dec) : '');

	document.write('<br>');
        document.write('<p class="file_information">');

        var info = "";
	if (platform_display_name!="" && lang_display_name!="" && filename!="") {
		 info = INFO_MESSAGE.
				replace('{0}', PRODUCT_NAME.replace('{0}',get_build_info(lang_id).BUILD_DISPLAY_VERSION)).
		 		replace('{1}', ((option_display_name != "") ? (' ' + option_display_name) : '')).
		 		replace('{2}', ((platform_id == 'zip') ? (platform_display_name) : (INSTALLER_MESSAGE.replace('{0}',platform_display_name)))).
		 		replace('{3}', lang_display_name).
		 		replace('{4}', lang_id).
		 		replace('{5}', short_name).
				replace('{6}', size).
		 		replace('{7}', md5);
    	} else if(filename!="") {
		var filename_short = filename.substring(filename.lastIndexOf("/") + 1, filename.length);
		info = INFO_MESSAGE_OTHER.
		 		replace('{0}', filename_short).
				replace('{1}', size).
		 		replace('{2}', md5);
	} else {
		info = NOFILE_MESSAGE;
	}
        document.write(info);
	document.write('</p>');
}
