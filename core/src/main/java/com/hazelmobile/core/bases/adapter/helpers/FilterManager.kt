package com.hazelmobile.cores.bases.adapter.helpers

import javax.inject.Inject

class FilterManager<Model : Any> @Inject constructor(
    private val filterStrategy: FilterStrategy<Model>?
) {
    fun filter(query: String, originalList: List<Model>, currentList: List<Model>, previousQuery: String): List<Model> {
        return when {
            query.isEmpty() -> originalList
            query.trim().length > previousQuery.trim().length && previousQuery.trim().isNotEmpty() -> {
                filterStrategy?.filter(query, currentList) ?: emptyList()
            }
            else -> {
                filterStrategy?.filter(query, originalList) ?: emptyList()
            }
        }
    }
}