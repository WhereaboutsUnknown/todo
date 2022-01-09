function resolveStarSelectors(points) {
    let classes = [];

    for (let i = 1; i <= 5; i++) {
        if (points >= i) {
            classes.push("rate100");
        }
    }
    if (classes.length === 5) {
        return classes;
    }

    const incompleteStarNumber = (points > 0 ? Math.ceil(points) : 1);
    const difference = points - (points | 0);

    const quarter = (roundQuarter(difference) * 100) | 0;

    classes.push((quarter > 0 ? "rate" + quarter : ""));
    for (let i = incompleteStarNumber + 1; i <= 5; i++) {
        classes.push("");
    }

    console.log(classes);
    return classes;
}

function resolveShareSelector(share) {
    return "share" + ((roundHalf(share * 10) * 10) | 0);
}

function roundHalf(a) {
    return Math.round(a * 2) / 2;
}

function roundQuarter(a) {
    return Math.round(a * 4) / 4;
}