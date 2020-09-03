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

class Base64 {

    //CS304 Issue link:https://github.com/perwendel/spark/issues/1061

    /**
     * @param toEncodeContent the String to be encode
     * @return String after encode
     */
    public static String encode(String toEncodeContent) {
        if (toEncodeContent == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(toEncodeContent.getBytes());
    }

    //CS304 Issue link:https://github.com/perwendel/spark/issues/1061

    /**
     * @param toDecodeContent the String to be decode
     * @return String after decode
     */
    public static String decode(String toDecodeContent) {
        if (toDecodeContent == null) {
            return null;
        }
        return new String(java.util.Base64.getDecoder().decode(toDecodeContent));
    }


}
