package com.example.android.medicinecabinet.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.android.medicinecabinet.R

class DeleteDialogFragment(private val onDelete: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.custom_bottom_sheet_delete, null)
        builder.setView(view)

        val btnDelete = view.findViewById<Button>(R.id.btn_delete)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        btnDelete.setOnClickListener {
            onDelete()
            dismiss()
            findNavController().popBackStack()
        }

        btnCancel.setOnClickListener {dismiss()}

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setGravity(Gravity.CENTER)
    }
}