(function ($) {
    $(document).on("click", "#test_connection", function (event) {
        $('#testConnectionMessage').html("");
        restRequest();
    });

    function restRequest() {

        if (!validateFields()) {
            return populateEmptyDropdownList();
        }
        var request = JSON.stringify(getInputData());


        function createRestRequest(method, url) {

            var resolvedUrl = AJS.contextPath() + url;

            var xhr = new XMLHttpRequest();
            if ("withCredentials" in xhr) {
                xhr.open(method, resolvedUrl, true);

            } else if (typeof XDomainRequest != "undefined") {
                xhr = new XDomainRequest();
                xhr.open(method, resolvedUrl);
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
            if (xhr.status == 200) {
                $('#testConnectionMessage').css('color', 'green');
                populateDropdownList(parsed.presetList, "#presetListId", "id", "value");
                populateDropdownList(parsed.teamPathList, "#teamPathListId", "id", "value");
            }
            else {
                $('#testConnectionMessage').css('color', '#d22020');
                populateEmptyDropdownList();
            }
            $('#testConnectionMessage').html(parsed.loginResponse);
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
        } else if ($('#username').val().length < 1) {
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

    function getInputData() {
        return {
            "url": $("#serverUrl").val(),
            "username": $('#username').val(),
            "pas": $('#password').val()
        };
    }

    function populateEmptyDropdownList() {
        var NO_PRESET_MESSAGE = "Unable to connect to server. Make sure URL and Credentials are valid to see presets list";
        var NO_TEAM_MESSAGE = "Unable to connect to server. Make sure URL and Credentials are valid to see teams list";
        var noPresetList = [{id: 'noPreset', value: NO_PRESET_MESSAGE}];
        var noTeamList = [{id: 'noTeamPath', value: NO_TEAM_MESSAGE}];
        populateDropdownList(noPresetList, "#presetListId", "id", "value");
        populateDropdownList(noTeamList, "#teamPathListId", "id", "value");
        return;

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
