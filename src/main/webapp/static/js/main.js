function root() {
    return 'http://localhost:8080';
}

function api() {
    return 'http://localhost:8080';
}

function rest(method, url, body, callback) {
    const token = $("meta[name='_csrf']").attr("content");
    const xhr = new XMLHttpRequest();

    xhr.open(method, api() + url);
    xhr.setRequestHeader("Content-type", "application/json; charset = utf-8");
    xhr.setRequestHeader("X-CSRF-TOKEN", token);
    if (body) {
        xhr.send(JSON.stringify(body));
    } else {
        xhr.send();
    }
    xhr.addEventListener("load", function () {
        if (xhr.status === 200) {
            let data = JSON.parse(xhr.response);
            callback(data);
        }
    });
}