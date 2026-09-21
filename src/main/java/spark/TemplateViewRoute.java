/*
* Copyright 2011- Per Wendel
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


/**
 * A TemplateViewRoute is a route whose handler returns a {@link ModelAndView} rather than the
 * response content directly. Unlike a plain {@link Route}, whose handle() result becomes the
 * response body via toString() if it isn't already a String, a TemplateViewRoute's ModelAndView
 * is rendered into the response body by a {@link TemplateEngine}.
 * <p>
 * The primary purpose is to provide a way to create generic and reusable components for rendering
 * output using a Template Engine. For example, to render objects to HTML by using the Freemarker
 * template engine.
 *
 * @author alex
 */
@FunctionalInterface
public interface TemplateViewRoute {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws java.lang.Exception when handle fails
     */
    // throws Exception is part of the public API; narrowing it would break existing implementations
    @SuppressWarnings("java:S112")
    ModelAndView handle(Request request, Response response) throws Exception;

}
