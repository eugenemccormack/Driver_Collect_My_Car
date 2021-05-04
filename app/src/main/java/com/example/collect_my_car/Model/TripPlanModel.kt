package com.example.collect_my_car.Model

class TripPlanModel {

    var user: String? = null

    var driver: String? = null

    var driverInfoModel: DriverInfoModel? = null

    var userModel: UserModel? = null

    var origin: String? = null

    var originString: String? = null

    var destination: String? = null

    var destinationString: String? = null

    var durationPickup: String? = null

    var durationDestination: String? = null

    var distancePickup: String? = null

    var distanceDestination: String? = null

    var currentLat = -1.0

    var currentLng = -1.0

    var done = false

    var isCancelled = false

    var time: String? = null

    var collectionPhotos: CollectionPhotos? = null

    var collectionNumber: String? = null

    var distanceValue = 0

    var durationValue = 0

    var estimatedPrice = 0.0

    var totalPrice = 0.0

    var brakingCount = 0

    var newDriverRating = 5

    var oldDriverRating = 1

    var distanceText: String?=""

    var durationText:String?=""

    var tripTime: String = ""

}