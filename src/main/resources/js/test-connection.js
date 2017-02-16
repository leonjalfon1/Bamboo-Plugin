(function ($) {
    $(document).on("click", "#test_connection", function (event) {
        restRequest();
    });

    $(document).on("click", "#global_test_connection", function (event) {
        restRequest();
    });

    function restRequest() {
        var request;
        if ($('#radioGroupglobalConfigurationServer').is(':checked')) {
            if (!validateGlobalFields()) {
                return;
            }
            request = JSON.stringify(getGlobalInputData());

        } else {
            if (!validateFields()) {
                return;
            }

            request = JSON.stringify(getInputData());
        }



        function createRestRequest(method, url) {

            var urli = AJS.contextPath() + url;

            var xhr = new XMLHttpRequest();
            if ("withCredentials" in xhr) {
                xhr.open(method, urli, true);

            } else if (typeof XDomainRequest != "undefined") {
                xhr = new XDomainRequest();
                xhr.open(method, url);
            } else {
                xhr = null;
            }
            return xhr;
        }

        var xhr = createRestRequest("POST", "/rest/checkmarx/1.0/test/connection");
        if (!xhr) {
            console.log("Request Failed");
            return;
        }

        xhr.onload = function () {
            var parsed = JSON.parse(xhr.responseText);
            var testConnectionMessage;
            if ($('#radioGroupglobalConfigurationServer').is(':checked')) {
                testConnectionMessage =  $('#globalTestConnectionMessage');
            }

            else {
                testConnectionMessage =$('#testConnectionMessage');
            }

            if (xhr.status == 200) {
                testConnectionMessage.css('color', 'green');
            }
            else {
                testConnectionMessage.css('color', '#d22020');
            }
            populateDropdownList(parsed.presetList, "#presetListId", "id", "value");
            populateDropdownList(parsed.teamPathList, "#teamPathListId", "id", "value");
            testConnectionMessage.html(parsed.loginResponse);
        };


        xhr.onerror = function () {
            console.log('There was an error!');
        };

        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(request);
    }

    function validateFields() {

        var messageElement = $('#testConnectionMessage');
        if ($('#serverUrl').val().length < 1) {
            messageElement.text('URL must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        } else if ($('#userName').val().length < 1) {
            messageElement.text('Username must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        } else if ($('#password').val().length < 1) {
            messageElement.text('Password must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        }

        return true;
    }

    function validateGlobalFields() {

        var messageElement = $('#globalTestConnectionMessage');
        if ($('#globalServerUrl').html().length < 1){
            messageElement.text('URL must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        } else if ($('#globalUserName').html().length < 1) {
            messageElement.text('Username must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        }
        return true;
    }

    function getInputData() {
        return {
            "url": $("#serverUrl").val(),
            "username": $('#userName').val(),
            "pas": $('#password').val(),
            "global": $('#radioGroupglobalConfigurationServer').is(':checked')
        };
    }

    function getGlobalInputData() {
        return {
            "url": $("#globalServerUrl").html(),
            "username": $('#globalUserName').html(),
            "global": $('#radioGroupglobalConfigurationServer').is(':checked')
        };
    }


    function populateDropdownList(data, selector, key, name) {
        $(selector).empty();
        for (var i in data) {
            if (i == 0) // select first item
                var itemval = '<option value="' + data[i][key] + '" selected>' + data[i][name] + '</option>';
            else
                var itemval = '<option value="' + data[i][key] + '">' + data[i][name] + '</option>';
            $(selector).append(itemval);
        }

    }

})
(AJS.$);
