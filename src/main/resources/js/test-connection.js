(function ($) {
    $(document).on("click", "#test_connection", function (event) {
        restRequest();
        //  hideAuthenticationResult();
        //  populateDropdownLists();
        // event.preventDefault();
    });

    function restRequest() {

        if(!validateFields()) {
            return;
        }

        var str = JSON.stringify(getInputData());
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
            var result = xhr.responseText;

            var testConnectionMessage = document.getElementById("testConnectionMessage");
            if (xhr.status == 200) {
                testConnectionMessage.style.color = "green";
            }
            else {
                testConnectionMessage.style.color = "#d22020";
            }
            testConnectionMessage.innerHTML = result;
        };


        xhr.onerror = function () {
            console.log('There was an error!');
        };

        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(str);
    }

    function validateFields() {

        var messageElement = $('#testConnectionMessage');
        if($('#serverUrl').val().length < 1) {
            messageElement.text('URL must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        } else if($('#userName').val().length < 1) {
            messageElement.text('Username must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        } else if($('#password').val().length < 1) {
            messageElement.text('Password must not be empty');
            messageElement.css('color', '#d22020');
            return false;
        }

        return true;
    }


    function getInputData() {
        return {
            "url": $("#serverUrl").val(),
            "username": $('#userName').val(),
            "password": $('#password').val()

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

})(AJS.$);
