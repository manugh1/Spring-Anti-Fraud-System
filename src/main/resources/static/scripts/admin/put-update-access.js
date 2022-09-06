function updateAccess() {
    let object = {
        username: document.getElementById("username").value,
        operation: document.getElementById("operation").value
    };

    let json = JSON.stringify(object);

    let xhr = new XMLHttpRequest();
    xhr.open("PUT", '/api/auth/access', false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(json);

    if (xhr.status == 200 || xhr.status == 201) {
        alert("Successfully updated access for " + object.username + "! Current status: " + object.operation + "ED");
    } else {
        alert("There was an error updating the role! Please try again.");
    }
}