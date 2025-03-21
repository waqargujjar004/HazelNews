package com.hazelmobile.cores.bases.adapter

sealed class Selection {
    data object SelectAll: Selection()
    data object UnSelectAll: Selection()
}