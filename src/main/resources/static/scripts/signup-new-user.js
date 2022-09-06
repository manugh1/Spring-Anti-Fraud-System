function send() {
    let object = {
        name: document.getElementById("signup-name").value,
        username: document.getElementById("signup-username").value,
        password: document.getElementById("signup-password").value
    };

    let json = JSON.stringify(object);

    let xhr = new XMLHttpRequest();
    xhr.open("POST", '/api/auth/user', false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(json);

    if (xhr.status == 200 || xhr.status == 201) {
        alert("Successfully signed up!");
    } else {
        alert("There was an error signing you up! Please try again.");
    }
}