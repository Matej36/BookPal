package com.example.bookpal.filters

import android.widget.Filter
import com.example.bookpal.adapters.AdapterBook
import com.example.bookpal.adapters.AdapterQuote
import com.example.bookpal.models.ModelBook
import com.example.bookpal.models.ModelQuote

class FilterQuote(
    var filterList: ArrayList<ModelQuote>,
    var adapter: AdapterQuote
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {


            val filteredModels = ArrayList<ModelQuote>()
            for (element in filterList) {

                if (element.quote.contains(constraint, true)) {

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
        adapter.quoteArrayList = results.values as ArrayList<ModelQuote>

        adapter.notifyDataSetChanged()
    }

}