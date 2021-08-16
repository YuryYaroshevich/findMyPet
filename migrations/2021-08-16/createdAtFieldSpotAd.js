var conn = new Mongo();
var db = conn.getDB("pets");

var cursor = db.spotAd.find();

while(cursor.hasNext()) {
  var ad = cursor.next();
  var createdAt = ad._id.getTimestamp()
  db.spotAd.update({_id: ad._id}, {$set: {createdAt: createdAt}})
}
