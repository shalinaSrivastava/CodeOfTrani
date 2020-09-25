var trapi = {};
trapi.isLoading = false;
trapi.apiVersion = .9;
trapi.lmsLogURI = "";
trapi.licenseId = "";
trapi.parameters = "";
trapi.version = "1.22";
trapi.debug = false;
trapi.sessionID = "";
trapi.href = "";
trapi.cache = {};
trapi.lastFunction = "";
trapi.ajaxTimeout = 6E4;
trapi.CONTEXT_PATH = "";
trapi.courseCompleted = function () {
    window.location.replace(trainor.contextPath + "/scorm/completed/" + trapi.licenseId)
}
;
trapi.toMyPage = function() {
    window.location.replace(trainor.contextPath + "/admin#/mypage")
}
;
trapi.getFromCache = function(key) {
    return trapi.cache[key]
}
;
trapi.getParameterByName = function(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regexS = "[\\?\x26]" + name + "\x3d([^\x26#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    if (results == null) {
        results = regex.exec(window.top.location.search);
        if (results == null)
            return ""
    }
    return decodeURIComponent(results[1].replace(/\+/g, " "))
}
;
trapi.navToDecode = function (uri) {
    //debugger
    var u = decodeURIComponent(uri);
    if (u.indexOf("../") != -1)
        u = u.substring(2);
    trapi.logToConsole("setting top.location\x3d" + u)
}
;
trapi.getURLParameter = function (name) {
    //debugger
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split("\x26");
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split("\x3d");
        if (sParameterName[0] == sParam)
            return sParameterName[1]
    }
}
;
trapi.logToConsole = function(msg) {
    if (!trapi.debug)
        return;
    if (console.log)
        try {
            console.log(msg + "\n")
        } catch (ignored) {}
}
;
trapi.init = function() {
    var log = trapi.logToConsole;
    log("Starter APIVersion: " + trapi.apiVersion);
    log("Variables:");
    log("trapi.apiVersion:" + trapi.apiVersion);
    log("trapi.lmsLogURI:" + trapi.lmsLogURI);
    log("trapi.courseResourceID:" + trapi.courseResourceID);
    log("trapi.courseID:" + trapi.courseID);
    log("trapi.parameters:" + trapi.parameters);
    log("trapi.version:" + trapi.version);
    log("trapi.debug:" + trapi.debug);
    log("trapi.sessionID:" + trapi.sessionID)
}
;
trapi.destroy = function() {
    trapi.logToConsole("trapi.destroy()")
}
;
trapi.postObjects = function (f, parameter, value, variables, isLastError) {
    var url = trapi.CONTEXT_PATH + "/data/scorm/" + f + "/" + trapi.sessionID;
    var post = {};
    post["f"] = f;
    post["itemid"] = trapi.itemId;
    post["licenseid"] = trapi.licenseId;
    if (parameter != null)
        post["parameter"] = parameter;
    if (value != null)
        post["value"] = value;
    if (variables != null)
        for (var i = 0; i < variables.length; i++)
            post[variables[i][0]] = variables[i][1];
    var returnString = "";
    $.ajax(url, {
        data: post,
        cache: false,
        global: false,
        dataType: "json",
        type: "POST",
        async: false,
        success: function(data) {
            returnString = data.response;
            if (data.cache)
                trapi.cache = data.cache;
           trapi.lastError = data.lastError
        },
        error: function(jqXHR, textStatus, errorThrown) {
            trapi.logToConsole("ERROR: " + textStatus);
            returnString = undefined;
            trapi.lastError = "102";
            if (isLastError)
                returnString = "102"
        }
    });
    trapi.lastFunction = f;
    trapi.logToConsole("RESPONSE FROM SERVER:" + returnString);
    return returnString
};

trapi.Initialize = function(parameter) {
    trapi.logToConsole("trapi.Initialize(" + parameter + ")");
    return "ok";
    //return trapi.postObjects("initialize", parameter, null, null)
};

trapi.LMSInitialize = function(parameter) {
    trapi.logToConsole("trapi.LMSInitialize(" + parameter + ")");
    return "ok";
    //return trapi.Initialize(parameter)
};

trapi.GetValue = function (parameter) {
    debugger;    
    trapi.logToConsole("trapi.GetValue(" + parameter + ")");
    var value = trapi.getFromCache(parameter);
	value = getSCORMStatus(parameter);
    if (value === undefined)
        return trapi.postObjects("getvalue", parameter, null, null);
    else {
        trapi.lastError = "0";
        return value
    }
};
trapi.LMSGetValue = function (parameter) {
    //alert("lov get method called");
    return trapi.GetValue(parameter)
};
trapi.SetValue = function (parameter, value) {
    updateSCORMStatus(parameter,value);
    trapi.logToConsole("trapi.SetValue(" + parameter + "," + value + ")");
    return trapi.postObjects("setvalue", parameter, value, null)
};
trapi.LMSSetValue = function (parameter, value) {
    trapi.logToConsole("trapi.LMSSetValue(" + parameter + "," + value + ")");
    return trapi.postObjects("setvalue", parameter, value, null)
};
trapi.Commit = function (parameter) {
    trapi.logToConsole("trapi.Commit(" + parameter + ")");
    return trapi.postObjects("commit", parameter, null, null)
};
trapi.LMSCommit = function (parameter) {
    //debugger;
    alert("trapi.LMSCommit(" + parameter + ")");
    trapi.logToConsole("trapi.LMSCommit(" + parameter + ")");
    return trapi.postObjects("commit", parameter, null, null)
};
trapi.Terminate = function(parameter) {
    if (trapi.isLoading)
        return "true";
    var ret = trapi.postObjects("terminate", parameter, null, null);
    if (!ret || ret === undefined)
        return "false";
    else if (ret === "true" || ret === "false")
        return ret;
    else if (ret === "continue")
        trapi.nextSco();
    else if (ret === "prev")
        trapi.prevSco();
    else if (ret === "exit")
        trapi.exitCourse()
};
trapi.LMSFinish = function(parameter) {
    trapi.logToConsole("trapi.LMSFinish(" + parameter + ")");
    return trapi.postObjects("terminate", parameter, null, null)
};
trapi.GetLastError = function() {
    trapi.logToConsole("trapi.GetLastError()");
    return "0";
};
trapi.LMSGetLastError = function() {
    trapi.logToConsole("trapi.LMSGetLastError()");
    return trapi.postObjects("getlasterror", null, null, null, true)
};
trapi.LMSGetErrorString = function(parameter) {
    trapi.logToConsole("trapi.LMSGetErrorString(" + parameter + ")");
    return trapi.postObjects("geterrorstring", parameter, null, null)
};
trapi.GetErrorString = function(parameter) {
    trapi.logToConsole("trapi.GetErrorString(" + parameter + ")");
    return trapi.postObjects("geterrorstring", parameter, null, null)
};
trapi.GetDiagnostic = function(parameter) {
    trapi.logToConsole("trapi.GetDiagnostic(" + parameter + ")");
    return trapi.postObjects("getdiagnostic", parameter, null, null)
};
trapi.LMSGetDiagnostic = function(parameter) {
    trapi.logToConsole("trapi.LMSGetDiagnostic(" + parameter + ")");
   return trapi.postObjects("getdiagnostic", parameter, null, null)
};
trapi.getAppletInfo = function() {
    trapi.logToConsole("trapi.getAppletInfo()");
    return "Trainor SCORM JS implementation"
};
trapi.getParameterInfo = function() {
    trapi.logToConsole("trapi.getParameterInfo()");
    var pinfo = [["param0", "String", ""]];
    return pinfow
};
trapi.Navigate = function(func) {
    alert("navigate");
    trapi.logToConsole("trapi.Navigate(" + func + ")");
    var target = trapi.postObjects("navigate", func, null, null);
    if (target != null && target != "")
        trapi.navToDecode(target)
};
trapi.NavigateTo = function (target) {
    alert("navigate to");
    trapi.logToConsole("trapi.NavigateTo(" + target + ")");
    var targetArray = [["target", target]];
    var navurl = trapi.postObjects("navigatecheck", "courseresourceid", trapi.courseResourceID, targetArray);
    if (navurl != null && navurl != "")
        trapi.navToDecode(navurl);
    trapi.Terminate(null)
};
trapi.SuspendAll = function() {
    trapi.logToConsole("trapi.SuspendAll()");
    trapi.postObjects("suspendall", null, null, null);
    document.close()
};
trapi.nextSco = function () {
    //debugger;
    var ret = trapi.postObjects("nextSco", null, null, null);
    if (ret === "true")
        trapi.frame.trigger("nextSco");
    else
        alert(ret)
};
trapi.prevSco = function() {
    var ret = trapi.postObjects("prevSco", null, null, null);
    if (ret === "true")
        trapi.frame.trigger("prevSco");
    else
        alert(ret)
};
trapi.exitCourse = function() {
    trapi.toMyPage()
};
trapi.async = {};
trapi.async.post = function(uri, postData, callback, terminateCallback) {
    var url = trapi.CONTEXT_PATH + "/data/scorm/async/" + uri + "/" + trapi.sessionID;
    $.ajax(url, {
        data: postData,
        cache: false,
        global: false,
        dataType: "json",
        type: "POST",
        async: true,
        timeout: trapi.ajaxTimeout,
        success: function(data) {
            if (data.cache)
                trapi.cache = data.cache;
            trapi.lastError = data.lastError;
            callback(true);
            if (terminateCallback)
                terminateCallback(data.response)
        },
        error: function(jqXHR, textStatus, errorThrown) {
            trapi.logToConsole("ERROR: " + textStatus);
            trapi.lastError = "102";
            callback(false)
        }
    })
};
trapi.async.setValue = function(keys, values, callback) {
    var postData = {};
    for (var i = 0; i < keys.length; i++)
        postData[keys[i]] = values[i];
    trapi.async.post("setValue", postData, callback)
}
;
trapi.async.terminate = function(callback) {
    trapi.async.post("terminate", "", callback, trapi.async.terminateCallback)
}
;
trapi.async.terminateCallback = function(response) {
    var ret = response;
    if (!ret || ret === undefined)
        return "false";
    else if (ret === "true" || ret === "false")
        return ret;
    else if (ret === "continue")
        trapi.nextSco();
    else if (ret === "prev")
        trapi.prevSco();
    else if (ret === "exit")
        trapi.exitCourse()
}
;
trapi.jsfr = {};
trapi.jsfr.base = trapi.CONTEXT_PATH + "/data/scorm/jsfr";
trapi.jsfr.urls = {
    initialize: function() {
        return trapi.CONTEXT_PATH + "/data/scorm/initialize/" + trapi.sessionID
    },
    getValues: function() {
        return trapi.CONTEXT_PATH + "/data/scorm/jsfr/getValues/" + trapi.sessionID
    },
    setValues: function() {
        return trapi.CONTEXT_PATH + "/data/scorm/jsfr/setValues/" + trapi.sessionID
    },
    terminate: function() {
        return trapi.CONTEXT_PATH + "/data/scorm/terminate/" + trapi.sessionID
    }
};
var API_1484_11 = trapi;
var API = trapi;

function updateSCORMStatus(parameter,value)
{
	JsHandler.updateSCORMStatus(parameter,value);
}

function getSCORMStatus(parameter)
{
	var value = JsHandler.getSCORMStatus(parameter);
	return value;
}




