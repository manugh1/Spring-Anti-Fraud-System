function manageStolenCard() {
    const number = document.getElementById("stolen-card-number").value;
    const action = document.getElementById("action").value;

    let xhr = new XMLHttpRequest();

    if (action == "add") {
        let json = JSON.stringify({number: number});
        xhr.open("POST", "/api/antifraud/stolencard", false);
        xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
        xhr.send(json);
    } else if (action == "remove") {
        xhr.open("DELETE", `/api/antifraud/stolencard/${number}`, false);
        xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
        xhr.send();
    }

    if (xhr.status == 200 || xhr.status == 201) {
        // Alert the user that the IP was added/removed
        alert(`Card ${number} ${action}ed successfully!`);
    } else {
        // Alert the user that the IP was not added/removed
        alert(`Error ${action}ing Card ${number}! Status code: ${xhr.status}`);
    }
}