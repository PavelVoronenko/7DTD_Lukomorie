package com.antago30.a7dtd_lukomorie.model

import androidx.fragment.app.Fragment

data class MenuItem(
    val title: String,
    val fragmentClass: Class<out Fragment>
)