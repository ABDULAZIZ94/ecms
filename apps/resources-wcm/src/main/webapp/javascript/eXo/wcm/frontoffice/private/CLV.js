function ContentListViewer() {}

ContentListViewer.prototype.initCheckedRadio = function(id) {
	eXo.core.Browser.chkRadioId = id;
};

ContentListViewer.prototype.initCondition = function(formid) {
	var formElement = document.getElementById(formid);
	var radioboxes = [];
	for(var i=0; i < formElement.elements.length;i++){
		if(formElement.elements[i].type=="radio") radioboxes.push(formElement.elements[i]);
	}
	var i = radioboxes.length;
	while(i--){
		radioboxes[i].onclick = eXo.ecm.CLV.chooseCondition;
	}
	if(eXo.core.Browser.chkRadioId && eXo.core.Browser.chkRadioId != "null"){
		var selectedRadio = document.getElementById(eXo.core.Browser.chkRadioId);
	} else{		
		var selectedRadio = radioboxes[0];
	}
	var itemSelectedContainer = gj(selectedRadio).parents(".ContentSearchForm:first")[0];
	var itemContainers = gj(selectedRadio.form).find("div.ContentSearchForm");
	for(var i = 0 ;i < itemContainers.length; i++){
		eXo.ecm.CLV.setCondition(itemContainers[i], true);
	}
	eXo.ecm.CLV.enableCondition(itemSelectedContainer);
};

ContentListViewer.prototype.chooseCondition = function() {
	var me = this;
	var hiddenField = gj(me.form).find("input.hidden:first")[0];
	hiddenField.value = me.id;
	var itemSelectedContainer = gj(me).parents(".ContentSearchForm:first")[0];
	var itemContainers = gj(me.form).find("div.ContentSearchForm");
	for(var i=0;i<itemContainers.length;i++){
		eXo.ecm.CLV.setCondition(itemContainers[i],true);
	}
	eXo.ecm.CLV.enableCondition(itemSelectedContainer);
	eXo.ecm.lastCondition = itemSelectedContainer; 
};

ContentListViewer.prototype.enableCondition = function(itemContainer) {
	if(eXo.ecm.lastCondition) eXo.ecm.CLV.setCondition(eXo.ecm.lastCondition,true);
	eXo.ecm.CLV.setCondition(itemContainer,false);
};

ContentListViewer.prototype.setCondition = function(itemContainer,state) {
	var action = gj(itemContainer).find("img");
	if(action && action.length > 0){
		for(var i = 0; i < action.length; i++){
			if(state) {
				action[i].style.visibility = "hidden";
			}	else {
				action[i].style.visibility = "";	
			}	
		}
	}
	
	var action = gj(itemContainer).find("input");
	if(action && (action.length > 0)){
		for(i = 0; i < action.length; i++){
			if(action[i].type != "radio") action[i].disabled = state;
		}
	}
	
	var action = gj(itemContainer).find("select");
	if(action && (action.length > 0)){
		for(i = 0; i < action.length; i++){
			action[i].disabled = state;
		}
	}
};

ContentListViewer.prototype.setHiddenValue = function() {
	var inputHidden = document.getElementById("checkedRadioId");
	if(eXo.core.Browser.chkRadioId == "null") {
		inputHidden.value = "name";
		document.getElementById("name").checked = true;
	} else {
		inputHidden.value = eXo.core.Browser.chkRadioId; 
		document.getElementById(eXo.core.Browser.chkRadioId).checked = true;
	}
};

ContentListViewer.prototype.checkModeViewer = function() {
	var formObj = document.getElementById("UICLVConfig");
	var OrderOptions = gj(formObj).find("tr.OrderBlock");
	var viewerModes = gj(formObj).find("input");
	for(var i = 0; i < viewerModes.length; i++) {
		if(viewerModes[i].getAttribute("name") == "ViewerMode") {
			if(viewerModes[i].value == "AutoViewerMode") {
				viewerModes[i].onclick = function() {
					for(var j = 0; j < OrderOptions.length; j++) {
						OrderOptions[j].style.display = "";
					}
				};
			} else if(viewerModes[i].value == "ManualViewerMode") {
				viewerModes[i].onclick = function() {
					for(var k = 0; k < OrderOptions.length; k++) {
						OrderOptions[k].style.display = "none";
					}
				};
			}
		}
	}
};

ContentListViewer.prototype.checkContextualFolderInput = function() {
	var formObj = document.getElementById("UICLVConfig");
	var tdContextualFolder = gj(formObj).find("td.ContextualRadio")[0];

	var inputs = gj(tdContextualFolder).children("input");
	var enableInput = inputs[0];
	var disableInput = inputs[1];
	
	var trContextual = gj(tdContextualFolder).parents("tr:first")[0];
	var trClv = gj(trContextual).nextAll("tr:first")[0];
	
	var clvInput = gj(trClv).find("input")[0];

	enableInput.setAttribute("onmouseup", "eXo.ecm.CLV.enableClvInput(this)");
	disableInput.setAttribute("onmouseup", "eXo.ecm.CLV.disableClvInput(this)");
	if (enableInput.checked) {
		clvInput.removeAttribute('readonly');
	} else {
		clvInput.setAttribute('readonly', '');
	}
};

ContentListViewer.prototype.enableClvInput = function(obj){
	var trContextual = gj(obj).parents("tr:first")[0];
	var trClv = gj(trContextual).nextAll("tr:first")[0];
	var clvInput = gj(trClv).find("input")[0];
	clvInput.removeAttribute('readonly');
};

ContentListViewer.prototype.disableClvInput = function(obj){
	var trContextual = gj(obj).parents("tr:first")[0];
	var trClv = gj(trContextual).nextAll("tr:first")[0];
	var clvInput = gj(trClv).find("input")[0];
	clvInput.setAttribute('readonly', '');
};

ContentListViewer.prototype.addURL = function(aDiv) {
  var strHref = aDiv.getAttribute("href");
  var fIdx = strHref.indexOf("&backto");
  if (fIdx < 0 ) fIdx = strHref.indexOf("?backto");
  if (fIdx<0) return;
  var lIdx = strHref.indexOf("&", fIdx+1);
  var lString ="";
  var fString =strHref;
  if (lIdx >0) {
    lString = strHref.substr(lIdx);
    fString = strHref.substr(0, lIdx);
  }
  strHref = fString + escape(location.search)  + lString;
  aDiv.setAttribute("href", strHref);
}

eXo.ecm.CLV = new ContentListViewer();