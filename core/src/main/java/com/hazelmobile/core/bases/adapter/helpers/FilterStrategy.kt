package com.hazelmobile.cores.bases.adapter.helpers

interface FilterStrategy<Model>{
    fun filter(query: String, list: List<Model>): List<Model>

}