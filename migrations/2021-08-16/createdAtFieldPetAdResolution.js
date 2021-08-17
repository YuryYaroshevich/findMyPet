var db = db.getSiblingDB('petfinder')

var cursor = db.petAdResolution.find();

while(cursor.hasNext()) {
  var resolution = cursor.next();
  var createdAt = resolution._id.getTimestamp()
  db.petAdResolution.update({_id: resolution._id}, {$set: {createdAt: createdAt}})
}
