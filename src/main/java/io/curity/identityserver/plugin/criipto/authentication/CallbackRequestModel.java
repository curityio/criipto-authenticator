/*
 *  Copyright 2017 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.curity.identityserver.plugin.criipto.authentication;

import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.web.Request;

import java.util.function.Function;

class CallbackRequestModel
{
    @Nullable
    private final String _error;

    @Nullable
    private final String _errorDescription;

    private final String _url;
    private final String _code;
    private final String _state;

    CallbackRequestModel(Request request)
    {
        _code = request.getParameterValueOrError("code");
        _state = request.getParameterValueOrError("state");
        _error = request.getParameterValueOrError("error");
        _errorDescription = request.getParameterValueOrError("error_description");
        _url = request.getUrl();
    }

    public String getCode()
    {
        return _code;
    }

    public String getState()
    {
        return _state;
    }

    @Nullable
    public String getErrorDescription()
    {
        return _errorDescription;
    }

    public String getRequestUrl()
    {
        return _url;
    }

    @Nullable
    public String getError()
    {
        return _error;
    }
}