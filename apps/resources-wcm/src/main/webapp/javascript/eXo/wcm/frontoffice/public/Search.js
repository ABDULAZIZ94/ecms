function SearchPortlet() {
}

SearchPortlet.prototype.showObject = function(obj) {
	var element = gj(obj).nextAll("div:first")[0];
	if (!element.style.display || element.style.display != 'block') {
		element.style.display = 'block';
	} else {
		element.style.display = 'none';
	}
};

SearchPortlet.prototype.getRuntimeContextPath = function() {
	return eXo.ecm.WCMUtils.getHostName() + eXo.env.portal.context + '/' + eXo.env.portal.portalName + '/';
};

SearchPortlet.prototype.getKeynum = function(event) {
	var keynum = false;
	if (window.event) { /* IE */
		keynum = window.event.keyCode;
		event = window.event;
	} else if (event.which) { /* Netscape/Firefox/Opera */
		keynum = event.which;
	}
	if (keynum == 0) {
		keynum = event.keyCode;
	}
	return keynum;
};

SearchPortlet.prototype.quickSearchOnEnter = function(event, resultPageURI) {
	var keyNum = eXo.ecm.SearchPortlet.getKeynum(event);
	if (keyNum == 13) {
		var searchBox = document.getElementById("siteSearchBox");
		var keyWordInput = gj(searchBox).find("input.keyword:first")[0];
		var keyword = encodeURI(keyWordInput.value);
		/*
		Filter user input on client
		- Escape markup on the client
    - Remove eval(), javascript, and script from client
		*/
		keyword = keyword.replace(/</g, "&lt;").replace(/>/g, "&gt;");
    keyword.replace(/[\"\'][\s]*javascript:(.*)[\"\']/gi, "\"\"");
    keyword = keyword.replace(/script(.*)/gi, "");
    keyword = keyword.replace(/eval\((.*)\)/gi, "");
		var resultPageURIDefault = "searchResult";
		var params = "portal=" + eXo.env.portal.portalName + "&keyword=" + keyword;
		var baseURI = eXo.ecm.WCMUtils.getHostName() + eXo.env.portal.context + "/" + eXo.env.portal.portalName;
		if (resultPageURI != undefined) {
			baseURI = baseURI + "/" + resultPageURI;
		} else {
			baseURI = baseURI + "/" + resultPageURIDefault;
		}
		window.location = baseURI + "?" + params;
	}
};

SearchPortlet.prototype.search = function(comId) {
	var searchForm = document.getElementById(comId);
	var inputKey = gj(searchForm).find("#keywordInput:first")[0];
	searchForm.onsubmit = function() {
		return false;
	};
	inputKey.onkeypress = function(event) {
		var keyNum = eXo.ecm.SearchPortlet.getKeynum(event);
		if (keyNum == 13) {
			var searchButton = gj(this.form).find("a.SearchButton:first")[0];
			searchButton.onclick();
			var href = searchButton.getAttribute('href');
			eval(href);
		}
	}
};

SearchPortlet.prototype.keepKeywordOnBoxSearch = function() {
	var queryRegex = /^portal=[\w%]+&keyword=[\w%]+/;
	var searchBox = document.getElementById("siteSearchBox");
	var keyWordInput = gj(searchBox).find("input.keyword:first")[0];
	var queryString = location.search.substring(1);
	if (!queryString.match(queryRegex)) {
		return;
	}
	var portalParam = queryString.split('&')[0];
	var keyword = decodeURI(queryString
			.substring((portalParam + "keyword=").length + 1));
	if (keyword != undefined && keyword.length != 0) {
		keyWordInput.value = unescape(keyword);
	}
	var uiSearchForm = document.getElementById("UISearchForm");
	if (uiSearchForm != null) {
		var inputKeyInForm = gj(uiSearchForm).find("#keywordInput:first")[0];
		if ((inputKeyInForm != null) && (inputKeyInForm.value.length != 0)) {
			keyWordInput.value=inputKeyInForm.value;
		}
	}
};

SearchPortlet.prototype.scrollToBottom = function(divId) {
	var divBlock = document.getElementById(divId);
	if (divBlock)
		divBlock.scrollTop = divBlock.scrollHeight;
};

SearchPortlet.prototype.changeColor = function(divId, count) {
	if (!count)
		count = 0;

	var basicColor = 219;
	var appliedColor = basicColor + count;

	var divBlock = document.getElementById(divId);
	if (divBlock) {
		divBlock.style.backgroundColor = "#" + appliedColor.toString(16) + appliedColor.toString(16) + appliedColor.toString(16);
		if (appliedColor < 255) {
			setTimeout("eXo.ecm.SearchPortlet.changeColor('" + divId + "'," + (count + 2) + ")", 100);
		}
	}
};

eXo.ecm.SearchPortlet = new SearchPortlet();
_module.SearchPortlet = eXo.ecm.SearchPortlet;
