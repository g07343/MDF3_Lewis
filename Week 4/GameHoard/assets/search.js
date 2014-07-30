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
    });
} else {
	//stupid jquery didn't get loaded for some reason
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
	} else {
  		Native.logString("NOT A VALID URL!");
	}
}