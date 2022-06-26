package com.example.bookpal.filters

import android.widget.Filter
import com.example.bookpal.adapters.AdapterBook
import com.example.bookpal.models.ModelBook

class FilterBook(
    var filterList: ArrayList<ModelBook>,
    var adapter: AdapterBook
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        val results = FilterResults()


        if (constraint != null && constraint.isNotEmpty()) {


            val filteredModels = ArrayList<ModelBook>()
            for (element in filterList) {

                if (element.name.contains(constraint, true)
                    || element.genre.contains(constraint, true)
                    || element.author.contains(constraint, true)) {

                    filteredModels.add(element)
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        } else {

            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {

        adapter.bookArrayList = results.values as ArrayList<ModelBook>
        adapter.notifyDataSetChanged()
    }

}