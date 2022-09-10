function manageIP() {
    const ip = document.getElementById("ip-address").value;
    const action = document.getElementById("action").value;

    let xhr = new XMLHttpRequest();

    if (action == "add") {
        let json = JSON.stringify({ip: ip});
        xhr.open("POST", "/api/antifraud/suspicious-ip", false);
        xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
        xhr.send(json);
    } else if (action == "remove") {
        xhr.open("DELETE", `/api/antifraud/suspicious-ip/${ip}`, false);
        xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
        xhr.send();
    }

    if (xhr.status == 200 || xhr.status == 201) {
        // Alert the user that the IP was added/removed
        alert(`IP ${ip} ${action}ed successfully!`);
    } else {
        // Alert the user that the IP was not added/removed
        alert(`Error ${action}ing IP ${ip}! Status code: ${xhr.status}`);
    }
}