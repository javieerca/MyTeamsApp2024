package com.myTeams.app.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

import com.myTeams.app.model.TeamModel
import kotlinx.coroutines.tasks.await

abstract class FirebaseUtils {

    private val db = FirebaseFirestore.getInstance()

     fun getTeamsByEmail(user: String): TeamModel? {
        val snapshot =db.collection("teams")
                        .whereEqualTo("user", user)
                        .get()

        val document = snapshot.result.documents.firstOrNull()
        return document?.let {
            document.toObject(TeamModel::class.java)
        }

    }
}