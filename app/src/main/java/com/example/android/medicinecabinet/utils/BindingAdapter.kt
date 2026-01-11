package com.example.android.medicinecabinet.utils

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

// Устанавливаем выбранное значение в AutoCompleteTextView
@BindingAdapter("selectedValue")
fun setSelectedValue(view: AutoCompleteTextView, value: String?) {
    Log.d("BindingAdapter", "setSelectedValue called")
    if (view.text.toString() != value) {
        view.setText(value ?: "", false)
    }
}

// Извлекаем выбранное значение (для @={})
@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun getSelectedValue(view: AutoCompleteTextView): String {
    return view.text.toString()
}

// Слушатель изменения значения
@BindingAdapter("selectedValueAttrChanged")
fun setSelectedValueListener(view: AutoCompleteTextView, listener: InverseBindingListener?) {
    if (listener != null) {
        view.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            listener.onChange()
        }
        view.onFocusChangeListener = View.OnFocusChangeListener {_, hasFocus ->
            if (!hasFocus) listener.onChange()
        }
    }
}