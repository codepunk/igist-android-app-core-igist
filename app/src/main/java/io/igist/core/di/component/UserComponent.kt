/*
 * Copyright (c) 2018 IGIST.io. All rights reserved.
 * Author(s): Scott Slater
 */

package io.igist.core.di.component

import io.igist.core.di.module.UserModule
import io.igist.core.di.scope.UserScope
import dagger.Component
import dagger.Subcomponent

/**
 * A [Component] for dependency injection scoped to a logged-in user.
 */
@UserScope
@Subcomponent(
    modules = [
        UserModule::class
    ]
)
interface UserComponent {

    // region Nested/inner classes

    /**
     * Helper class for creating an instance of [UserComponent].
     */
    @Subcomponent.Builder
    interface Builder {

        // region Methods

        /**
         * Builds the user subcomponent.
         */
        fun build(): UserComponent

        // endregion Methods

    }

    // endregion Nested/inner classes

}
