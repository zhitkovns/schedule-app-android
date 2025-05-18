// GroupAdapter.kt
package com.spbpu.schedule.presentation.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.spbpu.schedule.data.models.Group

class GroupAdapter(
    private val context: Context,
    private var originalData: List<Group>,
    private var filteredData: List<Group>
) : BaseAdapter(), Filterable {

    private val inflater = LayoutInflater.from(context)

    fun updateData(newData: List<Group>) {
        originalData = newData
        filteredData = newData
        notifyDataSetChanged()
    }

    override fun getCount(): Int = filteredData.size
    override fun getItem(position: Int): Group = filteredData[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        view.findViewById<TextView>(android.R.id.text1).text = getItem(position).name
        return view
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return FilterResults().apply {
                values = if (constraint.isNullOrEmpty()) {
                    originalData
                } else {
                    originalData.filter {
                        it.name.contains(constraint, ignoreCase = true)
                    }
                }
                count = (values as List<*>).size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredData = if (results?.values == null) {
                emptyList()
            } else {
                results.values as List<Group>
            }
            notifyDataSetChanged()
        }
    }
}