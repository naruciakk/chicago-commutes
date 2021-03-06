/**
 * Copyright 2019 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.exception

open class TrackerException constructor(message: String, e: Exception) : Exception(message, e)

class ConnectException(message: String, e: Exception) : TrackerException(message, e) {
    companion object {

        private const val ERROR = "Please check your connection"

        fun defaultException(e: Exception): ConnectException {
            return ConnectException(ERROR, e)
        }
    }
}

class CtaException(response: Any) : Exception("CTA error response [$response]")

class ParserException(e: Exception) : TrackerException("Parse exception", e)

class BaseException : RuntimeException()


