package com.github.lucaengel.packpilot.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object CreateTripRoute
@Serializable data class TripDetailsRoute(val tripId: String)
@Serializable data class PostTripReviewRoute(val tripId: String)
@Serializable object EssentialsRoute
@Serializable object TripTypesRoute
