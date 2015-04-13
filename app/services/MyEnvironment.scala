package services

/**
 * Copyright 2012-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import securesocial.core.RuntimeEnvironment
import securesocial.core.providers.{TwitterProvider, UsernamePasswordProvider, FacebookProvider}
import securesocial.core.services.UserService

import scala.collection.immutable.ListMap

class MyEnvironment extends RuntimeEnvironment.Default[User] {
  override val userService: UserService[User] = new InMemoryUserService()
  override lazy val providers = ListMap(
    include(
      new FacebookProvider(routes, cacheService,oauth2ClientFor(FacebookProvider.Facebook))
    ),
    include(
      new TwitterProvider(routes, cacheService, oauth1ClientFor(TwitterProvider.Twitter))
    ),
    include(
      new UsernamePasswordProvider[User](userService, avatarService, viewTemplates, passwordHashers)
    )
  )
}
