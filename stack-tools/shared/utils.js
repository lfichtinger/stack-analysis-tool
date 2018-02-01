module.exports.asyncLoop = function (iterations, fn, callback) {
    var done = false;

    var loop = {
        index: -1,
        canceled: false,

        next: function () {
            this.index++;
            if (this.index < iterations) {
                fn(loop);
            } else {
                this.break();
            }
        },

        break: function () {
            if (typeof callback === 'function') {
                callback(loop);
            }
        }
    };

    loop.next();
    return;
};