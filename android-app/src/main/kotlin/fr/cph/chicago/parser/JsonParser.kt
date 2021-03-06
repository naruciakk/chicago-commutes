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

package fr.cph.chicago.parser

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.cph.chicago.exception.ParserException
import fr.cph.chicago.util.Util
import java.io.InputStream

/**
 * Json

 * @author Carl-Philipp Harmant
 * *
 * @version 1
 */
object JsonParser {

    private val util = Util
    private val mapper = jacksonObjectMapper()

    init {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        mapper.setSerializationInclusion(Include.NON_NULL)
    }

    @Throws(ParserException::class)
    fun <T> parse(stream: InputStream, clazz: Class<T>): T {
        try {
            return mapper.readValue(stream, clazz)
        } catch (e: Exception) {
            throw ParserException(e)
        } finally {
            util.closeQuietly(stream)
        }
    }
}
