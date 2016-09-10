
function launch(theId) {
    var input = document.getElementById('launchId');
    if (input != null) {
        input.value = theId;
    }

    var form = document.getElementById('control');
    form.submit();
}

function destroy(theId) {
    go(theId,'destroy')
}

function apply(theId) {
    go(theId,'apply')
}

function go(theId,action) {
    var input = document.getElementById('launchId');
    if (input != null) {
        input.value = theId;
    }

    var input2 = document.getElementById('action');
    if (input2 != null) {
        input2.value = action;
    }

    var form = document.getElementById('control');
    form.submit();
}

