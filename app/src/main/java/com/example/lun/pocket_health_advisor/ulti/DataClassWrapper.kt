package com.example.lun.pocket_health_advisor.ulti

import org.json.JSONObject
import java.io.Serializable

/**
 * Created by wlun on 2/11/18.
 */

/**
 * A class that wrap all the data classes
 */
class DataClassWrapper {

    //create empty constructor for firestore recycleview
    data class ChatMessage(var message: String = "", var user: String = "")

    data class AuthUser(var id: String, var name: String = "") : Serializable
    data class RegisteredHospital(
            var id: String,
            var name: String,
            var email: String,
            var contactNo: String,
            var address: String,
            var consultationFee: String
    ) : Serializable

    data class Appointment(
            val id: String,
            val doctorName: String,
            val doctorId: String,
            val hospitalId: String,
            val hospitalName: String,
            val patientId: String,
            val time: String,
            val paymentStatus: Boolean
    )

    data class Response(var response: JSONObject)

    data class MapsHospital(
            var name: String,
            var openingStatus: String,
            var placeId: String,
            var distance: String? = null) : Serializable

    data class MapsHospitalDetails(
            var name: String,
            var placeId: String,
            var phoneNo: String,
            var address: String,
            var weekdayText: String,
            var rating: Double,
            var website: String,
            var url: String
    )

    data class Condition(
            var name: String,
            var commonName: String,
            var acuteness: String,
            var categories: ArrayList<String>,
            var hints: String,
            var prevalence: String,
            var severity: String,
            var triageLevel: String,
            var map: HashMap<*, *>
    ) : Serializable

    data class PossibleCondition(var name: String, var probability: Double, var map: HashMap<*, *>) : Serializable

    data class MedicReport(
            var diagnoseCondition: Condition,
            var initialSymptoms: ArrayList<InitialSyndrome>,
            var possibleConditions: ArrayList<PossibleCondition>,
            var questions: ArrayList<Question>,
            var timestamp: String,
            val reportId: String = ""
    ) : Serializable {
        // TODO: find better way to implement this
        fun generateMap(): HashMap<*, *> {
            val map = HashMap<Any, Any>()
            map["diagnose_condition"] = diagnoseCondition.map
            map["initial_symptoms"] = initialSymptoms[0].map as Any
            map["possible_conditions"] = possibleConditions[0].map as Any
            map["questions"] = questions[0].questionList as Any
            map["timestamp"] = timestamp
            return map
        }
    }

    data class InitialSyndrome(
            val name: String,
            val choice: String,
            var map: HashMap<*, *>
    ) : Serializable

    data class Question(
            val question: String,
            val symptom: String,
            val userResponse: String,
            var questionList: ArrayList<*>
    ) : Serializable
}