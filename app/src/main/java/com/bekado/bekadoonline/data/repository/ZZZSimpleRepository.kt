package com.bekado.bekadoonline.data.repository

import com.bekado.bekadoonline.data.model.TokoModel
import com.google.firebase.firestore.FirebaseFirestore

class ZZZSimpleRepository(private val firestore: FirebaseFirestore) {
    fun getDataToko(response: (TokoModel, Boolean) -> Unit) {
        firestore.collection("bekado")
            .document("aboutBekado").get()
            .addOnSuccessListener { snapshot ->
                val foto = snapshot.get("fotoToko") as? String? ?: ""
                val kontak = snapshot.get("kontakPerson") as? String? ?: ""
                val alamat = snapshot.get("lokasiToko") as? String? ?: ""
                val nama = snapshot.get("namaToko") as? String? ?: ""
                val operasional = snapshot.get("operasionalToko") as? String? ?: ""

                response.invoke(TokoModel(foto, kontak, alamat, nama, operasional), true)
            }.addOnFailureListener { response.invoke(TokoModel(), false) }
    }
}