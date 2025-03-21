package com.hazelmobile.cores.bases.usecase

import com.hazelmobile.cores.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * 1. violations of user centric behaviour
 * 2. required a separate data class in ui layer to send required parameters generic type
 * 3. Violation of, abstraction layer should be near to concrete implementation, principle.
 * **/

    interface BaseUseCase<in Params, Result> {
        suspend operator fun invoke(params: Params): Flow<Resource<List<Result>>>
    }
