var pg = require('pg');

var pgconfig = require('../config.json').db;

const pool = new pg.Pool(pgconfig);


pool.on('error', function (err, client) {
  // if an error is encountered by a client while it sits idle in the pool
  // the pool itself will emit an error event with both the error and
  // the client which emitted the original error
  // this is a rare occurrence but can happen if there is a network partition
  // between your application and the database, the database restarts, etc.
  // and so you might want to handle it and at least log it out
  console.error('idle client error', err.message, err.stack);
});


var db = module.exports = {
  //export the query method for passing queries to the pool
  query: function (text, values, callback) {
    //console.log('query:', text, values);
    return pool.query(text, values, callback);
  },

  // the pool also supports checking out a client for
  // multiple operations, such as a transaction
  connect: function (callback) {
    return pool.connect(callback);
  },

  rollback: function (client, done) {
    client.query('ROLLBACK', function (err) {
      // if error at rolling back - is a serious problem
      // return erro to done function -> close & remove this client from ppol
      return done(err);
    });
  },

  begin: function (client, done, queries) {
    client.query('BEGIN', function (err, res) {
      if (err || typeof queries !== 'function') {
        return db.rollback(client, done);
      }
     queries(client, done);
      //as long as we do not call the `done` callback we can do 
      //whatever we want...the client is ours until we call `done`
      //on the flip side, if you do call `done` before either COMMIT or ROLLBACK
      //what you are doing is returning a client back to the pool while it 
      //is in the middle of a transaction.  
      //Returning a client while its in the middle of a transaction
      //will lead to weird & hard to diagnose errors.
    })
  },

  commit: function (client, done) {
    client.query('COMMIT', done);
  }
};