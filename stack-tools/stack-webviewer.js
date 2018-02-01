var express = require('express');
var bodyparser = require('body-parser');
var db = require('./shared/db.js');
var fs = require('fs');
var config = require('./config.json');

var asyncLoop = function (iterations, fn, callback) {
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

var exp = express();
exp.use(bodyparser.json()); //Parses json body
exp.use(bodyparser.urlencoded({
    extended: true
})); // parses body of x-www-form-urlencoded


exp.use(function (req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
    next();
});

exp.get('/topic/:framework/:ids', function (req, res) {

    if (!req.params.framework || !req.params.ids) {
        res.status(400).send();
        return;
    }

    var search_text = req.query.text || null;
    var not_text = req.query.nottext || null;

    var topics = req.params.ids.split(",");
    var resultrows = new Array();

    asyncLoop(topics.length, function (loop) {

        var topic = topics[loop.index];
        var fetch_date = new Date(2017, 5, 13, 20, 15, 56, 188);
        values = [fetch_date];

        fs.readFile(config.viewer.frameworkdocs + req.params.framework + '-docs-' + topic + '.txt', function (err, data) {
            if (err) {
                console.error(err);
                loop.cancel = true;
                return;
            }

            if (data) {
                values[1] = data;
            }

            var quIdx = 1;
            var query = "select p.id, p.data, p.parent_query_tag, p.query_tags \
                        from posts p \
                        where p.fetch_date = $" + quIdx++ + " and p.id::text like any(string_to_array($" + quIdx++ + ", ',', '')) ";

            var resQuery = addSearchtextToQuery({
                query: query,
                values: values,
                quIdx: quIdx,
                search_text: search_text,
                not_text: not_text
            });
            query = addOrderByClause(req.query.order, resQuery.query);
            console.log(query);
            db.query(query, resQuery.values, function (err, result) {
                if (err) {
                    console.log('send error ' + err.toString());
                    loop.break();
                    return;
                }

                resultrows = resultrows.concat(result.rows);
                loop.next();
                return;
            });

        })
    }, function (loop) {
        if (loop.cancel) {
            res.status(200).json({
                ok: false
            });
            return;
        }

        console.log('send result');
        res.status(200).json({
            rows: resultrows,
            ok: true
        });
        return;

    });

});

var addSearchtextToQuery = function (opt) {
    var query = opt.query;
    var values = opt.values;
    var quIdx = opt.quIdx;

    if (opt.search_text) {
        var or_text = opt.search_text.split("|");
        query += " and (";
        for (var i = 0; i < or_text.length; i++) {
            if (i > 0) {
                query += " or ";
            }
            query += " p.tsv_all @@ plainto_tsquery($" + quIdx + ") ";
            values[quIdx - 1] = or_text[i];
            quIdx++;
        }
        query += ")";
    }

    if (opt.not_text) {
        var or_notText = opt.not_text.split("|");
        console.log(or_notText.length);
        query += " and ( ";
        for (var i = 0; i < or_notText.length; i++) {
            if (i > 0) {
                query += " and ";
            }

            query += " not(p.tsv_all @@ plainto_tsquery($" + quIdx + "))";
            values[quIdx - 1] = or_notText[i];
            quIdx++;
        }
        query += ")";
    }
    return {
        query: query,
        values: values,
        quIdx: quIdx
    };
};

var addOrderByClause = function (order, query) {
    if (query.indexOf("order by") === -1) {
        query += " order by ";
    }
    switch (order) {
        case 'view-desc':
            query += " p.data->'view_count' desc";
            break;
        case 'score-desc':
            query += " p.data->'score' desc";
            break;
        case 'create-desc':
            query += "  p.data->'creation_date' desc";
            break;
        case 'create-asc':
            query += " p.data->'creation_date' asc";
            break;
        case 'activity-desc':
            query += " p.data->'last_activity_date' desc";
            break;
        default:
            query += " p.id desc";
            break;
    }
    return query;
}

exp.get('/posts/:ids', function (req, res) {

    if (!req.params.ids) {
        res.status(400).send();
        return;
    }

    var search_text = req.query.text || null;
    var not_text = req.query.nottext || null;

    var quIdx = 1;
    var query = "select p.id, p.data, p.parent_query_tag, p.query_tags \
        from posts p \
        where p.fetch_date = $" + quIdx++ + " and p.id::text like any(string_to_array($" + quIdx++ + ", ',', ''))  ";

    var fetch_date = new Date(2017, 5, 13, 20, 15, 56, 188);
    var values = [fetch_date, req.params.ids.trim()];

    var resQuery = addSearchtextToQuery({
        query: query,
        values: values,
        quIdx: quIdx,
        search_text: search_text,
        not_text: not_text
    });

    query = addOrderByClause(req.query.order, resQuery.query);
    console.log(query);
    db.query(query, resQuery.values, function (err, result) {
        if (err) {
            console.log('send error ' + err.toString());
            res.status(200).json({
                ok: false
            });
            return;
        }

        console.log('send result');
        res.status(200).json({
            rows: result.rows,
            ok: true
        });
        return;
    });
});

exp.get('/posts', function (req, res) {
    var keys = req.query.keys || null;
    var limit = req.query.range || null;
    var offset = req.query.page ? (req.query.page - 1) * limit : 0;
    var tags = req.query.includedtags ? req.query.includedtags.split(',') : null;
    var nottags = req.query.excludedtags ? req.query.excludedtags.split(',') : null;

    console.log('query posts');
    //var select = 'select * from search_keywords($1::text[],$2)';
    var fetch_date = new Date(2017, 5, 13, 20, 15, 56, 188);
    var values = [fetch_date];
    var paramIdx = 1;
    var select = "select p.id, p.data, p.parent_query_tag, p.query_tags \
        from posts p \
        where p.fetch_date = $" + paramIdx;
    paramIdx++;

    if (keys) {
        select += " and (p.tsv_answers @@ plainto_tsquery($" + paramIdx + ") \
                        or p.tsv @@ plainto_tsquery($" + paramIdx + ") \
                        or p.tsv_comments @@ plainto_tsquery($" + paramIdx + "))";
        values.push(keys);
        paramIdx++;
    }

    if (tags) {
        select += " and p.query_tags ilike all($" + paramIdx + ")";
        values.push(tags);
        paramIdx++;
    }

    if (nottags) {
        select += " and p.query_tags not ilike all($" + paramIdx + ")";
        values.push(nottags);
        paramIdx++;
    }

    select += " order by (p.data->>'view_count')::bigint desc";

    db.query(select, values, function (err, result) {
        if (err) {
            console.log('send error ' + err.toString());
            res.status(200).json({
                ok: false
            });
            return;
        }

        console.log('send result');
        res.status(200).json({
            rows: result.rows,
            ok: true
        });
        return;
    });
});


exp.listen(9001, function () {
    console.log('http listening on *:' + 9001);
});