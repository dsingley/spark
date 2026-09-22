/*
 * Copyright 2020- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark;

import org.jspecify.annotations.Nullable;

class Base64 {

    private Base64() {
    }

    private static final java.util.Base64.Encoder urlEncoder = java.util.Base64.getUrlEncoder();

    /**
     * @param toEncodeContent the String to be encoded, or null
     * @return String after encoding, or null if toEncodeContent was null
     */
    public static @Nullable String encode(@Nullable String toEncodeContent) {
        if (toEncodeContent == null) {
            return null;
        }
        return urlEncoder.encodeToString(toEncodeContent.getBytes());
    }

}
