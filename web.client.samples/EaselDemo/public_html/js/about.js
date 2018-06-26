/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

function loadRest() {
    url = "rest/resources/manufacturer.json";
    $.getJSON(url, function(data) {
        content = "<div id='restDataList'><table border=0><tr>";
        for (i = 0; i < 4; i++) {
            content += "<td><a id='listItem' onclick='showDetails(" + data[i].manufacturerId + ")' class='btn btn-primary'>" + data[i].name + " &raquo;</a></td></tr>";
        }
        content += "</table></div>";
        $("#rest").html(content);
    });
}

function showDetails(id) {
    url = "rest/resources/id" + id+".json";
    $.getJSON(url, function(data) {
       $("#restName").text(data.name);
       $("#restRep").text(data.rep);
       $("#restEmail").text(data.email);
       $("#restPhone").text(data.phone);
    });
    dataSwitch();
}

function dataSwitch() {
    $('#restDataList').toggle(700, 'easeOutExpo');
    $('#restDetails').toggle(700, 'easeOutExpo');
}

// This is the jQuery animation code
$(document).ready(function() {
	$("#start").click(function() {
		$("#animation-div").animate({
			height:200,
			opacity: 0.0
		}, 500);
		$("#animation-div").animate({
			width:200,
			opacity: 1.0
		}, 500);
		$("#animation-div").animate({height:100}, 500);
		$("#animation-div").animate({width:100}, 500);
	});
	
	$('#switch').click(function() {
		$('#blue_div').toggle('slow', 'easeOutExpo');
		$('#red_div').toggle('slow', 'easeOutExpo');
	});
	
	/* Add extra jQuery examples here */

});

function loadWeather() {
    $.getJSON('http://api.openweathermap.org/data/2.5/find?q=Prague,cz&units=metric', function(data) {
        var result = data.list[0];
       $("#tweetImg").html("<img class='avatar' src='http://openweathermap.org/img/w/"+result.weather[0].icon+".png'/>");

       $("#tweetUsername").html(result.main.temp+" &deg;C");
       $("#tweetUser").text("Current weather in Prague");
       $("#tweetText").html(result.weather[0].main+"<br/>Humidity: "+result.main.humidity+" %<br/>Max. temperature: "+result.main.temp_max+" &deg;C<br/>Min. temperature: "+result.main.temp_min+" &deg;C<br/>");
    });
}

// This is the Weather Popup code section
$(document).ready(function() {
	$('a.tweet-window').click(function() {
		
		// Loading the data from a REST call
                loadWeather();

		//Getting the variable's value from a link 
		var tweetBox = $(this).attr('href');

		//Fade in the Popup
		$(tweetBox).fadeIn(300);

		//Set the center alignment padding + border see css style
		var popMargTop = ($(tweetBox).height() + 24) / 2;
		var popMargLeft = ($(tweetBox).width() + 24) / 2;

		$(tweetBox).css({
			'margin-top' : -popMargTop,
			'margin-left' : -popMargLeft
		});

		// Add the mask to the body
		$('body').append('<div id="mask"></div>');
		$('#mask').fadeIn(300);

		return false;
	});

	// When clicking on the close button or the mask layer the popup is closed
	$('a.tweet-close, #mask').live('click', function() {
		$('#mask , .tweet-popup').fadeOut(300, function() {
			$('#mask').remove();
		});
		return false;
	});
});
