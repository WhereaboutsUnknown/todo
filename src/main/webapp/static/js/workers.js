function workersCache() {
    return Todo.getValue('workersCache');
}

window.addEventListener("DOMContentLoaded", () => {
    Todo.store('workersCache', new Map());

    function loadWorkers() {
        let list = $("#workers-block-list");
        list.empty();
        const searchUrl = addSearchParams("/worker/search");
        rest(
            "GET",
            searchUrl,
            null,
            function (data) {
                console.log(data);
                const content = data.content;
                for (var i = 0; i < content.length; i++) {
                    workersCache().set(content[i].id, content[i]);
                    list.append(workerElement(content[i]));
                }
            }
        );
    }

    loadWorkers();

    $("#refresh-button").click(function () {
        loadWorkers();
    });
});