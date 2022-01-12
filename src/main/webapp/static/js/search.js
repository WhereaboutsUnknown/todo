function sortBtn() {
    return $('#sort-direction-btn');
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

function addSearchParams(url, page, size, type) {
    const sortDir = getSort();
    const sortBy = getSortSelector();
    const criteria = getCriteria(type);

    return url + "?" + "dir=" + sortDir + (sortBy ? "&by=" + sortBy : "") + (criteria ? "&crit=" + criteria : "") + "&page=" + (page ? page : 0) + "&size=" + (size ? size : 50);
}

sortBtn().click(function () {
    if ($(this).hasClass('direction-up')) {
        $(this).removeClass('direction-up');
    } else {
        $(this).addClass('direction-up');
    }
});