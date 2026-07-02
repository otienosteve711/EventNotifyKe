package com.sc.eventnotifyke.utils



/**
 * Single source of truth for event categories — used by PostEventScreen
 * (for selecting a category when posting/editing) and HomeScreen/BrowseScreen
 * (for filtering events).
 */
val eventCategories = listOf(
    "Music",
    "Sports",
    "Food",
    "Tech",
    "Arts",
    "Faith",
    "Community",
    "Business",
    "Education",
    "Other"
)

/**
 * Same list, prefixed with "All" — for use in filter chip rows where
 * an "All" option should appear first.
 */
val eventCategoriesWithAll = listOf("All") + eventCategories