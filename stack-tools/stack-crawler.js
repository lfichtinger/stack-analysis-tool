var http = require('http');
var zlib = require('zlib');
var request = require('request');
var db = require('./shared/db.js');
var util = require('./shared/utils.js');
var config = require('./config.json');

var httpCompressed = function (path, callback) {
    var cb = callback;
    var options = {
        url: "https://api.stackexchange.com/" + path + "&key= " + config.apikey ,
        headers: {
            'accept-encoding': 'gzp,deflate,br'
        },
        method: 'GET'
    };

    var stream = request(options).pipe(zlib.createGunzip());
    var body = "";

    stream.on('error', function (err) {
        console.log(err);
    });

    stream.on('end', function (d) {
        cb(null, JSON.parse(body));
    });

    stream.on('data', function (data) {
        body += data.toString();
    });
};

// INSERT DATA QUERIES
var getUrl = function (type, options) {
    var prefix = "/2.2/";
    var query = '?order=desc&sort=activity&site=stackoverflow';
    for (var k in options) {
        query += '&' + k + '=' + options[k];
    }
    return prefix + type + query;
};

var insertQueryBuilder = function (table, fields, options) {
    var values = "";
    for (var i = 0; i < fields.length; i++) {
        var num = i + 1;
        values += "$" + num;
        if (i < fields.length - 1) {
            values += ",";
        }
    }
    var text = "INSERT INTO " + table + "(" + fields.toString() + ") VALUES (" + values + ") ";
    if (options) {
        text += options;
    }
    return text;
};

var text_tag = function () {
    var fields = ["so_name", "so_count", "so_has_synonyms", "so_synonyms",
        "so_last_activity_date", "so_is_moderator_only", "so_is_required", "fetch_date", "query"
    ];
    return insertQueryBuilder('public.tags', fields);
};

var val_tag = function (tag, fetch_date, query) {
    return [
        tag.name,
        tag.count,
        tag.has_synonyms,
        tag.synonyms,
        tag.last_activity_date ? new Date(tag.last_activity_date * 1000) : null,
        tag.is_moderator_only,
        tag.is_required,
        fetch_date,
        query
    ];
};


// LOAD DATA
var getTags = function (inname, fetch_date, callback) {
    var cb = callback;
    httpCompressed('/2.2/tags?order=desc&sort=popular&inname=' + inname + '&site=stackoverflow', function (err, body) {
        if (err) {
            console.error(err);
            return;
        }
        if (Array.isArray(body.items)) {
            util.asyncLoop(body.items.length, function (loop) {
                var it = body.items[loop.index];
                db.query(text_tag(), val_tag(it, fetch_date, inname), function (err, res) {
                    if (err) {
                        console.log(err);
                    }
                    loop.next();
                });
            }, function () {
                console.log('tags geladen: ' + inname);
                if (typeof cb === 'function') {
                    cb();
                }
            });
        }
    });
};


var getPosts = function (searchTag, fetch_date, callback) {
    var cb = callback;
    var select = "SELECT so_name FROM public.tags WHERE query=$1;";

    db.query(select, [searchTag], function (err, res) {
        if (err) {
            console.error(err);
            return;
        }

        res.rows = res.rows || [];
        util.asyncLoop(res.rows.length, function (loopTag) {
            var tag = res.rows[loopTag.index];

            var fetchQuestionsAndSave = function (page, tag) {
                httpCompressed(getUrl('questions', {
                    tagged: tag.so_name,
                    page: page,
                    pagesize: 100
                }), function (err, body) {
                    if(err) {
                        console.error(err);
                        console.log('no result for tag: ' + tag.so_name);
                        loopTag.rollback = true;
                        loopTag.break();
                        return;
                    }

                    body.items = body.items || [];
                    util.asyncLoop(body.items.length, function (loop) {
                        var question = body.items[loop.index];
                        var insertQuestion = 'insert into posts(id, fetch_date, parent_query_tag, query_tags, data) values($1, $2, $3, $4, $5)';
                        insertQuestion += " ON CONFLICT ON CONSTRAINT posts_pkey DO UPDATE SET query_tags = posts.query_tags || ',' || EXCLUDED.query_tags";
                        db.query(insertQuestion, [question.question_id, fetch_date, searchTag, tag.so_name, JSON.stringify(question)], function (err, res) {
                            if (err) {
                                console.error(err);
                                console.log('question not saved ' + _question.question_id);
                                loop.rollback = true;
                                loop.break();
                                return;
                            }
                            loop.next();
                        });
                    }, function (loop) {
                        if (loop.rollback) {
                            console.log('not all questions saved for tag: ' + tag.so_name);
                            loopTag.rollback = true;
                            loopTag.break();
                            return;
                        }

                        if (body.has_more) {
                            var n = page + 1;
                            console.log('next page: ' + n);
                            fetchQuestionsAndSave(n, tag);
                            return;
                        }

                        console.log('all questions saved for tag: ' + tag.so_name);
                        loopTag.next();
                    });
                });
            };
            console.log('tag: ' + tag.so_name);
            fetchQuestionsAndSave(1, tag);

        }, function (loopTag) {
            console.log('iterate tags finished');
            if (typeof cb === 'function') {
                cb();
            }
        });
    });
};

var fetch_date = new Date();

// find tags from stack overflow
/*getTags('cordova', fetch_date, function() {
    getTags('xamarin', fetch_date, function() {
        getTags('react', fetch_date, function() {
                console.log('done!');
                process.exit();
        });
    });   
});*/

// find questions and answers from stack overflow
console.log('start: ' + fetch_date);
getPosts('cordova', fetch_date, function () {
    getPosts('xamarin', fetch_date, function () {
        getPosts('react', fetch_date, function () {
            console.log('done');
            console.log('end: ' + new Date());
            process.exit();
        });
    });
});

