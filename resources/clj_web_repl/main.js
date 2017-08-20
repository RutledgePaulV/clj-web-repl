function eval_clojure(code) {
    var endpoint = window.__root__ + "/exec";
    var data;
    $.ajax({
        url: endpoint,
        method: "POST",
        data: JSON.stringify({expr: code}),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        async: false,
        success: function (res) {
            data = res;
        }
    });
    return data;
}

function html_escape(val) {
    var result = val;
    result = result.replace(/\n/g, "<br/>");
    result = result.replace(/[<]/g, "&lt;");
    result = result.replace(/[>]/g, "&gt;");
    return result;
}

function doCommand(input) {
    console.log("I don't know any commands.");
}

function onValidate(input) {
    return (input !== "" && input.trim() !== "");
}

function onHandle(line, report) {
    var input = $.trim(line);

    // handle commands
    if (doCommand(input)) {
        report();
        return;
    }

    // perform evaluation
    var data = eval_clojure(input);

    // handle error
    if (data.error) {
        return [{msg: data.message, className: "jquery-console-message-error"}];
    }

    // display expr results
    return [{msg: data.result, className: "jquery-console-message-value"}];
}

$(document).ready(function () {

    var controller = $("#console").console({
        welcomeMessage: 'Give me some Clojure:',
        promptLabel: '> ',
        commandValidate: onValidate,
        commandHandle: onHandle,
        autofocus: true,
        animateScroll: true,
        promptHistory: true
    });

});