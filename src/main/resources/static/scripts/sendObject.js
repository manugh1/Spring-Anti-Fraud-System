function send() {
    let object = {
        name: document.getElementById("name").value,
        username: document.getElementById("username").value,
        password: document.getElementById("password").value
    };

    let json = JSON.stringify(object);

    let xhr = new XMLHttpRequest();
    xhr.open("POST", '/api/auth/user', false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(json);

    if (xhr.status == 200 || xhr.status == 201) {
        console.log("Success!");
    }
}