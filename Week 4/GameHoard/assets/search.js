if(typeof $ != "undefined") {
    $(document).ready(function() {
        Native.logString("jquery loaded!");
    });
} else {
    Native.logString("no jquery... :(");
}

function sendString () {
	var searchText = document.getElementById("searchField");
	var string = searchText.value;
	Native.logString(string);
}
