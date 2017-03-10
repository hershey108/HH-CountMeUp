
function updateVoteTable(data) {

    var keys = Object.keys(data);

    var html = "";

    for (var i = 0; i < keys.length; i++) {
        html += '<tr><td class="mdl-data-table__cell--non-numeric">' + keys[i] + '</td><td>' + data[keys[i]] + '</td></tr>';
    }
    $("#candidate-votes").html(html);
}

function getData() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/HH_CountMeUp_war_exploded/service/countmeup",
        success: function(data) {
            updateVoteTable(data);
        }
    })
}

$(document).ready(function(){
    getData();
});