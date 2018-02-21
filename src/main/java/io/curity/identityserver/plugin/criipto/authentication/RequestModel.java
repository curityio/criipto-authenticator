package io.curity.identityserver.plugin.criipto.authentication;

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

import org.hibernate.validator.constraints.NotBlank;
import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import javax.validation.Valid;
import java.util.Optional;

import static io.curity.identityserver.plugin.criipto.authentication.RequestModel.PhoneModel.MOBILE_NUMBER_PARAM;


public final class RequestModel
{

    @Nullable
    @Valid
    private final Post _postRequestModel;

    private final Request _request;
    private final Response _response;

    RequestModel(Request request, Response response)
    {
        _request = request;
        _response = response;
        if (request.isPostRequest())
        {
            if (request.getParameterNames().contains(MOBILE_NUMBER_PARAM))
            {
                _postRequestModel = new PhoneModel(request);
            }
            else
            {
                _postRequestModel = new SSNModel(request);
            }
        }
        else
        {
            _postRequestModel = null;
        }
    }

    Post getPostRequestModel()
    {
        return Optional.ofNullable(_postRequestModel).orElseThrow(() ->
                new RuntimeException("Post RequestModel does not exist"));
    }

    Request getRequest()
    {
        return _request;
    }

    public Response getResponse()
    {
        return _response;
    }

    interface Post
    {
        String getPhoneNumber();

        String getPersonalNumber();
    }

    class PhoneModel implements Post
    {

        public static final String MOBILE_NUMBER_PARAM = "phoneNumber";

        @NotBlank(message = "validation.error.phoneNumber.required")
        private final String _phoneNumber;


        PhoneModel(Request request)
        {
            _phoneNumber = request.getFormParameterValueOrError(MOBILE_NUMBER_PARAM);
        }

        @Override
        public String getPhoneNumber()
        {
            return _phoneNumber;
        }

        @Override
        public String getPersonalNumber()
        {
            return null;
        }

    }

    class SSNModel implements Post
    {
        public static final String PERSONAL_NUMBER_PARAM = "personalNumber";

        @NotBlank(message = "validation.error.personalNumber.required")
        private final String _personalNumber;

        SSNModel(Request request)
        {
            _personalNumber = request.getFormParameterValueOrError(PERSONAL_NUMBER_PARAM);
        }

        @Override
        public String getPhoneNumber()
        {
            return null;
        }

        @Override
        public String getPersonalNumber()
        {
            return _personalNumber;
        }
    }

}

