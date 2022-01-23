function sortBtn() {
    return $('#sort-direction-btn');
}

function searchBtn() {
    return $('#search-btn');
}

function searchInput() {
    return $('#search-input');
}

function getSort() {
    if (sortBtn().hasClass('direction-up')) {
        return 'asc';
    }
    return 'desc';
}

function getSortSelector() {
    return $('#sort-select-input').val();
}

function getWorkerCriteria() {

}

function getTaskCriteria() {

}

function getCriteria(type) {
    if (type) {
        if (type === 'task') {

        }
        else if (type === 'worker') {

        }
    }
    return null;
}

function getSearchRequest() {
    return searchInput().val();
}

function addSearchParams(url, page, size, type) {
    const sortDir = getSort();
    const sortBy = getSortSelector();
    const criteria = getCriteria(type);
    const req = getSearchRequest();

    return url + `?dir=${sortDir}${sortBy ? "&by=" + sortBy : ""}${criteria ? "&crit=" + criteria : ""}${req ? "&req=" + req : ""}&page=${page ? page : 0}&size=${size ? size : 50}`;
}

function addSortAndReq(url) {
    const sortDir = getSort();
    const sortBy = getSortSelector();
    const req = getSearchRequest();

    return url + `?sort=${sortBy ? sortBy + "," : ""}${sortDir}${req ? "&req=" + req : ""}&page=0&size=1000`;
}

sortBtn().click(function () {
    if ($(this).hasClass('direction-up')) {
        $(this).removeClass('direction-up');
    } else {
        $(this).addClass('direction-up');
    }
});