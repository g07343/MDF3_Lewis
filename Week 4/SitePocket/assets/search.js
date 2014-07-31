/*
Author Matthew Lewis

Project SitePocket

Purpose Search.js provides the javascript logic for user interaction within the web (html) portion of the interface

*/

//create an array to hold the values
var urls = [null, null, null, null];


if(typeof $ != "undefined") {
    $(document).ready(function() {
        Native.logString("jquery loaded!");
        

        //grab each of our url inputs and set listeners for when they lose focus
        $("#searchField1").focusout( function ()  {
        	//grab entered value from the text view
        	var firstUrl = $("#searchField1").val();
        	//check it to ensure it is a valid url
        	checkUrl(firstUrl, 1);     	
        });

        $("#searchField2").focusout(function () {
        	//grab entered value from the text view
        	var secondUrl = $("#searchField2").val();
        	
        		//check it to ensure it is a valid url
        	checkUrl(secondUrl, 2);       	
        });

        $("#searchField3").focusout(function () {
        	//grab entered value from the text view
        	var thirdUrl = $("#searchField3").val();

        	//check it to ensure it is a valid url
        	checkUrl(thirdUrl, 3);
        });
       
        $("#searchField4").focusout(function () {
        	//grab entered value from the text view
        	var fourthUrl = $("#searchField4").val();

        	//check it to ensure it is a valid url
        	checkUrl(fourthUrl, 4);
        });

        //add a listener for when the user taps the "save to device" button
        $("#saveBtn").click(function() {
        	saveInput();
        });

        //add listeners to our four invisible hotspots, that sit over the webview preview tiles
        $("#hotspot1").click(function() {
          launchIntent(1);
        });

        $("#hotspot2").click(function() {
          launchIntent(2);
        });

        $("#hotspot3").click(function() {
          launchIntent(3);
        });

        $("#hotspot4").click(function() {
          launchIntent(4);
        });

        //repopulate fields to anything that was saved (via native methods/callback)
        populateData();


    });
} else {
	//jquery didn't get loaded for some reason
    Native.logString("no jquery... :(");
}

function sendString () {
	var searchText = document.getElementById("searchField");
	var string = searchText.value;
	Native.logString(string);
}

function checkUrl (url , int) {
	//the following regex logic is from the official Jquery Validation Plugin found at: http://jqueryvalidation.org/
	//Decided to include the one line instead of the entire plugin, since this is all that's needed
	if(/^([a-z]([a-z]|\d|\+|-|\.)*):(\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?((\[(|(v[\da-f]{1,}\.(([a-z]|\d|-|\.|_|~)|[!\$&'\(\)\*\+,;=]|:)+))\])|((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=])*)(:\d*)?)(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*|(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)|((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)|((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)){0})(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(url)) {
  		Native.logString("VALID!");
  		
      //update the correct preview pane
      Native.showPreview(url, int);

  		//depending on the int passed, set to particular index in our array and ensure the correct one is made visible,
  		//and set the corresponding placeholder image to not be visible
  		if (int == 1) {
  			urls[0] = url;
  			Native.togglePreview("1", "VISIBLE");
  			toggleEmpty(1, "NULL");
  		} else if (int == 2) {
  			urls[1] = url;
  			Native.togglePreview("2", "VISIBLE");
  			toggleEmpty(2, "NULL");
  		} else if (int == 3) {
  			urls[2] = url;
  			Native.togglePreview("3", "VISIBLE");
  			toggleEmpty(3, "NULL");
  		} else {
  			urls[3] = url;
  			Native.togglePreview("4", "VISIBLE");
  			toggleEmpty(4, "NULL");
  		}
	} else {
  		Native.logString("NOT A VALID URL!");
  		//since url is invalid, ensure that the corresponding variable is set to null, and toggle visibility of 
  		//preview webviews and placeholder images
  		if (int == 1) {
  			urls[0] = null;
  			Native.togglePreview("1", "NULL");
  			toggleEmpty(1, "VISIBLE");
  		} else if (int == 2) {
  			urls[1] = null;
  			Native.togglePreview("2", "NULL");
  			toggleEmpty(2, "VISIBLE");
  		} else if (int == 3) {
  			urls[2] = null;
  			Native.togglePreview("3", "NULL");
  			toggleEmpty(3, "VISIBLE");
  		} else {
  			urls[3] = null;
  			Native.togglePreview("4", "NULL");
  			toggleEmpty(4, "VISIBLE");
  		}
	}
}

//this function simply passes our data to native code to save out to the device
function saveInput () {
	Native.saveToDevice(urls);

}

//grab any saved data from Native one by once since getting an array back is apparently very difficult
function populateData() {
	urls[0] = Native.returnSaved("1");
	urls[1] = Native.returnSaved("2");
	urls[2] = Native.returnSaved("3");
	urls[3] = Native.returnSaved("4");
	//alert(urls);

	if (urls[0] != "null") {
		$("#searchField1").val(urls[0]);
		checkUrl(urls[0], 1);
	}

	if (urls[1] != "null") {
		$("#searchField2").val(urls[1]);
		checkUrl(urls[1], 2);
	}

	if (urls[2] != "null") {
		$("#searchField3").val(urls[2]);
		checkUrl(urls[2], 3);
	}

	if (urls[3] != "null") {
		$("#searchField4").val(urls[3]);
		checkUrl(urls[3], 4);
	}
}

//this function nearly mirrors the native "togglePreview" method and is used to toggle the placeholder images
//when there is either no entered url, or an incorrect url was entered
function toggleEmpty (int, intention) {
	if (intention == "VISIBLE") {
		if (int == 1) {
			$("#empty1").removeClass("hidden");
      $("#hotspot1").addClass("hidden");
		} else if (int == 2) {
			$("#empty2").removeClass("hidden");
      $("#hotspot2").addClass("hidden");
		} else if (int == 3) {
			$("#empty3").removeClass("hidden");
      $("#hotspot3").addClass("hidden");
		} else {
			$("#empty4").removeClass("hidden");
      $("#hotspot4").addClass("hidden");
		}
	} else {
		if (int == 1) {
			$("#empty1").addClass("hidden");
      $("#hotspot1").removeClass("hidden");
		} else if (int == 2) {
			$("#empty2").addClass("hidden");
      $("#hotspot2").removeClass("hidden");
		} else if (int == 3) {
			$("#empty3").addClass("hidden");
      $("#hotspot3").removeClass("hidden");
		} else {
			$("#empty4").addClass("hidden");
      $("#hotspot4").removeClass("hidden");
		}
	}
}


//this function launches an intent through native code when the user taps one of the invisible overlays
function launchIntent(int) {
  var actualInt = int -1;
  var launchUrl = urls[actualInt];

  //send the string of the selected hotspot to Native code to be launched as intent
  Native.launchIntent(launchUrl);
}
