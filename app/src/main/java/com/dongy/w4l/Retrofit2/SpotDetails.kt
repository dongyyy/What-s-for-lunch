package com.dongy.w4l.Retrofit2

data class SpotDetails(val results: List<result>, val status: String) {
    class result(val formatted_address: String, val formatted_phone_number: String, val url: String)
}


//
//{
//    "html_attributions" : [],
//    "result" : {
//    "formatted_address" : "대한민국 인천광역시 서구 왕길동 643-7",
//    "formatted_phone_number" : "032-566-1822",
//    "name" : "도부리감자탕",
//    "rating" : 3.5,
//    "types" : [ "restaurant", "food", "point_of_interest", "establishment" ],
//    "url" : "https://maps.google.com/?cid=8506857208366029084"
//},
//    "status" : "OK"
//}
