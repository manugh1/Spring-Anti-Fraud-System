function updateRole() {
    let object = {
        username: document.getElementById("username").value,
        role: document.getElementById("userrole").value
    };

    let json = JSON.stringify(object);

    let xhr = new XMLHttpRequest();
    xhr.open("PUT", '/api/auth/role', false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(json);

    if (xhr.status == 200 || xhr.status == 201) {
        alert("Successfully updated role for " + object.username + "!");
    } else {
        alert("There was an error updating the role! Please try again.");
    }
}