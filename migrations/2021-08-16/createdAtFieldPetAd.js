var db = db.getSiblingDB('petfinder')

var cursor = db.petAd.find();

while(cursor.hasNext()) {
  var ad = cursor.next();
  var createdAt = ad._id.getTimestamp()
  db.petAd.update({_id: ad._id}, {$set: {createdAt: createdAt}})
}
