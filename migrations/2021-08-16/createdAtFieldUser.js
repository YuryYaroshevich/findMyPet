var db = db.getSiblingDB('petfinder')

var cursor = db.user.find();

while(cursor.hasNext()) {
  var user = cursor.next();
  var createdAt = user._id.getTimestamp()
  db.user.update({_id: user._id}, {$set: {createdAt: createdAt}})
}
