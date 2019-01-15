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
