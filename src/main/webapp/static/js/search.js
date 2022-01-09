function sortBtn() {
    return $('#sort-direction-btn');
}

function getSort() {
    if (sortBtn().hasClass('direction-up')) {
        return 'asc';
    }
    return 'desc';
}

sortBtn().click(function () {
    if ($(this).hasClass('direction-up')) {
        $(this).removeClass('direction-up');
    } else {
        $(this).addClass('direction-up');
    }
});