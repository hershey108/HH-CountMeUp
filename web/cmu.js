/**
 * Processes and updates the voting
 * @param data JSON response containing candidates standings
 */
function updateVoteTable(data) {

    // Get the object keys and sort them. These are the candidate IDs
    var keys = Object.keys(data);
    keys.sort();

    // Prepare our HTML by looping through the keys, getting their vote counts and formatting them.
    var html = "";

    for (var i = 0; i < keys.length; i++) {
        html += '<tr><td class="mdl-data-table__cell--non-numeric">' + keys[i] + '</td><td>' + data[keys[i]] + '</td></tr>';
    }
    // Set the table HTML
    $("#candidate-votes").html(html);
}

/**
 * Function to get the latest standings on the candidate table.
 */
function getData() {
    // Simply make the ajax call and pass the resul to updateVoteTable.
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/HH_CountMeUp_war_exploded/service/countmeup",
        success: function(data) {
            updateVoteTable(data);
        }
    })
}

/**
 * Handle the response from the voting function.
 * @param data JSON object containing details on the outcome.
 */
function voteResponse(data) {
    if (data.success == "true") {
        // Vote was successful
        $('#thank-you').modal();
    } else if (data.success == "false" && data.reason=='vote limit') {
        // Voter has reached their limit
        $('#limit').modal();
    } else {
        // Something else went wrong, we need to check the logs
        $('#error').modal();
    }
}

/**
 * Create and display the voting buttons, including the input text field.
 */
function createVoteButtons() {
    var html = '<div class="mdl-textfield mdl-js-textfield">' +
        '<input class="mdl-textfield__input" type="email" id="userId" maxlength="100">' +
        '<label class="mdl-textfield__label" for="sample1">Email address...</label>' +
        '</div><br>';

    for (var i = 1; i <= 5; i++) {
        html+= '<button id="vote-candidate-'+ i +'" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored" style="margin: 10px;">candidate-' + i + '</button><br>';
    }

    $("#vote-area").html(html);
}

/**
 * Connects the voting buttons so that they kick off a voting request when clicked.
 */
function linkVoting() {
    // Loop through the buttons
    for (var i = 1; i <= 5; i++) {
        $("#vote-candidate-" +i).click(function () {
            // Get the id - we can't use the counter as it will change by the time the function is called.
            // I make that mistake every time...
            var candidate = $(this).attr('id')[$(this).attr('id').length -1];
            // Check we don't have an empty userId
            var userId = $('#userId').val();
            if (userId.length == 0) {
                $('#require-user-id').modal();
                return;
            }
            // Make the voting request, send the response to the voteResponse function.
            $.ajax({
                type:"POST",
                url: "http://localhost:8080/HH_CountMeUp_war_exploded/service/countmeup/vote",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data:JSON.stringify({
                    'userId':userId,
                    'candidateId': 'candidate-'+candidate
                }),
                success:function(data) {
                    voteResponse(data);
                }
            });
        });
    }

}

function simulate() {
    if (simCalled) {
        // we've run it once for this page load, no need to again.
        return;
    }
    simCalled = true;
    // Fire and forget.
    $.ajax({
        type:"GET",
        url:"http://localhost:8080/HH_CountMeUp_war_exploded/service/countmeup/simulate"
    });
}

// My global variable to track whether the simulate function has been run before.
var simCalled = false;

/**
 * Kick off all the required functions and listeners once the page has loaded.
 */
$(document).ready(function(){
    // Get data every second
    setInterval(function(){
        getData();
    }, 1000);

    // Set up the voting buttons and hook them up to function.
    createVoteButtons();
    linkVoting();

    // Hook up the simulate button.
    $('#simulate').click(function () {
        simulate();
    });
});