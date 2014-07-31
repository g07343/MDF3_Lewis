
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

        //repopulate fields to anything that was saved
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
  		Native.showPreview(url, int);

  		//depending on the int passed, set to particular index in our array
  		if (int == 1) {
  			urls[0] = url;
  		} else if (int == 2) {
  			urls[1] = url;
  		} else if (int ==3) {
  			urls[2] = url;
  		} else {
  			urls[3] = url;
  		}
	} else {
  		Native.logString("NOT A VALID URL!");
  		//since url is invalid, ensure that the corresponding variable is set to null
  		if (int == 1) {
  			urls[0] = null;
  		} else if (int == 2) {
  			urls[1] = null;
  		} else if (int ==3) {
  			urls[2] = null;
  		} else {
  			urls[3] = null;
  		}
	}
}

function saveInput () {
	Native.saveToDevice(urls);

	Native.logString(urls.toString());
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