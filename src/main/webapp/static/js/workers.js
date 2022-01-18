function workersCache() {
    return Todo.getValue('workersCache');
}

window.addEventListener("DOMContentLoaded", () => {
    Todo.store('workersCache', new Map());

    function loadWorkers(initial) {
        let list = $("#workers-block-list");
        list.empty();
        const searchUrl = initial ? "/worker/search" : addSearchParams("/worker/search");
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

    loadWorkers(true);

    $("#refresh-button").click(function () {
        loadWorkers(false);
    });

    sortBtn().click(function () {
        loadWorkers(false);
    });

    searchBtn().click(function () {
        loadWorkers(false);
    });

    searchInput().on('keyup', function (e) {
        e.preventDefault();
        if (e.key === 'Enter' || e.keyCode === 13) {
            loadWorkers(false);
        }
    });
});