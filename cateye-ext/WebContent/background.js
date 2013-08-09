

chrome.browserAction.onClicked.addListener(function(tab) {
  var url = tab.url;
  if(url){
	  var xhr = new XMLHttpRequest();
	  xhr.open("GET", "http://www.baidu.com", true);
	  xhr.onreadystatechange = function() {
//	    if (xhr.readyState == 4) {
	    	var resp = xhr.responseText;
	    	var uur = "http://hc.yinyuetai.com/uploads/videos/common/975301405913C0C51A6D22F3ACC19702.flv?sc=4f36ab9b5c90e204&br=778";
	    	chrome.downloads.download({url: uur},function(id){});
//	    }
	  };
	  xhr.send();
  }
});